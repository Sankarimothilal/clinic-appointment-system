package com.example.demo.controller;

import com.example.demo.model.Appointment;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.service.EmailService;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/prescription")
public class PrescriptionController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private EmailService emailService;

    @GetMapping("/generate/{appointmentId}")
    public void generatePrescription(
            @PathVariable Long appointmentId,
            @RequestParam(required = false) String medicines,
            @RequestParam(required = false) String notes,
            HttpServletResponse response) throws IOException {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Generate PDF into byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Header
        document.add(new Paragraph("🏥 ClinicEase")
                .setFontSize(24).setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.BLUE));

        document.add(new Paragraph("PRESCRIPTION")
                .setFontSize(16).setBold()
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("─────────────────────────────────────────")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY));

        // Patient & Doctor Info
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(100));

        infoTable.addCell(new Cell().add(
            new Paragraph("Patient: " + appointment.getPatient().getName()).setBold()));
        infoTable.addCell(new Cell().add(
            new Paragraph("Doctor: " + appointment.getDoctor().getName()).setBold()));
        infoTable.addCell(new Cell().add(
            new Paragraph("Age: " + (appointment.getPatient().getAge() != null ?
                appointment.getPatient().getAge() : "N/A"))));
        infoTable.addCell(new Cell().add(
            new Paragraph("Specialization: " +
                (appointment.getDoctor().getSpecialization() != null ?
                appointment.getDoctor().getSpecialization() : "General"))));
        infoTable.addCell(new Cell().add(
            new Paragraph("Phone: " + (appointment.getPatient().getPhone() != null ?
                appointment.getPatient().getPhone() : "N/A"))));
        infoTable.addCell(new Cell().add(
            new Paragraph("Date: " + appointment.getSlot().getDate())));

        document.add(infoTable);

        // Token Number
        document.add(new Paragraph("\nToken Number: #" +
            (appointment.getTokenNumber() != null ?
                appointment.getTokenNumber() : "Walk-in"))
                .setFontSize(14).setBold()
                .setFontColor(ColorConstants.BLUE));

        // Reason
        document.add(new Paragraph("\nChief Complaint:").setBold().setFontSize(13));
        document.add(new Paragraph(appointment.getReason() != null ?
                appointment.getReason() : "Not specified")
                .setFontColor(ColorConstants.DARK_GRAY));

        // Medicines
        document.add(new Paragraph("\nMedicines Prescribed:").setBold().setFontSize(13));
        if (medicines != null && !medicines.isEmpty()) {
            String[] medicineList = medicines.split(",");
            for (int i = 0; i < medicineList.length; i++) {
                document.add(new Paragraph((i + 1) + ". " + medicineList[i].trim()));
            }
        } else {
            document.add(new Paragraph("No medicines prescribed")
                    .setFontColor(ColorConstants.GRAY));
        }

        // Notes
        document.add(new Paragraph("\nDoctor's Notes:").setBold().setFontSize(13));
        document.add(new Paragraph(notes != null && !notes.isEmpty() ?
                notes : "No additional notes")
                .setFontColor(ColorConstants.DARK_GRAY));

        // Footer
        document.add(new Paragraph("\n─────────────────────────────────────────")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY));
        document.add(new Paragraph("Generated on: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")))
                .setFontSize(10).setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("ClinicEase — Your Health, Our Priority")
                .setFontSize(10).setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        document.close();

        byte[] pdfBytes = baos.toByteArray();

        // Email PDF to patient
        emailService.sendPrescriptionEmail(
            appointment.getPatient().getEmail(),
            appointment.getPatient().getName(),
            appointment.getDoctor().getName(),
            pdfBytes
        );

        // Also download for doctor
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
            "attachment; filename=prescription_" + appointmentId + ".pdf");
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
    }
}