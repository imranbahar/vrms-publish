package com.vsms.vsms.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vsms.vsms.models.User;


// JPA allows connecting with the database using annotations
// CRUD operations happen here

public interface UserRepository extends JpaRepository<User, Integer> {
    //User findByUserEmail(String userEmail);

    // Find user by email and password for login
    User findByUserEmailAndUserPassword(String userEmail, String userPassword);
    
    // Find user by user name
    User findByUserName(String userName);
    // Find user by user name and password for login(x pakai)
    User findByUserNameAndUserPassword(String userName, String userPassword);
    // Find user by user name and user type for Homepage display(x pakai)
    User findByUserNameAndUserType(String userName, String userType);

    // Find user by user name, password and user type for login and homepage display
    User findByUserNameAndUserPasswordAndUserType(String userName, String userPassword, String userType);
    // Find user by ID
    Optional<User> findById(Integer userID);
    
    @Query("SELECT u FROM User u WHERE u.userEmail = :email")
    User findByUserEmail(@Param("email") String email);
    
}