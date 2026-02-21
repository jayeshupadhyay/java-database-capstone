package com.project.back_end.controllers;

import com.project.back_end.models.Prescription;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.PrescriptionService;
import com.project.back_end.services.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("${api.path}prescription")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final Service service;
    private final AppointmentService appointmentService;

    public PrescriptionController(PrescriptionService prescriptionService, Service service, AppointmentService appointmentService) {
        this.prescriptionService = prescriptionService;
        this.service = service;
        this.appointmentService = appointmentService;
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> savePrescription(@PathVariable String token, @Valid @RequestBody Prescription prescription) {
        var tokenRes = service.validateToken(token, "doctor");
        if (!tokenRes.getBody().isEmpty()) {
            return ResponseEntity.status(tokenRes.getStatusCode()).body(tokenRes.getBody());
        }

        // when prescription is added, mark appointment completed (status = 1)
        if (prescription.getAppointmentId() != null) {
            appointmentService.changeStatus(1, prescription.getAppointmentId());
        }

        return prescriptionService.savePrescription(prescription);
    }

    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<Map<String, Object>> getPrescription(@PathVariable Long appointmentId, @PathVariable String token) {
        var tokenRes = service.validateToken(token, "doctor");
        if (!tokenRes.getBody().isEmpty()) {
            return ResponseEntity.status(tokenRes.getStatusCode()).body(Map.of("message", tokenRes.getBody().get("message")));
        }
        return prescriptionService.getPrescription(appointmentId);
    }
}