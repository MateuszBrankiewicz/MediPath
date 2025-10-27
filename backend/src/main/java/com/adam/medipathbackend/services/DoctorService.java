package com.adam.medipathbackend.services;

import com.adam.medipathbackend.forms.DoctorUpdateForm;
import com.adam.medipathbackend.models.*;
import com.adam.medipathbackend.repository.*;
import com.adam.medipathbackend.config.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class DoctorService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    InstitutionRepository institutionRepository;
    @Autowired
    ScheduleRepository scheduleRepository;
    @Autowired
    VisitRepository visitRepository;

    public Map<String, Object> getDoctor(String id, String[] fields) throws IllegalArgumentException {

        Optional<User> user = userRepository.findDoctorById(id);
        if(user.isEmpty()) throw new IllegalArgumentException("invalid user id");

        User foundDoctor = user.get();

        Map<String, Object> outputFields = new HashMap<>();
        List<String> fieldsList = (fields == null) ?
                List.of("id", "name", "surname", "licence_number", "specialisations", "employers", "rating", "numofratings", "image") :
                List.of(fields);

        if(fieldsList.contains("id")) outputFields.put("id", foundDoctor.getId());
        if(fieldsList.contains("name")) outputFields.put("name", foundDoctor.getName());
        if(fieldsList.contains("surname")) outputFields.put("surname", foundDoctor.getSurname());

        if(fieldsList.contains("licence_number")) outputFields.put("licence_number", foundDoctor.getLicenceNumber());
        if(fieldsList.contains("specialisations")) outputFields.put("specialisations", foundDoctor.getSpecialisations());

        if(fieldsList.contains("rating")) outputFields.put("rating", foundDoctor.getRating());
        if(fieldsList.contains("employers")) outputFields.put("employers", foundDoctor.getEmployers());

        if(fieldsList.contains("numofratings")) outputFields.put("numofratings", foundDoctor.getNumOfRatings());
        if(fieldsList.contains("image")) outputFields.put("image", foundDoctor.getPfpimage());

        return Map.of("doctor", outputFields);
    }

    public Map<String, Object> getDoctorInstitutions(String id) throws IllegalArgumentException {

        Optional<User> user = userRepository.findDoctorById(id);
        if(user.isEmpty()) throw new IllegalArgumentException("invalid user id");

        User foundDoctor = user.get();
        ArrayList<InstitutionDigest> employers = foundDoctor.getEmployers();
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        boolean updated = false;

        for(int i = 0; i < employers.size(); i++) {

            InstitutionDigest digest = employers.get(i);
            Optional<Institution> institutionOpt = institutionRepository.findById(digest.getInstitutionId());

            if(institutionOpt.isEmpty()) {
                continue;
            }
            Institution institution = institutionOpt.get();

            if (!institution.getName().equals(digest.getInstitutionName())) {
                    employers.set(i, new InstitutionDigest(digest.getInstitutionId(), institution.getName()));
                    updated = true;
            }

            results.add(Map.of("institutionId", institution.getId(), "institutionName", institution.getName(),
                  "image", institution.getImage(), "address", institution.getAddress()));

        }
        if(updated) {

            foundDoctor.setEmployers(employers);
            userRepository.save(foundDoctor);

        }
        return Map.of("institutions", results);
    }

    public Map<String, Object> getDoctorsSchedules(String doctorid, String institution) throws IllegalArgumentException {

        Optional<User> doctorOpt = userRepository.findDoctorById(doctorid);
        if(doctorOpt.isEmpty()) throw new IllegalArgumentException("Doctor not found");

        if(institution == null) {
            return Map.of("schedules", scheduleRepository.getUpcomingSchedulesByDoctor(doctorid));
        }

        User doctor = doctorOpt.get();
        if(doctor.getEmployers().stream().noneMatch(
              employer -> employer.getInstitutionId().equals(institution)))
           throw new IllegalArgumentException("Doctor not employed at institution");

        return Map.of("schedules", scheduleRepository.getUpcomingSchedulesByDoctorInInstitution(doctorid, institution));

    }

    public Map<String, Object> getMySchedules(String loggedUserID) throws IllegalArgumentException, IllegalAccessException {

        if(userRepository.findDoctorById(loggedUserID).isEmpty()) throw new IllegalAccessException("User is not a doctor");
        ArrayList<Schedule> schedules = scheduleRepository.getSchedulesByDoctor(loggedUserID);

        return Map.of("schedules", schedules);
    }

    public void updateDoctor(String doctorid, DoctorUpdateForm doctorUpdateForm, String loggedUserID) throws IllegalArgumentException, IllegalAccessException {

        Optional<User> adminOpt = userRepository.findById(loggedUserID);
        if(adminOpt.isEmpty() || adminOpt.get().getRoleCode() < 8) throw new IllegalAccessException("User not authorized");
        if(!Utils.isValidMongoOID(doctorid)) throw new IllegalAccessException("Invalid doctor id");

        Optional<User> doctorOpt = userRepository.findDoctorById(doctorid);

        if(doctorOpt.isEmpty()) throw new IllegalAccessException("Doctor not found");
        if(doctorUpdateForm.getLicenceNumber() == null) throw new IllegalArgumentException("Missing licence number");
        if(doctorUpdateForm.getSpecialisations() == null) throw new IllegalArgumentException("Missing specialisations");

        User doctor = doctorOpt.get();
        doctor.setLicenceNumber(doctorUpdateForm.getLicenceNumber());
        doctor.setSpecialisations(doctorUpdateForm.getSpecialisations());

        userRepository.save(doctor);
    }

    public Map<String, Object> getMyVisitsByDate(String date, String loggedUserID) throws IllegalArgumentException, IllegalAccessException {

        if(!Utils.isValidMongoOID(loggedUserID)) throw new IllegalAccessException("Invalid user id");
        Optional<User> doctorOpt = userRepository.findDoctorById(loggedUserID);

        if(doctorOpt.isEmpty()) throw new IllegalAccessException("Doctor not found");

        if(date == null) {
            ArrayList<Visit> visits = visitRepository.getAllVisitsForDoctor(loggedUserID);
            return Map.of("visits", visits);
        }

        if(date.equals("today")) {
            date = LocalDate.now().toString();
        }

        LocalDate startDate;
        try {
            startDate = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("invalid date");
        }

        ArrayList<Visit> visits = visitRepository.
                getDoctorVisitsOnDay(loggedUserID, startDate.atStartOfDay(), startDate.plusDays(1).atStartOfDay());

        return Map.of("visits", visits);
    }

    public Map<String, Object> getMyPatients(String loggedUserID) throws IllegalArgumentException, IllegalAccessException {

        if(!Utils.isValidMongoOID(loggedUserID)) throw new IllegalAccessException("Invalid user id");
        Optional<User> doctorOpt = userRepository.findDoctorById(loggedUserID);

        if(doctorOpt.isEmpty()) throw new IllegalAccessException("Doctor not found");


        ArrayList<Visit> visits = visitRepository.getAllVisitsForDoctor(loggedUserID);

        return Map.of("patients", visits.stream().map(Visit::getPatient).toList());

    }
}
