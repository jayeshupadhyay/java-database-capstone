package com.project.back_end.services;

import com.project.back_end.models.Prescription;
import com.project.back_end.repo.PrescriptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    public ResponseEntity<Map<String, String>> savePrescription(Prescription prescription) {
        try {
            prescriptionRepository.save(prescription);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Prescription saved"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Internal server error"));
        }
    }

    public ResponseEntity<Map<String, Object>> getPrescription(Long appointmentId) {
        try {
            List<Prescription> list = prescriptionRepository.findByAppointmentId(appointmentId);
            if (list == null || list.isEmpty()) {
                return ResponseEntity.ok(Map.of("message", "No prescription found for appointment", "prescription", null));
            }
            return ResponseEntity.ok(Map.of("prescription", list.get(0)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Internal server error"));
        }
    }
}