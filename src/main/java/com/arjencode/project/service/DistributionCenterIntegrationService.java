package com.arjencode.project.service;

import com.arjencode.project.model.Item;
import com.arjencode.project.repository.ItemRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

@Service
public class DistributionCenterIntegrationService {
    
    @Value("${distribution.center.api.url:http://localhost:8081/api/distribution-centers}")
    private String distributionCenterApiUrl;
    
    @Value("${distribution.center.api.username:admin}")
    private String apiUsername;
    
    @Value("${distribution.center.api.password:admin123}")
    private String apiPassword;
    
    // Warehouse location (Downtown Toronto)
    @Value("${warehouse.latitude:43.6532}")
    private double warehouseLatitude;
    
    @Value("${warehouse.longitude:-79.3832}")
    private double warehouseLongitude;
    
    private final RestTemplate restTemplate;
    private final ItemRepository itemRepository;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public DistributionCenterIntegrationService(ItemRepository itemRepository) {
        this.restTemplate = new RestTemplate();
        this.itemRepository = itemRepository;
        this.objectMapper = new ObjectMapper();
    }
    
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(apiUsername, apiPassword);
        return headers;
    }
    
    // Get all distribution centers
    public List<Map<String, Object>> getAllDistributionCenters() {
        try {
            HttpEntity<String> entity = new HttpEntity<>(createAuthHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                distributionCenterApiUrl, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                List<Map<String, Object>> centers = new ArrayList<>();
                
                for (JsonNode centerNode : jsonNode) {
                    Map<String, Object> center = new HashMap<>();
                    center.put("id", centerNode.get("id").asLong());
                    center.put("name", centerNode.get("name").asText());
                    center.put("latitude", centerNode.get("latitude").asDouble());
                    center.put("longitude", centerNode.get("longitude").asDouble());
                    
                    // Calculate distance
                    double distance = calculateDistance(
                        warehouseLatitude, warehouseLongitude,
                        centerNode.get("latitude").asDouble(),
                        centerNode.get("longitude").asDouble()
                    );
                    center.put("distanceFromWarehouse", Math.round(distance * 100.0) / 100.0);
                    
                    // Get item count if items array exists
                    if (centerNode.has("items") && centerNode.get("items").isArray()) {
                        center.put("itemCount", centerNode.get("items").size());
                    } else {
                        center.put("itemCount", 0);
                    }
                    
                    centers.add(center);
                }
                
                return centers;
            }
        } catch (Exception e) {
            System.err.println("Error getting distribution centers: " + e.getMessage());
        }
        return Collections.emptyList();
    }
    
    // Request item from closest distribution center
    public boolean requestItemFromClosestCenter(String brand, String name) {
        try {
            // Find closest center with the item
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("brand", brand);
            requestBody.put("name", name);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, createAuthHeaders());
            
            String findClosestUrl = distributionCenterApiUrl + "/find-closest" +
                "?warehouseLatitude=" + warehouseLatitude +
                "&warehouseLongitude=" + warehouseLongitude;
            
            ResponseEntity<String> response = restTemplate.exchange(
                findClosestUrl, HttpMethod.POST, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode centerNode = objectMapper.readTree(response.getBody());
                Long centerId = centerNode.get("id").asLong();
                String centerName = centerNode.get("name").asText();
                
                System.out.println("Found item at: " + centerName + " (ID: " + centerId + ")");
                
                // Request the item from this center
                String requestUrl = distributionCenterApiUrl + "/" + centerId + "/request?quantity=1";
                ResponseEntity<String> requestResponse = restTemplate.exchange(
                    requestUrl, HttpMethod.POST, entity, String.class);
                
                if (requestResponse.getStatusCode() == HttpStatus.OK) {
                    // Add item to warehouse stock
                    return addItemToWarehouse(brand, name, centerNode);
                }
            }
        } catch (Exception e) {
            System.err.println("Error requesting item from distribution center: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    

    
    // Add item to warehouse stock
    private boolean addItemToWarehouse(String brand, String name, JsonNode centerResponse) {
        try {
            // Check if item already exists in warehouse
            Optional<Item> existingItem = itemRepository.findByBrandAndName(brand, name)
                .stream().findFirst();
            
            if (existingItem.isPresent()) {
                // Update quantity
                Item item = existingItem.get();
                item.setQuantity(item.getQuantity() + 1);
                itemRepository.save(item);
                System.out.println("Updated warehouse stock for: " + name + " by " + brand);
            } else {
                // Create new item in warehouse (with default values since we don't have full item details)
                Item newItem = new Item();
                newItem.setName(name);
                newItem.setBrand(brand);
                newItem.setCategory("Unknown"); // Default category
                newItem.setPrice(new BigDecimal("0.00")); // Default price
                newItem.setYear(2023); // Default year
                newItem.setQuantity(1);
                itemRepository.save(newItem);
                System.out.println("Added new item to warehouse: " + name + " by " + brand);
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error adding item to warehouse: " + e.getMessage());
            return false;
        }
    }
    
    // Get distribution center by ID with items
    public Map<String, Object> getDistributionCenterById(Long id) {
        try {
            HttpEntity<String> entity = new HttpEntity<>(createAuthHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                distributionCenterApiUrl + "/" + id, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode centerNode = objectMapper.readTree(response.getBody());
                Map<String, Object> center = new HashMap<>();
                center.put("id", centerNode.get("id").asLong());
                center.put("name", centerNode.get("name").asText());
                center.put("latitude", centerNode.get("latitude").asDouble());
                center.put("longitude", centerNode.get("longitude").asDouble());
                
                // Calculate distance
                double distance = calculateDistance(
                    warehouseLatitude, warehouseLongitude,
                    centerNode.get("latitude").asDouble(),
                    centerNode.get("longitude").asDouble()
                );
                center.put("distanceFromWarehouse", Math.round(distance * 100.0) / 100.0);
                
                // Get items if available
                List<Map<String, Object>> items = new ArrayList<>();
                if (centerNode.has("items") && centerNode.get("items").isArray()) {
                    for (JsonNode itemNode : centerNode.get("items")) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", itemNode.get("id").asLong());
                        item.put("name", itemNode.get("name").asText());
                        item.put("brand", itemNode.get("brand").asText());
                        item.put("category", itemNode.get("category").asText());
                        item.put("price", itemNode.get("price").asDouble());
                        item.put("year", itemNode.get("year").asInt());
                        item.put("quantity", itemNode.get("quantity").asInt());
                        items.add(item);
                    }
                }
                center.put("items", items);
                center.put("itemCount", items.size());
                
                return center;
            }
        } catch (Exception e) {
            System.err.println("Error getting distribution center by ID: " + e.getMessage());
        }
        return null;
    }
    
    // Add item to distribution center
    public boolean addItemToDistributionCenter(Long centerId, String name, String brand, 
                                             String category, Double price, Integer year, Integer quantity) {
        try {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("name", name);
            itemData.put("brand", brand);
            itemData.put("category", category);
            itemData.put("price", price);
            itemData.put("year", year);
            itemData.put("quantity", quantity);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(itemData, createAuthHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                distributionCenterApiUrl + "/" + centerId + "/items", 
                HttpMethod.POST, entity, String.class);
            
            return response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED;
        } catch (Exception e) {
            System.err.println("Error adding item to distribution center: " + e.getMessage());
            return false;
        }
    }
    
    // Delete item from distribution center
    public boolean deleteItemFromDistributionCenter(Long centerId, Long itemId) {
        try {
            HttpEntity<String> entity = new HttpEntity<>(createAuthHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                distributionCenterApiUrl + "/" + centerId + "/items/" + itemId, 
                HttpMethod.DELETE, entity, String.class);
            
            return response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.NO_CONTENT;
        } catch (Exception e) {
            System.err.println("Error deleting item from distribution center: " + e.getMessage());
            return false;
        }
    }
    
    // Request item with custom quantity
    public boolean requestItemFromClosestCenterWithQuantity(String brand, String name, Integer quantity) {
        try {
            // Find closest center with the item
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("brand", brand);
            requestBody.put("name", name);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, createAuthHeaders());
            
            String findClosestUrl = distributionCenterApiUrl + "/find-closest" +
                "?warehouseLatitude=" + warehouseLatitude +
                "&warehouseLongitude=" + warehouseLongitude;
            
            ResponseEntity<String> response = restTemplate.exchange(
                findClosestUrl, HttpMethod.POST, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode centerNode = objectMapper.readTree(response.getBody());
                Long centerId = centerNode.get("id").asLong();
                String centerName = centerNode.get("name").asText();
                
                System.out.println("Found item at: " + centerName + " (ID: " + centerId + ")");
                
                // Request the specified quantity from this center
                String requestUrl = distributionCenterApiUrl + "/" + centerId + "/request?quantity=" + quantity;
                
                // Create a new request body for the specific center request
                Map<String, Object> requestItemBody = new HashMap<>();
                requestItemBody.put("brand", brand);
                requestItemBody.put("name", name);
                HttpEntity<Map<String, Object>> requestItemEntity = new HttpEntity<>(requestItemBody, createAuthHeaders());
                
                ResponseEntity<String> requestResponse = restTemplate.exchange(
                    requestUrl, HttpMethod.POST, requestItemEntity, String.class);
                
                if (requestResponse.getStatusCode() == HttpStatus.OK) {
                    // Add items to warehouse stock
                    return addItemsToWarehouse(brand, name, quantity, centerNode);
                }
            }
        } catch (Exception e) {
            System.err.println("Error requesting item with quantity from distribution center: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    // Add multiple items to warehouse stock
    private boolean addItemsToWarehouse(String brand, String name, Integer quantity, JsonNode centerResponse) {
        try {
            // Check if item already exists in warehouse
            Optional<Item> existingItem = itemRepository.findByBrandAndName(brand, name)
                .stream().findFirst();
            
            if (existingItem.isPresent()) {
                // Update quantity
                Item item = existingItem.get();
                item.setQuantity(item.getQuantity() + quantity);
                itemRepository.save(item);
                System.out.println("Updated warehouse stock for: " + name + " by " + brand + " (+" + quantity + ")");
            } else {
                // Create new item in warehouse - get details from distribution center
                Item newItem = new Item();
                newItem.setName(name);
                newItem.setBrand(brand);
                
                // Try to get actual item details from the center response
                String category = "Unknown";
                BigDecimal price = new BigDecimal("1.00"); // Default positive price
                Integer year = 2023;
                
                if (centerResponse != null && centerResponse.has("items")) {
                    for (JsonNode itemNode : centerResponse.get("items")) {
                        if (itemNode.get("brand").asText().equals(brand) && 
                            itemNode.get("name").asText().equals(name)) {
                            category = itemNode.get("category").asText();
                            price = new BigDecimal(itemNode.get("price").asText());
                            year = itemNode.get("year").asInt();
                            break;
                        }
                    }
                }
                
                newItem.setCategory(category);
                newItem.setPrice(price);
                newItem.setYear(year);
                newItem.setQuantity(quantity);
                itemRepository.save(newItem);
                System.out.println("Added new item to warehouse: " + name + " by " + brand + " (quantity: " + quantity + ")");
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error adding items to warehouse: " + e.getMessage());
            return false;
        }
    }

    // Get all available items organized by brand (only items with quantity > 0)
    public Map<String, Object> getAvailableItemsByBrand() {
        try {
            HttpEntity<String> entity = new HttpEntity<>(createAuthHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                distributionCenterApiUrl, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                Map<String, Map<String, Integer>> brandItemMap = new HashMap<>(); // brand -> (itemName -> totalQuantity)
                Set<String> allBrands = new TreeSet<>();
                
                for (JsonNode centerNode : jsonNode) {
                    if (centerNode.has("items") && centerNode.get("items").isArray()) {
                        for (JsonNode itemNode : centerNode.get("items")) {
                            if (itemNode.has("name") && itemNode.has("brand") && itemNode.has("quantity")) {
                                int quantity = itemNode.get("quantity").asInt();
                                if (quantity > 0) { // Only include items with available stock
                                    String name = itemNode.get("name").asText();
                                    String brand = itemNode.get("brand").asText();
                                    
                                    allBrands.add(brand);
                                    
                                    brandItemMap.computeIfAbsent(brand, k -> new HashMap<>());
                                    brandItemMap.get(brand).merge(name, quantity, Integer::sum);
                                }
                            }
                        }
                    }
                }
                
                Map<String, Object> result = new HashMap<>();
                result.put("brands", new ArrayList<>(allBrands));
                result.put("itemsByBrand", brandItemMap);
                
                return result;
            }
        } catch (Exception e) {
            System.err.println("Error getting available items by brand: " + e.getMessage());
        }
        return Collections.emptyMap();
    }
    
    // Calculate distance using Haversine formula
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c; // Distance in kilometers
    }
} 