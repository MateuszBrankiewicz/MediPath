package com.adam.medipathbackend.repository;

import com.adam.medipathbackend.models.Institution;
import com.adam.medipathbackend.models.StaffDigest;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.ArrayList;

public interface InstitutionRepository extends MongoRepository<Institution, String> {

    @Aggregation({"{$match: { name: { $regex: /?0/i }}}"})
    ArrayList<Institution> findInstitutionByName(String name);

    @Aggregation({"{$match: { types: { $elemMatch: { $regex: /?0/i }}}}"})
    ArrayList<Institution> findInstitutionBySpec(String name);

    @Aggregation({"{$addFields: { cityProvince: {$concat: [\"$address.city\", \",\", \"$address.province\"]} }}", "{$match: { cityProvince: { $regex: /?0/i }, name: { $regex: /?1/i } }}"})
    ArrayList<Institution> findInstitutionByCity(String cityProvince, String name);

    @Aggregation({
            "{ \"$unwind\": \"$employees\" }",
            "{ \"$addFields\": { \"cityProvince\": { \"$concat\": [ \"$address.city\", \",\", \"$address.province\" ] }, \"doctorName\": { \"$concat\": [ \"$employees.name\", \" \", \"$employees.surname\" ] } } }",
            "{ \"$match\": { \"cityProvince\": { \"$regex\": ?0, \"$options\": \"i\" }, \"doctorName\": { \"$regex\": ?1, \"$options\": \"i\" }, \"employees.roleCode\": { \"$in\": [2, 3, 6, 7, 14, 15] } } }",
            "{ \"$project\": { \"_id\": 1, \"name\": \"$employees.name\", \"surname\": \"$employees.surname\", \"specialisations\": \"$employees.specialisations\", \"userId\": \"$employees.userId\", \"roleCode\": \"$employees.roleCode\" } }"
    })
    ArrayList<StaffDigest> findDoctorsByCity(String cityProvinceRegex, String doctorNameRegex);
}
