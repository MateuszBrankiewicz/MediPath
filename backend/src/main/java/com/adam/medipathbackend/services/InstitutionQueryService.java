package com.adam.medipathbackend.services;

import com.adam.medipathbackend.models.Schedule;
import com.adam.medipathbackend.models.StaffDigest;
import com.adam.medipathbackend.models.User;
import com.adam.medipathbackend.models.Visit;
import com.adam.medipathbackend.repository.InstitutionRepository;
import com.adam.medipathbackend.repository.ScheduleRepository;
import com.adam.medipathbackend.repository.UserRepository;
import com.adam.medipathbackend.repository.VisitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.IntStream;

@Service
public class InstitutionQueryService {

  @Autowired
  private InstitutionRepository institutionRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ScheduleRepository scheduleRepository;

  @Autowired
  private VisitRepository visitRepository;

  public List<Map<String, Object>> getDoctors(String institutionId, String specialisation) {
    ArrayList<StaffDigest> doctors = institutionRepository.findDoctorsInInstitution(institutionId);

    return doctors.stream()
        .filter(doctor -> specialisation == null || doctor.getSpecialisations().contains(specialisation))
        .map(doctor -> {

                    Optional<User> doctorProfileOpt = userRepository.findActiveById(doctor.getUserId());
                    if(doctorProfileOpt.isEmpty()) {
                        return null;
                    }
                    User doctorProfile = doctorProfileOpt.get();
                    Map<String, Object> doctorMap = new HashMap<>();
                    doctorMap.put("doctorId", doctor.getUserId());
                    doctorMap.put("doctorName", doctor.getName());

          doctorMap.put("doctorSurname", doctor.getSurname());
          doctorMap.put("doctorPfp", doctor.getPfpimage());
          doctorMap.put("doctorSchedules", scheduleRepository.getUpcomingSchedulesByDoctorInInstitution(
              doctor.getUserId(), institutionId));

          doctorMap.put("rating", doctorProfile.getRating());
          doctorMap.put("numofratings", doctorProfile.getNumOfRatings());
          doctorMap.put("licenceNumber", doctorProfile.getLicenceNumber());

          return doctorMap;
        })
        .toList();
  }

  public List<Map<String, Object>> getEmployees(String institutionId) {
    ArrayList<StaffDigest> employees = institutionRepository.findEmployeesInInstitution(institutionId);

    return employees.stream()
        .map(employee -> {

          User employeeProfile = userRepository.findById(employee.getUserId()).get();
          Map<String, Object> employeeMap = new HashMap<>();
          employeeMap.put("doctorId", employee.getUserId());
          employeeMap.put("doctorName", employee.getName());

          employeeMap.put("doctorSurname", employee.getSurname());
          employeeMap.put("doctorPfp", employee.getPfpimage());
          employeeMap.put("doctorSchedules", scheduleRepository.getUpcomingSchedulesByDoctorInInstitution(
              employee.getUserId(), institutionId));

          employeeMap.put("rating", employeeProfile.getRating());
          employeeMap.put("numofratings", employeeProfile.getNumOfRatings());
          employeeMap.put("licenceNumber", employeeProfile.getLicenceNumber());

          return employeeMap;
        })
        .toList();
  }

  public ArrayList<Schedule> getSchedules(String institutionId, String date) {
    if (date == null) {
      return scheduleRepository.getInstitutionSchedules(institutionId);
    }

    LocalDate startDate = parseMonthYearDate(date);
    return scheduleRepository.getInstitutionSchedulesOnDay(
        institutionId,
        startDate.atStartOfDay(),
        startDate.plusMonths(1).atStartOfDay());
  }

  public ArrayList<Visit> getVisits(String institutionId, String date) {

    if (date == null) {
      return visitRepository.getAllVisitsInInstitution(institutionId);
    }

    LocalDate startDate = parseMonthYearDate(date);
    return visitRepository.getInstitutionVisitsOnDay(
        institutionId,
        startDate.atStartOfDay(),
        startDate.plusMonths(1).atStartOfDay());
  }

  public ArrayList<Visit> getUpcomingVisits(String institutionId) {
    return visitRepository.getUpcomingVisitsInInstitution(institutionId);
  }

    public Map<String, Object> getInstitutionFields(String institutionId, String[] fields, boolean isEmployee) {
        var institution = institutionRepository.findActiveById(institutionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid institution id"));

        Map<String, Object> outputFields = new HashMap<>();
        List<String> fieldsList = (fields == null)
                ? List.of("id", "name", "types", "isPublic", "address", "employees", "rating", "image", "description", "numofratings")
                : List.of(fields);

    if (fieldsList.contains("id")) {
      outputFields.put("id", institution.getId());
    }

    if (fieldsList.contains("name")) {
      outputFields.put("name", institution.getName());
    }

    if (fieldsList.contains("address")) {
      outputFields.put("address", institution.getAddress());
    }

    if (fieldsList.contains("isPublic")) {
      outputFields.put("isPublic", institution.isPublic());
    }

    if (fieldsList.contains("types")) {
      outputFields.put("types", institution.getTypes());
    }

    if (fieldsList.contains("employees")) {

      if (isEmployee) {
        outputFields.put("employees", institution.getEmployees());
      } else {

        int[] validDoctorCodes = { 2, 3, 6, 7, 14, 15 };
        outputFields.put("employees", institution.getEmployees().stream()
            .filter(employee -> IntStream.of(validDoctorCodes)
                .anyMatch(x -> x == employee.getRoleCode())));
      }
    }

    if (fieldsList.contains("rating")) {
      outputFields.put("rating", institution.getRating());
    }

    if (fieldsList.contains("image")) {
      outputFields.put("image", institution.getImage());
    }

        if (fieldsList.contains("description")) {
            outputFields.put("description", institution.getDescription());
        }

        if (fieldsList.contains("numofratings")) {
            outputFields.put("numofratings", institution.getNumOfRatings());
        }

    return outputFields;
  }

  private LocalDate parseMonthYearDate(String date) {
    if (date.equals("now")) {
      return LocalDate.now().withDayOfMonth(1);
    }

    try {
      DateTimeFormatter fmt = new DateTimeFormatterBuilder()
          .appendPattern("MM-yyyy")
          .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
          .toFormatter();

      return LocalDate.parse(date, fmt);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date format");
    }
  }
}
