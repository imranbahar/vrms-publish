package com.vsms.vsms.models;

import java.sql.Timestamp;
import java.util.Base64;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;



// table creation
@Entity
@Table(name = "User")
public class User {

    // auto increment
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userID;

    private String userName;
    private String userEmail;
    private String userPassword;
    private String userPhoneNumber;
    private String userType;

    private Timestamp userLoginDate;
   
    //large object data
    @Lob
    private byte[] userDL;

    @Lob
    private byte[] userPic; 



    // Getters and setters
    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public void setUserPhoneNumber(String userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public byte[] getUserDL() {
        return userDL;
    }

    public void setUserDL(byte[] userDL) {
        this.userDL = userDL;
    }

    public Timestamp getUserLoginDate() {
        return userLoginDate;
    }

    public void setUserLoginDate(Timestamp userLoginDate) {
        this.userLoginDate = userLoginDate;
    }

    public byte[] getUserPic() {
        return userPic;
    }
    
    public void setUserPic(byte[] userPic) {
        this.userPic = userPic;
    }
    // Method to convert byte array to Base64 string for display
    public String getUserPicString() {
        return userPic != null ? Base64.getEncoder().encodeToString(userPic) : null;
    }
}

