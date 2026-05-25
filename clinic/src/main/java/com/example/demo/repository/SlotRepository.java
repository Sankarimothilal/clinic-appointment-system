package com.example.demo.repository;

import com.example.demo.model.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {
    List<Slot> findByDoctorIdAndDate(Long doctorId, LocalDate date);
    List<Slot> findByDoctorIdAndIsBooked(Long doctorId, Boolean isBooked);
}