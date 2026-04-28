package com.vsms.vsms.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.vsms.vsms.models.Owner;
import com.vsms.vsms.models.Rental;
import com.vsms.vsms.models.Review;
import com.vsms.vsms.models.User;
import com.vsms.vsms.models.Vehicle;
import com.vsms.vsms.models.Withdraw;
import com.vsms.vsms.services.EmailService;
import com.vsms.vsms.services.OwnerService;
import com.vsms.vsms.services.RentalService;
import com.vsms.vsms.services.ReviewService;
import com.vsms.vsms.services.UserService;
import com.vsms.vsms.services.VehicleService;
import com.vsms.vsms.services.WithdrawService;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService; // Service to send emails

    @Autowired
    private OwnerService ownerService;

    @Autowired
    private RentalService rentalService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private WithdrawService withdrawService;

    private Map<String, String> otpStorage = new HashMap<>(); // Store OTPs temporarily
    private Map<String, Long> otpExpiry = new HashMap<>(); // Store OTP expiry times

    @GetMapping("/userType")
    public String userType(Model model) {
        return "userType";
    }

    @PostMapping("/userType")
    public String userType(@RequestParam("userType") String userType, Model model) {
        if ("admin".equals(userType)) {
            model.addAttribute("userType", userType);
            return "adminValidation";
        } else {
            model.addAttribute("userType", userType);
            return "userRegister";
        }
    }

    @GetMapping("/adminValidation")
    public String adminValidation(@RequestParam("userType") String userType, Model model) {
        model.addAttribute("userType", userType);
        return "adminValidation";
    }

    @PostMapping("/adminValidation")
    public String validateAdmin(@RequestParam("adminCode") String adminCode, @RequestParam("userType") String userType, Model model) {
        if ("adm1n".equals(adminCode)) {
            model.addAttribute("userType", userType);
            return "userRegister";
        } else {
            model.addAttribute("error", "Invalid admin code");
            return "adminValidation";
        }
    }

    @GetMapping("/userRegister")
    public String userRegister(@RequestParam("userType") String userType, Model model) {
        model.addAttribute("userType", userType);
        model.addAttribute("user", new User());
        return "userRegister";
    }


    // Register a new user 
    @PostMapping("/userRegister")
    public String userRegister(@ModelAttribute("user") User user, @RequestParam("userType") String userType, Model model) {
        user.setUserType(userType);

        // Check for duplicate username or email
        if (userService.existsByUserName(user.getUserName())) {
            model.addAttribute("userType", userType);
            model.addAttribute("error", "Username has been taken");
            return "userRegister";
        }
        if (userService.existsByUserEmail(user.getUserEmail())) {
            model.addAttribute("userType", userType);
            model.addAttribute("error", "Email has been taken");
            return "userRegister";
        }
        // Phone number validation
        if (user.getUserPhoneNumber() == null || !user.getUserPhoneNumber().matches("\\d{10}")) {
            model.addAttribute("userType", userType);
            model.addAttribute("error", "Invalid phone number. Must be exactly 10 digits.");
            return "userRegister";
        }

        // Password validation
        if (user.getUserPassword() == null || user.getUserPassword().length() < 8) {
            model.addAttribute("userType", userType);
            model.addAttribute("error", "Password must be at least 8 characters long.");
            return "userRegister";
        }

        try {
            // Set the default profile picture if none is provided
            if (user.getUserPic() == null) {
                byte[] defaultPic = Files.readAllBytes(ResourceUtils.getFile("classpath:static/images/profilePlaceholder.png").toPath());
                user.setUserPic(defaultPic);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        User savedUser = userService.registerUser(user);
        System.out.println("User registered successfully");
        model.addAttribute("userID", savedUser.getUserID());
        return "redirect:/userDL?userID=" + savedUser.getUserID();
    }


    @GetMapping("/cancelUserRegister")
    public String cancelUserRegister(@RequestParam("userID") int userID) {
        userService.deleteUserById(userID);
        return "redirect:/userLogin";
    }

    @GetMapping("/userDL")
    public String userDL(@RequestParam("userID") int userID, Model model) {
        model.addAttribute("userID", userID);
        return "userDL";
    }

    @PostMapping("/userDL")
    public String uploadUserDL(@RequestParam("userID") int userID, @RequestParam("userDL") MultipartFile userDL, Model model) {
        try {
            userService.saveUserDL(userID, userDL);
            System.out.println("User DL uploaded successfully");
            // Redirect to agreementUser page, passing userID if needed
            return "redirect:/agreementUser?userID=" + userID;
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to upload DL picture");
            return "userDL";
        }
    }
    @GetMapping("/agreementUser")
    public String agreementUser(@RequestParam("userID") int userID, Model model) {
        model.addAttribute("userID", userID);
        return "agreementUser";
    }

    
    //user login
    @GetMapping("/userLogin")
    public String userLogin(Model model) {
        model.addAttribute("user", new User());
        return "userLogin";
    }

    @PostMapping("/userLogin")
    public String userValidation(@RequestParam("userName") String userName, 
                                @RequestParam("userPassword") String userPassword, 
                                @RequestParam("userType") String userType, 
                                Model model) {
        User user = userService.findUserByUserNameAndPasswordAndUserType(userName, userPassword, userType);
        if (user != null) {
            userService.updateUserLoginDate(user);
            if ("renter".equals(userType)) {
                return "redirect:/renterHome?userID=" + user.getUserID();
            } else if ("vehicleOwner".equals(userType)) {
                return "redirect:/vehicleOwnerHome?userID=" + user.getUserID();
            } else if ("admin".equals(userType)) {
                return "redirect:/adminHome?userID=" + user.getUserID();
            }
        } else {
            model.addAttribute("error", true);
            return "userLogin";
        }
        return "userLogin";
    }

    //profile & edit

    //renter
    
    @GetMapping("/userProfile")
    public String userProfile(@RequestParam("userID") int userID, Model model) {
        User user = userService.findUserById(userID);
        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("userPicString", user.getUserPicString()); // Add Base64 string to the model
            return "userProfile";
        } else {
            model.addAttribute("error", "User not found");
            return "renterHome";
        }
    }

    @GetMapping("/userEditPic")
    public String userEditPic(@RequestParam("userID") int userID, Model model) {
        model.addAttribute("userID", userID);
        return "userEditPic";
    }

    // Get user profile picture as byte array
    @GetMapping("/userPic")
    public ResponseEntity<byte[]> getUserPic(@RequestParam("userID") int userID) {
        User user = userService.findUserById(userID);
        if (user != null && user.getUserPic() != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "image/jpeg");
            return new ResponseEntity<>(user.getUserPic(), headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @PostMapping("/uploadUserPic")
    public String uploadUserPic(@RequestParam("userID") int userID, @RequestParam("userPic") MultipartFile userPic, Model model) {
        try {
            userService.saveUserPic(userID, userPic);
            model.addAttribute("message", "Profile picture updated successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to upload profile picture.");
        }
        return "redirect:/userProfile?userID=" + userID;
    }

    @GetMapping("/userEdit")
    public String userEdit(@RequestParam("userID") int userID, Model model) {
        User user = userService.findUserById(userID);
        if (user != null) {
            model.addAttribute("user", user);
            return "userEdit";
        } else {
            model.addAttribute("error", "User not found");
            return "userProfile";
        }
    }
    @PostMapping("/userEdit")
    public String updateUser(@RequestParam("userID") int userID,
                             @RequestParam("userName") String userName,
                             @RequestParam("userEmail") String userEmail,
                             @RequestParam("userPassword") String userPassword,
                             @RequestParam("userPhoneNumber") String userPhoneNumber,
                             Model model) {
        User user = userService.findUserById(userID);
        if (user != null) {
            user.setUserName(userName);
            user.setUserEmail(userEmail);
            user.setUserPassword(userPassword); // You might want to encode the password here
            user.setUserPhoneNumber(userPhoneNumber);
            userService.updateUser(user);
            model.addAttribute("user", user);
            return "userProfile";
        } else {
            model.addAttribute("error", "User not found");
            return "userEdit";
        }
    }


    //vehicle owner
    @GetMapping("/vehicleOwnerProfile")
    public String vehicleOwnerProfile(@RequestParam("userID") int userID, Model model) {
        User user = userService.findUserById(userID);
        if (user != null) {
            model.addAttribute("user", user);
            return "vehicleOwnerProfile";
        } else {
            model.addAttribute("error", "User not found");
            return "vehicleOwnerHome";
        }
    }

    @GetMapping("/vehicleOwnerEdit")
    public String vehicleOwnerEdit(@RequestParam("userID") int userID, Model model) {
        User user = userService.findUserById(userID);
        if (user != null) {
            model.addAttribute("user", user);
            return "vehicleOwnerEdit";
        } else {
            model.addAttribute("error", "User not found");
            return "vehicleOwnerProfile";
        }
    }
    @PostMapping("/vehicleOwnerEdit")
    public String updateVehicleOwner(@RequestParam("userID") int userID,
                             @RequestParam("userName") String userName,
                             @RequestParam("userEmail") String userEmail,
                             @RequestParam("userPassword") String userPassword,
                             @RequestParam("userPhoneNumber") String userPhoneNumber,
                             Model model) {
        User user = userService.findUserById(userID);
        if (user != null) {
            user.setUserName(userName);
            user.setUserEmail(userEmail);
            user.setUserPassword(userPassword); // You might want to encode the password here
            user.setUserPhoneNumber(userPhoneNumber);
            userService.updateUser(user);
            model.addAttribute("user", user);
            return "vehicleOwnerProfile";
        } else {
            model.addAttribute("error", "User not found");
            return "vehicleOwnerEdit";
        }
    }

    @GetMapping("/vehicleOwnerEditPic")
    public String vehicleOwnerEditPic(@RequestParam("userID") int userID, Model model) {
        model.addAttribute("userID", userID);
        return "vehicleOwnerEditPic";
    }

    @PostMapping("/uploadVehicleOwnerPic")
    public String uploadVehicleOwnerPic(@RequestParam("userID") int userID, @RequestParam("userPic") MultipartFile userPic, Model model) {
        try {
            userService.saveUserPic(userID, userPic);
            model.addAttribute("message", "Profile picture updated successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to upload profile picture.");
        }
        return "redirect:/vehicleOwnerProfile?userID=" + userID;
    }


    //admin
    @GetMapping("/adminProfile")
    public String adminProfile(@RequestParam("userID") int userID, Model model) {
        User user = userService.findUserById(userID);
        if (user != null) {
            model.addAttribute("user", user);
            return "adminProfile";
        } else {
            model.addAttribute("error", "User not found");
            return "adminHome";
        }
    }

    @GetMapping("/adminEdit")
    public String adminEdit(@RequestParam("userID") int userID, Model model) {
        User user = userService.findUserById(userID);
        if (user != null) {
            model.addAttribute("user", user);
            return "adminEdit";
        } else {
            model.addAttribute("error", "User not found");
            return "adminProfile";
        }
    }

    @PostMapping("/adminEdit")
    public String updateAdmin(@RequestParam("userID") int userID,
                             @RequestParam("userName") String userName,
                             @RequestParam("userEmail") String userEmail,
                             @RequestParam("userPassword") String userPassword,
                             @RequestParam("userPhoneNumber") String userPhoneNumber,
                             Model model) {
        User user = userService.findUserById(userID);
        if (user != null) {
            user.setUserName(userName);
            user.setUserEmail(userEmail);
            user.setUserPassword(userPassword); // You might want to encode the password here
            user.setUserPhoneNumber(userPhoneNumber);
            userService.updateUser(user);
            model.addAttribute("user", user);
            return "adminProfile";
        } else {
            model.addAttribute("error", "User not found");
            return "adminEdit";
        }

    }
    @GetMapping("/adminEditPic")
    public String adminEditPic(@RequestParam("userID") int userID, Model model) {
        model.addAttribute("userID", userID);
        return "adminEditPic";
    }

    @PostMapping("/uploadAdminPic")
    public String uploadAdminPic(@RequestParam("userID") int userID, @RequestParam("userPic") MultipartFile userPic, Model model) {
        try {
            userService.saveUserPic(userID, userPic);
            model.addAttribute("message", "Profile picture updated successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to upload profile picture.");
        }
        return "redirect:/adminProfile?userID=" + userID;
    }



    //logout
    @GetMapping("/logout")
    public String logout(Model model) {
        model.addAttribute("user", new User());
        return "userLogin";
    }

    //navigation bar

    @GetMapping("/navbarVO")
    public String navbarVO(@RequestParam("userID") int userID, Model model) {
        User user = userService.findUserById(userID);
        model.addAttribute("user", user);
        return "navbarVO";
    } 
    @GetMapping("/navbarRV")
    public String navbarRV(@RequestParam("userID") int userID, Model model) {
        User user = userService.findUserById(userID);
        model.addAttribute("user", user);
        return "navbarRV";
    }

    @GetMapping("/navbarA")
    public String navbarA(@RequestParam("userID") int userID, Model model) {
        User user = userService.findUserById(userID);
        model.addAttribute("user", user);
        return "navbarA";
    }
    @GetMapping("/sidebar")
    public String sidebar(Model model) {
        return "sidebar";
    }

    //homepage
    @GetMapping("/renterHome")
    public String renterHome(@RequestParam("userID") int userID, Model model) {
        User user = userService.findUserById(userID);
        model.addAttribute("user", user);
        return "renterHome";
    }

    @GetMapping("/vehicleOwnerHome")
    public String vehicleOwnerHome(@RequestParam("userID") int userID, Model model) {
        User user = userService.findUserById(userID);

        // Get all vehicles owned by this user
        List<Owner> owners = ownerService.findOwnersByUserID(userID);
        List<Vehicle> ownerVehicles = owners.stream()
            .flatMap(owner -> vehicleService.findVehiclesByOwnerID(owner.getOwnerID()).stream())
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        // Get all rentals for owner's vehicles
        List<Rental> ownerRentals = rentalService.findRentalsByVehicleList(ownerVehicles);

        // Get all reviews for those rentals
        List<Review> allReviews = ownerRentals.stream()
            .map(rental -> reviewService.findByRentalID(rental.getRentalID()))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        double avgRating = allReviews.isEmpty() ? 0.0 :
            allReviews.stream().mapToInt(Review::getRating).average().orElse(0.0);

        // 2. Sales for the current month
        java.time.LocalDate now = java.time.LocalDate.now();
        double salesThisMonth = ownerRentals.stream()
            .filter(r -> r.getStartDate() != null && r.getStartDate().toLocalDate().getMonth() == now.getMonth()
                && r.getStartDate().toLocalDate().getYear() == now.getYear())
            .mapToDouble(Rental::getTotalPrice)
            .sum();

        // 3. Accumulated money
        double accumulatedMoney = owners.stream()
            .mapToDouble(o -> o.getAccumulatedMoney() != null ? o.getAccumulatedMoney() : 0.0)
            .sum();

        model.addAttribute("user", user);
        model.addAttribute("avgRating", avgRating);
        model.addAttribute("salesThisMonth", salesThisMonth);
        model.addAttribute("accumulatedMoney", accumulatedMoney);

        return "vehicleOwnerHome";
    }

    //admin home
    @GetMapping("/adminHome")
    public String adminHome(@RequestParam("userID") int userID, Model model) {
        User user = userService.findUserById(userID);

        // 1. Number of vehicles needing approval (vehicleStatus = "requested")
        List<Vehicle> requestedVehicles = vehicleService.findAllVehicles().stream()
            .filter(v -> "requested".equalsIgnoreCase(v.getVehicleStatus()))
            .collect(Collectors.toList());
        int vehicleApprovalCount = requestedVehicles.size();

        // 2. Number of withdraws needing approval (withdrawStatus = "requested")
        // You need a WithdrawService with a method to get all withdraws
        List<Withdraw> requestedWithdraws = withdrawService.findAllWithdraws().stream()
            .filter(w -> "requested".equalsIgnoreCase(w.getWithdrawStatus()))
            .collect(Collectors.toList());
        int withdrawApprovalCount = requestedWithdraws.size();

        // 3. Rentals this month (count and sum)
        List<Rental> allRentals = rentalService.findAllRentals();
        java.time.LocalDate now = java.time.LocalDate.now();
        List<Rental> rentalsThisMonth = allRentals.stream()
            .filter(r -> r.getStartDate() != null &&
                r.getStartDate().toLocalDate().getMonth() == now.getMonth() &&
                r.getStartDate().toLocalDate().getYear() == now.getYear())
            .collect(Collectors.toList());
        int rentalCountThisMonth = rentalsThisMonth.size();
        double rentalSumThisMonth = rentalsThisMonth.stream()
            .mapToDouble(Rental::getTotalPrice)
            .sum();

        // number of users
        int userCount = userService.findAllUsers().size();

        model.addAttribute("userCount", userCount);
        model.addAttribute("user", user);
        model.addAttribute("vehicleApprovalCount", vehicleApprovalCount);
        model.addAttribute("withdrawApprovalCount", withdrawApprovalCount);
        model.addAttribute("rentalCountThisMonth", rentalCountThisMonth);
        model.addAttribute("rentalSumThisMonth", rentalSumThisMonth);

        return "adminHome";
    }

    
    //OTP
    // Step 1: Display Forgot Password Page
    @GetMapping("/forgotPassword")
    public String forgotPasswordPage() {
        return "forgotPassword";
    }

    // Step 2: Handle Email Submission and Send OTP
    @PostMapping("/sendOtp")
    public String sendOtp(@RequestParam("email") String email, Model model) {
        User user = userService.findUserByEmail(email);
        if (user != null) {
            String otp = String.valueOf((int) (Math.random() * 9000) + 1000); // Generate 4-digit OTP
            otpStorage.put(email, otp);
            otpExpiry.put(email, System.currentTimeMillis() + 5 * 60 * 1000); // OTP expires in 5 minutes

            // Send OTP via email
            emailService.sendOtpEmail(email, otp);

            model.addAttribute("email", email);
            return "verifyOtp";
        } else {
            model.addAttribute("error", "Email not found.");
            return "forgotPassword";
        }
    }

    // Step 3: Verify OTP
    @PostMapping("/verifyOtp")
    public String verifyOtp(@RequestParam("email") String email, @RequestParam("otp") String otp, Model model) {
        String storedOtp = otpStorage.get(email);
        Long expiryTime = otpExpiry.get(email);

        if (storedOtp != null && storedOtp.equals(otp) && expiryTime != null && System.currentTimeMillis() <= expiryTime) {
            otpStorage.remove(email); // Remove OTP after successful verification
            otpExpiry.remove(email);
            model.addAttribute("email", email);
            return "resetPassword";
        } else {
            model.addAttribute("error", "Invalid or expired OTP.");
            model.addAttribute("email", email);
            return "verifyOtp";
        }
    }

    // Step 4: Reset Password
    @PostMapping("/resetPassword")
    public String resetPassword(@RequestParam("email") String email, @RequestParam("newPassword") String newPassword, Model model) {
        User user = userService.findUserByEmail(email);
        if (user != null) {
            
            user.setUserPassword(newPassword); // Encode the new password
            userService.updateUser(user);
            model.addAttribute("message", "Password reset successfully.");
            return "userLogin";
        } else {
            model.addAttribute("error", "Failed to reset password.");
            return "resetPassword";
        }
    }
}