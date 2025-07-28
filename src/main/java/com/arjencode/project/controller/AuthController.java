package com.arjencode.project.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "logout", required = false) String logout,
                               Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password.");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                              @RequestParam String password,
                              @RequestParam String confirmPassword,
                              @RequestParam String email,
                              @RequestParam String role,
                              RedirectAttributes redirectAttributes) {
        
        // Basic validation
        if (username == null || username.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Username is required.");
            return "redirect:/register";
        }
        
        if (password == null || password.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Password is required.");
            return "redirect:/register";
        }
        
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/register";
        }
        
        if (email == null || email.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Email is required.");
            return "redirect:/register";
        }
        
        if (role == null || role.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Role is required.");
            return "redirect:/register";
        }
        
        // For now, we'll just redirect to login with a success message
        // In a real application, you would save the user to the database
        redirectAttributes.addFlashAttribute("message", "Registration successful! Please login with your new credentials.");
        return "redirect:/login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
} 