package com.prit.mmt.services;

import com.prit.mmt.models.Flight;
import com.prit.mmt.models.Hotel;
import com.prit.mmt.models.Users;
import com.prit.mmt.models.Users.Booking;
import com.prit.mmt.repositories.FlightRepository;
import com.prit.mmt.repositories.HotelRepository;
import com.prit.mmt.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookingService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private HotelRepository hotelRepository;

    public Users bookFlight(String userId, String flightId, int seats, double price, String selectedSeat) {
        Optional<Users> usersOptional = userRepository.findById(userId);
        Optional<Flight> flightOptional = flightRepository.findById(flightId);
        if (usersOptional.isPresent() && flightOptional.isPresent()) {
            Users user = usersOptional.get();
            Flight flight = flightOptional.get();
            if (flight.getAvailableSeats() >= seats) {
                flight.setAvailableSeats(flight.getAvailableSeats() - seats);
                flightRepository.save(flight);

                Booking booking = new Booking();
                booking.setId(UUID.randomUUID().toString());
                booking.setType("Flight");
                booking.setBookingId(flightId);
                booking.setDate(LocalDate.now().toString());
                booking.setQuantity(seats);
                booking.setTotalPrice(price);
                booking.setStatus("CONFIRMED");
                booking.setSelectedSeat(selectedSeat); // Save Seat

                user.getBookings().add(booking);
                return userRepository.save(user);
            } else {
                throw new RuntimeException("Not enough seats available");
            }
        }
        throw new RuntimeException("User or flight not found");
    }

    public Users bookhotel(String userId, String hotelId, int rooms, double price, String selectedRoom) {
        Optional<Users> usersOptional = userRepository.findById(userId);
        Optional<Hotel> hotelOptional = hotelRepository.findById(hotelId);
        if (usersOptional.isPresent() && hotelOptional.isPresent()) {
            Users user = usersOptional.get();
            Hotel hotel = hotelOptional.get();
            if (hotel.getAvailableRooms() >= rooms) {
                hotel.setAvailableRooms(hotel.getAvailableRooms() - rooms);
                hotelRepository.save(hotel);

                Booking booking = new Booking();
                booking.setId(UUID.randomUUID().toString());
                booking.setType("Hotel");
                booking.setBookingId(hotelId);
                booking.setDate(LocalDate.now().toString());
                booking.setQuantity(rooms);
                booking.setTotalPrice(price);
                booking.setStatus("CONFIRMED");
                booking.setSelectedRoom(selectedRoom); // Save Room

                user.getBookings().add(booking);
                return userRepository.save(user);
            } else {
                throw new RuntimeException("Not enough rooms available");
            }
        }
        throw new RuntimeException("User or hotel not found");
    }

    public Users cancelBooking(String userId, String uniqueBookingId, String reason) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        for (Booking booking : user.getBookings()) {
            if (uniqueBookingId.equals(booking.getId()) && !"CANCELLED".equals(booking.getStatus())) {
                booking.setStatus("CANCELLED");
                booking.setCancellationReason(reason);
                booking.setRefundAmount(booking.getTotalPrice() * 0.5);
                booking.setRefundStatus("PENDING");

                if ("Flight".equalsIgnoreCase(booking.getType())) {
                    flightRepository.findById(booking.getBookingId()).ifPresent(flight -> {
                        flight.setAvailableSeats(flight.getAvailableSeats() + booking.getQuantity());
                        flightRepository.save(flight);
                    });
                } else if ("Hotel".equalsIgnoreCase(booking.getType())) {
                    hotelRepository.findById(booking.getBookingId()).ifPresent(hotel -> {
                        hotel.setAvailableRooms(hotel.getAvailableRooms() + booking.getQuantity());
                        hotelRepository.save(hotel);
                    });
                }
                return userRepository.save(user);
            }
        }
        throw new RuntimeException("Booking not found or already cancelled");
    }

    // Add this to your BookingService.java
    public Users freezePrice(String userId, String targetId, String type, double lockedPrice) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Users.PriceFreeze freeze = new Users.PriceFreeze();
        freeze.setFreezeId(UUID.randomUUID().toString());
        freeze.setTargetId(targetId);
        freeze.setType(type); // "FLIGHT" or "HOTEL"
        freeze.setLockedPrice(lockedPrice);
        // Set expiry to exactly 24 hours from right now
        freeze.setExpiryTimestamp(System.currentTimeMillis() + (24 * 60 * 60 * 1000));

        user.getPriceFreezes().add(freeze);

        // Save the updated user back to MongoDB
        return userRepository.save(user);
    }
}