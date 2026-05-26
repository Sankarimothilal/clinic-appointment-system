package com.example.demo.repository;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import com.example.demo.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDoctorId(Long doctorId);
    long countByStatus(Appointment.AppointmentStatus status);
    
 // Find max token for a doctor on a date
    @Query("SELECT MAX(a.tokenNumber) FROM Appointment a WHERE a.doctor.id = :doctorId AND a.slot.date = :date")
    Integer findMaxTokenByDoctorAndDate(@Param("doctorId") Long doctorId, @Param("date") java.time.LocalDate date);

    // Find appointments by doctor and date ordered by token
    List<Appointment> findByDoctorIdAndSlot_DateOrderByTokenNumber(Long doctorId, java.time.LocalDate date);
}