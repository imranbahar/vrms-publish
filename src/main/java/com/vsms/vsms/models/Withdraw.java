package com.vsms.vsms.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Withdraw")   
public class Withdraw {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int withdrawID;

    private int userID; 
    private int ownerID; 
    private String withdrawStatus;
    private Double withdrawAmount; 
    
    @jakarta.persistence.Lob
    private byte[] receiptPdf;

    public int getWithdrawID() {
        return withdrawID;
    }

    public void setWithdrawID(int withdrawID) {
        this.withdrawID = withdrawID;
    }

    public int getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(int ownerID) {
        this.ownerID = ownerID;
    }

    public String getWithdrawStatus() {
        return withdrawStatus;
    }

    public void setWithdrawStatus(String withdrawStatus) {
        this.withdrawStatus = withdrawStatus;
    }

    public Double getWithdrawAmount() {
        return withdrawAmount;
    }

    public void setWithdrawAmount(Double withdrawAmount) {
        this.withdrawAmount = withdrawAmount;
    }

    public byte[] getReceiptPdf() {
        return receiptPdf;
    }

    public void setReceiptPdf(byte[] receiptPdf) {
        this.receiptPdf = receiptPdf;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    } 

}
