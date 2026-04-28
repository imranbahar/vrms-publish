package com.vsms.vsms.repositories;

import com.vsms.vsms.models.Review;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    Review findByRentalID(int rentalID);
    

}
