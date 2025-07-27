package com.arjencode.project.controller;

import com.arjencode.project.service.ItemService;
import com.arjencode.project.service.DistributionCenterIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    private final ItemService itemService;
    private final DistributionCenterIntegrationService distributionCenterService;
    
    @Autowired
    public AdminController(ItemService itemService, 
                          DistributionCenterIntegrationService distributionCenterService) {
        this.itemService = itemService;
        this.distributionCenterService = distributionCenterService;
    }
    
    // Show admin dashboard with distribution centers
    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model) {
        try {
            model.addAttribute("distributionCenters", distributionCenterService.getAllDistributionCenters());
            model.addAttribute("totalWarehouseItems", itemService.getAllItemsList().size());
        } catch (Exception e) {
            model.addAttribute("error", "Unable to load distribution centers: " + e.getMessage());
            model.addAttribute("distributionCenters", java.util.Collections.emptyList());
            model.addAttribute("totalWarehouseItems", itemService.getAllItemsList().size());
        }
        return "admin-dashboard";
    }
    
    // Request item from distribution center
    @PostMapping("/request-item")
    public String requestItem(@RequestParam String brand, 
                            @RequestParam String name,
                            RedirectAttributes redirectAttributes) {
        try {
            boolean success = distributionCenterService.requestItemFromClosestCenter(brand, name);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Item '" + name + "' by " + brand + " successfully requested and added to warehouse stock!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Item '" + name + "' by " + brand + " not available in any distribution center or stock replenishment failed.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error requesting item: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
    
    // Show distribution center details
    @GetMapping("/distribution-center/{id}")
    public String showDistributionCenterDetails(@PathVariable Long id, Model model) {
        try {
            Map<String, Object> center = distributionCenterService.getDistributionCenterById(id);
            model.addAttribute("distributionCenter", center);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Unable to load distribution center details: " + e.getMessage());
            return "redirect:/admin/dashboard";
        }
        return "admin-distribution-center-details";
    }
    
    // Add item to distribution center
    @PostMapping("/distribution-center/{id}/add-item")
    public String addItemToDistributionCenter(@PathVariable Long id,
                                             @RequestParam String name,
                                             @RequestParam String brand,
                                             @RequestParam String category,
                                             @RequestParam Double price,
                                             @RequestParam Integer year,
                                             @RequestParam Integer quantity,
                                             RedirectAttributes redirectAttributes) {
        try {
            boolean success = distributionCenterService.addItemToDistributionCenter(id, name, brand, category, price, year, quantity);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Item '" + name + "' successfully added to distribution center!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Failed to add item to distribution center.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error adding item: " + e.getMessage());
        }
        return "redirect:/admin/distribution-center/" + id;
    }
    
    // Delete item from distribution center
    @PostMapping("/distribution-center/{centerId}/delete-item/{itemId}")
    public String deleteItemFromDistributionCenter(@PathVariable Long centerId,
                                                   @PathVariable Long itemId,
                                                   RedirectAttributes redirectAttributes) {
        try {
            boolean success = distributionCenterService.deleteItemFromDistributionCenter(centerId, itemId);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Item successfully deleted from distribution center!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Failed to delete item from distribution center.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error deleting item: " + e.getMessage());
        }
        return "redirect:/admin/distribution-center/" + centerId;
    }
    
    // Request item with custom quantity
    @PostMapping("/request-item-with-quantity")
    public String requestItemWithQuantity(@RequestParam String brand, 
                                        @RequestParam String name,
                                        @RequestParam Integer quantity,
                                        RedirectAttributes redirectAttributes) {
        try {
            boolean success = distributionCenterService.requestItemFromClosestCenterWithQuantity(brand, name, quantity);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Successfully requested " + quantity + " x '" + name + "' by " + brand + " and added to warehouse stock!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Item '" + name + "' by " + brand + " not available in sufficient quantity in any distribution center.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error requesting item: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // Show error page
    @GetMapping("/error")
    public String showErrorPage(@RequestParam(required = false) String message, Model model) {
        model.addAttribute("errorMessage", message != null ? message : "An error occurred during the operation.");
        return "admin-error";
    }
} 