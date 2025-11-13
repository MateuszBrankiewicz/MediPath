package com.adam.medipathbackend.repository;

import com.adam.medipathbackend.models.Institution;
import com.adam.medipathbackend.models.StaffDigest;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

public interface InstitutionRepository extends MongoRepository<Institution, String> {

    @Query("{_id: {$oid: ?0}, isActive: true}")
    Optional<Institution> findActiveById(String id);

    @Aggregation({"{$match: { name: { $regex: /?0/i }}}"})
    ArrayList<Institution> findInstitutionByName(String name);

  @Aggregation({ "{$match: { types: { $elemMatch: { $regex: /?0/i }}}}" })
  ArrayList<Institution> findInstitutionBySpec(String name);

    @Aggregation({"{$addFields: { cityProvince: {$concat: [\"$address.city\", \",\", \"$address.province\"]} }}", "{$match: { cityProvince: { $regex: /?0/i }, name: { $regex: /?1/i }, isActive: true}}"})
    ArrayList<Institution> findInstitutionByCity(String cityProvince, String name);


    @Aggregation({"{$addFields: { cityProvince: {$concat: [\"$address.city\", \",\", \"$address.province\"]} }}", "{$match: { cityProvince: { $regex: /?0/i }, name: { $regex: /?1/i }, types: {$in: ?2 }, isActive: true }}"})
    ArrayList<Institution> findInstitutionByCityAndSpec(String cityProvince, String name, String[] types);

    @Aggregation({
            "{ \"$unwind\": \"$employees\" }",
            "{ \"$addFields\": { \"cityProvince\": { \"$concat\": [ \"$address.city\", \",\", \"$address.province\" ] }, \"doctorName\": { \"$concat\": [ \"$employees.name\", \" \", \"$employees.surname\" ] } } }",
            "{ \"$match\": { \"cityProvince\": { \"$regex\": ?0, \"$options\": \"i\" }, \"doctorName\": { \"$regex\": ?1, \"$options\": \"i\" }, \"employees.roleCode\": { \"$in\": [2, 3, 6, 7, 14, 15] }, isActive: true } }",
            "{ \"$project\": { \"_id\": 1, \"name\": \"$employees.name\", \"surname\": \"$employees.surname\", \"specialisations\": \"$employees.specialisations\", \"userId\": \"$employees.userId\", \"roleCode\": \"$employees.roleCode\", \"pfpimage\": \"$employees.pfpimage\"} }"
    })
    ArrayList<StaffDigest> findDoctorsByCity(String cityProvince, String doctorName);

    @Aggregation({
            "{ \"$unwind\": \"$employees\" }",
            "{ \"$addFields\": { \"cityProvince\": { \"$concat\": [ \"$address.city\", \",\", \"$address.province\" ] }, \"doctorName\": { \"$concat\": [ \"$employees.name\", \" \", \"$employees.surname\" ] } } }",
            "{ \"$match\": { \"cityProvince\": { \"$regex\": ?0, \"$options\": \"i\" }, \"doctorName\": { \"$regex\": ?1, \"$options\": \"i\" }, \"employees.roleCode\": { \"$in\": [2, 3, 6, 7, 14, 15] }, \"employees.specialisations\": { $in: ?2 }, isActive: true } }",
            "{ \"$project\": { \"_id\": 1, \"name\": \"$employees.name\", \"surname\": \"$employees.surname\", \"specialisations\": \"$employees.specialisations\", \"userId\": \"$employees.userId\", \"roleCode\": \"$employees.roleCode\", \"pfpimage\": \"$employees.pfpimage\" } }"
    })
    ArrayList<StaffDigest> findDoctorsByCityAndSpec(String cityProvince, String doctorName, String[] spec);

    @Aggregation({
            "{ \"$unwind\": \"$employees\" }",
            "{ \"$match\": { \"employees.userId\": ?0, \"_id\": {$oid: ?1}, \"employees.roleCode\": { \"$in\": [4, 5, 6, 7, 12, 13, 14, 15] }, isActive: true } }",
            "{ \"$project\": { \"_id\": 1, \"name\": \"$employees.name\", \"surname\": \"$employees.surname\", \"specialisations\": \"$employees.specialisations\", \"userId\": \"$employees.userId\", \"roleCode\": \"$employees.roleCode\", \"pfpimage\": \"$employees.pfpimage\"} }"
    })
    Optional<StaffDigest> findStaffById(String staffid, String institutionid);

