package com.vsms.vsms.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vsms.vsms.models.Owner;
import com.vsms.vsms.repositories.OwnerRepository;

@Service
public class OwnerService {
    
    @Autowired
    private OwnerRepository ownerRepository;

    public void saveOwner(Owner owner) {
        ownerRepository.save(owner);
    }

    public List<Owner> findOwnersByUserID(int userID) {
        return ownerRepository.findByUserID(userID); // Add this method
    }

    public void deleteOwnerById(int ownerID) {
        ownerRepository.deleteById(ownerID);
    }
    public Owner findOwnerById(int ownerID) {
        return ownerRepository.findById(ownerID).orElse(null);
    }

    public List<Owner> findAllOwners() {
        return ownerRepository.findAll();
    }
   public boolean existsByOwnerName(String ownerName) {
        return ownerRepository.findByOwnerName(ownerName) != null;
    }

    public boolean existsByOwnerEmail(String ownerEmail) {
        return ownerRepository.findByOwnerEmail(ownerEmail) != null;
    }

}