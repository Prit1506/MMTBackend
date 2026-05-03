package com.prit.mmt.repositories;

import com.prit.mmt.models.Flight;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FlightRepository  extends MongoRepository<Flight,String>{
}