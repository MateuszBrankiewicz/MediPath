package com.adam.medipathbackend.controllers;

import com.adam.medipathbackend.config.Utils;
import com.adam.medipathbackend.forms.AddEmployeeForm;
import com.adam.medipathbackend.models.*;
import com.adam.medipathbackend.repository.InstitutionRepository;
import com.adam.medipathbackend.repository.ScheduleRepository;
import com.adam.medipathbackend.repository.UserRepository;
import com.adam.medipathbackend.repository.VisitRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/institution")
public class InstitutionController {

    @Autowired
    InstitutionRepository institutionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    VisitRepository visitRepository;

    @PostMapping(value = {"/add", "/add/"})
    public ResponseEntity<Map<String, Object>> addInstitution(@RequestBody Institution institution, HttpSession session) {

        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Optional<User> adminOpt = userRepository.findAdminById(loggedUserID);


        if(adminOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        User admin = adminOpt.get();
        ArrayList<String> missingFields = new ArrayList<>();
        if(institution.getAddress() == null || !institution.getAddress().isValid()) {
            missingFields.add("address");
        }
        if(institution.getName() == null || institution.getName().isBlank()) {
            missingFields.add("name");
        }
        if(institution.getImage() == null) {
            institution.setImage("");
        }

        if(!missingFields.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "missing fields in request body", "fields", missingFields), HttpStatus.BAD_REQUEST);
        }

        ArrayList<Institution> possibleDuplicates = institutionRepository.findInstitutionByName(institution.getName());
        for(Institution dupl: possibleDuplicates) {
            if(dupl.isSimilar(institution)) {
                return new ResponseEntity<>(Map.of("message", "This institution is a possible duplicate"), HttpStatus.CONFLICT);
            }
        }
        institution.addEmployee(new StaffDigest(admin.getId(), admin.getName(), admin.getSurname(), admin.getSpecialisations(), admin.getRoleCode(), admin.getPfpimage()));

        Institution newInstitution = institutionRepository.save(institution);

        admin.addEmployer(new InstitutionDigest(newInstitution.getId(), newInstitution.getName()));

        userRepository.save(admin);

