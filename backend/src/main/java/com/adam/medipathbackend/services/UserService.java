package com.adam.medipathbackend.services;

import com.adam.medipathbackend.forms.*;
import com.adam.medipathbackend.models.*;
import com.adam.medipathbackend.repository.MedicalHistoryRepository;
import com.adam.medipathbackend.repository.PasswordResetEntryRepository;
import com.adam.medipathbackend.repository.UserRepository;
import com.adam.medipathbackend.repository.VisitRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class UserService {

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private MedicalHistoryRepository mhRepository;


    @Autowired
    PasswordResetEntryRepository preRepository;

    @Autowired
    JavaMailSender sender;

    @Autowired
    private UserRepository userRepository;

    public Map<String, Object> resetPassword(String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("missing address in request parameters");
        }

        Optional<User> u = userRepository.findByEmail(address);
        if (u.isPresent()) {
            PasswordResetEntry passwordResetEntry = null;
            try {
                MimeMessage message = sender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message);
                helper.setFrom(new InternetAddress("service@medipath.com", "MediPath"));
                helper.setSubject("Password reset request");
                helper.setTo(address);
                SecureRandom secureRandom = new SecureRandom();
                String token = Long.toHexString(secureRandom.nextLong());
                String content = "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "    <meta charset=\"UTF-8\" />\n" +
                        "    <title>Password Reset</title>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "    <h2>MediPath</h2>\n" +
                        "    <p>We have received a password reset request for your MediPath account.<br>To reset your password click the link below:</p>\n" +
                        "    <a href=\"http://localhost:4200/auth/resetpassword/" + token + "\">http://localhost:4200/auth/resetpassword/" + token + "</a>\n" +
                        "    <br>\n" +
                        "    <p>The link will expire within 10 minutes</p>\n" +
                        "    <p>If you have not sent a password reset request, ignore this email.</p>\n" +
                        "    <p>-MediPath development team</p>\n" +
                        "    \n" +
                        "</body>\n" +
                        "</html>";
                passwordResetEntry = preRepository.save(new PasswordResetEntry(address, token));

                helper.setText(content);
                sender.send(message);

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
        Optional<User> patientOpt = userRepository.findById(patientid);
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

        Optional<User> opt = userRepository.findById(id);

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

        data.put("notifications", user.getNotifications());
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

        if(userRepository.findByEmail(registrationForm.getEmail()).isPresent() || userRepository.findByGovID(registrationForm.getGovID()).isPresent()) {
            throw new IllegalStateException("this email or person is already registered");
        }
        Argon2PasswordEncoder argon2PasswordEncoder = new Argon2PasswordEncoder(16, 32, 1, 60000, 10);
        String passwordHash = argon2PasswordEncoder.encode(registrationForm.getPassword());
        UserSettings userSettings = new UserSettings("PL", true, true, 1);
        userRepository.save(new User(
                registrationForm.getEmail(),
                registrationForm.getName(),
                registrationForm.getSurname(),
                registrationForm.getGovID(),
                LocalDate.parse(registrationForm.getBirthDate(), DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                new Address(registrationForm.getProvince(), registrationForm.getCity(), registrationForm.getStreet(), registrationForm.getNumber(), registrationForm.getPostalCode()),
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
        Argon2PasswordEncoder argon2PasswordEncoder = new Argon2PasswordEncoder(16, 32, 1, 60000, 10);
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


}
