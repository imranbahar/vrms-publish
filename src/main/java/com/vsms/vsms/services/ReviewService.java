package com.vsms.vsms.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vsms.vsms.models.Review;
import com.vsms.vsms.repositories.ReviewRepository;



@Service
public class ReviewService {
    
    @Autowired
    private ReviewRepository reviewRepository;

    public Review findByRentalID(int rentalID) {
        return reviewRepository.findByRentalID(rentalID);
    }

    public void saveReview(Review review) {
        reviewRepository.save(review);
    }

    

}
