package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendAppointmentConfirmation(
            String toEmail,
            String patientName,
            String doctorName,
            String date,
            String time) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("✅ Appointment Confirmed - ClinicEase");
            helper.setText(buildConfirmationEmail(patientName, doctorName, date, time), true);

            mailSender.send(message);
            System.out.println("Confirmation email sent to: " + toEmail);
        } catch (Exception e) {
            System.out.println("Email failed: " + e.getMessage());
        }
    }

    public void sendCancellationEmail(
            String toEmail,
            String patientName,
            String doctorName,
            String date,
            String time) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("❌ Appointment Cancelled - ClinicEase");
            helper.setText(buildCancellationEmail(patientName, doctorName, date, time), true);

            mailSender.send(message);
            System.out.println("Cancellation email sent to: " + toEmail);
        } catch (Exception e) {
            System.out.println("Email failed: " + e.getMessage());
        }
    }

    private String buildConfirmationEmail(
            String patientName,
            String doctorName,
            String date,
            String time) {
        return "<div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;'>"
            + "<div style='background:linear-gradient(135deg,#667eea,#764ba2);padding:30px;text-align:center;border-radius:10px 10px 0 0'>"
            + "<h1 style='color:white;margin:0'>🏥 ClinicEase</h1>"
            + "</div>"
            + "<div style='background:#f9f9f9;padding:30px;border-radius:0 0 10px 10px'>"
            + "<h2 style='color:#333'>Appointment Confirmed! ✅</h2>"
            + "<p style='color:#666'>Dear <strong>" + patientName + "</strong>,</p>"
            + "<p style='color:#666'>Your appointment has been successfully booked.</p>"
            + "<div style='background:white;border:1px solid #e0e0e0;border-radius:8px;padding:20px;margin:20px 0'>"
            + "<p>👨‍⚕️ <strong>Doctor:</strong> " + doctorName + "</p>"
            + "<p>📅 <strong>Date:</strong> " + date + "</p>"
            + "<p>⏰ <strong>Time:</strong> " + time + "</p>"
            + "</div>"
            + "<p style='color:#666'>Please arrive 10 minutes early.</p>"
            + "<p style='color:#999;font-size:12px'>— ClinicEase Team</p>"
            + "</div></div>";
    }

    private String buildCancellationEmail(
            String patientName,
            String doctorName,
            String date,
            String time) {
        return "<div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;'>"
            + "<div style='background:linear-gradient(135deg,#e74c3c,#c0392b);padding:30px;text-align:center;border-radius:10px 10px 0 0'>"
            + "<h1 style='color:white;margin:0'>🏥 ClinicEase</h1>"
            + "</div>"
            + "<div style='background:#f9f9f9;padding:30px;border-radius:0 0 10px 10px'>"
            + "<h2 style='color:#333'>Appointment Cancelled ❌</h2>"
            + "<p style='color:#666'>Dear <strong>" + patientName + "</strong>,</p>"
            + "<p style='color:#666'>Your appointment has been cancelled.</p>"
            + "<div style='background:white;border:1px solid #e0e0e0;border-radius:8px;padding:20px;margin:20px 0'>"
            + "<p>👨‍⚕️ <strong>Doctor:</strong> " + doctorName + "</p>"
            + "<p>📅 <strong>Date:</strong> " + date + "</p>"
            + "<p>⏰ <strong>Time:</strong> " + time + "</p>"
            + "</div>"
            + "<p style='color:#666'>You can book a new appointment anytime.</p>"
            + "<p style='color:#999;font-size:12px'>— ClinicEase Team</p>"
            + "</div></div>";
    }
    
    public void sendPrescriptionEmail(
            String toEmail,
            String patientName,
            String doctorName,
            byte[] pdfBytes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("📄 Your Prescription - ClinicEase");
            helper.setText(
                "<div style='font-family:Arial,sans-serif;max-width:600px;margin:auto'>"
                + "<div style='background:linear-gradient(135deg,#667eea,#764ba2);padding:30px;text-align:center;border-radius:10px 10px 0 0'>"
                + "<h1 style='color:white;margin:0'>🏥 ClinicEase</h1>"
                + "</div>"
                + "<div style='background:#f9f9f9;padding:30px;border-radius:0 0 10px 10px'>"
                + "<h2 style='color:#333'>Your Prescription is Ready! 📄</h2>"
                + "<p style='color:#666'>Dear <strong>" + patientName + "</strong>,</p>"
                + "<p style='color:#666'>Dr. " + doctorName + " has issued your prescription.</p>"
                + "<p style='color:#666'>Please find the prescription PDF attached to this email.</p>"
                + "<p style='color:#666'>Show this at your nearest pharmacy.</p>"
                + "<p style='color:#999;font-size:12px;margin-top:20px'>— ClinicEase Team</p>"
                + "</div></div>", true);

            // Attach PDF
            helper.addAttachment("prescription.pdf",
                new org.springframework.core.io.ByteArrayResource(pdfBytes),
                "application/pdf");

            mailSender.send(message);
            System.out.println("Prescription email sent to: " + toEmail);
        } catch (Exception e) {
            System.out.println("Prescription email failed: " + e.getMessage());
        }
    }
}