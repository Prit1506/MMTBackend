package com.prit.mmt.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "users")
public class Users {
    @Id
    private String _id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String role;
    private String phoneNumber;
    private List<Booking> bookings = new ArrayList<>();

    public String getFirstName() { return firstName; }
    public String getId() { return _id; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }
    public List<Booking> getBookings() { return bookings; }
    public void setBookings(List<Booking> bookings) { this.bookings = bookings; }

    public static class Booking {
        private String id;
        private String type;
        private String bookingId;
        private String date;
        private int quantity;
        private double totalPrice;

        // --- NEW FIELDS FOR SEAT/ROOM SELECTION ---
        private String selectedSeat;
        private String selectedRoom;

        // Cancellation & Refund Fields
        private String status = "CONFIRMED";
        private String cancellationReason;
        private double refundAmount;
        private String refundStatus;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getBookingId() { return bookingId; }
        public void setBookingId(String bookingId) { this.bookingId = bookingId; }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public double getTotalPrice() { return totalPrice; }
        public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

        public String getSelectedSeat() { return selectedSeat; }
        public void setSelectedSeat(String selectedSeat) { this.selectedSeat = selectedSeat; }

        public String getSelectedRoom() { return selectedRoom; }
        public void setSelectedRoom(String selectedRoom) { this.selectedRoom = selectedRoom; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getCancellationReason() { return cancellationReason; }
        public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

        public double getRefundAmount() { return refundAmount; }
        public void setRefundAmount(double refundAmount) { this.refundAmount = refundAmount; }

        public String getRefundStatus() { return refundStatus; }
        public void setRefundStatus(String refundStatus) { this.refundStatus = refundStatus; }
    }

    // Add this inside your Users class, next to the Booking class
    private List<PriceFreeze> priceFreezes = new ArrayList<>();

    public List<PriceFreeze> getPriceFreezes() { return priceFreezes; }
    public void setPriceFreezes(List<PriceFreeze> priceFreezes) { this.priceFreezes = priceFreezes; }

    public static class PriceFreeze {
        private String freezeId;
        private String targetId; // Flight ID or Hotel ID
        private String type; // "FLIGHT" or "HOTEL"
        private double lockedPrice;
        private long expiryTimestamp; // Unix timestamp for when the freeze expires

        // Getters and Setters
        public String getFreezeId() { return freezeId; }
        public void setFreezeId(String freezeId) { this.freezeId = freezeId; }
        public String getTargetId() { return targetId; }
        public void setTargetId(String targetId) { this.targetId = targetId; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public double getLockedPrice() { return lockedPrice; }
        public void setLockedPrice(double lockedPrice) { this.lockedPrice = lockedPrice; }
        public long getExpiryTimestamp() { return expiryTimestamp; }
        public void setExpiryTimestamp(long expiryTimestamp) { this.expiryTimestamp = expiryTimestamp; }
    }
}