package com.adam.medipathbackend.services;

import com.adam.medipathbackend.models.Institution;
import com.adam.medipathbackend.models.Schedule;
import com.adam.medipathbackend.models.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import com.adam.medipathbackend.models.Visit;
import com.adam.medipathbackend.config.Constants;
import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender sender;

    private void sendGenericMail(String email, String content, String header, String title)
            throws MailException, MessagingException, UnsupportedEncodingException {
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(new InternetAddress("service@medipath.com", "MediPath"));
        helper.setSubject(title);
        helper.setTo(email);

        String emailContent = String.format(Constants.GENERIC_MAIL_FORMAT, header, content);
        helper.setText(emailContent, true);

        sender.send(message);
    }

    public void sendEmployeeRegistrationEmail(User employee, Institution institution, String token)
            throws MailException, MessagingException, UnsupportedEncodingException {

        String content = buildEmployeeRegistrationEmailContent(institution, token, employee.getUserSettings().getLanguage());
        String title, header;
        if(employee.getUserSettings().getLanguage().equals("PL")) {
            title = "Konto Medipath zostało dla ciebie stworzone.";
            header = "Twoje nowe konto Medipath.";

        } else {
            title = "A Medipath account has been created for you.";
            header = "Your Medipath account.";
        }

        sendGenericMail(employee.getEmail(), content, header, title);
    }



    private String buildEmployeeRegistrationEmailContent(Institution institution, String token, String language) {

        if(language.equals("PL")) {
            return Constants.EMPLOYEE_REGISTRATION_MAIL_FORMAT_PL.formatted(institution.getName(), token, token);
        } else {
            return Constants.EMPLOYEE_REGISTRATION_MAIL_FORMAT_EN.formatted(institution.getName(), token, token);
        }
    }

    public void sendResetMail(User user, String token) throws MessagingException, UnsupportedEncodingException {

        String title, header;
        String content = buildPasswordResetMail(token, user.getUserSettings().getLanguage());

        if(user.getUserSettings().getLanguage().equals("PL")) {
            title = "Prośba o zmianę hasła Medipath";
            header = "Zmiana hasła Medipath";

        } else {
            title = "Medipath password change request";
            header = "Medipath password change";
        }

        sendGenericMail(user.getEmail(), content, header, title);
    }

    private String buildPasswordResetMail(String token, String language) {
        if(language.equals("PL")) {
            return Constants.PASSWORD_RESET_MAIL_FORMAT_PL.formatted(token, token);
        } else {
            return Constants.PASSWORD_RESET_MAIL_FORMAT_EN.formatted(token, token);
        }
    }

    public void sendResetConfirmationMail(User user) throws MessagingException, UnsupportedEncodingException {
        String title, header;
        String content;

        if(user.getUserSettings().getLanguage().equals("PL")) {
            title = "Prośba o zmianę hasła Medipath";
            header = "Zmiana hasła Medipath";
            content = Constants.PASSWORD_RESET_SUCCESS_MAIL_FORMAT_PL;

        } else {
            title = "Medipath password change request";
            header = "Medipath password change";
            content = Constants.PASSWORD_RESET_SUCCESS_MAIL_FORMAT_EN;
        }

        sendGenericMail(user.getEmail(), content, header, title);
    }

    public void sendVisitRescheduleMail(User user, Schedule oldSchedule, Schedule newSchedules) throws MessagingException, UnsupportedEncodingException {
        String title, header;
        String content = buildVisitRescheduleEmailContent(oldSchedule, newSchedules, user.getUserSettings().getLanguage());

        if(user.getUserSettings().getLanguage().equals("PL")) {
            title = "Twoja wizyta została przełożona";
            header = "Przełożenie wizyty";

        } else {
            title = "Your visit has been rescheduled";
            header = "Visit reschedule";
        }

        sendGenericMail(user.getEmail(), content, header, title);
    }

    private String buildVisitRescheduleEmailContent(Schedule oldSchedule, Schedule newSchedule, String language) {
        if(language.equals("PL")) {
            return Constants.VISIT_RESCHEDULING_MAIL_FORMAT_PL.formatted(
                    oldSchedule.getInstitution().getInstitutionName(), oldSchedule.getStartHour().toString(),
                    oldSchedule.getDoctor().getDoctorName() + " " + oldSchedule.getDoctor().getDoctorSurname(),
                    newSchedule.getInstitution().getInstitutionName(), newSchedule.getStartHour().toString(),
                    newSchedule.getDoctor().getDoctorName() + " " + newSchedule.getDoctor().getDoctorSurname());
        } else {
            return Constants.VISIT_RESCHEDULING_MAIL_FORMAT_EN.formatted(
                    oldSchedule.getInstitution().getInstitutionName(), oldSchedule.getStartHour().toString(),
                    oldSchedule.getDoctor().getDoctorName() + " " + oldSchedule.getDoctor().getDoctorSurname(),
                    newSchedule.getInstitution().getInstitutionName(), newSchedule.getStartHour().toString(),
                    newSchedule.getDoctor().getDoctorName() + " " + newSchedule.getDoctor().getDoctorSurname());
        }
    }


    public void sendVisitCancelMail(User user, Visit oldVisit) throws MessagingException, UnsupportedEncodingException {
        String title, header;
        String content = buildVisitCancellationEmailContent(oldVisit, user.getUserSettings().getLanguage());

        if(user.getUserSettings().getLanguage().equals("PL")) {
            title = "Twoja wizyta została odwołana";
            header = "Odwołanie wizyty";

        } else {
            title = "Your visit has been cancelled";
            header = "Visit cancellation";
        }

        sendGenericMail(user.getEmail(), content, header, title);
    }

    private String buildVisitCancellationEmailContent(Visit visit, String language) {
        if(language.equals("PL")) {
            return Constants.VISIT_CANCELLATION_MAIL_FORMAT_PL.formatted(visit.getInstitution().getInstitutionName(),
                    visit.getTime().getStartTime().toString(),
                    visit.getDoctor().getDoctorName() + " " + visit.getDoctor().getDoctorSurname());
        } else {
            return Constants.VISIT_CANCELLATION_MAIL_FORMAT_EN.formatted(visit.getInstitution().getInstitutionName(),
                    visit.getTime().getStartTime().toString(),
                    visit.getDoctor().getDoctorName() + " " + visit.getDoctor().getDoctorSurname());
        }
    }




}
