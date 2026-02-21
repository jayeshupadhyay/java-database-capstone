package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@org.springframework.stereotype.Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TokenService tokenService;
    private final Service service;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              TokenService tokenService,
                              Service service) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.tokenService = tokenService;
        this.service = service;
    }

    @Transactional
    public int bookAppointment(Appointment appointment) {
        try {
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment) {
        try {
            Map<String, String> res = new HashMap<>();
            if (appointment.getId() == null) {
                res.put("message", "Appointment id missing");
                return ResponseEntity.badRequest().body(res);
            }

            Optional<Appointment> existingOpt = appointmentRepository.findById(appointment.getId());
            if (existingOpt.isEmpty()) {
                res.put("message", "Appointment not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
            }

            Appointment existing = existingOpt.get();

            // Ensure same patient
            if (appointment.getPatient() != null && appointment.getPatient().getId() != null) {
                if (!existing.getPatient().getId().equals(appointment.getPatient().getId())) {
                    res.put("message", "Unauthorized");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
                }
            }

            // Validate appointment slot
            int valid = service.validateAppointment(appointment);
            if (valid == -1) {
                res.put("message", "Invalid doctor id");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
            }
            if (valid == 0) {
                res.put("message", "Appointment slot unavailable");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
            }

            existing.setAppointmentTime(appointment.getAppointmentTime());
            existing.setStatus(appointment.getStatus());
            existing.setDoctor(appointment.getDoctor());
            existing.setPatient(appointment.getPatient());

            appointmentRepository.save(existing);
            res.put("message", "Appointment updated");
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Internal server error"));
        }
    }

    @Transactional
    public ResponseEntity<Map<String, String>> cancelAppointment(long id, String token) {
        try {
            Map<String, String> res = new HashMap<>();
            Optional<Appointment> existingOpt = appointmentRepository.findById(id);
            if (existingOpt.isEmpty()) {
                res.put("message", "Appointment not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
            }

            Appointment appt = existingOpt.get();
            String email = tokenService.extractIdentifier(token);
            Patient p = patientRepository.findByEmail(email);
            if (p == null || !p.getId().equals(appt.getPatient().getId())) {
                res.put("message", "Unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
            }

            appointmentRepository.delete(appt);
            res.put("message", "Appointment cancelled");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Internal server error"));
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAppointment(String pname, LocalDate date, String token) {
        String doctorEmail = tokenService.extractIdentifier(token);
        Doctor doc = doctorRepository.findByEmail(doctorEmail);

        if (doc == null) return Map.of("appointments", List.of());

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay().minusNanos(1);

        List<Appointment> appointments;
        if (pname == null || pname.isBlank() || "null".equalsIgnoreCase(pname)) {
            appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doc.getId(), start, end);
        } else {
            appointments = appointmentRepository.findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                    doc.getId(), pname, start, end
            );
        }

        return Map.of("appointments", appointments);
    }

    @Transactional
    public void changeStatus(int status, long id) {
        appointmentRepository.updateStatus(status, id);
    }
}