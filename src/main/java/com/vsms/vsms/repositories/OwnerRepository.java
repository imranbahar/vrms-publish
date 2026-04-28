package com.vsms.vsms.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vsms.vsms.models.Owner;

import java.util.List;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Integer> {
    List<Owner> findByUserID(int userID); // userID find by it

    Owner findByOwnerName(String ownerName);
    Owner findByOwnerEmail(String ownerEmail);

}
