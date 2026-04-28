package com.vsms.vsms.controllers;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import com.vsms.vsms.models.Owner;
import com.vsms.vsms.models.Rental;
import com.vsms.vsms.models.Review;
import com.vsms.vsms.models.User;
import com.vsms.vsms.models.Vehicle;
import com.vsms.vsms.services.OwnerService;
import com.vsms.vsms.services.RentalService;
import com.vsms.vsms.services.ReviewService;
import com.vsms.vsms.services.UserService;
import com.vsms.vsms.services.VehicleService;

@Controller
public class RentalController {

    @Value("${geocoding.api.key}")
    private String geocodingApiKey;
    @Autowired
    private RentalService rentalService;

    @Autowired
    private UserService userService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private OwnerService ownerService;

    @Autowired
    private ReviewService reviewService;


    @Value("${payment.gateway.userSecretKey}")
    private String userSecretKey;

    @Value("${payment.gateway.categoryCode}")
    private String categoryCode;

    @Value("${payment.gateway.url}")
    private String paymentGatewayUrl;

    @Value("${payment.gateway.returnUrl}")
    private String billReturnUrl;

    @Value("${payment.gateway.callbackUrl}")
    private String billCallbackUrl;

    // Rental search page
    @GetMapping("/rentalSearch")
    public String rentalSearch(@RequestParam("userID") int userID, Model model) {
        User user = userService.findUserById(userID);
        model.addAttribute("userID", userID);
        model.addAttribute("user", user);
        return "rentalSearch";
    }

    // Calculate distance between two coordinates in km
    private static double distanceInKm(double lat1, double lon1, double lat2, double lon2) {
    double earthRadius = 6371; // km
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return earthRadius * c;
}

    // Search results after form submission
    @PostMapping("/rentalSearch")
    public String rentalSearchPost(@RequestParam("userID") int userID,
                                @RequestParam("vehicleType") String vehicleType,
                                @RequestParam("startDate") Date startDate,
                                @RequestParam("endDate") Date endDate,
                                @RequestParam("startTime") String startTimeStr,
                                @RequestParam("endTime") String endTimeStr,
                                @RequestParam(value = "sort", required = false) String sort,
                                @RequestParam(value = "searchLocation", required = false) String searchLocation,
                                @RequestParam(value = "searchLocationAddress", required = false) String searchLocationAddress,
                                Model model) {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalTime nowTime = java.time.LocalTime.now();

        // 1. Start/end date not in the past
        if (startDate.toLocalDate().isBefore(today) || endDate.toLocalDate().isBefore(today)) {
            model.addAttribute("error", "Start and end date cannot be in the past.");
            model.addAttribute("userID", userID);
            return "rentalSearch";
        }
        // 2. End date not before start date
        if (endDate.toLocalDate().isBefore(startDate.toLocalDate())) {
            model.addAttribute("error", "End date cannot be before start date.");
            model.addAttribute("userID", userID);
            return "rentalSearch";
        }
        // 3. If start date is today, start time not in the past
        if (startDate.toLocalDate().isEqual(today)) {
            if (java.time.LocalTime.parse(startTimeStr).isBefore(nowTime)) {
                model.addAttribute("userID", userID);
                model.addAttribute("error", "Start time cannot be in the past.");
                return "rentalSearch";
            }
        }
        // 4. If same day, end time must be at least 1 hour after start time
        if (startDate.toLocalDate().isEqual(endDate.toLocalDate())) {
            java.time.LocalTime st = java.time.LocalTime.parse(startTimeStr);
            java.time.LocalTime et = java.time.LocalTime.parse(endTimeStr);
            if (!et.isAfter(st)) {
                model.addAttribute("userID", userID);
                model.addAttribute("error", "End time must be after start time.");
                return "rentalSearch";
            }
            if (java.time.Duration.between(st, et).toMinutes() < 60) {
                model.addAttribute("userID", userID);
                model.addAttribute("error", "Rental duration must be at least 1 hour.");
                return "rentalSearch";
            }
        }
        Time startTime = Time.valueOf(startTimeStr + ":00");
        Time endTime = Time.valueOf(endTimeStr + ":00");
        List<Vehicle> availableVehicles = rentalService.findAvailableVehicles(vehicleType, startDate, endDate, startTime, endTime);

        
        // --- Filter by distance if searchLocation is provided ---
        if (searchLocation != null && !searchLocation.isEmpty()) {
        String[] parts = searchLocation.split(",");
        double searchLat = Double.parseDouble(parts[0]);
        double searchLng = Double.parseDouble(parts[1]);
        availableVehicles = availableVehicles.stream()
            .filter(v -> {
                if (v.getVehicleLocation() == null || !v.getVehicleLocation().contains(",")) return false;
                String[] loc = v.getVehicleLocation().split(",");
                double vLat = Double.parseDouble(loc[0]);
                double vLng = Double.parseDouble(loc[1]);
                double dist = distanceInKm(searchLat, searchLng, vLat, vLng);
                System.out.println("Vehicle " + v.getVehiclePlateNumber() + " at " + vLat + "," + vLng +
                                " is " + dist + " km from search location " + searchLat + "," + searchLng);
                return dist <= 1.0;
            })
            .collect(Collectors.toList());
    }

        model.addAttribute("availableVehicles", availableVehicles);
        model.addAttribute("userID", userID);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("startTime", startTimeStr);
        model.addAttribute("endTime", endTimeStr);
        model.addAttribute("vehicleType", vehicleType);
        model.addAttribute("searchLocation", searchLocation);
        model.addAttribute("searchLocationAddress", searchLocationAddress);
        return "rentalAvailableVehicleList";
    }

