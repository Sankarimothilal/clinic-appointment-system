package com.example.demo.controller;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.model.Patient;
import com.example.demo.model.Role;
import com.example.demo.repository.PatientRepository;
import com.example.demo.repository.DoctorRepository;
import com.example.demo.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // REGISTER - Patient only
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        try {
            if (patientRepository.existsByEmail(request.getEmail())) {
                AuthResponse response = new AuthResponse();
                response.setMessage("Email already exists!");
                return ResponseEntity.badRequest().body(response);
            }

            Patient patient = new Patient();
            patient.setName(request.getName());
            patient.setEmail(request.getEmail());
            patient.setPassword(passwordEncoder.encode(request.getPassword()));
            patient.setPhone(request.getPhone());
            patient.setAge(request.getAge());
            patient.setGender(request.getGender());
            patient.setRole(Role.PATIENT);

            patientRepository.save(patient);

            String token = jwtUtil.generateToken(
                patient.getEmail(),
                patient.getRole().name()
            );

            AuthResponse response = new AuthResponse();
            response.setToken(token);
            response.setRole("PATIENT");
            response.setName(patient.getName());
            response.setMessage("Registered successfully!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            AuthResponse response = new AuthResponse();
            response.setMessage("Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // LOGIN - Patient
    @PostMapping("/login/patient")
    public ResponseEntity<AuthResponse> loginPatient(@RequestBody AuthRequest request) {
        try {
            Optional<Patient> patientOpt = patientRepository.findByEmail(request.getEmail());
            if (patientOpt.isEmpty()) {
                AuthResponse response = new AuthResponse();
                response.setMessage("Patient not found!");
                return ResponseEntity.badRequest().body(response);
            }

            Patient patient = patientOpt.get();
            if (!passwordEncoder.matches(request.getPassword(), patient.getPassword())) {
                AuthResponse response = new AuthResponse();
                response.setMessage("Wrong password!");
                return ResponseEntity.badRequest().body(response);
            }

            String token = jwtUtil.generateToken(
                patient.getEmail(),
                patient.getRole().name()
            );

            AuthResponse response = new AuthResponse();
            response.setToken(token);
            response.setRole("PATIENT");
            response.setName(patient.getName());
            response.setMessage("Login successful!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            AuthResponse response = new AuthResponse();
            response.setMessage("Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // LOGIN - Doctor
    @PostMapping("/login/doctor")
    public ResponseEntity<AuthResponse> loginDoctor(@RequestBody AuthRequest request) {
        try {
            var doctorOpt = doctorRepository.findByEmail(request.getEmail());
            if (doctorOpt.isEmpty()) {
                AuthResponse response = new AuthResponse();
                response.setMessage("Doctor not found!");
                return ResponseEntity.badRequest().body(response);
            }

            var doctor = doctorOpt.get();
            if (!passwordEncoder.matches(request.getPassword(), doctor.getPassword())) {
                AuthResponse response = new AuthResponse();
                response.setMessage("Wrong password!");
                return ResponseEntity.badRequest().body(response);
            }

            String token = jwtUtil.generateToken(
                doctor.getEmail(),
                doctor.getRole().name()
            );

            AuthResponse response = new AuthResponse();
            response.setToken(token);
            response.setRole("DOCTOR");
            response.setName(doctor.getName());
            response.setMessage("Login successful!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            AuthResponse response = new AuthResponse();
            response.setMessage("Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}