package com.prit.mmt.services;

import com.prit.mmt.models.Flight;
import com.prit.mmt.models.Hotel;
import com.prit.mmt.models.RecommendationResponse;
import com.prit.mmt.models.Users;
import com.prit.mmt.repositories.FlightRepository;
import com.prit.mmt.repositories.HotelRepository;
import com.prit.mmt.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    @Autowired private UserRepository userRepository;
    @Autowired private FlightRepository flightRepository;
    @Autowired private HotelRepository hotelRepository;

    /**
     * Main recommendation engine.
     * Combines content-based filtering (user history/preferences) with
     * lightweight collaborative filtering (other users' patterns).
     */
    public List<RecommendationResponse> getRecommendations(String userId) {
        List<Flight> allFlights = flightRepository.findAll();
        List<Hotel> allHotels = hotelRepository.findAll();

        // Anonymous / new user: return trending items
        if (userId == null || userId.isBlank()) {
            return buildTrendingRecommendations(allFlights, allHotels, Collections.emptySet(), Collections.emptySet());
        }

        Optional<Users> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return buildTrendingRecommendations(allFlights, allHotels, Collections.emptySet(), Collections.emptySet());
        }

        Users user = userOpt.get();
        List<Users.Booking> activeBookings = user.getBookings().stream()
                .filter(b -> !"CANCELLED".equals(b.getStatus()))
                .collect(Collectors.toList());

        // ── Extract preferences ────────────────────────────────────────────────
        Set<String> bookedFlightIds = new HashSet<>();
        Set<String> bookedHotelIds = new HashSet<>();
        Set<String> visitedDestinations = new HashSet<>();
        Set<String> visitedOrigins = new HashSet<>();
        double totalFlightSpend = 0; int flightCount = 0;
        double totalHotelSpend = 0; int hotelCount = 0;

        for (Users.Booking b : activeBookings) {
            if ("Flight".equals(b.getType())) {
                bookedFlightIds.add(b.getBookingId());
                flightRepository.findById(b.getBookingId()).ifPresent(f -> {
                    visitedDestinations.add(f.getTo().trim().toLowerCase());
                    visitedOrigins.add(f.getFrom().trim().toLowerCase());
                });
                totalFlightSpend += b.getTotalPrice();
                flightCount++;
            } else if ("Hotel".equals(b.getType())) {
                bookedHotelIds.add(b.getBookingId());
                hotelRepository.findById(b.getBookingId()).ifPresent(h ->
                        visitedDestinations.add(h.getLocation().trim().toLowerCase()));
                totalHotelSpend += b.getTotalPrice();
                hotelCount++;
            }
        }

        double avgFlightPrice = flightCount > 0 ? totalFlightSpend / flightCount : 0;
        double avgHotelNightlyPrice = hotelCount > 0 ? (totalHotelSpend / hotelCount) / 3.0 : 0;

        // IDs the user marked as "not helpful" → exclude from results
        Set<String> irrelevantIds = user.getRecommendationFeedbacks().stream()
                .filter(f -> !f.isHelpful())
                .map(Users.RecommendationFeedback::getTargetId)
                .collect(Collectors.toSet());

        // ── Collaborative filtering: find users with overlapping bookings ──────
        Set<String> collaborativeDestinations = new HashSet<>();
        if (!visitedDestinations.isEmpty()) {
            List<Users> allUsers = userRepository.findAll();
            for (Users other : allUsers) {
                if (other.getId().equals(userId)) continue;
                boolean overlap = other.getBookings().stream()
                        .filter(b -> "Flight".equals(b.getType()) && !"CANCELLED".equals(b.getStatus()))
                        .anyMatch(b -> {
                            Optional<Flight> fOpt = flightRepository.findById(b.getBookingId());
                            return fOpt.map(f -> visitedDestinations.contains(f.getTo().trim().toLowerCase())).orElse(false);
                        });
                if (overlap) {
                    // Collect destinations this "similar" user went to
                    for (Users.Booking b : other.getBookings()) {
                        if ("Flight".equals(b.getType()) && !"CANCELLED".equals(b.getStatus())) {
                            flightRepository.findById(b.getBookingId()).ifPresent(f ->
                                    collaborativeDestinations.add(f.getTo().trim().toLowerCase()));
                        }
                        if ("Hotel".equals(b.getType()) && !"CANCELLED".equals(b.getStatus())) {
                            hotelRepository.findById(b.getBookingId()).ifPresent(h ->
                                    collaborativeDestinations.add(h.getLocation().trim().toLowerCase()));
                        }
                    }
                }
            }
            collaborativeDestinations.removeAll(visitedDestinations);
        }

        // ── New user with no history ──────────────────────────────────────────
        if (activeBookings.isEmpty()) {
            return buildTrendingRecommendations(allFlights, allHotels, irrelevantIds, Collections.emptySet());
        }

        // ── Score flights ─────────────────────────────────────────────────────
        List<RecommendationResponse> recommendations = new ArrayList<>();

        for (Flight flight : allFlights) {
            if (bookedFlightIds.contains(flight.getId())) continue;
            if (irrelevantIds.contains(flight.getId())) continue;

            double score = 0;
            List<String> reasonParts = new ArrayList<>();
            String category = "trending";

            String dest = flight.getTo().trim().toLowerCase();
            String origin = flight.getFrom().trim().toLowerCase();

            // You've been to this destination before
            if (visitedDestinations.contains(dest)) {
                score += 6;
                reasonParts.add("You've previously flown to " + flight.getTo() + " — explore it again!");
                category = "past_destination";
            }
            // You depart from a city you know
            if (visitedOrigins.contains(origin) && !visitedDestinations.contains(dest)) {
                score += 3;
                reasonParts.add("Departing from " + flight.getFrom() + ", which you frequently use");
                if (category.equals("trending")) category = "origin_match";
            }
            // Collaborative: similar travelers went here
            if (collaborativeDestinations.contains(dest)) {
                score += 4;
                reasonParts.add("Travelers with similar tastes also loved " + flight.getTo());
                if (category.equals("trending")) category = "collaborative";
            }
            // Budget match
            if (avgFlightPrice > 0) {
                double ratio = Math.abs(flight.getPrice() * 1.15 - avgFlightPrice) / avgFlightPrice;
                if (ratio < 0.3) {
                    score += 2;
                    reasonParts.add("Matches your usual budget range");
                    if (category.equals("trending")) category = "price_match";
                }
            }

            if (score > 0) {
                RecommendationResponse rec = new RecommendationResponse();
                rec.setId(flight.getId());
                rec.setType("FLIGHT");
                rec.setName(flight.getFlightName());
                rec.setDescription(flight.getFrom() + " → " + flight.getTo());
                rec.setFrom(flight.getFrom());
                rec.setTo(flight.getTo());
                rec.setPrice(flight.getPrice());
                rec.setReason(String.join(". ", reasonParts));
                rec.setReasonCategory(category);
                rec.setMatchScore(score);
                recommendations.add(rec);
            }
        }

        // ── Score hotels ──────────────────────────────────────────────────────
        for (Hotel hotel : allHotels) {
            if (bookedHotelIds.contains(hotel.getId())) continue;
            if (irrelevantIds.contains(hotel.getId())) continue;

            double score = 0;
            List<String> reasonParts = new ArrayList<>();
            String category = "trending";

            String loc = hotel.getLocation().trim().toLowerCase();

            // User has flown to this city
            if (visitedDestinations.contains(loc)) {
                score += 7;
                reasonParts.add("You've visited " + hotel.getLocation() + " — find a great place to stay!");
                category = "past_destination";
            }
            // Collaborative match
            if (collaborativeDestinations.contains(loc)) {
                score += 4;
                reasonParts.add("Popular with travelers who share your preferences");
                if (category.equals("trending")) category = "collaborative";
            }
            // Budget match (compare nightly rate to ~⅓ of avg hotel booking price)
            if (avgHotelNightlyPrice > 0) {
                double ratio = Math.abs(hotel.getPricePerNight() - avgHotelNightlyPrice) / avgHotelNightlyPrice;
                if (ratio < 0.4) {
                    score += 2;
                    reasonParts.add("Priced within your comfort zone");
                    if (category.equals("trending")) category = "price_match";
                }
            }
            // Keyword-based preference inference (beach, hill, luxury…)
            if (hotel.getAmenities() != null) {
                String amenitiesLower = hotel.getAmenities().toLowerCase();
                boolean userLikesBeach = visitedDestinations.stream()
                        .anyMatch(d -> d.contains("goa") || d.contains("beach") || d.contains("coast"));
                boolean userLikesLuxury = avgHotelNightlyPrice > 3000;

                if (userLikesBeach && (amenitiesLower.contains("pool") || amenitiesLower.contains("beach")
                        || hotel.getLocation().toLowerCase().contains("goa"))) {
                    score += 3;
                    reasonParts.add("You love beach getaways — this is a perfect match!");
                    category = "preference_inferred";
                }
                if (userLikesLuxury && (amenitiesLower.contains("spa") || amenitiesLower.contains("luxury")
                        || hotel.getPricePerNight() > 3000)) {
                    score += 2;
                    reasonParts.add("Matches your preference for premium stays");
                    if (category.equals("trending")) category = "preference_inferred";
                }
            }

            if (score > 0) {
                RecommendationResponse rec = new RecommendationResponse();
                rec.setId(hotel.getId());
                rec.setType("HOTEL");
                rec.setName(hotel.getHotelName());
                rec.setDescription(hotel.getLocation());
                rec.setLocation(hotel.getLocation());
                rec.setPrice(hotel.getPricePerNight());
                rec.setAmenities(hotel.getAmenities());
                rec.setReason(String.join(". ", reasonParts));
                rec.setReasonCategory(category);
                rec.setMatchScore(score);
                recommendations.add(rec);
            }
        }

        // Sort descending by score
        recommendations.sort((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()));

        // Fill up to 8 with trending if we have fewer
        if (recommendations.size() < 8) {
            Set<String> existingIds = recommendations.stream().map(RecommendationResponse::getId).collect(Collectors.toSet());
            existingIds.addAll(bookedFlightIds);
            existingIds.addAll(bookedHotelIds);
            existingIds.addAll(irrelevantIds);
            buildTrendingRecommendations(allFlights, allHotels, existingIds, Collections.emptySet())
                    .stream()
                    .limit(8 - recommendations.size())
                    .forEach(recommendations::add);
        }

        return recommendations.stream().limit(8).collect(Collectors.toList());
    }

    /** Submit helpful / not-helpful feedback to refine future results */
    public Users submitFeedback(String userId, String targetId, String targetType, boolean helpful) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Remove any existing feedback for this item first (idempotent)
        user.getRecommendationFeedbacks().removeIf(f -> f.getTargetId().equals(targetId));

        Users.RecommendationFeedback fb = new Users.RecommendationFeedback();
        fb.setTargetId(targetId);
        fb.setTargetType(targetType);
        fb.setHelpful(helpful);
        fb.setTimestamp(java.time.LocalDateTime.now().toString());
        user.getRecommendationFeedbacks().add(fb);

        return userRepository.save(user);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private List<RecommendationResponse> buildTrendingRecommendations(
            List<Flight> flights, List<Hotel> hotels,
            Set<String> excludeIds, Set<String> extraExclude) {

        List<RecommendationResponse> result = new ArrayList<>();
        Set<String> skip = new HashSet<>(excludeIds);
        skip.addAll(extraExclude);

        for (int i = 0; i < flights.size() && result.size() < 4; i++) {
            Flight f = flights.get(i);
            if (skip.contains(f.getId())) continue;
            RecommendationResponse rec = new RecommendationResponse();
            rec.setId(f.getId());
            rec.setType("FLIGHT");
            rec.setName(f.getFlightName());
            rec.setDescription(f.getFrom() + " → " + f.getTo());
            rec.setFrom(f.getFrom());
            rec.setTo(f.getTo());
            rec.setPrice(f.getPrice());
            rec.setReason("Trending route popular among MakeMyTour travelers this week");
            rec.setReasonCategory("trending");
            rec.setMatchScore(1);
            result.add(rec);
        }

        for (int i = 0; i < hotels.size() && result.size() < 8; i++) {
            Hotel h = hotels.get(i);
            if (skip.contains(h.getId())) continue;
            RecommendationResponse rec = new RecommendationResponse();
            rec.setId(h.getId());
            rec.setType("HOTEL");
            rec.setName(h.getHotelName());
            rec.setDescription(h.getLocation());
            rec.setLocation(h.getLocation());
            rec.setPrice(h.getPricePerNight());
            rec.setAmenities(h.getAmenities());
            rec.setReason("Highly rated by the MakeMyTour community — a crowd favourite!");
            rec.setReasonCategory("trending");
            rec.setMatchScore(1);
            result.add(rec);
        }

        return result;
    }
}