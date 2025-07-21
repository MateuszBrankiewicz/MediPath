package com.adam.medipathbackend.repository;

import com.adam.medipathbackend.models.City;
import com.adam.medipathbackend.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    @Query("{'email': ?0}")
    Optional<User> findByEmail(String email);

    @Query("{'govID': ?0}")
    Optional<User> findByGovID(String id);

    long count();

}
