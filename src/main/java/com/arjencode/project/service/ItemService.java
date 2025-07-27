package com.arjencode.project.service;

import com.arjencode.project.model.Item;
import com.arjencode.project.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemService {
    
    private final ItemRepository itemRepository;
    
    @Autowired
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }
    
    // Save a new item
    public Item saveItem(Item item) {
        return itemRepository.save(item);
    }
    
    // Get all items with pagination and sorting
    public Page<Item> getAllItems(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return itemRepository.findAll(pageable);
    }
    
    // Get items by brand with pagination and sorting
    public Page<Item> getItemsByBrand(String brand, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return itemRepository.findByBrand(brand, pageable);
    }
    
    // Get items by year with pagination and sorting
    public Page<Item> getItemsByYear(Integer year, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return itemRepository.findByYear(year, pageable);
    }
    
    // Get items by brand and year with pagination and sorting
    public Page<Item> getItemsByBrandAndYear(String brand, Integer year, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return itemRepository.findByBrandAndYear(brand, year, pageable);
    }
    
    // Custom query to get items by brand and year 2022
    public List<Item> getItemsByBrandAndYear2022(String brand) {
        return itemRepository.findItemsByBrandAndYear2022(brand);
    }
    
    // Get item by ID
    public Item getItemById(Long id) {
        return itemRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Item not found with id: " + id));
    }
    
    // Get all items (without pagination for simple listing)
    public List<Item> getAllItemsList() {
        return itemRepository.findAll();
    }
    
    // Get items by category with pagination and sorting
    public Page<Item> getItemsByCategory(String category, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return itemRepository.findByCategory(category, pageable);
    }
    
    // Search items by name (case-insensitive)
    public Page<Item> searchItemsByName(String name, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return itemRepository.findByNameContainingIgnoreCase(name, pageable);
    }
    
    // Search items by brand (case-insensitive)
    public Page<Item> searchItemsByBrand(String brand, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return itemRepository.findByBrandContainingIgnoreCase(brand, pageable);
    }
} 