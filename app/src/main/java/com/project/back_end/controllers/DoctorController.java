package com.project.back_end.controllers;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("${api.path}doctor")
public class DoctorController {

    private final DoctorService doctorService;
    private final Service service;

    public DoctorController(DoctorService doctorService, Service service) {
        this.doctorService = doctorService;
        this.service = service;
    }

    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getAvailability(
            @PathVariable String user,
            @PathVariable Long doctorId,
            @PathVariable String date,
            @PathVariable String token
    ) {
        var tokenRes = service.validateToken(token, user);
        if (!tokenRes.getBody().isEmpty()) {
            return ResponseEntity.status(tokenRes.getStatusCode()).body(Map.of("message", tokenRes.getBody().get("message")));
        }
        LocalDate d = LocalDate.parse(date);
        return ResponseEntity.ok(Map.of("availability", doctorService.getDoctorAvailability(doctorId, d)));
    }

    @GetMapping
    public Map<String, Object> getDoctors() {
        return Map.of("doctors", doctorService.getDoctors());
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> addDoctor(@Valid @RequestBody Doctor doctor, @PathVariable String token) {
        var tokenRes = service.validateToken(token, "admin");
        if (!tokenRes.getBody().isEmpty()) return ResponseEntity.status(tokenRes.getStatusCode()).body(tokenRes.getBody());

        int saved = doctorService.saveDoctor(doctor);
        if (saved == 1) return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Doctor added to db"));
        if (saved == -1) return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Doctor already exists"));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Some internal error occurred"));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> doctorLogin(@RequestBody Login login) {
        return doctorService.validateDoctor(login);
    }

    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateDoctor(@Valid @RequestBody Doctor doctor, @PathVariable String token) {
        var tokenRes = service.validateToken(token, "admin");
        if (!tokenRes.getBody().isEmpty()) return ResponseEntity.status(tokenRes.getStatusCode()).body(tokenRes.getBody());

        int updated = doctorService.updateDoctor(doctor);
        if (updated == 1) return ResponseEntity.ok(Map.of("message", "Doctor updated"));
        if (updated == -1) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Doctor not found"));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Some internal error occurred"));
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> deleteDoctor(@PathVariable long id, @PathVariable String token) {
        var tokenRes = service.validateToken(token, "admin");
        if (!tokenRes.getBody().isEmpty()) return ResponseEntity.status(tokenRes.getStatusCode()).body(tokenRes.getBody());

        int deleted = doctorService.deleteDoctor(id);
        if (deleted == 1) return ResponseEntity.ok(Map.of("message", "Doctor deleted successfully"));
        if (deleted == -1) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Doctor not found with id"));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Some internal error occurred"));
    }

    @GetMapping("/filter/{name}/{time}/{speciality}")
    public Map<String, Object> filterDoctors(@PathVariable String name, @PathVariable String time, @PathVariable String speciality) {
        return service.filterDoctor(name, speciality, time);
    }
}