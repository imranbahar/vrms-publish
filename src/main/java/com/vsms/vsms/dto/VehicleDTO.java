package com.vsms.vsms.dto;

import org.springframework.web.multipart.MultipartFile;

//data transfer object for vehicle
//not annotated with JPA
//used to transfer data between the controller and the service
//separate concern of data transfer and data persistence
public class VehicleDTO {

    private String vehiclePlateNumber;
    private String vehicleType;
    private double vehiclePrice;
    private String vehicleStatus;

    private int ownerID;
    private MultipartFile vehiclePicture;
    private MultipartFile vehicleOC;
    private String vehicleLocation; 
    

    // Getters and Setters
    public String getVehiclePlateNumber() {
        return vehiclePlateNumber;
    }

    public void setVehiclePlateNumber(String vehiclePlateNumber) {
        this.vehiclePlateNumber = vehiclePlateNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public double getVehiclePrice() {
        return vehiclePrice;
    }

    public void setVehiclePrice(double vehiclePrice) {
        this.vehiclePrice = vehiclePrice;
    }

    public String getVehicleStatus() {
        return vehicleStatus;
    }

    public void setVehicleStatus(String vehicleStatus) {
        this.vehicleStatus = vehicleStatus;
    }

    public int getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(int ownerID) {
        this.ownerID = ownerID;
    }

    public MultipartFile getVehiclePicture() {
        return vehiclePicture;
    }

    public void setVehiclePicture(MultipartFile vehiclePicture) {
        this.vehiclePicture = vehiclePicture;
    }

    public MultipartFile getVehicleOC() {
        return vehicleOC;
    }

    public void setVehicleOC(MultipartFile vehicleOC) {
        this.vehicleOC = vehicleOC;
    }

    public String getVehicleLocation() {
        return vehicleLocation;
    }

    public void setVehicleLocation(String vehicleLocation) {
        this.vehicleLocation = vehicleLocation;
    }
}