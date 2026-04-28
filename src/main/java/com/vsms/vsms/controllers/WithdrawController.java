package com.vsms.vsms.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.vsms.vsms.models.Owner;
import com.vsms.vsms.models.Rental;
import com.vsms.vsms.models.Review;
import com.vsms.vsms.models.User;
import com.vsms.vsms.models.Vehicle;
import com.vsms.vsms.models.Withdraw;
import com.vsms.vsms.services.OwnerService;
import com.vsms.vsms.services.RentalService;
import com.vsms.vsms.services.UserService;
import com.vsms.vsms.services.VehicleService;
import com.vsms.vsms.services.WithdrawService;
import com.vsms.vsms.services.ReviewService;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class WithdrawController {
    
    @Value("${geocoding.api.key}")
    private String geocodingApiKey; 
     @Autowired
    private VehicleService vehicleService;

    @Autowired
    private OwnerService ownerService;

    @Autowired
    private RentalService rentalService;

    @Autowired
    private WithdrawService withdrawService;

    @Autowired
    private UserService userService;  

    @Autowired
    private ReviewService reviewService;
    
   @GetMapping("/voRentalList")
    public String voRentalList(@RequestParam("userID") int userID, 
                               @RequestParam(value = "tab", required = false, defaultValue = "rental") String tab,
                               Model model)  {
        List<Owner> owners = ownerService.findOwnersByUserID(userID);

        // Get all vehicles for all owners
        List<Vehicle> ownedVehicles = owners.stream()
            .flatMap(owner -> vehicleService.findVehiclesByOwnerID(owner.getOwnerID()).stream())
            .collect(Collectors.toList());

        // Get all rentals for these vehicles
        List<Rental> voRentals = rentalService.findRentalsByVehicleList(ownedVehicles);

        Map<Integer, Review> reviewMap = voRentals.stream()
            .map(r -> reviewService.findByRentalID(r.getRentalID()))
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(
                Review::getRentalID,
                r -> r,
                (existing, replacement) -> existing
            ));

        model.addAttribute("reviewMap", reviewMap);

        // total money from rentals
        double totalMoney = voRentals.stream()
            .mapToDouble(Rental::getTotalPrice)
            .sum();

        // Get all withdraws for this user (owner)
        List<Withdraw> withdraws = owners.stream()
            .flatMap(owner -> withdrawService.findByOwnerID(owner.getOwnerID()).stream())
            .collect(Collectors.toList());

        // Calculate total online bank payments
        double totalOnlineBank = voRentals.stream()
            .filter(r -> r.getPaymentType() != null && r.getPaymentType().equalsIgnoreCase("online bank"))
            .mapToDouble(Rental::getTotalPrice)
            .sum();

        // Calculate total withdrawn/requested
        double totalWithdrawn = withdraws.stream()
            .filter(w -> w.getWithdrawStatus() != null && 
                (w.getWithdrawStatus().equalsIgnoreCase("REQUESTED") || w.getWithdrawStatus().equalsIgnoreCase("COMPLETED")))
            .mapToDouble(w -> w.getWithdrawAmount() != null ? w.getWithdrawAmount() : 0.0)
            .sum();

        // Total to withdraw = online bank payments - already withdrawn/requested
        double totalToWithdraw = totalOnlineBank - totalWithdrawn;
        if (totalToWithdraw < 0) totalToWithdraw = 0;

        
        // For COMPLETED and REJECTED withdraws, map userID to User (manager)
        Map<Integer, User> managerMap = withdraws.stream()
            .filter(w -> w.getWithdrawStatus() != null &&
                (w.getWithdrawStatus().equalsIgnoreCase("COMPLETED") || w.getWithdrawStatus().equalsIgnoreCase("REJECTED")))
            .map(w -> userService.findUserById(w.getUserID()))
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(
                User::getUserID,
                u -> u,
                (existing, replacement) -> existing // in case of duplicate userID
            ));                      

        for (Vehicle v : ownedVehicles) {
            if (v.getVehicleLocation() != null && v.getVehicleLocation().contains(",")) {
                v.setVehicleAddress(getAddressFromCoordinates(v.getVehicleLocation()));
            } else {
                v.setVehicleAddress("-");
            }
        }      
                          

        // Add to model
        model.addAttribute("managerMap", managerMap);
        model.addAttribute("ownedVehicles", ownedVehicles);
        model.addAttribute("voRentals", voRentals);
        model.addAttribute("userID", userID);
        model.addAttribute("totalMoney", totalMoney);
        model.addAttribute("totalWithdrawn", totalWithdrawn);
        model.addAttribute("totalToWithdraw", totalToWithdraw);
        model.addAttribute("withdraws", withdraws);
        model.addAttribute("tab", tab);
        return "voRentalList";
    }

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

    @GetMapping("/previewReceipt")
    public void previewReceipt(@RequestParam("withdrawID") int withdrawID, HttpServletResponse response) throws IOException {
        Withdraw withdraw = withdrawService.findById(withdrawID);
        if (withdraw != null && withdraw.getReceiptPdf() != null) {
            response.setContentType("application/pdf");
            response.getOutputStream().write(withdraw.getReceiptPdf());
            response.getOutputStream().flush();
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Receipt not found");
        }
    }

    @GetMapping("/withdrawRequest")
    public String withdrawRequest(@RequestParam("userID") int userID,
                                @RequestParam("maxAmount") double maxAmount,
                                Model model) {
        model.addAttribute("userID", userID);
        model.addAttribute("maxAmount", maxAmount);
        return "withdrawRequest";
    }

  @PostMapping("/submitWithdrawRequest")
    public String submitWithdrawRequest(@RequestParam("userID") int userID,
                                    @RequestParam("withdrawAmount") double withdrawAmount,
                                    Model model) {
        List<Owner> owners = ownerService.findOwnersByUserID(userID);

        // Sort owners by accumulatedMoney descending (highest first)
        owners.sort((o1, o2) -> Double.compare(
            o2.getAccumulatedMoney() != null ? o2.getAccumulatedMoney() : 0.0,
            o1.getAccumulatedMoney() != null ? o1.getAccumulatedMoney() : 0.0
        ));

        double amountToDeduct = withdrawAmount;
        for (Owner owner : owners) {
            double acc = owner.getAccumulatedMoney() != null ? owner.getAccumulatedMoney() : 0.0;
            if (acc <= 0) continue;
            if (amountToDeduct <= 0) break;

            if (acc >= amountToDeduct) {
                owner.setAccumulatedMoney(acc - amountToDeduct);
                ownerService.saveOwner(owner);

                // Save the withdraw record for this owner
                Withdraw withdraw = new Withdraw();
                withdraw.setOwnerID(owner.getOwnerID());
                withdraw.setUserID(userID);
                withdraw.setWithdrawStatus("REQUESTED");
                withdraw.setWithdrawAmount(amountToDeduct);
                withdrawService.saveWithdraw(withdraw);

                amountToDeduct = 0;
            } else {
                // Deduct all from this owner, and continue to next
                owner.setAccumulatedMoney(0.0);
                ownerService.saveOwner(owner);

                // Save the withdraw record for this owner (partial)
                Withdraw withdraw = new Withdraw();
                withdraw.setOwnerID(owner.getOwnerID());
               
                withdraw.setWithdrawStatus("REQUESTED");
                withdraw.setWithdrawAmount(acc);
                withdrawService.saveWithdraw(withdraw);

                amountToDeduct -= acc;
            }
        }

        // Optionally, handle if amountToDeduct > 0 (not enough funds)
        // You can add a message to the model if needed

        return "redirect:/voRentalList?userID=" + userID + "&tab=withdraw&success=true";
    }

    @GetMapping("/withdrawApproval")
    public String withdrawApproval(@RequestParam("userID") int userID, Model model) {
        List<Withdraw> requestedWithdraws = withdrawService.findByWithdrawStatus("REQUESTED");
        // Map ownerID to Owner for quick lookup in the template
        Map<Integer, Owner> ownerMap = requestedWithdraws.stream()
            .map(w -> ownerService.findOwnerById(w.getOwnerID()))
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(
                Owner::getOwnerID,
                o -> o,
                (existing, replacement) -> existing // keep the first Owner if duplicate ownerID
            ));
        model.addAttribute("userID", userID);
        model.addAttribute("requestedWithdraws", requestedWithdraws);
        model.addAttribute("ownerMap", ownerMap);
        return "withdrawApproval";
    }

    @GetMapping("/uploadWithdrawTransactionReceipt")
    public String uploadReceipt(@RequestParam("withdrawID") int withdrawID, @RequestParam("userID") int userID, Model model) {
        model.addAttribute("withdrawID", withdrawID);
        model.addAttribute("userID", userID);
        return "uploadWithdrawTransactionReceipt";
    }

    @PostMapping("/submitReceipt")
    public String submitReceipt(@RequestParam("withdrawID") int withdrawID,
                                @RequestParam("userID") int userID,
                                @RequestParam("receipt") MultipartFile receipt,
                                Model model) throws IOException {
        Withdraw withdraw = withdrawService.findById(withdrawID);
        if (withdraw != null) {
            withdraw.setUserID(userID);
            withdraw.setWithdrawStatus("COMPLETED");
            withdraw.setReceiptPdf(receipt.getBytes());
            withdrawService.saveWithdraw(withdraw);
        }
        return "redirect:/withdrawApproval?userID=" + userID;
    }

    @PostMapping("/rejectWithdraw")
    public String rejectWithdraw(@RequestParam("withdrawID") int withdrawID,
                                @RequestParam("userID") int userID) {
        Withdraw withdraw = withdrawService.findById(withdrawID);   
        if (withdraw != null) {
            withdraw.setUserID(userID);
            withdraw.setWithdrawStatus("REJECTED");
            withdrawService.saveWithdraw(withdraw);
        }
        return "redirect:/withdrawApproval?userID=" + userID;
    }
}
