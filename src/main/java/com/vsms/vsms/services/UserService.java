package com.vsms.vsms.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.vsms.vsms.models.User;
import com.vsms.vsms.repositories.UserRepository;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;  

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User registerUser(User user) {
        // Encode the password before saving 
        //salt and hash the password
        user.setUserPassword(passwordEncoder.encode(user.getUserPassword()));
        return userRepository.save(user);
    }
    // Method to check if a user exists by username or email for registration cant same name or email
    public boolean existsByUserName(String userName) {
        return userRepository.findByUserName(userName) != null;
    }

    public boolean existsByUserEmail(String userEmail) {
        return userRepository.findByUserEmail(userEmail) != null;
    }


    // Method to find a user by username and password for login
    public User findUserByUserNameAndPassword(String userName, String userPassword) {
        User user = userRepository.findByUserName(userName);
        //use stored saalt to hash the password
        if (user != null && passwordEncoder.matches(userPassword, user.getUserPassword())) {
            return user;
        }
        return null;
    }

    public User findUserByUserNameAndPasswordAndUserType(String userName, String userPassword, String userType) {
        User user = userRepository.findByUserNameAndUserType(userName, userType);
        if (user != null && passwordEncoder.matches(userPassword, user.getUserPassword())) {
            return user;
        }
        return null;
    }

    public User findUserById(int userID) {
        return userRepository.findById(userID).orElse(null);
    }

    public User updateUser(User user) {
        user.setUserPassword(passwordEncoder.encode(user.getUserPassword())); // Encode the password
        return userRepository.save(user);
    }

    public void saveUserDL(int userID, MultipartFile userDL) throws IOException {
        User user = findUserById(userID);
        if (user != null) {
            user.setUserDL(userDL.getBytes());
            userRepository.save(user);
        }
    }

    public void updateUserLoginDate(User user) {
        user.setUserLoginDate(new Timestamp(System.currentTimeMillis()));
        userRepository.save(user);
    }
    //admin
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User findUserByEmail(String email) {
        return userRepository.findByUserEmail(email);
    }

    public void saveUserPic(int userID, MultipartFile userPic) throws IOException {
        User user = findUserById(userID);
        if (user != null) {
            user.setUserPic(userPic.getBytes());
            userRepository.save(user);
        }
    }

    public void deleteUserById(int userID) {
        userRepository.deleteById(userID);
    }
}