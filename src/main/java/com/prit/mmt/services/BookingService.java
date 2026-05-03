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

    public Users bookFlight(String userId, String flightId, int seats, double price) {
        Optional<Users> usersOptional = userRepository.findById(userId);
        Optional<Flight> flightOptional = flightRepository.findById(flightId);
        if (usersOptional.isPresent() && flightOptional.isPresent()) {
            Users user = usersOptional.get();
            Flight flight = flightOptional.get();
            if (flight.getAvailableSeats() >= seats) {
                flight.setAvailableSeats(flight.getAvailableSeats() - seats);
                flightRepository.save(flight);

                Booking booking = new Booking();
                booking.setId(UUID.randomUUID().toString()); // Generate unique ID
                booking.setType("Flight");
                booking.setBookingId(flightId);
                booking.setDate(LocalDate.now().toString());
                booking.setQuantity(seats);
                booking.setTotalPrice(price);
                booking.setStatus("CONFIRMED");
                user.getBookings().add(booking);
                return userRepository.save(user); // Return updated User
            } else {
                throw new RuntimeException("Not enough seats available");
            }
        }
        throw new RuntimeException("User or flight not found");
    }

    public Users bookhotel(String userId, String hotelId, int rooms, double price) {
        Optional<Users> usersOptional = userRepository.findById(userId);
        Optional<Hotel> hotelOptional = hotelRepository.findById(hotelId);
        if (usersOptional.isPresent() && hotelOptional.isPresent()) {
            Users user = usersOptional.get();
            Hotel hotel = hotelOptional.get();
            if (hotel.getAvailableRooms() >= rooms) {
                hotel.setAvailableRooms(hotel.getAvailableRooms() - rooms);
                hotelRepository.save(hotel);

                Booking booking = new Booking();
                booking.setId(UUID.randomUUID().toString()); // Generate unique ID
                booking.setType("Hotel");
                booking.setBookingId(hotelId);
                booking.setDate(LocalDate.now().toString());
                booking.setQuantity(rooms);
                booking.setTotalPrice(price);
                booking.setStatus("CONFIRMED");
                user.getBookings().add(booking);
                return userRepository.save(user); // Return updated User
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

                // Calculate 50% Refund
                booking.setRefundAmount(booking.getTotalPrice() * 0.5);
                booking.setRefundStatus("PENDING");

                // Restore Inventory based on type
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
}