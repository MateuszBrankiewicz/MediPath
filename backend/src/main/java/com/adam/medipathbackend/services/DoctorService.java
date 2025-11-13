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
import java.util.stream.Collectors;
import java.time.LocalDateTime;

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
    if (user.isEmpty())
      throw new IllegalArgumentException("invalid user id");

    User foundDoctor = user.get();

    Map<String, Object> outputFields = new HashMap<>();
    List<String> fieldsList = (fields == null) ? List.of("id", "name", "surname", "licence_number", "specialisations",
        "employers", "rating", "numofratings", "image") : List.of(fields);

    if (fieldsList.contains("id"))
      outputFields.put("id", foundDoctor.getId());
    if (fieldsList.contains("name"))
      outputFields.put("name", foundDoctor.getName());
    if (fieldsList.contains("surname"))
      outputFields.put("surname", foundDoctor.getSurname());

    if (fieldsList.contains("licence_number"))
      outputFields.put("licence_number", foundDoctor.getLicenceNumber());
    if (fieldsList.contains("specialisations"))
      outputFields.put("specialisations", foundDoctor.getSpecialisations());

    if (fieldsList.contains("rating"))
      outputFields.put("rating", foundDoctor.getRating());
    if (fieldsList.contains("employers"))
      outputFields.put("employers", foundDoctor.getEmployers());

    if (fieldsList.contains("numofratings"))
      outputFields.put("numofratings", foundDoctor.getNumOfRatings());
    if (fieldsList.contains("image"))
      outputFields.put("image", foundDoctor.getPfpimage());

    return Map.of("doctor", outputFields);
  }

  public Map<String, Object> getDoctorInstitutions(String id) throws IllegalArgumentException {

    Optional<User> user = userRepository.findDoctorById(id);
    if (user.isEmpty())
      throw new IllegalArgumentException("invalid user id");

    User foundDoctor = user.get();
    ArrayList<InstitutionDigest> employers = foundDoctor.getEmployers();
    ArrayList<Map<String, Object>> results = new ArrayList<>();
    boolean updated = false;

    for (int i = 0; i < employers.size(); i++) {

            InstitutionDigest digest = employers.get(i);
            Optional<Institution> institutionOpt = institutionRepository.findActiveById(digest.getInstitutionId());

      if (institutionOpt.isEmpty()) {
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
    if (updated) {

      foundDoctor.setEmployers(employers);
      userRepository.save(foundDoctor);

    }
    return Map.of("institutions", results);
  }

  public Map<String, Object> getDoctorsSchedules(String doctorid, String institution) throws IllegalArgumentException {

    Optional<User> doctorOpt = userRepository.findDoctorById(doctorid);
    if (doctorOpt.isEmpty())
      throw new IllegalArgumentException("Doctor not found");

    if (institution == null) {
      return Map.of("schedules", scheduleRepository.getUpcomingSchedulesByDoctor(doctorid));
    }

    User doctor = doctorOpt.get();
    if (doctor.getEmployers().stream().noneMatch(
        employer -> employer.getInstitutionId().equals(institution)))
      throw new IllegalArgumentException("Doctor not employed at institution");

    return Map.of("schedules", scheduleRepository.getUpcomingSchedulesByDoctorInInstitution(doctorid, institution));

  }

  public Map<String, Object> getMySchedules(String loggedUserID)
      throws IllegalArgumentException, IllegalAccessException {

    if (userRepository.findDoctorById(loggedUserID).isEmpty())
      throw new IllegalAccessException("User is not a doctor");
    ArrayList<Schedule> schedules = scheduleRepository.getSchedulesByDoctor(loggedUserID);

    return Map.of("schedules", schedules);
  }

  public void updateDoctor(String doctorid, DoctorUpdateForm doctorUpdateForm, String loggedUserID)
      throws IllegalArgumentException, IllegalAccessException {

        Optional<User> adminOpt = userRepository.findActiveById(loggedUserID);
        if(adminOpt.isEmpty() || adminOpt.get().getRoleCode() < 8) throw new IllegalAccessException("User not authorized");
        if(!Utils.isValidMongoOID(doctorid)) throw new IllegalAccessException("Invalid doctor id");

    Optional<User> doctorOpt = userRepository.findDoctorById(doctorid);

    if (doctorOpt.isEmpty())
      throw new IllegalAccessException("Doctor not found");
    if (doctorUpdateForm.getLicenceNumber() == null)
      throw new IllegalArgumentException("Missing licence number");
    if (doctorUpdateForm.getSpecialisations() == null)
      throw new IllegalArgumentException("Missing specialisations");

    User doctor = doctorOpt.get();
    doctor.setLicenceNumber(doctorUpdateForm.getLicenceNumber());
    doctor.setSpecialisations(doctorUpdateForm.getSpecialisations());

    userRepository.save(doctor);
  }

  public Map<String, Object> getMyVisitsByDate(String date, String loggedUserID)
      throws IllegalArgumentException, IllegalAccessException {

    if (!Utils.isValidMongoOID(loggedUserID))
      throw new IllegalAccessException("Invalid user id");
    Optional<User> doctorOpt = userRepository.findDoctorById(loggedUserID);

    if (doctorOpt.isEmpty())
      throw new IllegalAccessException("Doctor not found");

    if (date == null) {
      ArrayList<Visit> visits = visitRepository.getAllVisitsForDoctor(loggedUserID);
      return Map.of("visits", visits);
    }

    if (date.equals("today")) {
      date = LocalDate.now().toString();
    }

    LocalDate startDate;
    try {
      startDate = LocalDate.parse(date);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("invalid date");
    }

    ArrayList<Visit> visits = visitRepository.getDoctorVisitsOnDay(loggedUserID, startDate.atStartOfDay(),
        startDate.plusDays(1).atStartOfDay());

    return Map.of("visits", visits);
  }

  public Map<String, Object> getMyPatients(String loggedUserID)
      throws IllegalArgumentException, IllegalAccessException {

    if (!Utils.isValidMongoOID(loggedUserID))
      throw new IllegalAccessException("Invalid user id");
    Optional<User> doctorOpt = userRepository.findDoctorById(loggedUserID);

    if (doctorOpt.isEmpty())
      throw new IllegalAccessException("Doctor not found");

    ArrayList<Visit> visits = visitRepository.getAllVisitsForDoctor(loggedUserID);

    Map<String, List<Visit>> visitsByPatient = visits.stream()
        .filter(visit -> "Completed".equals(visit.getStatus()))
        .collect(Collectors.groupingBy(visit -> visit.getPatient().getUserId()));

    List<Map<String, Object>> patients = visitsByPatient.entrySet().stream()
        .map(entry -> {
          String patientId = entry.getKey();
          List<Visit> patientVisits = entry.getValue();

          PatientDigest patientDigest = patientVisits.get(0).getPatient();

          Visit lastVisit = patientVisits.stream()
              .max(Comparator.comparing(v -> v.getTime().getStartTime()))
              .orElse(null);

          return Map.of(
              "id", patientDigest.getUserId(),
              "name", patientDigest.getName(),
              "surname", patientDigest.getSurname(),
              "lastVisit", lastVisit != null ? Map.of(
                  "id", lastVisit.getId(),
                  "startTime", lastVisit.getTime().getStartTime(),
                  "endTime", lastVisit.getTime().getEndTime(),
                  "status", lastVisit.getStatus()) : null);
        })
        .sorted((p1, p2) -> {
          Map<String, Object> visit1 = (Map<String, Object>) p1.get("lastVisit");
          Map<String, Object> visit2 = (Map<String, Object>) p2.get("lastVisit");

          if (visit1 == null && visit2 == null)
            return 0;
          if (visit1 == null)
            return 1;
          if (visit2 == null)
            return -1;

          LocalDateTime time1 = (LocalDateTime) visit1.get("startTime");
          LocalDateTime time2 = (LocalDateTime) visit2.get("startTime");

          return time2.compareTo(time1);
        })
        .toList();

    return Map.of("patients", patients);
  }

  public Map<String, Object> getPatientVisits(String loggedUserID, String patientId)
      throws IllegalArgumentException, IllegalAccessException {

    if (!Utils.isValidMongoOID(loggedUserID))
      throw new IllegalAccessException("Invalid doctor id");
    if (!Utils.isValidMongoOID(patientId))
      throw new IllegalArgumentException("Invalid patient id");

    Optional<User> doctorOpt = userRepository.findDoctorById(loggedUserID);
    if (doctorOpt.isEmpty())
      throw new IllegalAccessException("Doctor not found");

    List<Visit> visits = visitRepository.findVisitsByDoctorAndPatient(loggedUserID, patientId)
        .stream()
        .sorted(Comparator.comparing((Visit v) -> v.getTime().getStartTime()).reversed())
        .toList();

    if (visits.isEmpty()) {
      throw new IllegalArgumentException("No visits found for this patient");
    }

    List<Map<String, Object>> visitsList = visits.stream()
        .map(visit -> {
          Map<String, Object> visitMap = new HashMap<>();
          visitMap.put("id", visit.getId());
          visitMap.put("startTime", visit.getTime().getStartTime());
          visitMap.put("endTime", visit.getTime().getEndTime());
          visitMap.put("status", visit.getStatus());
          visitMap.put("note", visit.getNote() != null ? visit.getNote() : "");
          visitMap.put("institution", visit.getInstitution().getInstitutionName());
          visitMap.put("patientRemarks", visit.getPatientRemarks() != null ? visit.getPatientRemarks() : "");
          if (visit.getCodes() != null) {
            visitMap.put("codes", visit.getCodes());
          }
          return visitMap;
        })
        .toList();

    return Map.of(
        "visits", visitsList,
        "totalVisits", visitsList.size());

  }

  public Map<String, Object> getDoctorFullInfo(String doctorId) throws IllegalArgumentException {

    if (!Utils.isValidMongoOID(doctorId))
      throw new IllegalArgumentException("Invalid doctor id");

    Optional<User> doctorOpt = userRepository.findEmployeeById(doctorId);
    if (doctorOpt.isEmpty())
      throw new IllegalArgumentException("Doctor not found");

    User doctor = doctorOpt.get();

    ArrayList<InstitutionDigest> employers = doctor.getEmployers();
    ArrayList<Map<String, Object>> institutionsList = new ArrayList<>();
    boolean updated = false;

    for (int i = 0; i < employers.size(); i++) {
      InstitutionDigest digest = employers.get(i);
      Optional<Institution> institutionOpt = institutionRepository.findById(digest.getInstitutionId());

      if (institutionOpt.isEmpty()) {
        continue;
      }
      Institution institution = institutionOpt.get();

      if (!institution.getName().equals(digest.getInstitutionName())) {
        employers.set(i, new InstitutionDigest(digest.getInstitutionId(), institution.getName()));
        updated = true;
      }

      institutionsList.add(Map.of(
          "institutionId", institution.getId(),
          "institutionName", institution.getName(),
          "image", institution.getImage(),
          "address", institution.getAddress()));
    }

    if (updated) {
      doctor.setEmployers(employers);
      userRepository.save(doctor);
    }

    Map<String, Object> result = new HashMap<>();
    result.put("id", doctor.getId());
    result.put("name", doctor.getName());
    result.put("surname", doctor.getSurname());
    result.put("email", doctor.getEmail());
    result.put("phoneNumber", doctor.getPhoneNumber());
    result.put("dateOfBirth", doctor.getBirthDate());
    result.put("address", doctor.getAddress());
    result.put("govId", doctor.getGovId());
    result.put("pwzNumber", doctor.getLicenceNumber());
    result.put("licenceNumber", doctor.getLicenceNumber());
    result.put("specialisations", doctor.getSpecialisations());
    result.put("rating", doctor.getRating());
    result.put("numOfRatings", doctor.getNumOfRatings());
    result.put("image", doctor.getPfpimage());
    result.put("institutions", institutionsList);
    result.put("roleCode", doctor.getRoleCode());

    return Map.of("doctor", result);
  }
}
