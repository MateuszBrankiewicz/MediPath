package com.adam.medipathbackend.services;

import com.adam.medipathbackend.models.*;
import com.adam.medipathbackend.repository.InstitutionRepository;
import com.adam.medipathbackend.repository.ScheduleRepository;
import com.adam.medipathbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;

@Service
public class SearchService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    InstitutionRepository institutionRepository;

    @Autowired
    ScheduleRepository scheduleRepository;

    public List<Map<String, Serializable>> searchInstitutions(String[] specialisations,
                                                              String city,
                                                              String query) {

        ArrayList<Institution> institutions;

        if(specialisations == null || specialisations.length == 0) {
            institutions = institutionRepository.
                    findInstitutionByCity(city + ".*", query);
        } else {
            institutions = institutionRepository.
                    findInstitutionByCityAndSpec(city + ".*", query, specialisations);
        }
        if(institutions.isEmpty()) {
            return List.of();
        }

       return institutions.stream().map(institution ->
               Map.of("id", institution.getId(),
                       "name", institution.getName(),
                       "types", institution.getTypes(),
                       "image", institution.getImage(),
                       "address", institution.getAddress().toString(),
                       "isPublic", institution.isPublic(),
                       "rating", institution.getRating(),
                       "numOfRatings", institution.getNumOfRatings()))
               .toList();

    }


    public List<Map<?, Object>> searchDoctors(String[] specialisations,
                                                              String city,
                                                              String query) {

        ArrayList<StaffDigest> doctors;

        if(specialisations == null || specialisations.length == 0) {
            doctors = institutionRepository
                    .findDoctorsByCity(city + ".*", query);
        } else {
            doctors = institutionRepository
                    .findDoctorsByCityAndSpec(city + ".*", query, specialisations);
        }

        if(doctors.isEmpty()) {
           return List.of();
        }

        Set<String> doctorIds = new HashSet<>();
        for(StaffDigest doc: doctors) {
            doctorIds.add(doc.getUserId());
        }

        return doctorIds.stream().map(doctor -> {
                    Optional<User> doctorOpt =  userRepository.findById(doctor);
                    if(doctorOpt.isEmpty()) {
                        return Map.of();
                    }
                    User doctorProfile = doctorOpt.get();
                    return Map.of("id", doctor,
                            "name", doctorProfile.getName(),
                            "surname", doctorProfile.getSurname(),
                            "specialisations", doctorProfile.getSpecialisations(),
                            "addresses", getAddressesForDoctor(doctor),
                            "schedules", getSchedulesTruncatedForDoctor(doctor),
                            "image", doctorProfile.getPfpimage(),
                            "rating", doctorProfile.getRating(),
                            "numOfRatings", doctorProfile.getNumOfRatings());
                }

        ).toList();
    }

    private ArrayList<Pair<InstitutionDigest, String>> getAddressesForDoctor(String userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if(userOptional.isEmpty()) {
            return new ArrayList<>();
        }
        User user = userOptional.get();
        ArrayList<Pair<InstitutionDigest ,String>> addresses = new ArrayList<>();
        for(InstitutionDigest digest: user.getEmployers()) {
            Optional<Institution> institutionOptional = institutionRepository.findById(digest.getInstitutionId());
            if(institutionOptional.isEmpty()) continue;
            addresses.add(Pair.of(digest, institutionOptional.get().getAddress().toString()));
        }
        return addresses;
    }

    private Object getSchedulesTruncatedForDoctor(String userid) {
        ArrayList<Schedule> schedules = scheduleRepository.getUpcomingSchedulesByDoctor(userid);
        return schedules.stream().map(schedule ->
                Map.of("id", schedule.getId(),
                        "startTime", schedule.getStartHour().toString(),
                        "isBooked", schedule.isBooked(),
                        "institution", schedule.getInstitution())).toList();
    }

}
