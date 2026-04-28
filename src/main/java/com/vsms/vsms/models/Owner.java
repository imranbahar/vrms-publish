package com.vsms.vsms.models;

import java.util.Base64;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "Owner")
public class Owner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ownerID;

    private int userID;
    
    private String ownerName;   
    private String ownerEmail;
    private String ownerPhoneNumber; 
    private String accountNumber;
    private String bankName; 
    
    private Double accumulatedMoney; 
   

    @Lob
    private byte[] ownerDL;
    
    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
    public Double getAccumulatedMoney() {
        return accumulatedMoney;
    }

    public void setAccumulatedMoney(Double accumulatedMoney) {
        this.accumulatedMoney = accumulatedMoney;
    }

    //largte object data    
  

    public int getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(int ownerID) {
        this.ownerID = ownerID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getOwnerPhoneNumber() {
        return ownerPhoneNumber;
    }

    public void setOwnerPhoneNumber(String ownerPhoneNumber) {
        this.ownerPhoneNumber = ownerPhoneNumber;
    }
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }


    public byte[] getOwnerDL() {
        return ownerDL;
    }

    public void setOwnerDL(byte[] ownerDL) {
        this.ownerDL = ownerDL;
    }

    public String getOwnerDLString() {
        return ownerDL != null ? Base64.getEncoder().encodeToString(ownerDL) : null;
    }
    

}
