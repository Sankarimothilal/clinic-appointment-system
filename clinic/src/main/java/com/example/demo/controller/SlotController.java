package com.example.demo.controller;

import com.example.demo.model.Doctor;
import com.example.demo.model.Slot;
import com.example.demo.repository.DoctorRepository;
import com.example.demo.repository.SlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/slots")
public class SlotController {

    @Autowired
    private SlotRepository slotRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    // ADD Slot for a doctor
    @PostMapping("/add/{doctorId}")
    public ResponseEntity<?> addSlot(@PathVariable Long doctorId,
                                      @RequestBody Slot slot) {
        try {
            Doctor doctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));
            slot.setDoctor(doctor);
            slot.setIsBooked(false);
            slot.setStatus(Slot.SlotStatus.AVAILABLE);
            slotRepository.save(slot);
            return ResponseEntity.ok("Slot added successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // GET Available slots for a doctor on a date
    @GetMapping("/available/{doctorId}")
    public ResponseEntity<?> getAvailableSlots(@PathVariable Long doctorId,
                                                @RequestParam String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            List<Slot> slots = slotRepository
                    .findByDoctorIdAndDate(doctorId, localDate);
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
