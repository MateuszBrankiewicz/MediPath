package com.adam.medipathbackend.services;

import com.adam.medipathbackend.forms.*;
import com.adam.medipathbackend.models.*;
import com.adam.medipathbackend.repository.*;
import jakarta.mail.IllegalWriteException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private MedicalHistoryRepository mhRepository;


    @Autowired
    PasswordResetEntryRepository preRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CommentRepository commentRepository;


    @Autowired
    InstitutionRepository institutionRepository;

    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    EmailService emailService;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private EmployeeManagementService employeeManagementService;

    private Argon2PasswordEncoder argon2PasswordEncoder = new Argon2PasswordEncoder(16, 32, 1, 60000, 10);


    public Map<String, Object> resetPassword(String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("missing address in request parameters");
        }

        Optional<User> userOpt = userRepository.findByEmail(address);
        if (userOpt.isPresent()) {
            PasswordResetEntry passwordResetEntry = null;
            try {

                SecureRandom secureRandom = new SecureRandom();
                String token = Long.toHexString(secureRandom.nextLong());

                passwordResetEntry = preRepository.save(new PasswordResetEntry(address, token));;
                emailService.sendResetMail(userOpt.get(), token);

            } catch (MailException | MessagingException | UnsupportedEncodingException e) {
                if (passwordResetEntry != null) {
                    preRepository.delete(passwordResetEntry);
                }
                throw new IllegalStateException("the mail service threw an error: " + e.getMessage());
            }
        }
        return Map.of("message", "password reset mail has been sent, if the account exists");
    }

    public Map<String, Object> getMyReferrals(String loggedUserID, String type) {
        if (!(type == null || type.equals("referrals") || type.equals("prescriptions"))) {
            throw new IllegalArgumentException("invalid code type");
        }

        ArrayList<Map<String, Object>> codes = visitRepository.getCodesForPatient(loggedUserID);

        if (codes.isEmpty()) {
            return Map.of("codes", new ArrayList<String>());
        }

        if (type == null) {
            return Map.of("codes", codes);

        } else if (type.equals("prescriptions")) {

            return Map.of("codes", codes.stream().filter(code -> {
                if (!code.containsKey("codes") || !(code.get("codes") instanceof Map<?, ?> subDoc)) {
                    return false;
                }
                return subDoc.containsKey("codeType") && subDoc.get("codeType").equals("PRESCRIPTION");
            }).toList());

        } else {

            return Map.of("codes", codes.stream().filter(code -> {
                if (!code.containsKey("codes") || !(code.get("codes") instanceof Map<?, ?> subDoc)) {
                    return false;
                }
                return subDoc.containsKey("codeType") && subDoc.get("codeType").equals("REFERRAL");
            }).toList());

        }
    }


    public Map<String, Object> getPatient(String loggedUserID, String patientid) throws IllegalAccessException {
        if (loggedUserID == null || patientid == null) {
            throw new IllegalArgumentException("Missing user or patient ID");
        }

        Optional<User> doctorStaffOpt = userRepository.findDoctorOrStaffById(loggedUserID);

        if (doctorStaffOpt.isEmpty()) {
            throw new IllegalAccessException("User is not doctor or staff");
        }

        boolean isAnyPresent = false;
        Optional<User> patientOpt = userRepository.findActiveById(patientid);
        User doctor = doctorStaffOpt.get();

        for (InstitutionDigest digest : doctor.getEmployers()) {
            isAnyPresent |= !visitRepository.getAllVisitsForPatientInInstitution(patientid, digest.getInstitutionId()).isEmpty();
        }

        if (patientOpt.isEmpty() || !isAnyPresent) {
            throw new IllegalAccessException("Patient not found or no visits");
        }

        User patient = patientOpt.get();
        Map<String, Object> result = new HashMap<>();

        result.put("name", patient.getName());
        result.put("surname", patient.getSurname());
        result.put("govId", patient.getGovId());
        result.put("birthDate", patient.getBirthDate());
        result.put("phoneNumber", patient.getPhoneNumber());
        result.put("pfp", patient.getPfpimage());
        result.put("medicalHistory", mhRepository.getEntriesForPatient(patientid));

        return result;
    }

    public Map<String, Object> getProfile(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Missing user ID");
        }

        Optional<User> opt = userRepository.findActiveById(id);

        if (opt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = opt.get();
        HashMap<String, Object> data = new HashMap<>();

        data.put("name", user.getName());
        data.put("surname", user.getSurname());
        data.put("govId", user.getGovId());
        data.put("birthDate", user.getBirthDate().toString());

        data.put("address", user.getAddress());
        data.put("phoneNumber", user.getPhoneNumber());
        data.put("licenceNumber", user.getLicenceNumber());
        data.put("specialisations", user.getSpecialisations());
        data.put("latestMedicalHistory", user.getLatestMedicalHistory());
        data.put("roleCode", user.getRoleCode());

        data.put("rating", user.getRating());
        data.put("employers", user.getEmployers());
        data.put("numOfRatings", user.getNumOfRatings());
        data.put("pfpImage", user.getPfpimage());
        data.put("userSettings", user.getUserSettings());

        return data;
    }


    public Map<String, Object> registerUser(RegistrationForm registrationForm) {
        ArrayList<String> missingFields = getMissingFields(registrationForm);
        if(!missingFields.isEmpty()) {
            throw new IllegalArgumentException("missing fields in request body: " + missingFields);
        }

        if(userRepository.findByEmail(registrationForm.getEmail()).isPresent()
                || userRepository.findByGovID(registrationForm.getGovID()).isPresent()) {
            throw new IllegalStateException("this email or person is already registered");
        }

        String passwordHash = argon2PasswordEncoder.encode(registrationForm.getPassword());
        UserSettings userSettings = new UserSettings("PL",
                true, true, 1);

        userRepository.save(new User(
                registrationForm.getEmail(),
                registrationForm.getName(),
                registrationForm.getSurname(),
                registrationForm.getGovID(),
                LocalDate.parse(registrationForm.getBirthDate(),
                        DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                new Address(registrationForm.getProvince(),
                        registrationForm.getCity(),
                        registrationForm.getStreet(),
                        registrationForm.getNumber(),
                        registrationForm.getPostalCode()),
                registrationForm.getPhoneNumber(),
                passwordHash,
                userSettings
        ));
        return Map.of("message", "Success");
    }

    public String loginUser(LoginForm loginForm) throws IllegalAccessException {

        ArrayList<String> missingFields = new ArrayList<>();
        if(loginForm.getEmail() == null || loginForm.getEmail().isBlank()) {
            missingFields.add("email");
        }

        if(loginForm.getPassword() == null || loginForm.getPassword().isBlank()) {
            missingFields.add("password");
        }

        if(!missingFields.isEmpty()) {
            throw new IllegalArgumentException("missing fields in request body: " + missingFields);
        }
        
        Optional<User> user = userRepository.findByEmail(loginForm.getEmail());

        if(user.isEmpty() || !argon2PasswordEncoder.matches(loginForm.getPassword(), user.get().getPasswordHash())) {
            throw new IllegalAccessException("invalid email or password");
        }
        return user.get().getId();
    }

    private ArrayList<String> getMissingFields(RegistrationForm registrationForm) {

        ArrayList<String> missingFields = new ArrayList<>();
        if(registrationForm.getName() == null || registrationForm.getName().isBlank()) {
            missingFields.add("name");
        }

        if(registrationForm.getSurname() == null || registrationForm.getSurname().isBlank()) {
            missingFields.add("surname");
        }

        if(registrationForm.getEmail() == null || registrationForm.getEmail().isBlank()) {
            missingFields.add("email");
        }

        if(registrationForm.getCity() == null || registrationForm.getCity().isBlank()) {
            missingFields.add("city");
        }

        if(registrationForm.getProvince() == null || registrationForm.getProvince().isBlank()) {
            missingFields.add("province");
        }

        if(registrationForm.getNumber() == null || registrationForm.getNumber().isBlank()) {
            missingFields.add("number");
        }

        if(registrationForm.getPostalCode() == null || registrationForm.getPostalCode().isBlank()) {
            missingFields.add("postalCode");
        }

        if(registrationForm.getBirthDate() == null || registrationForm.getBirthDate().isBlank()) {
            missingFields.add("birthDate");
        }

        if(registrationForm.getGovID() == null || registrationForm.getGovID().isBlank()) {
            missingFields.add("govID");
        }

        if(registrationForm.getPhoneNumber() == null || registrationForm.getPhoneNumber().isBlank()) {
            missingFields.add("phoneNumber");
        }

        if(registrationForm.getPassword() == null || registrationForm.getPassword().isBlank()) {
            missingFields.add("password");
        }
        return missingFields;
    }


    public Map<String, Object> getMyComments(String loggedUserID) {
        ArrayList<Comment> comments = commentRepository.getCommentsForUser(loggedUserID);

        if(comments.isEmpty()) {
            return Map.of("comments", new ArrayList<>());
        }

        return Map.of(
                "comments", comments.stream().map(
                                comment -> Map.of(
                                        "id", comment.getId(),
                                        "doctor", comment.getDoctorDigest().getDoctorName() + " " + comment.getDoctorDigest().getDoctorSurname(),
                                        "institution", comment.getInstitution().getInstitutionName(),
                                        "doctorRating", comment.getDoctorRating(),
                                        "institutionRating", comment.getInstitutionRating(),
                                        "content", comment.getContent(),
                                        "createdAt", comment.getCreatedAtString()))
                        .toList()
        );
    }

    public void postResetPassword(ResetForm resetForm) throws IllegalWriteException {
        ArrayList<String> missingFields = new ArrayList<>();

        if(resetForm.getToken() == null || resetForm.getToken().isBlank()) {
            missingFields.add("token");
        }

        if(resetForm.getPassword() == null || resetForm.getPassword().isBlank()) {
            missingFields.add("password");
        }

        if(!missingFields.isEmpty()) {
            throw new IllegalArgumentException("missing fields in request body" + missingFields.stream().reduce(" ", (total, string) -> total + string + " "));
        }

        Optional<PasswordResetEntry> p = preRepository.findValidToken(resetForm.getToken());
        if(p.isEmpty()) {
            throw new IllegalStateException("token invalid or expired");
        }

        Optional<User> u = userRepository.findByEmail(p.get().getEmail());
        if(u.isEmpty()) {
            throw new IllegalWriteException("invalid user referenced by token");
        }

        User user = u.get();
        String passwordHash = argon2PasswordEncoder.encode(resetForm.getPassword());

        user.setPasswordHash(passwordHash);
        userRepository.save(user);
    }

    public void resetMyPassword(String loggedUserID, ResetMyPasswordForm form) throws IllegalWriteException, IllegalAccessException {
        Optional<User> userOpt = userRepository.findActiveById(loggedUserID);

        if(userOpt.isEmpty() || !argon2PasswordEncoder.matches(form.getCurrentPassword(), userOpt.get().getPasswordHash())) {
            throw new IllegalAccessException("invalid password");
        }

        String passwordHash = argon2PasswordEncoder.encode(form.getNewPassword());

        User user = userOpt.get();
        user.setPasswordHash(passwordHash);
        try {

            emailService.sendResetConfirmationMail(user);

        } catch (MailException | MessagingException | UnsupportedEncodingException e) {
            throw new IllegalWriteException("the mail service threw an error: " + e.getMessage());
        }

        userRepository.save(user);

    }

    public List<Visit> getMyVisits(String loggedUserID, String upcoming) {

        if(upcoming.isBlank()) {
            return visitRepository.getAllVisitsForPatient(loggedUserID);
        } else {
            return visitRepository.getUpcomingVisits(loggedUserID);
        }

    }

    public void updatePanel(String value, String loggedUserID) {

        Optional<User> userOpt = userRepository.findActiveById(loggedUserID);
        if(userOpt.isEmpty()) {
            throw new IllegalStateException();
        }

        int panel;
        try {
            panel = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid panel ID");
        }

        if(panel != 1 && panel != 2 && panel != 4 && panel != 8) {
            throw new IllegalArgumentException("invalid panel ID");
        }

        User user = userOpt.get();
        if((user.getRoleCode() & panel) == 0) {
            throw new IllegalStateException();
        }

        UserSettings settings = user.getUserSettings();
        settings.setLastPanel(panel);
        user.setUserSettings(settings);
        userRepository.save(user);
    }

    public void updateMe(UpdateUserForm updateUserForm, String loggedUserID) {
        Optional<User> userOpt = userRepository.findActiveById(loggedUserID);
        if(userOpt.isEmpty()) {
            throw new IllegalStateException();
        }

        User user = userOpt.get();
        boolean anyChanges = false;
        boolean addressChanged = false;

        Address userAddress = user.getAddress();
        if(updateUserForm.getCity() != null && !updateUserForm.getCity().isBlank()) {
            userAddress.setCity(updateUserForm.getCity());
            addressChanged = true;
        }

        if(updateUserForm.getStreet() != null && !updateUserForm.getStreet().isBlank()) {
            userAddress.setStreet(updateUserForm.getStreet());
            addressChanged = true;
        }

        if(updateUserForm.getNumber() != null && !updateUserForm.getNumber().isBlank()) {
            userAddress.setNumber(updateUserForm.getNumber());
            addressChanged = true;
        }

        if(updateUserForm.getPostalCode() != null && !updateUserForm.getPostalCode().isBlank()) {
            userAddress.setPostalCode(updateUserForm.getPostalCode());
            addressChanged = true;
        }

        if(updateUserForm.getProvince() != null && !updateUserForm.getProvince().isBlank()) {
            userAddress.setProvince(updateUserForm.getProvince());
            addressChanged = true;
        }

        if(addressChanged) {
            user.setAddress(userAddress);
            anyChanges = true;
        }

        if(updateUserForm.getName() != null && !updateUserForm.getName().isBlank()) {
            user.setName(updateUserForm.getName());
            anyChanges = true;
        }

        if(updateUserForm.getSurname() != null && !updateUserForm.getSurname().isBlank()) {
            user.setSurname(updateUserForm.getSurname());
            anyChanges = true;
        }

        if(updateUserForm.getPhoneNumber() != null && !updateUserForm.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(updateUserForm.getPhoneNumber());
            anyChanges = true;
        }

        if(anyChanges) userRepository.save(user);

    }
    public void updateSettings(UserSettings userSettings, String loggedUserID) {

        Optional<User> userOpt = userRepository.findActiveById(loggedUserID);
        if(userOpt.isEmpty()) {
            throw new IllegalStateException();
        }

        User user = userOpt.get();
        if(userSettings.getLanguage() == null || userSettings.getLanguage().isBlank()) {
            userSettings.setLanguage(user.getUserSettings().getLanguage());
        }

        if(!user.getUserSettings().equals(userSettings))  {
            user.setUserSettings(userSettings);
            userRepository.save(user);
        }

    }

    public UserSettings getUserSettings(String loggedUserID) {

        Optional<User> userOpt = userRepository.findActiveById(loggedUserID);
        if(userOpt.isEmpty()) {
            throw new IllegalStateException();
        }

        return userOpt.get().getUserSettings();
    }

    public List<MedicalHistory> getMyMedicalHistories(String loggedUserID) {
        return mhRepository.getEntriesForPatient(loggedUserID);
    }

    public List<MedicalHistory> getMedicalHistoriesForPatient(String loggedUserID, String patientId) throws IllegalAccessException {
        Optional<User> userOpt = userRepository.findActiveById(loggedUserID);
        if(userOpt.isEmpty()) {
            throw new IllegalStateException();
        }


        authorizationService.startAuthChain(loggedUserID, null).doctorServedPatient(patientId).check();

        return getMyMedicalHistories(patientId);
    }

    public List<?> getMyNotifications(String loggedUserID, String filter) {

        Optional<User> userOpt = userRepository.findActiveById(loggedUserID);
        if(userOpt.isEmpty()) {
            return new ArrayList<>();
        }

        User user = userOpt.get();

        if(filter == null) {

            return user.getNotifications().stream()
                    .map(notification -> Map.of("title", notification.getTitle(), "content",
                            notification.getContent(), "timestamp", notification.getTimestamp().toString(), "read",
                            notification.isRead(), "system", notification.isSystem())
                    ).toList();

        } else if(filter.equals("received")) {

            return user.getNotifications().stream()
                    .filter(notification -> notification.getTimestamp().isBefore(LocalDateTime.now()))
                    .map(notification -> Map.of("title", notification.getTitle(), "content",
                            notification.getContent(), "timestamp", notification.getTimestamp().toString(), "read",
                            notification.isRead(), "system", notification.isSystem())
                    ).toList();

        } else if(filter.equals("upcoming")) {

            return user.getNotifications().stream()
                    .filter(notification -> notification.getTimestamp().isAfter(LocalDateTime.now()))
                    .map(notification -> Map.of("title", notification.getTitle(), "content",
                            notification.getContent(), "timestamp", notification.getTimestamp().toString(), "read",
                            notification.isRead(), "system", notification.isSystem())
                    ).toList();

        } else {

            throw new IllegalArgumentException("invalid filter");
        }

    }



    public List<?> getMyInstitutions(String loggedUserID, String role) {
        Optional<User> userOpt = userRepository.findActiveById(loggedUserID);
        if(userOpt.isEmpty()) {
            return new ArrayList<>();
        }

        User admin = userOpt.get();
        if(role.equals("admin")) {

            if(admin.getRoleCode() < 8) {
                return new ArrayList<>();
            }
            return institutionRepository.findInstitutionsWhereAdmin(loggedUserID);

        } else if(role.equals("staff")) {
            if(admin.getRoleCode() < 4) {
                return new ArrayList<>();
            }
            return institutionRepository.findInstitutionsWhereStaff(loggedUserID);

        } else {
            throw new IllegalArgumentException("invalid role");
        }
    }

    public void deactivateMe(String loggedUserId) throws IllegalAccessException {
        Optional<User> userOpt = userRepository.findActiveById(loggedUserId);
        if(userOpt.isEmpty()) {
            throw new IllegalAccessException();
        }
        User user = userOpt.get();
        employeeManagementService.removeEmployeeFromAllInstitutions(user);

        user.setActive(false);
        userRepository.save(user);
    }
}
