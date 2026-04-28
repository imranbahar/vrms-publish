package com.vsms.vsms.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vsms.vsms.models.Vehicle;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {
    // Find vehicle by owner ID for vehicle list because owner ID is the foreign key in vehicle table
    List<Vehicle> findByOwnerID(int ownerID); 
    // Find vehicle by vehicle status for vehicle list
    List<Vehicle> findByVehicleStatus(String status);

    List<Vehicle> findByVehicleTypeAndVehicleStatus(String vehicleType, String vehicleStatus);
}