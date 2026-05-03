package com.prit.mmt.repositories;

import com.prit.mmt.models.Hotel;
import org.springframework.data.mongodb.repository.MongoRepository;
public interface HotelRepository extends MongoRepository<Hotel,String>{
}