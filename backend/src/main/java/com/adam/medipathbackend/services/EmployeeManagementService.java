package com.adam.medipathbackend.services;

import com.adam.medipathbackend.forms.AddComboForm;
import com.adam.medipathbackend.forms.AddEmployeeForm;
import com.adam.medipathbackend.forms.DoctorUpdateForm;
import com.adam.medipathbackend.forms.RegistrationForm;
import com.adam.medipathbackend.models.*;
import com.adam.medipathbackend.repository.InstitutionRepository;
import com.adam.medipathbackend.repository.PasswordResetEntryRepository;
import com.adam.medipathbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

@Service
public class EmployeeManagementService {

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetEntryRepository preRepository;

    public void addEmployeesToInstitution(String institutionId, ArrayList<AddEmployeeForm> employees) {
        Institution institution = institutionRepository.findActiveById(institutionId)
                .orElseThrow(() -> new IllegalArgumentException("Institution not found"));

        validateEmployeeIds(employees);

        for (AddEmployeeForm employeeForm : employees) {
            User user = userRepository.findActiveById(employeeForm.getUserID())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            if(institution.getEmployees().stream()
                    .anyMatch(employee -> employee.getUserId().equals(employeeForm.getUserID())))
            {
                throw new IllegalArgumentException("User with id " + employeeForm.getUserID() + " is already added");
            }
            addEmployeeToInstitution(institution, user, employeeForm);
        }

        institutionRepository.save(institution);
    }

     
    public User registerEmployee(AddComboForm comboForm, String institutionId) {
        validateComboForm(comboForm);

        Institution institution = institutionRepository.findActiveById(institutionId)
                .orElseThrow(() -> new IllegalArgumentException("Institution not found"));

        RegistrationForm registrationForm = comboForm.getUserDetails();

        if (userRepository.findByEmail(registrationForm.getEmail()).isPresent() ||
                userRepository.findByGovID(registrationForm.getGovID()).isPresent()) {
            throw new IllegalStateException("This email or person is already registered");
        }

        User newUser = createUserFromForm(registrationForm);
        newUser.addEmployer(new InstitutionDigest(institution.getId(), institution.getName()));

        newUser.setRoleCode(1 + comboForm.getEmployeeDetails().getRoleCode());
        newUser.setSpecialisations(comboForm.getEmployeeDetails().getSpecialisations());
        newUser.setLicenceNumber(comboForm.getLicenceNumber());

        User savedUser = userRepository.save(newUser);

        institution.addEmployee(new StaffDigest(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getSurname(),
                savedUser.getSpecialisations(),
                comboForm.getEmployeeDetails().getRoleCode(),
                savedUser.getPfpimage()));

        institutionRepository.save(institution);

        return savedUser;
    }

    public PasswordResetEntry createPasswordResetEntry(String email) {
        SecureRandom secureRandom = new SecureRandom();
        String token = Long.toHexString(secureRandom.nextLong());
        PasswordResetEntry entry = new PasswordResetEntry(email, token);
        entry.setDateExpiry(LocalDateTime.now().plusDays(1));
        return preRepository.save(entry);
    }

    public void deletePasswordResetEntry(PasswordResetEntry entry) {
        if (entry != null) {
            preRepository.delete(entry);
        }
    }

     
    public void updateEmployee(String institutionId, AddEmployeeForm employeeUpdate, String adminId) {
        Institution institution = institutionRepository.findActiveById(institutionId)
                .orElseThrow(() -> new IllegalArgumentException("Institution not found"));

        User user = userRepository.findActiveById(employeeUpdate.getUserID())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (institution.getEmployees().stream()
                .noneMatch(employee -> employee.getUserId().equals(employeeUpdate.getUserID()))) {
            throw new IllegalArgumentException("User is not an employee of this institution");
        }

        if (employeeUpdate.getUserID().equals(adminId) && employeeUpdate.getRoleCode() < 12) {
            throw new IllegalStateException("You cannot remove administrator privileges from your account");
        }

        updateEmployeeInInstitution(institution, user, employeeUpdate);
        institutionRepository.save(institution);

        user.setRoleCode(recalculateRoleCode(user.getId()));
        userRepository.save(user);
    }

     
    public void removeEmployee(String institutionId, String userId, String adminId) {
        if (userId.equals(adminId)) {
            throw new IllegalStateException("You cannot delete yourself from the employee list");
        }

        Institution institution = institutionRepository.findActiveById(institutionId)
                .orElseThrow(() -> new IllegalArgumentException("Institution not found"));

        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (institution.getEmployees().stream()
                .noneMatch(employee -> employee.getUserId().equals(userId))) {
            throw new IllegalArgumentException("User is not an employee of this institution");
        }

        removeEmployeeFromInstitution(institution, user, institutionId);

        institutionRepository.save(institution);
        user.setRoleCode(recalculateRoleCode(user.getId()));
        userRepository.save(user);
    }

