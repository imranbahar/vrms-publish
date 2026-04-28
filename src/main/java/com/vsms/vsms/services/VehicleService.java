package com.vsms.vsms.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vsms.vsms.models.Vehicle;
import com.vsms.vsms.repositories.VehicleRepository;

@Service
public class VehicleService {
    
    @Autowired
    private VehicleRepository vehicleRepository;

    public Optional<Vehicle> getVehicleById(int vehicleID) {
        return vehicleRepository.findById(vehicleID);
    }

    public Vehicle saveVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    public List<Vehicle> findVehiclesByOwnerID(int ownerID) {
        return vehicleRepository.findByOwnerID(ownerID); 
    }

    public Vehicle findVehicleById(int vehicleID) {
        return vehicleRepository.findById(vehicleID).orElse(null);
    }

    public void deleteVehicleById(int vehicleID) {
        vehicleRepository.deleteById(vehicleID);
    }
    
    public List<Vehicle> findAllVehicles() {
        return vehicleRepository.findAll();
    }

    public List<Vehicle> findVehiclesByStatus(String status) {
        return vehicleRepository.findByVehicleStatus(status);
    }
   
    public List<Vehicle> findByVehicleTypeAndVehicleStatus(String vehicleType, String vehicleStatus) {
        return vehicleRepository.findByVehicleTypeAndVehicleStatus(vehicleType, vehicleStatus);
    }
    
}

