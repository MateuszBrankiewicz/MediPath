package com.adam.medipathbackend.repository;

import com.adam.medipathbackend.models.Institution;
import com.adam.medipathbackend.models.User;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InstitutionRepository extends MongoRepository<Institution, String> {

    @Aggregation({"{$project: { name: 1 }}","{ $match: { name: { $regex: /?0/i }}}]"})
    List<User> findInstitutionByName(String name);

    @Aggregation({"{$project: { name: 1, types: 1}}", "{$match: { types: { $elemMatch: { $regex: /?0/i }}}"})
    List<User> findInstitutionBySpec(String name);
}