        return new ResponseEntity<>(Map.of("message", "Success"), HttpStatus.CREATED);
    }

    @PostMapping(value = {"/{institutionid}/employees/", "/{institutionid}/employees"})
    public ResponseEntity<Map<String, Object>> addEmployeeToInstitution(@PathVariable String institutionid, @RequestBody ArrayList<AddEmployeeForm> employeeIds, HttpSession session) {

        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Optional<Institution> optionalInstitution = institutionRepository.findById(institutionid);
        if(optionalInstitution.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if(!isAdminOfInstitution(loggedUserID, institutionid)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if(employeeIds.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "list of users is empty"), HttpStatus.BAD_REQUEST);
        }


        for(AddEmployeeForm employee: employeeIds) {
            if(!userRepository.existsById(employee.getUserID())) {
                return new ResponseEntity<>(Map.of("message", "one or more user IDs is invalid"), HttpStatus.BAD_REQUEST);
            }
        }

        Institution currentInstitution = optionalInstitution.get();
        for(AddEmployeeForm employee: employeeIds) {

            Optional<User> currentUserOpt = userRepository.findById(employee.getUserID());

            if(currentUserOpt.isPresent()) {

                User currentUser = currentUserOpt.get();
                StaffDigest digest = new StaffDigest(currentUser.getId(), currentUser.getName(), currentUser.getSurname(), employee.getSpecialisations(), employee.getRoleCode(), currentUser.getPfpimage());

                currentInstitution.addEmployee(digest);
                currentUser.setRoleCode(currentUser.getRoleCode() | employee.getRoleCode());

                InstitutionDigest institutionDigest = new InstitutionDigest(currentInstitution.getId(), currentInstitution.getName());
                currentUser.addEmployer(institutionDigest);
                userRepository.save(currentUser);
            }
        }

        institutionRepository.save(currentInstitution);
        return new ResponseEntity<>(Map.of("message", "success"), HttpStatus.OK);
    }

    private boolean isAdminOfInstitution(String userId, String institutionId) {
        if(!Utils.isValidMongoOID(userId)) return false;
        return institutionRepository.findAdminById(userId, institutionId).isPresent();
    }

    @GetMapping(value = {"/{institutionid}/doctors", "/{institutionid}/doctors/"})
    public ResponseEntity<Map<String, Object>> getDoctors(@PathVariable String institutionid, @RequestParam(required = false) String specialisation) {
        if(!institutionRepository.existsById(institutionid)) {
            return new ResponseEntity<>(Map.of("message", "invalid institution id"), HttpStatus.BAD_REQUEST);
        }
        ArrayList<StaffDigest> doctors = institutionRepository.findDoctorsInInstitution(institutionid);

        if(specialisation == null) {
            return new ResponseEntity<>(Map.of("doctors", doctors.stream().map(doctor -> {

                User doctorProfile = userRepository.findById(doctor.getUserId()).get();
                return Map.of("doctorId", doctor.getUserId(), "doctorName", doctor.getName(),
                        "doctorSurname", doctor.getSurname(), "doctorPfp", doctor.getPfpimage(), "doctorSchedules", scheduleRepository.getUpcomingSchedulesByDoctorInInstitution(doctor.getUserId(), institutionid),
                        "rating", doctorProfile.getRating(), "numofratings", doctorProfile.getNumOfRatings(), "licenceNumber", doctorProfile.getLicenceNumber());

            }).toList()), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(Map.of("doctors", doctors.stream().filter(doctor -> doctor.getSpecialisations().contains(specialisation)).map(doctor -> {

                User doctorProfile = userRepository.findById(doctor.getUserId()).get();
                return Map.of("doctorId", doctor.getUserId(), "doctorName", doctor.getName(),
                        "doctorSurname", doctor.getSurname(), "doctorPfp", doctor.getPfpimage(), "doctorSchedules", scheduleRepository.getUpcomingSchedulesByDoctorInInstitution(doctor.getUserId(), institutionid),
                        "rating", doctorProfile.getRating(), "numofratings", doctorProfile.getNumOfRatings(), "licenceNumber", doctorProfile.getLicenceNumber());

            }).toList()), HttpStatus.OK);
        }

    }

    @PutMapping(value = {"/{institutionid}/employee", "/{institutionid}/employee/"})
    public ResponseEntity<Map<String, Object>> editEmployee(@PathVariable String institutionid, @RequestBody AddEmployeeForm employeeUpdate, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Optional<Institution> optionalInstitution = institutionRepository.findById(institutionid);
        if(optionalInstitution.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if(!isAdminOfInstitution(loggedUserID, institutionid)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Optional<User> updatedUserOpt = userRepository.findById(employeeUpdate.getUserID());
        if(updatedUserOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Institution institution = optionalInstitution.get();
        User updatedUser = updatedUserOpt.get();
        if(institution.getEmployees().stream().noneMatch(employee -> employee.getUserId().equals(employeeUpdate.getUserID()))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if(employeeUpdate.getUserID().equals(loggedUserID) && employeeUpdate.getRoleCode() < 12) {
            return new ResponseEntity<>(Map.of("message", "you cannot remove administrator privileges from your account"), HttpStatus.BAD_REQUEST);
        }
        ArrayList<StaffDigest> employees = institution.getEmployees();
        for(int i = 0; i < employees.size(); i++) {
            StaffDigest current = employees.get(i);
            if(current.getUserId().equals(employeeUpdate.getUserID())) {
                current.setName(updatedUser.getName());
                current.setSurname(updatedUser.getSurname());
                current.setPfpimage(updatedUser.getPfpimage());
                current.setRoleCode(employeeUpdate.getRoleCode());
                current.setSpecialisations(employeeUpdate.getSpecialisations());
                employees.set(i, current);
                break;
            }
        }
        institution.setEmployees(employees);
        institutionRepository.save(institution);
        updatedUser.setRoleCode(recalculateRoleCode(updatedUser.getId()));
        userRepository.save(updatedUser);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(value = {"/{institutionid}/employee/{userId}", "/{institutionid}/employee/{userId}/"})
    public ResponseEntity<Map<String, Object>> removeEmployee(@PathVariable String institutionid, @PathVariable String userId, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Optional<Institution> optionalInstitution = institutionRepository.findById(institutionid);
        if(optionalInstitution.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if(!isAdminOfInstitution(loggedUserID, institutionid)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Optional<User> updatedUserOpt = userRepository.findById(userId);
        if(updatedUserOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Institution institution = optionalInstitution.get();
        User updatedUser = updatedUserOpt.get();
        if(institution.getEmployees().stream().noneMatch(employee -> employee.getUserId().equals(userId))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if(updatedUser.getId().equals(loggedUserID)) {
            return new ResponseEntity<>(Map.of("message", "you cannot delete yourself from the employee list"), HttpStatus.BAD_REQUEST);
        }

        ArrayList<StaffDigest> employees = institution.getEmployees();
        int index = 0;
        for(int i = 0; i < institution.getEmployees().size(); i++) {
            StaffDigest current = employees.get(i);
            if(current.getUserId().equals(userId)) {
                index = i;
                break;
            }
        }
        employees.remove(index);
        institution.setEmployees(employees);
        ArrayList<InstitutionDigest> employers = updatedUser.getEmployers();
        for(int i = 0; i < employers.size(); i++) {
            InstitutionDigest current = employers.get(i);
            index = i;
            if(current.getInstitutionId().equals(institutionid)) {
                break;
            }
        }
        employers.remove(index);
        updatedUser.setEmployers(employers);
        institutionRepository.save(institution);
        updatedUser.setRoleCode(recalculateRoleCode(updatedUser.getId()));
        userRepository.save(updatedUser);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private int recalculateRoleCode(String userId) {
        int code = 1;
        ArrayList<Map<String, Object>> rolecodes = institutionRepository.getRoleCodes(userId);
        for(Map<String, Object> currentInstitution: rolecodes) {
            String currentId = currentInstitution.get("_id").toString();
            if(currentId == null) continue;
            code |= (Integer) currentInstitution.getOrDefault("roleCode", 0);
        }
        return code;
    }
    @GetMapping(value = {"/{id}", "/{id}/"})
    public ResponseEntity<Map<String, Object>> getInstitution(@PathVariable String id, @RequestParam(value = "fields", required = false) String[] fields, HttpSession session) {
        Optional<Institution> institutionOptional = institutionRepository.findById(id);
        if(institutionOptional.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "invalid institution id"), HttpStatus.BAD_REQUEST);
        }
        Institution institution = institutionOptional.get();
        Map<String, Object> outputFields = new HashMap<>();
        System.out.println(fields == null);
        List<String> fieldsList;
        if(fields == null) {
            fieldsList = List.of("id", "name", "types", "isPublic", "address", "employees", "rating", "image");
        } else {
            fieldsList = List.of(fields);
        }

        if(fieldsList.contains("id")) {
            outputFields.put("id", institution.getId());
        }
        if(fieldsList.contains("name")) {
            outputFields.put("name", institution.getName());
        }
        if(fieldsList.contains("address")) {
            outputFields.put("address", institution.getAddress());
        }
        if(fieldsList.contains("isPublic")) {
            outputFields.put("isPublic", institution.isPublic());
        }
        if(fieldsList.contains("types")) {
            outputFields.put("types", institution.getTypes());
        }
        if(fieldsList.contains("employees")) {
            String loggedUserID = (String) session.getAttribute("id");
            if(isLoggedAsEmployeeOfInstitution(loggedUserID, id)) {
                outputFields.put("employees", institution.getEmployees());
            } else {
                int[] validDoctorCodes = {2, 3, 6, 7, 14, 15};
                outputFields.put("employees", institution.getEmployees().stream().filter(employee -> IntStream.of(validDoctorCodes).anyMatch(x -> x == employee.getRoleCode())));
            }

        }
        if(fieldsList.contains("rating")) {
            outputFields.put("rating", institution.getRating());
        }
        if(fieldsList.contains("image")) {
            outputFields.put("image", institution.getImage());
        }
        return new ResponseEntity<>(Map.of("institution", outputFields), HttpStatus.OK);
    }


    @GetMapping(value = {"/{institutionid}/upcomingvisits/", "/{institutionid}/upcomingvisits"})
    public ResponseEntity<Map<String, Object>> getUpcomingVisitsForInstitution(@PathVariable String institutionid, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Optional<Institution> optionalInstitution = institutionRepository.findById(institutionid);
        if(optionalInstitution.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if(!isLoggedAsEmployeeOfInstitution(loggedUserID, institutionid)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        ArrayList<Visit> visits = visitRepository.getUpcomingVisitsInInstitution(institutionid);
        return new ResponseEntity<>(Map.of("visits", visits), HttpStatus.OK);
    }

    @PutMapping(value = {"/{institutionid}", "/{institutionid}/"})
    public ResponseEntity<Map<String, Object>> updateInstitution(@PathVariable String institutionid, @RequestBody Institution newInstitution, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Optional<Institution> optionalInstitution = institutionRepository.findById(institutionid);
        if(optionalInstitution.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if(!isAdminOfInstitution(loggedUserID, institutionid)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Institution institution = optionalInstitution.get();
        ArrayList<String> missingFields = new ArrayList<>();
        if(newInstitution.getAddress() == null || !newInstitution.getAddress().isValid()) {
            missingFields.add("address");
        }
        if(newInstitution.getName() == null || newInstitution.getName().isBlank()) {
            missingFields.add("name");
        }
        if(newInstitution.getImage() == null) {
            missingFields.add("image");
        }
        if(!missingFields.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "missing fields in request body", "fields", missingFields), HttpStatus.BAD_REQUEST);
        }



        institution.setName(newInstitution.getName());
        institution.setImage(newInstitution.getImage());
        institution.setPublic(newInstitution.isPublic());
        institution.setTypes(newInstitution.getTypes());
        if(!newInstitution.getAddress().equals(institution.getAddress())) {
            ArrayList<Institution> possibleDuplicates = institutionRepository.findInstitutionByName(institution.getName());
            for(Institution dupl: possibleDuplicates) {
                if(dupl.isSimilar(institution)) {
                    return new ResponseEntity<>(Map.of("message", "This institution is a possible duplicate"), HttpStatus.CONFLICT);
                }
            }
            institution.setAddress(newInstitution.getAddress());
        }


        institutionRepository.save(institution);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = {"/{institutionid}/schedules/{date}", "/{institutionid}/schedules/{date}/", "/{institutionid}/schedules/", "/{institutionid}/schedules"})
    public ResponseEntity<Map<String, Object>> getSchedulesForInstitution(@PathVariable String institutionid, @PathVariable(required = false) String date, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<Institution> optionalInstitution = institutionRepository.findById(institutionid);
        if(optionalInstitution.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if(!isLoggedAsEmployeeOfInstitution(loggedUserID, institutionid)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if(date == null) {
            ArrayList<Schedule> schedules = scheduleRepository.getInstitutionSchedules(institutionid);
            return new ResponseEntity<>(Map.of("schedules", schedules), HttpStatus.OK);
        } else {
            if(date.equals("now")) {
                date = LocalDate.now().withDayOfMonth(1).toString();
            }
            LocalDate startDate;
            try {
                DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                        .appendPattern("MM-yyyy")
                        .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                        .toFormatter();
                startDate = LocalDate.parse(date, fmt);
            } catch (DateTimeParseException e) {
                return new ResponseEntity<>(Map.of("message","invalid date"), HttpStatus.BAD_REQUEST);
            }
            ArrayList<Schedule> schedules = scheduleRepository.getInstitutionSchedulesOnDay(institutionid, startDate.atStartOfDay(), startDate.plusMonths(1).atStartOfDay());
            return new ResponseEntity<>(Map.of("schedules", schedules), HttpStatus.OK);
        }


    }

    @GetMapping(value = {"/{institutionid}/visits/{date}", "/{institutionid}/visits/{date}/", "/{institutionid}/visits/", "/{institutionid}/visits"})
    public ResponseEntity<Map<String, Object>> getVisitsForInstitution(@PathVariable String institutionid, @PathVariable(required = false) String date, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<Institution> optionalInstitution = institutionRepository.findById(institutionid);
        if(optionalInstitution.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if(!isLoggedAsEmployeeOfInstitution(loggedUserID, institutionid)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if(date == null) {
            ArrayList<Visit> visits = visitRepository.getAllVisitsInInstitution(institutionid);
            return new ResponseEntity<>(Map.of("visits", visits), HttpStatus.OK);
        } else {
            if(date.equals("now")) {
                date = LocalDate.now().withDayOfMonth(1).toString();
            }
            LocalDate startDate;
            try {
                DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                        .appendPattern("MM-yyyy")
                        .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                        .toFormatter();
                startDate = LocalDate.parse(date, fmt);
            } catch (DateTimeParseException e) {
                return new ResponseEntity<>(Map.of("message","invalid date"), HttpStatus.BAD_REQUEST);
            }
            ArrayList<Visit> visits = visitRepository.getInstitutionVisitsOnDay(institutionid, startDate.atStartOfDay(), startDate.plusMonths(1).atStartOfDay());
            return new ResponseEntity<>(Map.of("visits", visits), HttpStatus.OK);
        }


    }

    private boolean isLoggedAsEmployeeOfInstitution(String userID, String institutionID) {
        if(!Utils.isValidMongoOID(userID) || !Utils.isValidMongoOID(institutionID)) {
            return false;
        }
        return institutionRepository.findStaffById(userID, institutionID).isPresent();
    }
}
