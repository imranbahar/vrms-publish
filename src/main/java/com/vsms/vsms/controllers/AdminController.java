package com.vsms.vsms.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.vsms.vsms.dto.VehicleOwnerDTO;
import com.vsms.vsms.services.UserService;
import com.vsms.vsms.services.VehicleService;
import com.vsms.vsms.services.WithdrawService;
import com.vsms.vsms.models.Owner;
import com.vsms.vsms.models.Rental;
import com.vsms.vsms.models.User;
import com.vsms.vsms.models.Vehicle;
import com.vsms.vsms.models.Withdraw;
import com.vsms.vsms.services.OwnerService;
import com.vsms.vsms.services.RentalService;


@Controller
public class AdminController {
    
    @Value("${geocoding.api.key}")
    private String geocodingApiKey;

    @Autowired 
    private VehicleService vehicleService;

    @Autowired
    private UserService userService;

    @Autowired
    private RentalService rentalService;
    

    @Autowired
    private WithdrawService withdrawService;

    @Autowired
    private OwnerService ownerService;
    
    @GetMapping("/approvalType")
    public String approvalType(@RequestParam("userID") int userID, Model model) {
        model.addAttribute("userID", userID);
        return "approvalType";
    }

    @GetMapping("/reportType")
    public String reportType(@RequestParam("userID") int userID, Model model) {
        model.addAttribute("userID", userID);
        return "reportType";
    }

    @GetMapping("/userReport")
    public String userReport(@RequestParam("userID") int userID, Model model) {
        model.addAttribute("userID", userID);
        model.addAttribute("users", userService.findAllUsers());
        return "userReport";
    }

    @GetMapping("/vehicleReport")
    public String vehicleReport(@RequestParam("userID") int userID, Model model) {
        List<Vehicle> vehicles = vehicleService.findAllVehicles();
        List<VehicleOwnerDTO> vehicleOwnerDTOs = vehicles.stream()
            .map(vehicle -> {
                Owner owner = ownerService.findOwnerById(vehicle.getOwnerID());
                VehicleOwnerDTO dto = new VehicleOwnerDTO();
                dto.setVehicleID(vehicle.getVehicleID());
                dto.setVehiclePlateNumber(vehicle.getVehiclePlateNumber());
                dto.setVehicleType(vehicle.getVehicleType());
                dto.setVehiclePrice(vehicle.getVehiclePrice());
                dto.setOwnerName(owner != null ? owner.getOwnerName() : "");
                dto.setVehiclePicString(vehicle.getVehiclePicString());
                dto.setVehicleStatus(vehicle.getVehicleStatus());
                dto.setVehicleOCString(vehicle.getVehicleOC() != null ? java.util.Base64.getEncoder().encodeToString(vehicle.getVehicleOC()) : null);
                dto.setOwnerDLString(owner != null ? owner.getOwnerDLString() : null);
                if (vehicle.getVehicleLocation() != null && vehicle.getVehicleLocation().contains(",")) {
                    dto.setVehicleAddress(getAddressFromCoordinates(vehicle.getVehicleLocation()));
                } else {
                    dto.setVehicleAddress("-");
                }
                return dto;
            })
            .collect(java.util.stream.Collectors.toList());
        model.addAttribute("vehicleOwnerDTOs", vehicleOwnerDTOs);
        model.addAttribute("userID", userID);
        return "vehicleReport";
    }


    //Method converts coordinates to address using Google Maps Geocoding API
    private String getAddressFromCoordinates(String coordinates) {
        try {
            String[] parts = coordinates.split(",");
            String lat = parts[0].trim();
            String lng = parts[1].trim();
           
            String urlStr = "https://api.geoapify.com/v1/geocode/reverse?lat=" + lat + "&lon=" + lng + "&apiKey=" + geocodingApiKey;

            java.net.URL url = new java.net.URL(urlStr);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();

            String response = sb.toString();
            JSONObject json = new JSONObject(response);
            JSONArray results = json.getJSONArray("results");
            // Loop through results and skip Plus Codes
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                JSONArray types = result.getJSONArray("types");
                boolean isPlusCode = false;
                for (int j = 0; j < types.length(); j++) {
                    if (types.getString(j).equalsIgnoreCase("plus_code")) {
                        isPlusCode = true;
                        break;
                    }
                }
                if (!isPlusCode) {
                    return result.getString("formatted_address");
                }
            }
            // If all are Plus Codes, fallback to the first result
            if (results.length() > 0) {
                return results.getJSONObject(0).getString("formatted_address");
            }
            System.out.println("Geocode API response: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return coordinates; // fallback
    }
    
    @GetMapping("/rentalReport")
    public String rentalReport(@RequestParam("userID") int userID, Model model) {
    // Fetch all rentals with vehicle and user details
        List<Rental> rentals = rentalService.findAllRentalsWithDetails();

        for (Rental rental : rentals) {
            Vehicle v = rental.getVehicle();
            if (v != null && v.getVehicleLocation() != null && v.getVehicleLocation().contains(",")) {
                v.setVehicleAddress(getAddressFromCoordinates(v.getVehicleLocation()));
            } else if (v != null) {
                v.setVehicleAddress("-");
            }
        }

        // Add rentals and userID to the model
        model.addAttribute("userID", userID);
        model.addAttribute("rentals", rentals);

        return "rentalReport";
    }

    @GetMapping("/withdrawReport")
    public String withdrawReport(@RequestParam("userID") int userID, Model model) {
        model.addAttribute("userID", userID);
        List<Withdraw> withdraws = withdrawService.findAllWithdraws();
        model.addAttribute("withdraws", withdraws);

        // Build ownerMap: ownerID -> User
        Map<Integer, User> ownerMap = new HashMap<>();
        // Build managerMap: userID -> User
        Map<Integer, User> managerMap = new HashMap<>();
        for (Withdraw w : withdraws) {
            // Owner
            User owner = userService.findUserById(w.getOwnerID());
            if (owner != null) {
                ownerMap.put(w.getOwnerID(), owner);
            }
            // Manager
            User manager = userService.findUserById(w.getUserID());
            if (manager != null) {
                managerMap.put(w.getUserID(), manager);
            }
        }
        model.addAttribute("ownerMap", ownerMap);
        model.addAttribute("managerMap", managerMap);

        return "withdrawReport";
    }


}