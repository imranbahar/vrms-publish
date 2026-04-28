package com.vsms.vsms.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vsms.vsms.models.Owner;
import com.vsms.vsms.models.User;
import com.vsms.vsms.services.OwnerService;
import com.vsms.vsms.services.UserService;

@Controller
public class OwnerController {
    @Autowired
    private OwnerService ownerService;

    @Autowired
    private UserService userService;
    
    //CRUD owner
    @GetMapping("/chooseOwner")
    public String chooseOwner(@RequestParam("userID") int userID, Model model) {
        model.addAttribute("userID", userID);
        return "chooseOwner";
    }

    @GetMapping("/agreementOwner")
    public String agreementOwner(@RequestParam("userID") int userID, Model model) {
        model.addAttribute("userID", userID);
        return "agreementOwner";
    }

    @GetMapping("/existingOwnerList")
    public String existingOwnerList(@RequestParam("userID") int userID, Model model, RedirectAttributes redirectAttributes) {
        List<Owner> owners = ownerService.findOwnersByUserID(userID);
        if (owners == null || owners.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No existing owner found for this user.");
            return "redirect:/chooseOwner?userID=" + userID;
        }
        model.addAttribute("userID", userID);
        model.addAttribute("owners", owners);
        return "existingOwnerList";
    }

    @GetMapping("/ownerType")
    public String ownerType(@RequestParam("userID") int userID, Model model) {
        model.addAttribute("userID", userID);
        return "ownerType";
    }
 
    @GetMapping("/ownerRegister")
    public String ownerRegister(@RequestParam("ownerType") String ownerType, @RequestParam("userID") int userID, Model model) {
        if ("ownVehicle".equals(ownerType)) {
            // Check if this user already registered as owner
            List<Owner> existingOwners = ownerService.findOwnersByUserID(userID);
            if (existingOwners != null && !existingOwners.isEmpty()) {
                model.addAttribute("userID", userID);
                model.addAttribute("error", "You have already registered as an owner.");
                return "ownerType";
            }
            model.addAttribute("userID", userID);
            return "ownerAccountNumberRegister";
        } else if ("otherOwner".equals(ownerType)) {
            model.addAttribute("owner", new Owner());
            model.addAttribute("userID", userID);
            return "ownerRegister";
        }
        return "redirect:/vehicleOwnerHome?userID=" + userID;
    }

    @PostMapping("/ownerAccountNumberRegister")
    public String registerAccountNumber(@RequestParam("userID") int userID,
                                        @RequestParam("accountNumber") String accountNumber,
                                        @RequestParam("bankName") String bankName) {
        User user = userService.findUserById(userID);
        if (user != null) {
            Owner owner = new Owner();
            owner.setUserID(user.getUserID());
            owner.setOwnerName(user.getUserName());
            owner.setOwnerEmail(user.getUserEmail());
            owner.setOwnerPhoneNumber(user.getUserPhoneNumber());
            owner.setOwnerDL(user.getUserDL());
            owner.setAccountNumber(accountNumber);
            owner.setBankName(bankName); // Save bank name
            owner.setAccumulatedMoney(0.0);
            ownerService.saveOwner(owner);
            return "redirect:/vehicleRegister?userID=" + userID + "&ownerID=" + owner.getOwnerID();
        }
        return "redirect:/vehicleOwnerHome?userID=" + userID;
    }

    @PostMapping("/ownerRegister")
    public String registerOwner(@RequestParam("userID") int userID,
                            @RequestParam("ownerName") String ownerName,
                            @RequestParam("ownerEmail") String ownerEmail,
                            @RequestParam("ownerPhoneNumber") String ownerPhoneNumber,
                            @RequestParam("accountNumber") String accountNumber,
                            @RequestParam("bankName") String bankName,
                            @RequestParam("ownerDL") MultipartFile ownerDL,
                            Model model) {

        // Check for duplicate owner name or email
        if (ownerService.existsByOwnerName(ownerName) || ownerService.existsByOwnerEmail(ownerEmail)) {
            model.addAttribute("userID", userID);
            model.addAttribute("error", "Owner name or email has already been registered.");
            return "ownerRegister";
        }

        Owner owner = new Owner();
        owner.setUserID(userID);
        owner.setOwnerName(ownerName);
        owner.setOwnerEmail(ownerEmail);
        owner.setOwnerPhoneNumber(ownerPhoneNumber);
        owner.setAccountNumber(accountNumber);
        owner.setBankName(bankName);
        owner.setAccumulatedMoney(0.0);
        try {
            if (ownerDL != null && !ownerDL.isEmpty()) {
                owner.setOwnerDL(ownerDL.getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ownerService.saveOwner(owner);
        return "redirect:/vehicleRegister?userID=" + userID + "&ownerID=" + owner.getOwnerID();
    }

    @GetMapping("/deleteOwner")
    public String deleteOwner(@RequestParam("ownerID") int ownerID, @RequestParam("userID") int userID) {
        ownerService.deleteOwnerById(ownerID);
        return "redirect:/vehicleOwnerHome?userID=" + userID;
    }
}