    @Aggregation({
            "{ \"$unwind\": \"$employees\" }",
            "{ \"$match\": { \"employees.userId\": ?0, \"_id\": {$oid: ?1}, isActive: true } }",
            "{ \"$project\": { \"_id\": 1, \"name\": \"$employees.name\", \"surname\": \"$employees.surname\", \"specialisations\": \"$employees.specialisations\", \"userId\": \"$employees.userId\", \"roleCode\": \"$employees.roleCode\", \"pfpimage\": \"$employees.pfpimage\"} }"
    })
    Optional<StaffDigest> findStaffORDoctorById(String staffid, String institutionid);

    @Aggregation({
            "{ \"$unwind\": \"$employees\" }",
            "{ \"$match\": { \"employees.userId\": ?0, \"_id\": {$oid: ?1}, \"employees.roleCode\": { \"$in\": [2, 3, 6, 7, 10, 11, 14, 15] }, isActive: true } }",
            "{ \"$project\": { \"_id\": 1, \"name\": \"$employees.name\", \"surname\": \"$employees.surname\", \"specialisations\": \"$employees.specialisations\", \"userId\": \"$employees.userId\", \"roleCode\": \"$employees.roleCode\", \"pfpimage\": \"$employees.pfpimage\"} }"
    })
    Optional<StaffDigest> findDoctorById(String staffid, String institutionid);

    @Aggregation({
            "{ \"$unwind\": \"$employees\" }",
            "{ \"$match\": { \"_id\": {$oid: ?0}, \"employees.roleCode\": { \"$in\": [2, 3, 6, 7, 10, 11, 14, 15] }, isActive: true } }",
            "{ \"$project\": { \"_id\": 1, \"name\": \"$employees.name\", \"surname\": \"$employees.surname\", \"specialisations\": \"$employees.specialisations\", \"userId\": \"$employees.userId\", \"roleCode\": \"$employees.roleCode\", \"pfpimage\": \"$employees.pfpimage\"} }"
    })
    ArrayList<StaffDigest> findDoctorsInInstitution(String institutionid);

    @Aggregation({
            "{ \"$unwind\": \"$employees\" }",
            "{ \"$match\": { \"employees.userId\": ?0, \"_id\": {$oid: ?1}, \"employees.roleCode\": { \"$in\": [8, 9, 10, 11, 12, 13, 14, 15] }, isActive: true } }",
            "{ \"$project\": { \"_id\": 1, \"name\": \"$employees.name\", \"surname\": \"$employees.surname\", \"specialisations\": \"$employees.specialisations\", \"userId\": \"$employees.userId\", \"roleCode\": \"$employees.roleCode\", \"pfpimage\": \"$employees.pfpimage\"} }"
    })
    Optional<StaffDigest> findAdminById(String adminid, String institutionid);

    @Aggregation({
            "{ \"$unwind\": \"$employees\" }",
            "{ \"$match\": { \"employees.userId\": ?0, \"employees.roleCode\": { \"$in\": [8, 9, 10, 11, 12, 13, 14, 15] }, isActive: true } }",
    })
    ArrayList<Institution> findInstitutionsWhereAdmin(String adminid);

  @Aggregation({
      "{ \"$unwind\": \"$employees\" }",
      "{ \"$match\": { \"employees.userId\": ?0, \"employees.roleCode\": { \"$in\": [4, 5, 6, 7, 12, 13, 14, 15] } } }",
  })
  ArrayList<Institution> findInstitutionsWhereStaff(String staffid);

    @Aggregation({
            "{ \"$unwind\": \"$employees\" }",
            "{ \"$match\": { \"employees.userId\": ?0 }, isActive: true }",
            "{ \"$project\": { \"_id\": 1, \"roleCode\": \"$employees.roleCode\"} }"
    })
    ArrayList<Map<String, Object>> getRoleCodes(String userId);


  @Aggregation({
      "{ \"$unwind\": \"$employees\" }",
      "{ \"$match\": { \"_id\": {$oid: ?0}, \"employees.roleCode\": { \"$gt\": 1} } }",
      "{ \"$project\": { \"_id\": 1, \"name\": \"$employees.name\", \"surname\": \"$employees.surname\", \"specialisations\": \"$employees.specialisations\", \"userId\": \"$employees.userId\", \"roleCode\": \"$employees.roleCode\", \"pfpimage\": \"$employees.pfpimage\"} }"
  })
  ArrayList<StaffDigest> findEmployeesInInstitution(String institutionid);
}
