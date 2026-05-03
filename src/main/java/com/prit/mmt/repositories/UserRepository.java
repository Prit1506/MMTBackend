package com.prit.mmt.repositories;
import com.prit.mmt.models.Users;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<Users,String>{
    Users findByEmail(String email);
}