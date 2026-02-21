package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@org.springframework.stereotype.Service
public class Service {

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public Service(
            TokenService tokenService,
            AdminRepository adminRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository,
            DoctorService doctorService,
            PatientService patientService
    ) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    public ResponseEntity<Map<String, String>> validateToken(String token, String user) {
        Map<String, String> res = new HashMap<>();
        boolean valid = tokenService.validateToken(token, user);
        if (!valid) {
            res.put("message", "Invalid or expired token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
        }
        return ResponseEntity.ok(Map.of());
    }

    public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
        Map<String, String> res = new HashMap<>();
        try {
            Admin existing = adminRepository.findByUsername(receivedAdmin.getUsername());
            if (existing == null || !existing.getPassword().equals(receivedAdmin.getPassword())) {
                res.put("message", "Invalid credentials");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
            }

            String token = tokenService.generateToken(existing.getUsername());
            res.put("token", token);
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            res.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    public Map<String, Object> filterDoctor(String name, String specialty, String time) {
        // "null" from frontend means no filter
        String n = normalize(name);
        String s = normalize(specialty);
        String t = normalize(time);

        if (n == null && s == null && t == null) {
            return Map.of("doctors", doctorService.getDoctors());
        }

        if (n != null && s != null && t != null) {
            return doctorService.filterDoctorsByNameSpecilityandTime(n, s, t);
        }
        if (n != null && t != null) {
            return doctorService.filterDoctorByNameAndTime(n, t);
        }
        if (n != null && s != null) {
            return doctorService.filterDoctorByNameAndSpecility(n, s);
        }
        if (s != null && t != null) {
            return doctorService.filterDoctorByTimeAndSpecility(s, t);
        }
        if (n != null) {
            return doctorService.findDoctorByName(n);
        }
        if (s != null) {
            return doctorService.filterDoctorBySpecility(s);
        }
        return doctorService.filterDoctorsByTime(t);
    }

    public int validateAppointment(Appointment appointment) {
        if (appointment == null || appointment.getDoctor() == null || appointment.getDoctor().getId() == null) return -1;

        Long doctorId = appointment.getDoctor().getId();
        var doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) return -1;

        // Check availability list for that day
        var available = doctorService.getDoctorAvailability(doctorId, appointment.getAppointmentTime().toLocalDate());
        String requestedSlot = appointment.getAppointmentTime().toLocalTime().toString(); // "09:00"
        boolean ok = available.stream().anyMatch(slot -> slot.startsWith(requestedSlot));
        return ok ? 1 : 0;
    }

    public boolean validatePatient(Patient patient) {
        Patient existing = patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone());
        return existing == null;
    }

    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
        Map<String, String> res = new HashMap<>();
        try {
            Patient p = patientRepository.findByEmail(login.getIdentifier());
            if (p == null || !p.getPassword().equals(login.getPassword())) {
                res.put("message", "Invalid credentials");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
            }
            String token = tokenService.generateToken(p.getEmail());
            res.put("token", token);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String name, String token) {
        try {
            String email = tokenService.extractIdentifier(token);
            Patient p = patientRepository.findByEmail(email);
            if (p == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized"));
            }

            Long patientId = p.getId();
            String c = normalize(condition);
            String n = normalize(name);

            if (c != null && n != null) return patientService.filterByDoctorAndCondition(c, n, patientId);
            if (c != null) return patientService.filterByCondition(c, patientId);
            if (n != null) return patientService.filterByDoctor(n, patientId);

            return patientService.getPatientAppointment(patientId, token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Internal server error"));
        }
    }

    private String normalize(String v) {
        if (v == null) return null;
        String t = v.trim();
        if (t.isEmpty() || "null".equalsIgnoreCase(t)) return null;
        return t;
    }
}