package com.adam.medipathbackend.repository;

import com.adam.medipathbackend.models.PasswordResetEntry;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PasswordResetEntryRepository extends MongoRepository<PasswordResetEntry, String> {

    @Query("{'email': ?0, 'dateIssued': {$gt: {$dateSubtract: {startDate: Date(), unit: 'day', amount: 1}}}}")
    List<PasswordResetEntry> findLatestByEmail(String email);

    @Query("{'token': ?0, 'gotUsed': false, 'dateExpiry': {$lt: {$dateAdd: {startDate: Date(), unit: 'minute', amount: 10}}}}")
    Optional<PasswordResetEntry> findValidToken(String token);

}
