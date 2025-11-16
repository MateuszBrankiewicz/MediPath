package com.adam.medipathbackend.services;

import com.adam.medipathbackend.forms.DoctorUpdateForm;
import com.adam.medipathbackend.models.*;
import com.adam.medipathbackend.repository.*;
import com.adam.medipathbackend.config.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

        if(doctorOpt.isEmpty()) throw new IllegalAccessException("Doctor not found");
        if(doctorUpdateForm.licenceNumber() == null) throw new IllegalArgumentException("Missing licence number");

        User doctor = doctorOpt.get();
        doctor.setLicenceNumber(doctorUpdateForm.licenceNumber());

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
    
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    List<Map<String, Object>> patients = visitsByPatient.entrySet().stream()
        .map(entry -> {
            List<Visit> patientVisits = entry.getValue();
            PatientDigest patientDigest = patientVisits.get(0).getPatient();
            
            Visit lastVisit = patientVisits.stream()
                .max(Comparator.comparing(v -> v.getTime().getStartTime()))
                .orElse(null);
            
            Map<String, Object> patientMap = new HashMap<>();
            patientMap.put("id", patientDigest.getUserId());
            patientMap.put("govId", patientDigest.getGovID());
            patientMap.put("name", patientDigest.getName());
            patientMap.put("surname", patientDigest.getSurname());
            patientMap.put("lastVisit", lastVisit != null ? Map.of(
                "id", lastVisit.getId(),
                "startTime", lastVisit.getTime().getStartTime().format(formatter),
                "endTime", lastVisit.getTime().getEndTime().format(formatter),
                "status", lastVisit.getStatus()
            ) : null);
            patientMap.put("_sortTime", lastVisit != null ? lastVisit.getTime().getStartTime() : null);
            
            return patientMap;
        })
        .sorted(Comparator.comparing(
            (Map<String, Object> p) -> (LocalDateTime) p.get("_sortTime"),
            Comparator.nullsLast(Comparator.reverseOrder())
        ))
        .peek(p -> p.remove("_sortTime"))
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
            visitMap.put("startTime", visit.getTime().getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            visitMap.put("endTime", visit.getTime().getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            visitMap.put("status", visit.getStatus());
            visitMap.put("note", visit.getNote() != null ? visit.getNote() : "");
            visitMap.put("institution", visit.getInstitution().getInstitutionName());
            visitMap.put("patientRemarks", visit.getPatientRemarks() != null ? visit.getPatientRemarks() : "");
            if(visit.getCodes() != null) {
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

    // Pobierz wszystkie instytucje, w których pracuje lekarz
    ArrayList<Institution> institutions = institutionRepository.findInstitutionsByEmployeeId(doctorId);
    ArrayList<Map<String, Object>> institutionsList = new ArrayList<>();
    
    // Dane lekarza z pierwszej instytucji (lub wartości domyślne)
    ArrayList<String> specialisations = new ArrayList<>();
    Integer roleCode = null;

    for (Institution institution : institutions) {
      // Znajdź dane pracownika w tej instytucji
      Optional<StaffDigest> staffOpt = institution.getEmployees().stream()
          .filter(emp -> emp.getUserId().equals(doctorId))
          .findFirst();

      if (staffOpt.isPresent()) {
        StaffDigest staff = staffOpt.get();
        
        // Ustaw specjalizacje i roleCode z pierwszej instytucji (jeśli jeszcze nie ustawione)
        if (specialisations.isEmpty() && staff.getSpecialisations() != null) {
          specialisations = staff.getSpecialisations();
        }
        if (roleCode == null) {
          roleCode = staff.getRoleCode();
        }

        institutionsList.add(Map.of(
            "institutionId", institution.getId(),
            "institutionName", institution.getName(),
            "image", institution.getImage(),
            "address", institution.getAddress(),
            "specialisations", staff.getSpecialisations() != null ? staff.getSpecialisations() : new ArrayList<String>(),
            "roleCode", staff.getRoleCode()));
      }
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
    result.put("specialisations", specialisations);
    result.put("rating", doctor.getRating());
    result.put("numOfRatings", doctor.getNumOfRatings());
    result.put("image", doctor.getPfpimage());
    result.put("institutionsEmployee", institutionsList);
    result.put("roleCode", roleCode != null ? roleCode : 0);

    return Map.of("doctor", result);
  }
}
