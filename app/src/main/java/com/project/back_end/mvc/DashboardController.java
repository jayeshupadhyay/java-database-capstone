package com.project.back_end.mvc;

import com.project.back_end.services.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class DashboardController {

    private final TokenService tokenService;

    @Autowired
    public DashboardController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable String token) {
        boolean valid = tokenService.validateToken(token, "admin");
        return valid ? "admin/adminDashboard" : "redirect:/";
    }

    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable String token) {
        boolean valid = tokenService.validateToken(token, "doctor");
        return valid ? "doctor/doctorDashboard" : "redirect:/";
    }
}