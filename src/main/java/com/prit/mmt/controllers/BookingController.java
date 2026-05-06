package com.prit.mmt.controllers;

import com.prit.mmt.models.Users;
import com.prit.mmt.services.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/booking")
@CrossOrigin(origins = "*")
public class BookingController {
    @Autowired
    private BookingService bookingService;

    @PostMapping("/flight")
    public Users bookFlight(@RequestParam String userId, @RequestParam String flightId, @RequestParam int seats, @RequestParam double price, @RequestParam(required = false) String selectedSeat) {
        return bookingService.bookFlight(userId, flightId, seats, price, selectedSeat);
    }

    @PostMapping("/hotel")
    public Users bookhotel(@RequestParam String userId, @RequestParam String hotelId, @RequestParam int rooms, @RequestParam double price, @RequestParam(required = false) String selectedRoom) {
        return bookingService.bookhotel(userId, hotelId, rooms, price, selectedRoom);
    }

    @PostMapping("/cancel")
    public Users cancelBooking(@RequestParam String userId, @RequestParam String bookingId, @RequestParam String reason) {
        return bookingService.cancelBooking(userId, bookingId, reason);
    }
}