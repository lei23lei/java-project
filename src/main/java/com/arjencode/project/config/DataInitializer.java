package com.arjencode.project.config;

import com.arjencode.project.model.Item;
import com.arjencode.project.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private final ItemRepository itemRepository;
    
    @Autowired
    public DataInitializer(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }
    
    @Override
    public void run(String... args) throws Exception {
        // Check if data already exists
        if (itemRepository.count() == 0) {
            // Populate with sample clothes items
            itemRepository.save(new Item("Nike Air Max 270", "Nike", "Shoes", new BigDecimal("129.99"), 2022));
            itemRepository.save(new Item("Adidas Ultraboost 22", "Adidas", "Shoes", new BigDecimal("179.99"), 2022));
            itemRepository.save(new Item("Levi's 501 Original Jeans", "Levi's", "Pants", new BigDecimal("89.99"), 2022));
            itemRepository.save(new Item("Calvin Klein T-Shirt", "Calvin Klein", "Shirts", new BigDecimal("29.99"), 2022));
            itemRepository.save(new Item("Tommy Hilfiger Polo", "Tommy Hilfiger", "Shirts", new BigDecimal("59.99"), 2022));
            itemRepository.save(new Item("Nike Dri-FIT Shorts", "Nike", "Shorts", new BigDecimal("34.99"), 2022));
            itemRepository.save(new Item("Adidas Track Jacket", "Adidas", "Jackets", new BigDecimal("79.99"), 2022));
            itemRepository.save(new Item("Levi's Denim Jacket", "Levi's", "Jackets", new BigDecimal("119.99"), 2022));
            itemRepository.save(new Item("Calvin Klein Underwear", "Calvin Klein", "Underwear", new BigDecimal("24.99"), 2022));
            itemRepository.save(new Item("Tommy Hilfiger Sweater", "Tommy Hilfiger", "Sweaters", new BigDecimal("89.99"), 2022));
            
            // Add some items from other years for variety
            itemRepository.save(new Item("Nike Air Jordan 1", "Nike", "Shoes", new BigDecimal("169.99"), 2021));
            itemRepository.save(new Item("Adidas Stan Smith", "Adidas", "Shoes", new BigDecimal("89.99"), 2021));
            itemRepository.save(new Item("Levi's 511 Slim Jeans", "Levi's", "Pants", new BigDecimal("79.99"), 2021));
            itemRepository.save(new Item("Calvin Klein Hoodie", "Calvin Klein", "Hoodies", new BigDecimal("69.99"), 2021));
            itemRepository.save(new Item("Tommy Hilfiger Jeans", "Tommy Hilfiger", "Pants", new BigDecimal("99.99"), 2021));
            
            // Add some items from 2023
            itemRepository.save(new Item("Nike Air Force 1", "Nike", "Shoes", new BigDecimal("109.99"), 2023));
            itemRepository.save(new Item("Adidas Gazelle", "Adidas", "Shoes", new BigDecimal("84.99"), 2023));
            itemRepository.save(new Item("Levi's 721 High Rise", "Levi's", "Pants", new BigDecimal("94.99"), 2023));
            itemRepository.save(new Item("Calvin Klein Dress", "Calvin Klein", "Dresses", new BigDecimal("129.99"), 2023));
            itemRepository.save(new Item("Tommy Hilfiger Blazer", "Tommy Hilfiger", "Blazers", new BigDecimal("199.99"), 2023));
            
            System.out.println("Sample clothes data has been initialized!");
        } else {
            System.out.println("Database already contains data. Skipping initialization.");
        }
    }
} 