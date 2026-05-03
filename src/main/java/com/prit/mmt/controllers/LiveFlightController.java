package com.prit.mmt.controllers;

import com.prit.mmt.models.Flight;
import com.prit.mmt.models.FlightStatusResponse;
import com.prit.mmt.repositories.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@RestController
@RequestMapping("/live")
@CrossOrigin(origins = "*")
public class LiveFlightController {

    @Autowired
    private FlightRepository flightRepository;

    @GetMapping("/flight/{id}")
    public ResponseEntity<FlightStatusResponse> getFlightStatus(@PathVariable String id) {
        Optional<Flight> flightOpt = flightRepository.findById(id);
        if (!flightOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Flight flight = flightOpt.get();

        // Simulate real-time status changes every 2 minutes for demonstration purposes
        long timeWindow = System.currentTimeMillis() / 120000;
        int hash = Math.abs((id + timeWindow).hashCode());
        int state = hash % 5;

        FlightStatusResponse res = new FlightStatusResponse();
        res.setFlightId(id);
        res.setFlightName(flight.getFlightName());
        res.setFrom(flight.getFrom());
        res.setTo(flight.getTo());
        res.setOriginalDeparture(flight.getDepartureTime());
        res.setOriginalArrival(flight.getArrivalTime());

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        try {
            LocalDateTime dep = LocalDateTime.parse(flight.getDepartureTime(), formatter);
            LocalDateTime arr = LocalDateTime.parse(flight.getArrivalTime(), formatter);

            if (state == 0) {
                res.setStatus("ON TIME");
                res.setDelayMinutes(0);
                res.setMessage("Flight is on schedule. Proceed to security check.");
                res.setEstimatedDeparture(dep.toString());
                res.setEstimatedArrival(arr.toString());
            } else if (state == 1) {
                res.setStatus("BOARDING");
                res.setDelayMinutes(0);
                res.setMessage("Passengers are currently boarding at Gate 4.");
                res.setEstimatedDeparture(dep.toString());
                res.setEstimatedArrival(arr.toString());
            } else if (state == 2) {
                res.setStatus("DELAYED");
                res.setDelayMinutes(45);
                res.setMessage("Delayed by 45 mins due to air traffic congestion.");
                res.setEstimatedDeparture(dep.plusMinutes(45).toString());
                res.setEstimatedArrival(arr.plusMinutes(45).toString());
            } else if (state == 3) {
                res.setStatus("DELAYED");
                res.setDelayMinutes(120);
                res.setMessage("Delayed by 2 hours due to bad weather conditions.");
                res.setEstimatedDeparture(dep.plusMinutes(120).toString());
                res.setEstimatedArrival(arr.plusMinutes(120).toString());
            } else {
                res.setStatus("DEPARTED");
                res.setDelayMinutes(0);
                res.setMessage("Flight has successfully taken off.");
                res.setEstimatedDeparture(dep.toString());
                res.setEstimatedArrival(arr.toString());
            }
        } catch (Exception e) {
            // Fallback if time parsing fails
            res.setStatus("ON TIME");
            res.setDelayMinutes(0);
            res.setMessage("Flight is on schedule.");
            res.setEstimatedDeparture(flight.getDepartureTime());
            res.setEstimatedArrival(flight.getArrivalTime());
        }

        return ResponseEntity.ok(res);
    }
}