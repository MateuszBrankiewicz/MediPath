package com.adam.medipathbackend.services;

import com.adam.medipathbackend.models.Institution;
import com.adam.medipathbackend.models.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender sender;

    public void sendEmployeeRegistrationEmail(User employee, Institution institution, String token)
            throws MailException, MessagingException, UnsupportedEncodingException {

        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom(new InternetAddress("service@medipath.com", "MediPath"));
        helper.setSubject("An account has been created for you");
        helper.setTo(employee.getEmail());

        String content = buildEmployeeRegistrationEmailContent(institution, token);
        helper.setText(content, true);

        sender.send(message);
    }

    private String buildEmployeeRegistrationEmailContent(Institution institution, String token) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\" />\n" +
                "    <title>Password Reset</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h2>MediPath</h2>\n" +
                "    <p>An administrator for " + institution.getName() +
                " has created an account for you in the Medipath system. Use the link below to set a new password.</p>\n"
                +
                "    <a href=\"http://localhost:4200/auth/resetpassword/" + token +
                "\">http://localhost:4200/auth/resetpassword/" + token + "</a>\n" +
                "    <br>\n" +
                "    <p>The link will expire within 24 hours</p>\n" +
                "    <p>If you have not sent a password reset request, ignore this email.</p>\n" +
                "    <p>-MediPath development team</p>\n" +
                "    \n" +
                "</body>\n" +
                "</html>";
    }
}