    public int recalculateRoleCode(String userId) {
        int code = 1;
        ArrayList<Map<String, Object>> roleCodes = institutionRepository.getRoleCodes(userId);

        for (Map<String, Object> institution : roleCodes) {
            String currentId = institution.get("_id").toString();
            if (currentId != null) {
                code |= (Integer) institution.getOrDefault("roleCode", 0);
            }
        }
        return code;
    }

    private void addEmployeeToInstitution(Institution institution, User user, AddEmployeeForm form) {
        verifyAddEmployeeForm(form);
        if(form.getSpecialisations() == null) {
            form.setSpecialisations(new ArrayList<>());
        }
        StaffDigest digest = new StaffDigest(
                user.getId(),
                user.getName(),
                user.getSurname(),
                form.getSpecialisations(),
                form.getRoleCode(),
                user.getPfpimage());

        institution.addEmployee(digest);
        user.setRoleCode(user.getRoleCode() | form.getRoleCode());

        InstitutionDigest institutionDigest = new InstitutionDigest(
                institution.getId(),
                institution.getName());
        user.addEmployer(institutionDigest);

        if(Stream.of(2, 6, 14).anyMatch(value -> value == form.getRoleCode())) {
            ArrayList<String> specialisations = user.getSpecialisations();
            specialisations.addAll(form.getSpecialisations());

            user.setSpecialisations(new ArrayList<>(specialisations.stream().distinct().toList()));
        }
        userRepository.save(user);
    }

    private void verifyAddEmployeeForm(AddEmployeeForm addEmployeeForm) {
        ArrayList<String> fields = new ArrayList<>();
        if(addEmployeeForm.getUserID() == null || addEmployeeForm.getUserID().isBlank()) {
            fields.add("userID");
        }
        if(Stream.of(2, 4, 6, 12, 14).noneMatch(value -> value == addEmployeeForm.getRoleCode())) {
            fields.add("roleCode");
        } else if(Stream.of(2, 6, 14).anyMatch(value -> value == addEmployeeForm.getRoleCode()) &&
                addEmployeeForm.getSpecialisations() == null)
        {
            fields.add("specialisations");
        }
        if(!fields.isEmpty()) {
            String fieldsTotal = fields.stream().reduce("", (full, value) -> full + value + " ");
            throw new IllegalArgumentException("missing fields in request body: " + fieldsTotal);
        }


    }

    private void updateEmployeeInInstitution(Institution institution, User user, AddEmployeeForm form) {
        ArrayList<StaffDigest> employees = institution.getEmployees();

        for (int i = 0; i < employees.size(); i++) {
            StaffDigest current = employees.get(i);
            if (current.getUserId().equals(form.getUserID())) {

                current.setName(user.getName());
                current.setSurname(user.getSurname());
                current.setPfpimage(user.getPfpimage());

                current.setRoleCode(form.getRoleCode());
                current.setSpecialisations(form.getSpecialisations());
                employees.set(i, current);

                break;
            }
        }
        institution.setEmployees(employees);
    }

    private void removeEmployeeFromInstitution(Institution institution, User user, String institutionId) {
        ArrayList<StaffDigest> employees = institution.getEmployees();
        employees.removeIf(e -> e.getUserId().equals(user.getId()));
        institution.setEmployees(employees);

        ArrayList<InstitutionDigest> employers = user.getEmployers();
        employers.removeIf(e -> e.getInstitutionId().equals(institutionId));
        user.setEmployers(employers);
    }


    private boolean checkIfEmployeeIsLastAdminOfInstitution(String userId, Institution institution) {
        for(StaffDigest digest: institution.getEmployees()) {
            if(digest.getUserId().equals(userId) && digest.getRoleCode() > 8) {
                return institution.getEmployees().stream()
                        .filter(employee -> employee.getRoleCode() > 8).count() == 1;
            }
        }
        return false;
    }

    public void removeAllEmployeesFromInstitution(Institution institution) {
        for(StaffDigest digest: institution.getEmployees()) {
            User user = userRepository.findActiveById(digest.getUserId()).get();
            ArrayList<InstitutionDigest> employers = user.getEmployers();
            employers.removeIf(institutionDigest -> institutionDigest.getInstitutionId().equals(institution.getId()));
            user.setEmployers(employers);
            userRepository.save(user);
        }
        institution.setEmployees(new ArrayList<>());
    }

