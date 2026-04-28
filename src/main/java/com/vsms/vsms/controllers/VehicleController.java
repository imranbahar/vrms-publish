package com.vsms.vsms.controllers;

import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.vsms.vsms.dto.VehicleDTO;
import com.vsms.vsms.dto.VehicleOwnerDTO;
import com.vsms.vsms.models.Owner;
import com.vsms.vsms.models.Rental;
import com.vsms.vsms.models.User;
import com.vsms.vsms.models.Vehicle;
import com.vsms.vsms.services.OwnerService;
import com.vsms.vsms.services.RentalService;
import com.vsms.vsms.services.UserService;
import com.vsms.vsms.services.VehicleService;

@Controller
public class VehicleController {

    @Value("${geocoding.api.key}")
    private String geocodingApiKey;
    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private OwnerService ownerService;

    @Autowired
    private UserService userService;

    @Autowired
    private RentalService rentalService;

    //CRUD vehicle
    @GetMapping("/vehicleRegister")
    public String vehicleRegister(@RequestParam("userID") int userID, @RequestParam("ownerID") int ownerID, Model model) {
        model.addAttribute("vehicleDTO", new VehicleDTO());
        model.addAttribute("userID", userID);
        model.addAttribute("ownerID", ownerID);
        return "vehicleRegister";
    }

    @PostMapping("/vehicleRegister")
    public String registerVehicle(@RequestParam("userID") int userID, @RequestParam("ownerID") int ownerID, @ModelAttribute VehicleDTO vehicleDTO) throws IOException {
        Vehicle vehicle = new Vehicle();
        vehicle.setOwnerID(ownerID);
        vehicle.setVehiclePlateNumber(vehicleDTO.getVehiclePlateNumber());
        vehicle.setVehicleType(vehicleDTO.getVehicleType());
        vehicle.setVehiclePrice(vehicleDTO.getVehiclePrice());
        vehicle.setVehicleStatus("requested");
        vehicle.setVehicleLocation(vehicleDTO.getVehicleLocation());
        if (!vehicleDTO.getVehiclePicture().isEmpty()) {
            vehicle.setVehiclePicture(vehicleDTO.getVehiclePicture().getBytes());
        }
        if (!vehicleDTO.getVehicleOC().isEmpty()) {
            vehicle.setVehicleOC(vehicleDTO.getVehicleOC().getBytes());
        }
        vehicleService.saveVehicle(vehicle);
        return "redirect:/vehicleList?userID=" + userID + "&registerSuccess=true";
    }
     
    @GetMapping("/vehicleEdit")
    public String vehicleEdit(@RequestParam("vehicleID") int vehicleID, @RequestParam("userID") int userID, Model model) {
        Vehicle vehicle = vehicleService.findVehicleById(vehicleID);
        VehicleDTO vehicleDTO = new VehicleDTO();
        vehicleDTO.setVehiclePlateNumber(vehicle.getVehiclePlateNumber());
        vehicleDTO.setVehicleType(vehicle.getVehicleType());
        vehicleDTO.setVehiclePrice(vehicle.getVehiclePrice());
        vehicleDTO.setOwnerID(vehicle.getOwnerID());
        model.addAttribute("vehicleDTO", vehicleDTO);
        model.addAttribute("vehicleID", vehicleID);
        model.addAttribute("userID", userID);
        return "vehicleEdit";
    }

    @PostMapping("/vehicleEdit")
    public String vehicleEdit(@RequestParam("vehicleID") int vehicleID, @RequestParam("userID") int userID, @ModelAttribute VehicleDTO vehicleDTO) throws IOException {
        Vehicle vehicle = vehicleService.findVehicleById(vehicleID);
        vehicle.setVehiclePlateNumber(vehicleDTO.getVehiclePlateNumber());
        vehicle.setVehicleType(vehicleDTO.getVehicleType());
        vehicle.setVehiclePrice(vehicleDTO.getVehiclePrice());
        vehicle.setVehicleLocation(vehicleDTO.getVehicleLocation());
        if (!vehicleDTO.getVehiclePicture().isEmpty()) {
            vehicle.setVehiclePicture(vehicleDTO.getVehiclePicture().getBytes());
        }
        if (!vehicleDTO.getVehicleOC().isEmpty()) {
            vehicle.setVehicleOC(vehicleDTO.getVehicleOC().getBytes());
        }
        vehicleService.saveVehicle(vehicle);
        return "redirect:/vehicleList?userID=" + userID;
    }

    @GetMapping("/vehicleDelete")
    public String vehicleDelete(@RequestParam("vehicleID") int vehicleID, @RequestParam("userID") int userID) {
        vehicleService.deleteVehicleById(vehicleID);
        return "redirect:/vehicleList?userID=" + userID;
    }

    // admin approval
    @GetMapping("/vehicleApproval")
    public String vehicleApproval(@RequestParam("userID") int userID, Model model) {
        List<Vehicle> requestedVehicles = vehicleService.findVehiclesByStatus("requested");
        List<VehicleOwnerDTO> vehicleOwnerDTOs = requestedVehicles.stream()
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
            .collect(Collectors.toList());
        model.addAttribute("requestedVehicles", vehicleOwnerDTOs);
        model.addAttribute("userID", userID);
        return "vehicleApproval";
    }

