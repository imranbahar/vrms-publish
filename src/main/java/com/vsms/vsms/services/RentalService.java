package com.vsms.vsms.services;

import java.sql.Date;
import java.sql.Time;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vsms.vsms.models.Rental;
import com.vsms.vsms.models.Vehicle;
import com.vsms.vsms.repositories.RentalRepository;
import com.vsms.vsms.repositories.VehicleRepository;

@Service
public class RentalService {
    
    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private RentalRepository rentalRepository;

    
    public List<Rental> findAllRentals() {
        return rentalRepository.findAll();
    }

    public List<Vehicle> findAvailableVehicles(String vehicleType, Date startDate, Date endDate, Time startTime, Time endTime) {
        List<Vehicle> allVehicles = vehicleRepository.findByVehicleTypeAndVehicleStatus(vehicleType, "approved");
        List<Rental> conflictingRentals = rentalRepository.findConflictingRentals(startDate, endDate, startTime, endTime);
        List<Integer> unavailableVehicleIds = conflictingRentals.stream()
            .map(Rental::getVehicleID)
            .collect(Collectors.toList());
        return allVehicles.stream()
            .filter(vehicle -> !unavailableVehicleIds.contains(vehicle.getVehicleID()))
            .collect(Collectors.toList());
    }
    public void saveRental(Rental rental) {
        rentalRepository.save(rental);
    }

    public void deleteRentalById(int rentalID) {
        rentalRepository.deleteById(rentalID);
    }

    // Method to find all rentals by user ID
    public Rental findRentalById(int rentalID) {
        return rentalRepository.findById(rentalID).orElse(null);
    }
    // Method to find a rental by bill code
    public Rental findRentalByBillCode(String billCode) {
        return rentalRepository.findByBillCode(billCode);
    }

    public List<Rental> findRentalsByUserID(int userID) {
        return rentalRepository.findByUserID(userID);
    }
    

    public List<Rental> findAllRentalsWithDetails() {
        return rentalRepository.findAllRentalsWithDetails();
    }

    public List<Rental> findRentalsByVehicleList(List<Vehicle> vehicles) {
        List<Integer> vehicleIds = vehicles.stream().map(Vehicle::getVehicleID).toList();
        return rentalRepository.findByVehicleIDIn(vehicleIds);
    }

    public List<Vehicle> filterAvailableVehicles(List<Vehicle> vehicles, Date startDate, Date endDate, Time startTime, Time endTime) {
        // Get all rentals that conflict with the given date/time range
        List<Rental> conflictingRentals = rentalRepository.findConflictingRentals(startDate, endDate, startTime, endTime);
        List<Integer> unavailableVehicleIds = conflictingRentals.stream()
            .map(Rental::getVehicleID)
            .collect(Collectors.toList());
        // Only return vehicles from the provided list that are not in the unavailable list
        return vehicles.stream()
            .filter(vehicle -> !unavailableVehicleIds.contains(vehicle.getVehicleID()))
            .collect(Collectors.toList());
    }
}