    public void removeEmployeeFromAllInstitutions(User user) {
        for(InstitutionDigest institutionDigest: user.getEmployers()) {
            Optional<Institution> institutionOpt = institutionRepository.findActiveById(institutionDigest.getInstitutionId());
            if(institutionOpt.isEmpty()) continue;
            Institution institution = institutionOpt.get();
            if(checkIfEmployeeIsLastAdminOfInstitution(user.getId(), institution)) {
                throw new IllegalStateException("You are last administrator of institution " +
                        institution.getName() + ". Please deactivate the institution or add another administrator.");
            }
            ArrayList<StaffDigest> employees = institution.getEmployees();
            employees.removeIf(e -> e.getUserId().equals(user.getId()));
            institution.setEmployees(employees);
            institutionRepository.save(institution);
        }
        user.setEmployers(new ArrayList<>());
    }

    private void validateEmployeeIds(ArrayList<AddEmployeeForm> employees) {
        if (employees.isEmpty()) {
            throw new IllegalArgumentException("List of users is empty");
        }

        for (AddEmployeeForm employee : employees) {
            if (!userRepository.existsById(employee.getUserID())) {
                throw new IllegalArgumentException("One or more user IDs is invalid");
            }
        }
    }

    private User createUserFromForm(RegistrationForm form) {
        Argon2PasswordEncoder encoder = new Argon2PasswordEncoder(16, 32, 1, 60000, 10);
        SecureRandom secureRandom = new SecureRandom();
        String passwordHash = encoder.encode(Long.toHexString(secureRandom.nextLong()));
        UserSettings userSettings = new UserSettings("PL", true, true, 1);

        return new User(
                form.getEmail(),
                form.getName(),
                form.getSurname(),

                form.getGovID(),
                LocalDate.parse(form.getBirthDate(), DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                new Address(form.getProvince(), form.getCity(), form.getStreet(),
                        form.getNumber(), form.getPostalCode()),

                form.getPhoneNumber(),
                passwordHash,
                userSettings);
    }

    private void validateComboForm(AddComboForm comboForm) {
        ArrayList<String> missingFields = getMissingFields(comboForm);
        if (!missingFields.isEmpty()) {
            throw new IllegalArgumentException("Missing fields: " + String.join(", ", missingFields));
        }
    }

    private ArrayList<String> getMissingFields(AddComboForm comboForm) {
        ArrayList<String> missingFields = new ArrayList<>();
        if (comboForm.getUserDetails() == null) {
            missingFields.add("userDetails");
        } else {
            RegistrationForm registrationForm = comboForm.getUserDetails();
            if (registrationForm.getName() == null || registrationForm.getName().isBlank()) {
                missingFields.add("userDetails.name");
            }

            if (registrationForm.getSurname() == null || registrationForm.getSurname().isBlank()) {
                missingFields.add("userDetails.surname");
            }

            if (registrationForm.getEmail() == null || registrationForm.getEmail().isBlank()) {
                missingFields.add("userDetails.email");
            }

            if (registrationForm.getCity() == null || registrationForm.getCity().isBlank()) {
                missingFields.add("userDetails.city");
            }

            if (registrationForm.getProvince() == null || registrationForm.getProvince().isBlank()) {
                missingFields.add("userDetails.province");
            }

            if (registrationForm.getNumber() == null || registrationForm.getNumber().isBlank()) {
                missingFields.add("userDetails.number");
            }

            if (registrationForm.getPostalCode() == null || registrationForm.getPostalCode().isBlank()) {
                missingFields.add("userDetails.postalCode");
            }

            if (registrationForm.getBirthDate() == null || registrationForm.getBirthDate().isBlank()) {
                missingFields.add("userDetails.birthDate");
            }

            if (registrationForm.getGovID() == null || registrationForm.getGovID().isBlank()) {
                missingFields.add("userDetails.govID");
            }

            if (registrationForm.getPhoneNumber() == null || registrationForm.getPhoneNumber().isBlank()) {
                missingFields.add("userDetails.phoneNumber");
            }
        }

        if(comboForm.getEmployeeDetails() == null) {
            missingFields.add("employeeDetails");
        } else {
            if (Stream.of(2, 6, 14)
                    .anyMatch(code -> code == comboForm.getEmployeeDetails().getRoleCode())) {
                if (comboForm.getLicenceNumber() == null) {
                    missingFields.add("licenceNumber");
                }
                if(comboForm.getEmployeeDetails().getSpecialisations() == null) {
                    missingFields.add("employeeDetails.specialisations");
                }
            } else if (Stream.of(4, 8, 12)
                    .noneMatch(code -> code == comboForm.getEmployeeDetails().getRoleCode())) {
                missingFields.add("employeeDetails.roleCode");
            }
        }


        return missingFields;
    }
}
