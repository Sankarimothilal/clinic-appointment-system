package com.example.demo.controller;

import com.example.demo.model.Appointment;
import com.example.demo.model.Doctor;
import com.example.demo.model.Patient;
import com.example.demo.model.Slot;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.DoctorRepository;
import com.example.demo.repository.PatientRepository;
import com.example.demo.repository.SlotRepository;
import com.example.demo.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private SlotRepository slotRepository;

    @Autowired
    private EmailService emailService;

    // BOOK Appointment
    @PostMapping("/book")
    public ResponseEntity<?> bookAppointment(
            @RequestParam Long patientId,
            @RequestParam Long doctorId,
            @RequestParam Long slotId,
            @RequestParam(required = false) String reason) {
        try {
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new RuntimeException("Patient not found"));
            Doctor doctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));
            Slot slot = slotRepository.findById(slotId)
                    .orElseThrow(() -> new RuntimeException("Slot not found"));

            if (slot.getIsBooked()) {
                return ResponseEntity.badRequest().body("Slot already booked!");
            }

            slot.setIsBooked(true);
            slot.setStatus(Slot.SlotStatus.BOOKED);
            slotRepository.save(slot);

            Appointment appointment = new Appointment();
            appointment.setPatient(patient);
            appointment.setDoctor(doctor);
            appointment.setSlot(slot);
            appointment.setReason(reason);
            appointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
         // Generate token number
            Integer maxToken = appointmentRepository
                .findMaxTokenByDoctorAndDate(doctorId, slot.getDate());
            appointment.setTokenNumber(maxToken != null ? maxToken + 1 : 1);
            appointmentRepository.save(appointment);

            // Send confirmation email
            emailService.sendAppointmentConfirmation(
                patient.getEmail(),
                patient.getName(),
                doctor.getName(),
                slot.getDate().toString(),
                slot.getStartTime().toString()
            );

            return ResponseEntity.ok("Appointment booked successfully!");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // GET Patient appointments
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getPatientAppointments(@PathVariable Long patientId) {
        try {
            return ResponseEntity.ok(appointmentRepository.findByPatientId(patientId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // GET Doctor appointments
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<?> getDoctorAppointments(@PathVariable Long doctorId) {
        try {
            return ResponseEntity.ok(appointmentRepository.findByDoctorId(doctorId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // CANCEL Appointment
    @PutMapping("/cancel/{appointmentId}")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long appointmentId) {
        try {
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));
            appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);

            Slot slot = appointment.getSlot();
            slot.setIsBooked(false);
            slot.setStatus(Slot.SlotStatus.AVAILABLE);
            slotRepository.save(slot);
            appointmentRepository.save(appointment);

            // Send cancellation email
            emailService.sendCancellationEmail(
                appointment.getPatient().getEmail(),
                appointment.getPatient().getName(),
                appointment.getDoctor().getName(),
                slot.getDate().toString(),
                slot.getStartTime().toString()
            );

            return ResponseEntity.ok("Appointment cancelled successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // COMPLETE Appointment
    @PutMapping("/complete/{appointmentId}")
    public ResponseEntity<?> completeAppointment(@PathVariable Long appointmentId) {
        try {
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));
            appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
            appointmentRepository.save(appointment);
            return ResponseEntity.ok("Appointment marked as completed!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}