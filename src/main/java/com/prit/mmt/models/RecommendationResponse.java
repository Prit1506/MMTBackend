package com.prit.mmt.models;

public class RecommendationResponse {
    private String id;
    private String type; // "FLIGHT" or "HOTEL"
    private String name;
    private String description;
    private String location;
    private double price;
    private String reason;        // "Why this recommendation?" text
    private String reasonCategory; // e.g. "past_destination", "price_match", "collaborative", "trending"
    private double matchScore;
    private String amenities;
    private String from;
    private String to;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getReasonCategory() { return reasonCategory; }
    public void setReasonCategory(String reasonCategory) { this.reasonCategory = reasonCategory; }

    public double getMatchScore() { return matchScore; }
    public void setMatchScore(double matchScore) { this.matchScore = matchScore; }

    public String getAmenities() { return amenities; }
    public void setAmenities(String amenities) { this.amenities = amenities; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
}