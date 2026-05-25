package com.example.demo.controller;

import com.example.demo.model.Doctor;
import com.example.demo.model.Role;
import com.example.demo.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ADD Doctor (Admin only)
    @PostMapping("/add")
    public ResponseEntity<?> addDoctor(@RequestBody Doctor doctor) {
        try {
            if (doctorRepository.existsByEmail(doctor.getEmail())) {
                return ResponseEntity.badRequest().body("Email already exists!");
            }
            doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));
            doctor.setRole(Role.DOCTOR);
            doctorRepository.save(doctor);
            return ResponseEntity.ok("Doctor added successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // GET All Doctors (Patients can view)
    @GetMapping("/all")
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return ResponseEntity.ok(doctorRepository.findAll());
    }

    // GET Doctor by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getDoctorById(@PathVariable Long id) {
        return doctorRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
