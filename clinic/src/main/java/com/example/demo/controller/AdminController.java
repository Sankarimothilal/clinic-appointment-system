package com.example.demo.controller;

import com.example.demo.model.Doctor;
import com.example.demo.model.Role;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.DoctorRepository;
import com.example.demo.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // GET dashboard stats
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalDoctors", doctorRepository.count());
            stats.put("totalPatients", patientRepository.count());
            stats.put("totalAppointments", appointmentRepository.count());
            stats.put("confirmedAppointments",
                appointmentRepository.countByStatus(
                    com.example.demo.model.Appointment.AppointmentStatus.CONFIRMED));
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // GET all doctors
    @GetMapping("/doctors")
    public ResponseEntity<?> getAllDoctors() {
        try {
            return ResponseEntity.ok(doctorRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // GET all patients
    @GetMapping("/patients")
    public ResponseEntity<?> getAllPatients() {
        try {
            return ResponseEntity.ok(patientRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // GET all appointments
    @GetMapping("/appointments")
    public ResponseEntity<?> getAllAppointments() {
        try {
            return ResponseEntity.ok(appointmentRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // ADD new doctor
    @PostMapping("/doctors/add")
    public ResponseEntity<?> addDoctor(@RequestBody Doctor doctor) {
        try {
            if (doctorRepository.existsByEmail(doctor.getEmail())) {
                return ResponseEntity.badRequest().body("Email already exists!");
            }
            doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));
            doctor.setRole(Role.DOCTOR);
            doctor.setIsAvailable(true);
            doctorRepository.save(doctor);
            return ResponseEntity.ok("Doctor added successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // TOGGLE doctor availability
    @PutMapping("/doctors/toggle/{id}")
    public ResponseEntity<?> toggleDoctor(@PathVariable Long id) {
        try {
            Doctor doctor = doctorRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));
            doctor.setIsAvailable(!doctor.getIsAvailable());
            doctorRepository.save(doctor);
            return ResponseEntity.ok(doctor.getIsAvailable() ? "Doctor activated!" : "Doctor deactivated!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // DELETE doctor
    @DeleteMapping("/doctors/delete/{id}")
    public ResponseEntity<?> deleteDoctor(@PathVariable Long id) {
        try {
            doctorRepository.deleteById(id);
            return ResponseEntity.ok("Doctor deleted successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
