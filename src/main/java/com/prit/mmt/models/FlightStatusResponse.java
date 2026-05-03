package com.prit.mmt.models;

public class FlightStatusResponse {
    private String flightId;
    private String flightName;
    private String from;
    private String to;
    private String status;
    private int delayMinutes;
    private String originalDeparture;
    private String originalArrival;
    private String estimatedDeparture;
    private String estimatedArrival;
    private String message;

    // Getters and Setters
    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId; }

    public String getFlightName() { return flightName; }
    public void setFlightName(String flightName) { this.flightName = flightName; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getDelayMinutes() { return delayMinutes; }
    public void setDelayMinutes(int delayMinutes) { this.delayMinutes = delayMinutes; }

    public String getOriginalDeparture() { return originalDeparture; }
    public void setOriginalDeparture(String originalDeparture) { this.originalDeparture = originalDeparture; }

    public String getOriginalArrival() { return originalArrival; }
    public void setOriginalArrival(String originalArrival) { this.originalArrival = originalArrival; }

    public String getEstimatedDeparture() { return estimatedDeparture; }
    public void setEstimatedDeparture(String estimatedDeparture) { this.estimatedDeparture = estimatedDeparture; }

    public String getEstimatedArrival() { return estimatedArrival; }
    public void setEstimatedArrival(String estimatedArrival) { this.estimatedArrival = estimatedArrival; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}