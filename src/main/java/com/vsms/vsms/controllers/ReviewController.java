package com.vsms.vsms.controllers;

import com.vsms.vsms.models.Review;
import com.vsms.vsms.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

   

    @GetMapping("/giveReview")
    public String giveReview(@RequestParam("rentalID") int rentalID,
                             @RequestParam("userID") int userID,
                             org.springframework.ui.Model model) {
        model.addAttribute("rentalID", rentalID);
        model.addAttribute("userID", userID);
        return "giveReview";
    }

    @PostMapping("/submitReview")
    public String submitReview(@RequestParam("rentalID") int rentalID,
                               @RequestParam("userID") int userID,
                               @RequestParam("rating") int rating,
                               @RequestParam("comment") String comment) {
        Review review = new Review();
        review.setRentalID(rentalID);
        review.setRating(rating);
        review.setComment(comment);
        reviewService.saveReview(review);

        // Optionally, update rental to link to reviewID if needed
        // Rental rental = rentalService.findById(rentalID);
        // rental.setReviewID(review.getReviewID());
        // rentalService.save(rental);

        return "redirect:/renterRentalList?userID=" + userID;
    }
}
