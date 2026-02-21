package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@org.springframework.stereotype.Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public DoctorService(DoctorRepository doctorRepository,
                         AppointmentRepository appointmentRepository,
                         TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        Optional<Doctor> docOpt = doctorRepository.findById(doctorId);
        if (docOpt.isEmpty()) return List.of();

        Doctor doctor = docOpt.get();
        List<String> baseSlots = doctor.getAvailableTimes() == null ? new ArrayList<>() : new ArrayList<>(doctor.getAvailableTimes());

        // remove booked slots for that date
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay().minusNanos(1);

        List<Appointment> booked = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end);

        Set<String> bookedStartTimes = new HashSet<>();
        for (Appointment a : booked) {
            bookedStartTimes.add(a.getAppointmentTime().toLocalTime().toString()); // "09:00"
        }

        List<String> available = new ArrayList<>();
        for (String slot : baseSlots) {
            // slot format "09:00-10:00" OR "09:00 -10:00"
            String normalized = slot.replace(" ", "");
            String startTime = normalized.split("-")[0];
            if (!bookedStartTimes.contains(startTime)) {
                available.add(normalized);
            }
        }
        return available;
    }

    public int saveDoctor(Doctor doctor) {
        try {
            Doctor existing = doctorRepository.findByEmail(doctor.getEmail());
            if (existing != null) return -1;

            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public int updateDoctor(Doctor doctor) {
        try {
            if (doctor.getId() == null) return -1;
            Optional<Doctor> existing = doctorRepository.findById(doctor.getId());
            if (existing.isEmpty()) return -1;

            Doctor d = existing.get();
            d.setName(doctor.getName());
            d.setEmail(doctor.getEmail());
            d.setPhone(doctor.getPhone());
            d.setPassword(doctor.getPassword());
            d.setSpecialty(doctor.getSpecialty());
            d.setAvailableTimes(doctor.getAvailableTimes());

            doctorRepository.save(d);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public List<Doctor> getDoctors() {
        return doctorRepository.findAll();
    }

    public int deleteDoctor(long id) {
        try {
            Optional<Doctor> existing = doctorRepository.findById(id);
            if (existing.isEmpty()) return -1;

            appointmentRepository.deleteAllByDoctorId(id);
            doctorRepository.deleteById(id);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
        try {
            Doctor d = doctorRepository.findByEmail(login.getIdentifier());
            if (d == null || !d.getPassword().equals(login.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid credentials"));
            }
            String token = tokenService.generateToken(d.getEmail());
            return ResponseEntity.ok(Map.of("token", token));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Internal server error"));
        }
    }

    public Map<String, Object> findDoctorByName(String name) {
        return Map.of("doctors", doctorRepository.findByNameLike(name));
    }

    public Map<String, Object> filterDoctorsByNameSpecilityandTime(String name, String specialty, String amOrPm) {
        List<Doctor> docs = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        return Map.of("doctors", filterDoctorByTime(docs, amOrPm));
    }

    public Map<String, Object> filterDoctorByNameAndTime(String name, String amOrPm) {
        List<Doctor> docs = doctorRepository.findByNameLike(name);
        return Map.of("doctors", filterDoctorByTime(docs, amOrPm));
    }

    public Map<String, Object> filterDoctorByNameAndSpecility(String name, String specilty) {
        List<Doctor> docs = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specilty);
        return Map.of("doctors", docs);
    }

    public Map<String, Object> filterDoctorByTimeAndSpecility(String specilty, String amOrPm) {
        List<Doctor> docs = doctorRepository.findBySpecialtyIgnoreCase(specilty);
        return Map.of("doctors", filterDoctorByTime(docs, amOrPm));
    }

    public Map<String, Object> filterDoctorBySpecility(String specilty) {
        return Map.of("doctors", doctorRepository.findBySpecialtyIgnoreCase(specilty));
    }

    public Map<String, Object> filterDoctorsByTime(String amOrPm) {
        return Map.of("doctors", filterDoctorByTime(doctorRepository.findAll(), amOrPm));
    }

    private List<Doctor> filterDoctorByTime(List<Doctor> doctors, String amOrPm) {
        if (amOrPm == null || amOrPm.isBlank() || "null".equalsIgnoreCase(amOrPm)) return doctors;

        String target = amOrPm.trim().toUpperCase(Locale.ROOT);

        List<Doctor> filtered = new ArrayList<>();
        for (Doctor d : doctors) {
            List<String> times = d.getAvailableTimes() == null ? List.of() : d.getAvailableTimes();
            boolean match = times.stream().anyMatch(slot -> {
                String s = slot.replace(" ", "");
                // basic AM/PM inference from start hour
                String start = s.split("-")[0]; // "09:00"
                int hour = Integer.parseInt(start.split(":")[0]);
                boolean isAm = hour < 12;
                return ("AM".equals(target) && isAm) || ("PM".equals(target) && !isAm);
            });
            if (match) filtered.add(d);
        }
        return filtered;
    }
}