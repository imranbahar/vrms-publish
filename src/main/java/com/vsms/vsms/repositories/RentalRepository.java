package com.vsms.vsms.repositories;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vsms.vsms.models.Rental;

public interface RentalRepository extends JpaRepository<Rental, Integer> {

    @Query("SELECT r FROM Rental r WHERE r.startDate <= :endDate AND r.endDate >= :startDate AND r.startTime <= :endTime AND r.endTime >= :startTime")
    List<Rental> findConflictingRentals(Date startDate, Date endDate, Time startTime, Time endTime);

    @Query("SELECT r FROM Rental r WHERE r.billCode = :billCode")
    Rental findByBillCode(@Param("billCode") String billCode);

    @Query("SELECT r FROM Rental r JOIN FETCH r.vehicle WHERE r.userID = :userID ORDER BY r.startDate DESC, r.startTime DESC")
    List<Rental> findByUserID(@Param("userID") int userID);

    @Query("SELECT r FROM Rental r JOIN FETCH r.vehicle v JOIN FETCH r.user u ORDER BY r.startDate DESC, r.startTime DESC")
    List<Rental> findAllRentalsWithDetails();

    List<Rental> findByVehicleIDIn(List<Integer> vehicleIds);
}