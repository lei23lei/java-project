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