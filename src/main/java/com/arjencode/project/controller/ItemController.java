package com.arjencode.project.controller;

import com.arjencode.project.model.Item;
import com.arjencode.project.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/items")
public class ItemController {
    
    private final ItemService itemService;
    
    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }
    
    // Show form to add new item
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("item", new Item());
        return "add-item";
    }
    
    // Handle form submission to add new item
    @PostMapping("/add")
    public String addItem(@Valid @ModelAttribute("item") Item item, 
                         BindingResult result, 
                         Model model) {
        if (result.hasErrors()) {
            return "add-item";
        }
        
        itemService.saveItem(item);
        return "redirect:/items/list";
    }
    
    // Show list of all items with pagination and sorting
    @GetMapping("/list")
    public String listItems(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(defaultValue = "id") String sortBy,
                           @RequestParam(defaultValue = "asc") String sortDir,
                           Model model) {
        
        Page<Item> itemPage = itemService.getAllItems(page, size, sortBy, sortDir);
        
        model.addAttribute("items", itemPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", itemPage.getTotalPages());
        model.addAttribute("totalItems", itemPage.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        
        return "list-items";
    }
    
    // Show items filtered by brand and year 2022
    @GetMapping("/filter")
    public String filterItemsByBrandAndYear2022(@RequestParam String brand, Model model) {
        List<Item> filteredItems = itemService.getItemsByBrandAndYear2022(brand);
        model.addAttribute("items", filteredItems);
        model.addAttribute("filteredBrand", brand);
        model.addAttribute("filteredYear", 2022);
        return "list-items";
    }
    
    // Show items by brand with pagination and sorting
    @GetMapping("/brand/{brand}")
    public String listItemsByBrand(@PathVariable String brand,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(defaultValue = "name") String sortBy,
                                  @RequestParam(defaultValue = "asc") String sortDir,
                                  Model model) {
        
        Page<Item> itemPage = itemService.getItemsByBrand(brand, page, size, sortBy, sortDir);
        
        model.addAttribute("items", itemPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", itemPage.getTotalPages());
        model.addAttribute("totalItems", itemPage.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("filteredBrand", brand);
        
        return "list-items";
    }
    
    // Show items by year with pagination and sorting
    @GetMapping("/year/{year}")
    public String listItemsByYear(@PathVariable Integer year,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(defaultValue = "name") String sortBy,
                                 @RequestParam(defaultValue = "asc") String sortDir,
                                 Model model) {
        
        Page<Item> itemPage = itemService.getItemsByYear(year, page, size, sortBy, sortDir);
        
        model.addAttribute("items", itemPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", itemPage.getTotalPages());
        model.addAttribute("totalItems", itemPage.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("filteredYear", year);
        
        return "list-items";
    }
    
    // Show item details
    @GetMapping("/{id}")
    public String showItemDetails(@PathVariable Long id, Model model) {
        Item item = itemService.getItemById(id);
        model.addAttribute("item", item);
        return "item-details";
    }
    
    // Show form to edit item
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Item item = itemService.getItemById(id);
        model.addAttribute("item", item);
        return "edit-item";
    }
    
    // Handle form submission to update item
    @PostMapping("/edit/{id}")
    public String updateItem(@PathVariable Long id,
                           @Valid @ModelAttribute("item") Item item,
                           BindingResult result,
                           Model model) {
        if (result.hasErrors()) {
            item.setId(id); // Ensure the ID is preserved
            return "edit-item";
        }
        
        item.setId(id); // Ensure the ID is set for update
        itemService.saveItem(item);
        return "redirect:/items/list";
    }
    
    // Delete item
    @GetMapping("/delete/{id}")
    public String deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return "redirect:/items/list";
    }
    
    // Home page redirect to list
    @GetMapping("/")
    public String home() {
        return "redirect:/items/list";
    }
} 