package com.vsms.vsms.models;

import java.util.Base64;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "Vehicle")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int vehicleID;
    private int ownerID;

    private String vehiclePlateNumber;
    private String vehicleType;
    private double vehiclePrice;
    private String vehicleStatus;
    private String vehicleLocation;
    @Transient
    private String vehicleAddress;

    @Lob
    private byte[] vehicleOC;

    @Lob
    private byte[] vehiclePicture;

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

    public int getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(int ownerID) {
        this.ownerID = ownerID;
    }

    public byte[] getVehiclePicture() {
        return vehiclePicture;
    }

    public void setVehiclePicture(byte[] vehiclePicture) {
        this.vehiclePicture = vehiclePicture;
    }

    public byte[] getVehicleOC() {
        return vehicleOC;
    }

    public void setVehicleOC(byte[] vehicleOC) {
        this.vehicleOC = vehicleOC;
    }

    public String getVehiclePicString() {
        return Base64.getEncoder().encodeToString(this.vehiclePicture);
    }


      public String getVehicleLocation() {
        return vehicleLocation;
    }

    public void setVehicleLocation(String vehicleLocation) {
        this.vehicleLocation = vehicleLocation;
    }


    public String getVehicleAddress() {
        return vehicleAddress;
    }


    public void setVehicleAddress(String vehicleAddress) {
        this.vehicleAddress = vehicleAddress;
    }
}