package com.arjencode.project.controller;

import com.arjencode.project.service.ItemService;
import com.arjencode.project.service.DistributionCenterIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    
    // Show error page
    @GetMapping("/error")
    public String showErrorPage(@RequestParam(required = false) String message, Model model) {
        model.addAttribute("errorMessage", message != null ? message : "An error occurred during the operation.");
        return "admin-error";
    }
} 