    @PostMapping("/vehicleApproval")
    public String approveVehicle(@RequestParam("vehicleID") int vehicleID, @RequestParam("userID") int userID) {
        Vehicle vehicle = vehicleService.findVehicleById(vehicleID);
        vehicle.setVehicleStatus("approved");
        vehicleService.saveVehicle(vehicle);
        return "redirect:/vehicleApproval?userID=" + userID;
    }

    @PostMapping("/rejectVehicle")
    public String rejectVehicle(@RequestParam("vehicleID") int vehicleID, @RequestParam("userID") int userID) {
        Vehicle vehicle = vehicleService.findVehicleById(vehicleID);
        vehicle.setVehicleStatus("rejected");
        vehicleService.saveVehicle(vehicle);
        return "redirect:/vehicleApproval?userID=" + userID;
    }

    // vehicle list
    // vehicle list for owner
    @GetMapping("/vehicleList")
    public String vehicleList(@RequestParam("userID") int userID, Model model) {
        List<Owner> owners = ownerService.findOwnersByUserID(userID);
        List<VehicleOwnerDTO> vehiclesWithOwners = owners.stream()
            .flatMap(owner -> vehicleService.findVehiclesByOwnerID(owner.getOwnerID()).stream()
                .map(vehicle -> {
                    VehicleOwnerDTO dto = new VehicleOwnerDTO();
                    dto.setVehicleID(vehicle.getVehicleID());
                    dto.setVehiclePlateNumber(vehicle.getVehiclePlateNumber());
                    dto.setVehicleType(vehicle.getVehicleType());
                    dto.setVehiclePrice(vehicle.getVehiclePrice());
                    dto.setOwnerName(owner.getOwnerName());
                    dto.setVehiclePicString(vehicle.getVehiclePicString());
                    dto.setVehicleStatus(vehicle.getVehicleStatus());
                    // Convert coordinates to address
                    if (vehicle.getVehicleLocation() != null && vehicle.getVehicleLocation().contains(",")) {
                        dto.setVehicleAddress(getAddressFromCoordinates(vehicle.getVehicleLocation()));
                    } else {
                        dto.setVehicleAddress("-"); // <-- FIXED HERE
                    }
                    return dto;
                }))
            .collect(Collectors.toList());
        model.addAttribute("vehiclesWithOwners", vehiclesWithOwners);
        model.addAttribute("userID", userID);
        return "vehicleList";
    }
    //Convertor cooridnate to address
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return coordinates; // fallback
    }


    @GetMapping("/bookSearch")
    public String rentalSearch(@RequestParam("userID") int userID, Model model) {
        User user = userService.findUserById(userID);
        model.addAttribute("userID", userID);
        model.addAttribute("user", user);
        return "bookSearch";
    }

    @PostMapping("/bookSearch")
        public String bookSearch(@RequestParam("userID") int userID,
                                    @RequestParam("vehicleType") String vehicleType,
                                    @RequestParam("startDate") Date startDate,
                                    @RequestParam("endDate") Date endDate,
                                    @RequestParam("startTime") String startTimeStr,
                                    @RequestParam("endTime") String endTimeStr,
                                    Model model) {
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalTime nowTime = java.time.LocalTime.now();

            // 1. Start/end date not in the past
            if (startDate.toLocalDate().isBefore(today) || endDate.toLocalDate().isBefore(today)) {
                model.addAttribute("error", "Start and end date cannot be in the past.");
                model.addAttribute("userID", userID);
                return "bookSearch";
            }
            // 2. End date not before start date
            if (endDate.toLocalDate().isBefore(startDate.toLocalDate())) {
                model.addAttribute("error", "End date cannot be before start date.");
                model.addAttribute("userID", userID);
                return "bookSearch";
            }
            // 3. If start date is today, start time not in the past
            if (startDate.toLocalDate().isEqual(today)) {
                if (java.time.LocalTime.parse(startTimeStr).isBefore(nowTime)) {
                    model.addAttribute("userID", userID);
                    model.addAttribute("error", "Start time cannot be in the past.");
                    return "bookSearch";
                }
            }
            // 4. If same day, end time must be at least 1 hour after start time
            if (startDate.toLocalDate().isEqual(endDate.toLocalDate())) {
                java.time.LocalTime st = java.time.LocalTime.parse(startTimeStr);
                java.time.LocalTime et = java.time.LocalTime.parse(endTimeStr);
                if (!et.isAfter(st)) {
                    model.addAttribute("userID", userID);
                    model.addAttribute("error", "End time must be after start time.");
                    return "bookSearch";
                }
                if (java.time.Duration.between(st, et).toMinutes() < 60) {
                    model.addAttribute("userID", userID);
                    model.addAttribute("error", "Rental duration must be at least 1 hour.");
                    return "bookSearch";
                }
            }
            Time startTime = Time.valueOf(startTimeStr + ":00");
            Time endTime = Time.valueOf(endTimeStr + ":00");
            
             // 1. Get all owners for this user
                List<Owner> owners = ownerService.findOwnersByUserID(userID);

                // 2. Get all vehicles for these owners and filter by type
                List<Vehicle> ownerVehicles = owners.stream()
                    .flatMap(owner -> vehicleService.findVehiclesByOwnerID(owner.getOwnerID()).stream())
                    .filter(vehicle -> vehicle.getVehicleType().equalsIgnoreCase(vehicleType))
                    .filter(vehicle -> vehicle.getVehicleStatus().equalsIgnoreCase("approved"))
                    .collect(Collectors.toList());

                // 3. Filter only available vehicles in the given range
                List<Vehicle> availableVehicles = rentalService.filterAvailableVehicles(ownerVehicles, startDate, endDate, startTime, endTime);

                model.addAttribute("availableVehicles", availableVehicles);
                model.addAttribute("userID", userID);
                model.addAttribute("startDate", startDate);
                model.addAttribute("endDate", endDate);
                model.addAttribute("startTime", startTimeStr);
                model.addAttribute("endTime", endTimeStr);
                return "ownerAvailableVehicleList";
        }


    @GetMapping("/ownerAvailableVehicleList")
    public String ownerAvailableVehicleList(
        @RequestParam("userID") int userID,
        @RequestParam("startDate") Date startDate,
        @RequestParam("endDate") Date endDate,
        @RequestParam("startTime") String startTimeStr,
        @RequestParam("endTime") String endTimeStr,
        @RequestParam(value = "vehicleType", required = false) String vehicleType,
        @RequestParam(value = "search", required = false) String search,
        Model model) {

        Time startTime = Time.valueOf(startTimeStr + ":00");
        Time endTime = Time.valueOf(endTimeStr + ":00");

        // 1. Get all owners for this user
        List<Owner> owners = ownerService.findOwnersByUserID(userID);

        // 2. Get all vehicles for these owners and filter by type
        List<Vehicle> ownerVehicles = owners.stream()
            .flatMap(owner -> vehicleService.findVehiclesByOwnerID(owner.getOwnerID()).stream())
            .filter(vehicle -> vehicle.getVehicleStatus().equalsIgnoreCase("approved"))
            .collect(Collectors.toList());

        if (vehicleType != null && !vehicleType.isEmpty()) {
            ownerVehicles = ownerVehicles.stream()
                .filter(vehicle -> vehicle.getVehicleType().equalsIgnoreCase(vehicleType))
                .collect(Collectors.toList());
        }

        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            ownerVehicles = ownerVehicles.stream()
                .filter(vehicle -> vehicle.getVehiclePlateNumber().toLowerCase().contains(searchLower))
                .collect(Collectors.toList());
        }

        // 3. Filter only available vehicles in the given range
        List<Vehicle> availableVehicles = rentalService.filterAvailableVehicles(ownerVehicles, startDate, endDate, startTime, endTime);

        model.addAttribute("availableVehicles", availableVehicles);
        model.addAttribute("userID", userID);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("startTime", startTimeStr);
        model.addAttribute("endTime", endTimeStr);
        model.addAttribute("vehicleType", vehicleType);
        model.addAttribute("search", search);
        return "ownerAvailableVehicleList";
    }

    @PostMapping("/ownerAvailableVehicleList")
        public String ownerAvailableVehicleList(@RequestParam("vehicleID") int vehicleID,
                                    @RequestParam("userID") int userID,
                                    @RequestParam("startDate") Date startDate,
                                    @RequestParam("endDate") Date endDate,
                                    @RequestParam("startTime") String startTimeStr,
                                    @RequestParam("endTime") String endTimeStr,
                                    Model model) {
            Time startTime = Time.valueOf(startTimeStr + ":00");
            Time endTime = Time.valueOf(endTimeStr + ":00");

            LocalDateTime startDateTime = LocalDateTime.of(startDate.toLocalDate(), startTime.toLocalTime());
            LocalDateTime endDateTime = LocalDateTime.of(endDate.toLocalDate(), endTime.toLocalTime());
            long hours = Duration.between(startDateTime, endDateTime).toHours();
            long days = hours / 24;
            long remainingHours = hours % 24;
            String duration = (days > 0 ? days + " days " : "") + remainingHours + " hours";


            Vehicle vehicle = vehicleService.findVehicleById(vehicleID);

            // Create rental with paymentType "owner use" and totalPrice 0.0
            Rental rental = new Rental();
            rental.setUserID(userID);
            rental.setVehicleID(vehicleID);
            rental.setOwnerID(vehicle.getOwnerID());
            rental.setStartDate(startDate);
            rental.setEndDate(endDate);
            rental.setStartTime(startTime);
            rental.setEndTime(endTime);
            rental.setPaymentType("owner use");
            rental.setBillCode("OWNUSE");
            rental.setTotalPrice(0.0);
            rental.setDuration(duration); // You can set duration if needed

            rentalService.saveRental(rental);

            model.addAttribute("message", "Vehicle booked for owner use!");
            model.addAttribute("userID", userID);
            return "redirect:/voRentalList?userID=" + userID + "&bookSuccess=true";
        }

}