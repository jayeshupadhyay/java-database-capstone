package com.project.back_end.controllers;

import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("${api.path}appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final Service service;

    public AppointmentController(AppointmentService appointmentService, Service service) {
        this.appointmentService = appointmentService;
        this.service = service;
    }

    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @PathVariable String date,
            @PathVariable String patientName,
            @PathVariable String token
    ) {
        var tokenRes = service.validateToken(token, "doctor");
        if (!tokenRes.getBody().isEmpty()) {
            return ResponseEntity.status(tokenRes.getStatusCode()).body(Map.of("message", tokenRes.getBody().get("message")));
        }
        LocalDate d = LocalDate.parse(date);
        return ResponseEntity.ok(appointmentService.getAppointment(patientName, d, token));
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> bookAppointment(@Valid @RequestBody Appointment appointment, @PathVariable String token) {
        var tokenRes = service.validateToken(token, "patient");
        if (!tokenRes.getBody().isEmpty()) return ResponseEntity.status(tokenRes.getStatusCode()).body(tokenRes.getBody());

        int valid = service.validateAppointment(appointment);
        if (valid == -1) return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Invalid doctor id"));
        if (valid == 0) return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Appointment slot unavailable"));

        int booked = appointmentService.bookAppointment(appointment);
        if (booked == 1) return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Appointment booked"));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Internal server error"));
    }

    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateAppointment(@Valid @RequestBody Appointment appointment, @PathVariable String token) {
        var tokenRes = service.validateToken(token, "patient");
        if (!tokenRes.getBody().isEmpty()) return ResponseEntity.status(tokenRes.getStatusCode()).body(tokenRes.getBody());

        return appointmentService.updateAppointment(appointment);
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> cancelAppointment(@PathVariable long id, @PathVariable String token) {
        var tokenRes = service.validateToken(token, "patient");
        if (!tokenRes.getBody().isEmpty()) return ResponseEntity.status(tokenRes.getStatusCode()).body(tokenRes.getBody());

        return appointmentService.cancelAppointment(id, token);
    }
}