package com.project.back_end.controllers;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Patient;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("${api.path}patient")
public class PatientController {

    private final PatientService patientService;
    private final Service service;

    public PatientController(PatientService patientService, Service service) {
        this.patientService = patientService;
        this.service = service;
    }

    @GetMapping("/{token}")
    public ResponseEntity<Map<String, Object>> getPatient(@PathVariable String token) {
        var tokenRes = service.validateToken(token, "patient");
        if (!tokenRes.getBody().isEmpty()) {
            return ResponseEntity.status(tokenRes.getStatusCode()).body(Map.of("message", tokenRes.getBody().get("message")));
        }
        return patientService.getPatientDetails(token);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createPatient(@Valid @RequestBody Patient patient) {
        boolean ok = service.validatePatient(patient);
        if (!ok) return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Patient with email id or phone no already exist"));

        int created = patientService.createPatient(patient);
        if (created == 1) return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Signup successful"));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Internal server error"));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Login login) {
        return service.validatePatientLogin(login);
    }

    // Supports frontend format: /patient/{id}/{user}/{token}
    @GetMapping("/{id}/{user}/{token}")
    public ResponseEntity<Map<String, Object>> getPatientAppointment(@PathVariable Long id, @PathVariable String user, @PathVariable String token) {
        var tokenRes = service.validateToken(token, user);
        if (!tokenRes.getBody().isEmpty()) {
            return ResponseEntity.status(tokenRes.getStatusCode()).body(Map.of("message", tokenRes.getBody().get("message")));
        }
        return patientService.getPatientAppointment(id, token);
    }

    @GetMapping("/filter/{condition}/{name}/{token}")
    public ResponseEntity<Map<String, Object>> filterPatientAppointment(@PathVariable String condition, @PathVariable String name, @PathVariable String token) {
        var tokenRes = service.validateToken(token, "patient");
        if (!tokenRes.getBody().isEmpty()) {
            return ResponseEntity.status(tokenRes.getStatusCode()).body(Map.of("message", tokenRes.getBody().get("message")));
        }
        return service.filterPatient(condition, name, token);
    }
}