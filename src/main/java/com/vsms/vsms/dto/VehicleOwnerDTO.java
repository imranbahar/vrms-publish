package com.vsms.vsms.dto;

public class VehicleOwnerDTO {
    private int vehicleID;
    private String vehiclePlateNumber;
    private String vehicleType;
    private double vehiclePrice;
    private String vehicleStatus;
    

    private String ownerName;
    private String vehiclePicString;
    private String vehicleOCString; 
    private String ownerDLString;
    private String vehicleAddress;

    public String getVehicleAddress() {
        return vehicleAddress;
    }

    public void setVehicleAddress(String vehicleAddress) {
        this.vehicleAddress = vehicleAddress;
    }

    public String getVehicleOCString() {
        return vehicleOCString;
    }

    public void setVehicleOCString(String vehicleOCString) {
        this.vehicleOCString = vehicleOCString;
    }

    public String getOwnerDLString() {
        return ownerDLString;
    }

    public void setOwnerDLString(String ownerDLString) {
        this.ownerDLString = ownerDLString;
    }

    // Getters and Setters
    public int getVehicleID() {
        return vehicleID;
    }

    public void setVehicleID(int vehicleID) {
        this.vehicleID = vehicleID;
    }

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

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getVehiclePicString() {
        return vehiclePicString;
    }

    public void setVehiclePicString(String vehiclePicString) {
        this.vehiclePicString = vehiclePicString;
    }
}
