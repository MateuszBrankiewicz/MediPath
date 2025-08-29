package com.adam.medipathbackend.repository;

import com.adam.medipathbackend.models.PasswordResetEntry;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PasswordResetEntryRepository extends MongoRepository<PasswordResetEntry, String> {

    @Query("{'email': ?0, 'dateIssued': {$gt: new Date(ISODate().getTime() - 1000*3600*24)}")
    List<PasswordResetEntry> findLatestByEmail(String email);

    @Query("{'token': ?0, 'gotUsed': false, 'dateExpiry': { $gt: new Date() }}")
    Optional<PasswordResetEntry> findValidToken(String token);

}
