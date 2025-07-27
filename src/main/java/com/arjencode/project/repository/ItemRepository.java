package com.arjencode.project.repository;

import com.arjencode.project.model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    
    // Find all items with pagination and sorting
    Page<Item> findAll(Pageable pageable);
    
    // Find items by brand with pagination and sorting
    Page<Item> findByBrand(String brand, Pageable pageable);
    
    // Find items by year with pagination and sorting
    Page<Item> findByYear(Integer year, Pageable pageable);
    
    // Find items by brand and year with pagination and sorting
    Page<Item> findByBrandAndYear(String brand, Integer year, Pageable pageable);
    
    // Find items by brand and name (for integration service)
    List<Item> findByBrandAndName(String brand, String name);
    
    // Custom query to find items by specific brand and year 2022
    @Query("SELECT i FROM Item i WHERE i.brand = :brand AND i.year = 2022 ORDER BY i.name ASC")
    List<Item> findItemsByBrandAndYear2022(@Param("brand") String brand);
    
    // Find items by category with pagination and sorting
    Page<Item> findByCategory(String category, Pageable pageable);
    
    // Find items by price range with pagination and sorting
    Page<Item> findByPriceBetween(java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, Pageable pageable);
    
    // Find items by brand containing (case-insensitive search)
    Page<Item> findByBrandContainingIgnoreCase(String brand, Pageable pageable);
    
    // Find items by name containing (case-insensitive search)
    Page<Item> findByNameContainingIgnoreCase(String name, Pageable pageable);
} 