    @GetMapping("/rentalAvailableVehicleList")
    public String rentalAvailableVehicleList(@RequestParam("userID") int userID,
                                            @RequestParam("startDate") Date startDate,
                                            @RequestParam("endDate") Date endDate,
                                            @RequestParam("startTime") String startTimeStr,
                                            @RequestParam("endTime") String endTimeStr,
                                            @RequestParam(value = "vehicleType", required = false) String vehicleType,
                                            @RequestParam(value = "sort", required = false) String sort,
                                            @RequestParam(value = "searchLocation", required = false) String searchLocation,
                                            Model model) {
                                               
        Time startTime = Time.valueOf(startTimeStr + ":00");
        Time endTime = Time.valueOf(endTimeStr + ":00");
        List<Vehicle> availableVehicles = rentalService.findAvailableVehicles(vehicleType, startDate, endDate, startTime, endTime);
        
        // --- Filter by distance if searchLocation is provided ---
        if (searchLocation != null && !searchLocation.isEmpty()) {
        String[] parts = searchLocation.split(",");
        double searchLat = Double.parseDouble(parts[0]);
        double searchLng = Double.parseDouble(parts[1]);
        availableVehicles = availableVehicles.stream()
            .filter(v -> {
                if (v.getVehicleLocation() == null || !v.getVehicleLocation().contains(",")) return false;
                String[] loc = v.getVehicleLocation().split(",");
                double vLat = Double.parseDouble(loc[0]);
                double vLng = Double.parseDouble(loc[1]);
                return distanceInKm(searchLat, searchLng, vLat, vLng) <= 1.0;
            })
            .collect(Collectors.toList());
    }

        // Sorting logic
        if ("expensive".equals(sort)) {
            availableVehicles.sort(Comparator.comparing(Vehicle::getVehiclePrice).reversed());
        } else if ("cheapest".equals(sort)) {
            availableVehicles.sort(Comparator.comparing(Vehicle::getVehiclePrice));
        }

        model.addAttribute("availableVehicles", availableVehicles);
        model.addAttribute("userID", userID);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("startTime", startTimeStr);
        model.addAttribute("endTime", endTimeStr);
        model.addAttribute("vehicleType", vehicleType);
        model.addAttribute("sort", sort);
        model.addAttribute("searchLocation", searchLocation);
        return "rentalAvailableVehicleList";
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

    // Rental confirmation page
    @GetMapping("/rentalConfirmation")
    public String rentalConfirmation(@RequestParam("userID") int userID,
                                    @RequestParam("vehicleID") int vehicleID,
                                    @RequestParam("startDate") Date startDate,
                                    @RequestParam("endDate") Date endDate,
                                    @RequestParam("startTime") String startTimeStr,
                                    @RequestParam("endTime") String endTimeStr,
                                    Model model) {
        // Fetch user and vehicle details
        User user = userService.findUserById(userID);
        Vehicle vehicle = vehicleService.findVehicleById(vehicleID);

        // Convert time strings to Time objects
        Time startTime = Time.valueOf(startTimeStr + ":00");
        Time endTime = Time.valueOf(endTimeStr + ":00");

        // Calculate rental duration
        LocalDateTime startDateTime = LocalDateTime.of(startDate.toLocalDate(), startTime.toLocalTime());
        LocalDateTime endDateTime = LocalDateTime.of(endDate.toLocalDate(), endTime.toLocalTime());
        long hours = Duration.between(startDateTime, endDateTime).toHours();
        double totalPrice = hours * vehicle.getVehiclePrice();

        // Format the duration as a string (e.g., "2 days 5 hours")
        long days = hours / 24;
        long remainingHours = hours % 24;
        String duration = (days > 0 ? days + " days " : "") + remainingHours + " hours";

        // Use vehicle location for rental
        String vehicleLocation = vehicle.getVehicleLocation();

        // Create and save a new rental
        Rental rental = new Rental();
        rental.setUserID(userID);
        rental.setVehicleID(vehicleID);
        rental.setOwnerID(vehicle.getOwnerID());
        rental.setStartDate(startDate);
        rental.setEndDate(endDate);
        rental.setStartTime(startTime);
        rental.setEndTime(endTime);
        rental.setTotalPrice(totalPrice);
        rental.setDuration(duration);
        rental.setLocation(vehicleLocation); // <-- Save vehicle location
        rentalService.saveRental(rental);

        // Update accumulatedMoney for the owner
        Owner owner = ownerService.findOwnerById(vehicle.getOwnerID());
        if (owner != null) {
            double current = owner.getAccumulatedMoney() != null ? owner.getAccumulatedMoney() : 0.0;
            double newAccumulated = new java.math.BigDecimal(current + totalPrice)
                .setScale(2, java.math.RoundingMode.HALF_UP)
                .doubleValue();
            owner.setAccumulatedMoney(newAccumulated);
            ownerService.saveOwner(owner);
        }

        // Pass rental details to the view
        model.addAttribute("user", user);
        model.addAttribute("vehicle", vehicle);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("startTime", startTimeStr);
        model.addAttribute("endTime", endTimeStr);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("duration", duration);
        model.addAttribute("rentalID", rental.getRentalID());

        // Convert vehicle location to address for display
        String address = null;
        if (vehicleLocation != null && vehicleLocation.contains(",")) {
            address = getAddressFromCoordinates(vehicleLocation);
        }
        model.addAttribute("vehicleLocation", vehicleLocation);
        model.addAttribute("vehicleLocationAddress", address);

        return "rentalConfirmation";
    }

    // DELETE RENTAL
    @PostMapping("/rentalDelete")
    public String rentalDelete(@RequestParam("rentalID") int rentalID, @RequestParam("userID") int userID) {
        rentalService.deleteRentalById(rentalID);
        return "redirect:/renterHome?userID=" + userID;
    }

    // Payment type page
    @GetMapping("/rentalPaymentType")
    public String rentalPaymentType(@RequestParam("userID") int userID, @RequestParam("rentalID") int rentalID, Model model) {
        model.addAttribute("userID", userID);
        model.addAttribute("rentalID", rentalID);
        return "rentalPaymentType";
    }

    // Process payment method
    @PostMapping("/processPayment")
    public String processPayment(@RequestParam("rentalID") int rentalID,
                                 @RequestParam("paymentType") String paymentType,
                                 @RequestParam("userID") int userID,
                                 Model model) {
        Rental rental = rentalService.findRentalById(rentalID);
        if (rental == null) {
            model.addAttribute("error", "Rental not found.");
            model.addAttribute("userID", userID);
            return "rentalPaymentType";
        }

        if ("onlineBank".equals(paymentType)) {
            User user = userService.findUserById(rental.getUserID());
            double totalPrice = rental.getTotalPrice();

            try {
                String postData = String.format("userSecretKey=%s&categoryCode=%s&billName=%s&billDescription=%s" +
                                "&billPriceSetting=1&billPayorInfo=1&billAmount=%.0f&billReturnUrl=%s&billCallbackUrl=%s" +
                                "&billTo=%s&billEmail=%s&billPhone=%s&billExternalReferenceNo=%d&billExpiryDate=",
                        userSecretKey,
                        categoryCode,
                        "Rental Payment",
                        "Payment for rental ID " + rentalID,
                        totalPrice * 100,
                        billReturnUrl,
                        billCallbackUrl,
                        user.getUserName(),
                        user.getUserEmail(),
                        user.getUserPhoneNumber(),
                        rentalID
                );

                URL url = new URL(paymentGatewayUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8))) {
                    writer.write(postData);
                    writer.flush();
                }

                String response;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    response = reader.lines().collect(Collectors.joining());
                }

                JSONArray responseArray = new JSONArray(response);
                JSONObject bill = responseArray.getJSONObject(0);
                String billCode = bill.getString("BillCode");

                rental.setPaymentType("online bank");
                rental.setBillCode(billCode);
                rentalService.saveRental(rental);

                return "redirect:https://dev.toyyibpay.com/" + billCode;
            } catch (Exception e) {
                e.printStackTrace();
                model.addAttribute("error", "Error during payment initiation: " + e.getMessage());
                return "paymentError";
            }
        } else if ("cash".equals(paymentType)) {
            rental.setPaymentType("cash");
            rental.setBillCode("CASH"); // No bill code for cash payment
            rentalService.saveRental(rental);
            model.addAttribute("message", "Cash payment recorded successfully.");
            model.addAttribute("userID", userID);
            return "paymentSuccess";
        }

        model.addAttribute("error", "Invalid payment type selected.");
        return "rentalPaymentType";
    }

    // Payment callback from ToyyibPay
    @PostMapping("/paymentCallback")
    public String paymentCallback(@RequestParam("billCode") String billCode, Model model) {
        System.out.println("Received POST callback from ToyyibPay with billCode: " + billCode);
        try {
            String apiUrl = "https://dev.toyyibpay.com/index.php/api/getBillTransactions";
            String postData = "billCode=" + billCode;

            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()))) {
                writer.write(postData);
                writer.flush();
            }

            String response;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                response = reader.lines().collect(Collectors.joining());
            }

            JSONArray array = new JSONArray(response);
            if (array.length() > 0) {
                JSONObject transaction = array.getJSONObject(0);
                String status = transaction.getString("billpaymentStatus");

                if ("1".equals(status)) {
                    Rental rental = rentalService.findRentalByBillCode(billCode);
                    if (rental != null) {
                        rental.setPaymentType("onlineBank");
                        rentalService.saveRental(rental);

                        model.addAttribute("userID", rental.getUserID());
                        model.addAttribute("billStatus", "Successful");
                        return "paymentSuccess";

                    } else {
                        model.addAttribute("error", "Rental not found.");
                        return "paymentError";
                    }
                } else {
                    model.addAttribute("billStatus", "Failed");
                    return "paymentFailure";
                }
            } else {
                model.addAttribute("error", "No transactions found.");
                return "paymentError";
            }

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Callback failed: " + e.getMessage());
            return "paymentError";
        }
    }

    // GET endpoint for manual payment success view (optional)
    @GetMapping("/paymentCallback")
    public String handleReturnUrl(@RequestParam("billcode") String billCode, Model model) {
        Rental rental = rentalService.findRentalByBillCode(billCode);
        if (rental != null) {
            model.addAttribute("userID", rental.getUserID());
            model.addAttribute("billStatus", "Successful");
            return "paymentSuccess";
        } else {
        
            model.addAttribute("error", "Rental not found.");
            return "paymentError";
    }
}


    // POST endpoint for manual payment success view (optional)
    @PostMapping("/paymentSuccess")
    public String paymentSuccess(@RequestParam("userID") int userID, Model model) {
        model.addAttribute("userID", userID);
        model.addAttribute("billStatus", "Successful");
        return "paymentSuccess";
    }

    // POST endpoint for manual payment failure view (optional)
    @PostMapping("/paymentFailure")
    public String paymentFailure(@RequestParam("rentalID") int rentalID, @RequestParam("userID") int userID, Model model) {
        rentalService.deleteRentalById(rentalID);
        model.addAttribute("userID", userID);
        model.addAttribute("billStatus", "Failed");
        return "paymentFailure";
    }

    // POST endpoint for manual payment error view (optional)
    @PostMapping("/paymentError")
    public String paymentError(@RequestParam("rentalID") int rentalID, @RequestParam("userID") int userID, @RequestParam("error") String error, Model model) {
        rentalService.deleteRentalById(rentalID);
        model.addAttribute("error", error);
        return "paymentError";
    }

    @GetMapping("/renterRentalList")
    public String renterRentalList(@RequestParam("userID") int userID,
                                @RequestParam(value = "sort", required = false) String sort,
                                Model model) {
        List<Rental> pastRentals = rentalService.findRentalsByUserID(userID);
        Map<Integer, Review> reviewMap = new HashMap<>();
        for (Rental rental : pastRentals) {
            Review review = reviewService.findByRentalID(rental.getRentalID());
            if (review != null) {
                reviewMap.put(rental.getRentalID(), review);
            }
            // Convert vehicle location to address for display
            Vehicle v = rental.getVehicle();
            if (v != null && v.getVehicleLocation() != null && v.getVehicleLocation().contains(",")) {
                v.setVehicleAddress(getAddressFromCoordinates(v.getVehicleLocation()));
            } else if (v != null) {
                v.setVehicleAddress("-");
            }
        }

        if ("newest".equals(sort)) {
            pastRentals.sort(Comparator.comparing(Rental::getStartDate).reversed());
        } else if ("oldest".equals(sort)) {
            pastRentals.sort(Comparator.comparing(Rental::getStartDate));
        } else if ("expensive".equals(sort)) {
            pastRentals.sort(Comparator.comparing(Rental::getTotalPrice).reversed());
        } else if ("cheapest".equals(sort)) {
            pastRentals.sort(Comparator.comparing(Rental::getTotalPrice));
        } else if ("giveReview".equals(sort)) {
            // Only rentals that have no review
            pastRentals = pastRentals.stream()
                .filter(r -> !reviewMap.containsKey(r.getRentalID()))
                .collect(Collectors.toList());
        } else if ("reviewed".equals(sort)) {
            // Only rentals that have a review
            pastRentals = pastRentals.stream()
                .filter(r -> reviewMap.containsKey(r.getRentalID()))
                .collect(Collectors.toList());
        }

        model.addAttribute("pastRentals", pastRentals);
        model.addAttribute("reviewMap", reviewMap);
        model.addAttribute("userID", userID);
        model.addAttribute("sort", sort);
        return "renterRentalList";
    }

    

}
