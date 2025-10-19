package com.adam.medipathbackend.services;

import com.adam.medipathbackend.forms.AddVisitForm;
import com.adam.medipathbackend.forms.CompleteVisitForm;
import com.adam.medipathbackend.models.Code;
import com.adam.medipathbackend.models.Notification;
import com.adam.medipathbackend.models.PatientDigest;
import com.adam.medipathbackend.models.Schedule;
import com.adam.medipathbackend.models.User;
import com.adam.medipathbackend.models.Visit;
import com.adam.medipathbackend.models.VisitTime;
import com.adam.medipathbackend.repository.InstitutionRepository;
import com.adam.medipathbackend.repository.ScheduleRepository;
import com.adam.medipathbackend.repository.UserRepository;
import com.adam.medipathbackend.repository.VisitRepository;
import org.springframework.beans.factory.annotation.Autowired;
// ...existing code...
import org.springframework.stereotype.Service;
// ...existing code...

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import jakarta.servlet.http.HttpSession;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VisitService {

    public Schedule validateVisitForm(AddVisitForm visit) {
        if(visit.getScheduleID() == null || visit.getScheduleID().isBlank()) {
            throw new IllegalArgumentException("scheduleID is required");
        }
        Optional<Schedule> schedule = scheduleRepository.findById(visit.getScheduleID());
        if(schedule.isEmpty() || schedule.get().isBooked()) {
            throw new IllegalArgumentException("visit time is invalid or booked");
        }
        return schedule.get();
    }

    @Transactional
    public void addVisit(AddVisitForm visit, User foundUser) {

        if(visit.getPatientRemarks() == null) {
            visit.setPatientRemarks("");
        }

        Schedule foundSchedule = validateVisitForm(visit);

        PatientDigest foundUserDigest = new PatientDigest(foundUser.getId(), foundUser.getName(), foundUser.getSurname(), foundUser.getGovId());
        VisitTime time = new VisitTime(foundSchedule.getId(), foundSchedule.getStartHour(), foundSchedule.getEndHour());
        Visit newVisit = new Visit(foundUserDigest, foundSchedule.getDoctor(), time,foundSchedule.getInstitution(), visit.getPatientRemarks());

        foundSchedule.setBooked(true);

        ArrayList<Code> codes = new ArrayList<>();
        newVisit.setCodes(codes);
        if(foundUser.getUserSettings().isSystemNotifications()) {
            String content, title;
            if(foundUser.getUserSettings().getLanguage().equals("PL")) {
                content = String.format("Przypominamy o wizycie w ośrodku %s dnia %s o godzinie %s", foundSchedule.getInstitution().getInstitutionName(), foundSchedule.getStartHour().toLocalDate(), foundSchedule.getStartHour().toLocalTime());
                title = "Przypomnienie o wizycie";
            } else {
                content = String.format("We would like to remind you of your upcoming visit in %s on the day %s at %s", foundSchedule.getInstitution().getInstitutionName(), foundSchedule.getStartHour().toLocalDate(), foundSchedule.getStartHour().toLocalTime());
                title = "Visit reminder";
            }
            Notification notification = new Notification(title,  content, foundSchedule.getStartHour().minusDays(1).withHour(12).withMinute(0), true, false);
            foundUser.addNotification(notification);
            userRepository.save(foundUser);
        }

        scheduleRepository.save(foundSchedule);
        visitRepository.save(newVisit);
    }

    @Transactional
    public void cancelVisit(String visitid, String loggedUserID) throws IllegalAccessException {

        Optional<Visit> optVisit = visitRepository.findById(visitid);
        if(optVisit.isEmpty()) {
            throw new IllegalStateException();
        }
        Visit visitToCancel = optVisit.get();
        AuthorizationService authorizationService = new AuthorizationService();

        authorizationService.startAuthChain(loggedUserID, visitToCancel.getInstitution().getInstitutionId()).either().
                patientInVisit(visitToCancel).employeeOfInstitution().check();

        if(visitToCancel.getStatus().equals("Completed")) {
            throw new IllegalArgumentException("this visit is completed");
        }
        if(visitToCancel.getStatus().equals("Cancelled")) {
            throw new IllegalArgumentException("this visit is already cancelled");
        }
        Optional<User> userOptional = userRepository.findById(visitToCancel.getPatient().getUserId());
        Optional<Schedule> scheduleOptional = scheduleRepository.findById(visitToCancel.getTime().getScheduleId());
        if(userOptional.isEmpty() || scheduleOptional.isEmpty()) {
            throw new IllegalAccessException();
        }
        visitToCancel.setStatus("Cancelled");

        Schedule oldSchedule = scheduleOptional.get();
        oldSchedule.setBooked(false);
        User patient = userOptional.get();
        int foundIndex = 0;
        for(int i = 0; i < patient.getNotifications().size(); i++) {
            Notification notification = patient.getNotifications().get(i);
            if(notification.getTimestamp().isEqual(oldSchedule.getStartHour().minusDays(1).withHour(12).withMinute(0)) && notification.isSystem()) {
                foundIndex = i;
            }
        }
        if(patient.getUserSettings().isSystemNotifications()) {
            Notification cancellationNotification;
            if(patient.getUserSettings().getLanguage().equals("PL")) {
                cancellationNotification = new Notification("Odwołanie wizyty",
                        "Twoja wizyta w placówce" + visitToCancel.getInstitution().getInstitutionName() +
                                " u specjalisty " + visitToCancel.getDoctor().getDoctorName() + " " +
                                visitToCancel.getDoctor().getDoctorSurname() + " została odwołana.<br>Skontaktuj się z placówką, by dowiedzieć się więcej.",
                        java.time.LocalDateTime.now().plusMinutes(5), true, false);
            } else {
                cancellationNotification = new Notification("Visit cancellation",
                        "Your visit in " + visitToCancel.getInstitution().getInstitutionName() + " with " +
                        visitToCancel.getDoctor().getDoctorName() + " " + visitToCancel.getDoctor().getDoctorSurname() + " was cancelled. <br>Contact the institution for details.",
                        java.time.LocalDateTime.now().plusMinutes(5), true, false);
            }
            patient.addNotification(cancellationNotification);
        }
        patient.removeNotification(patient.getNotifications().get(foundIndex));
        userRepository.save(patient);
        scheduleRepository.save(oldSchedule);
        visitRepository.save(visitToCancel);
        // Email sending omitted for brevity


    }


    @Transactional
    public Visit rescheduleVisit(String visitid, String newScheduleId, String loggedUserID) throws IllegalAccessException {

        if(newScheduleId.isBlank()) {
            throw new IllegalArgumentException("newschedule parameter missing");
        }
        Optional<Visit> optVisit = visitRepository.findById(visitid);
        if(optVisit.isEmpty()) {
            throw new IllegalAccessException();
        }
        Visit visitToReschedule = optVisit.get();
        AuthorizationService authorizationService = new AuthorizationService();

        authorizationService.startAuthChain(loggedUserID, visitToReschedule.getInstitution().getInstitutionId()).either().
                patientInVisit(visitToReschedule).employeeOfInstitution().check();

        if(visitToReschedule.getStatus().equals("Completed")) {
            throw new IllegalArgumentException("this visit is completed");
        }
        if(visitToReschedule.getStatus().equals("Cancelled")) {
            throw new IllegalArgumentException("this visit is already cancelled");
        }

        Optional<Schedule> newScheduleOptional = scheduleRepository.findById(newScheduleId);
        if(newScheduleOptional.isEmpty() || newScheduleOptional.get().isBooked()) {
            throw new IllegalArgumentException("invalid new schedule id or schedule is booked");
        }
        Schedule newSchedule = newScheduleOptional.get();
        Optional<Schedule> scheduleOptional = scheduleRepository.findById(visitToReschedule.getTime().getScheduleId());
        if(scheduleOptional.isEmpty()) {
            throw new IllegalComponentStateException();
        }
        Optional<User> userOptional = userRepository.findById(visitToReschedule.getPatient().getUserId());
        if(userOptional.isEmpty()) {
            throw new IllegalComponentStateException();
        }
        Schedule oldSchedule = scheduleOptional.get();
        User patient = userOptional.get();
        if(patient.getUserSettings().isSystemNotifications()) {
            int foundIndex = 0;
            for(int i = 0; i < patient.getNotifications().size(); i++) {
                Notification notification = patient.getNotifications().get(i);
                if(notification.getTimestamp().isEqual(oldSchedule.getStartHour().minusDays(1).withHour(12).withMinute(0)) && notification.isSystem()) {
                    foundIndex = i;
                }
            }
            patient.removeNotification(patient.getNotifications().get(foundIndex));
            String content, title;
            if(patient.getUserSettings().getLanguage().equals("PL")) {
                content = String.format("Przypominamy o wizycie w ośrodku %s dnia %s o godzinie %s", newSchedule.getInstitution().getInstitutionName(), newSchedule.getStartHour().toLocalDate(), newSchedule.getStartHour().toLocalTime());
                title = "Przypomnienie o wizycie";
            } else {
                content = String.format("We would like to remind you of your upcoming visit in %s on the day %s at %s", newSchedule.getInstitution().getInstitutionName(), newSchedule.getStartHour().toLocalDate(), newSchedule.getStartHour().toLocalTime());
                title = "Visit reminder";
            }
            Notification notification = new Notification(title,  content, newSchedule.getStartHour().minusDays(1).withHour(12).withMinute(0), true, false);
            patient.addNotification(notification);
        }
        oldSchedule.setBooked(false);
        newSchedule.setBooked(true);
        visitToReschedule.setTime(new VisitTime(newSchedule.getId(), newSchedule.getStartHour(), newSchedule.getEndHour()));
        visitToReschedule.setDoctor(newSchedule.getDoctor());
        visitToReschedule.setInstitution(newSchedule.getInstitution());
        scheduleRepository.save(oldSchedule);
        scheduleRepository.save(newSchedule);

        // Email sending omitted for brevity
        userRepository.save(patient);
        return visitRepository.save(visitToReschedule);
    }


    @Transactional
    public Visit completeVisit(String visitid, CompleteVisitForm completionForm, String loggedUserID) throws IllegalAccessException {
        Optional<Visit> optVisit = visitRepository.findById(visitid);
        if(optVisit.isEmpty()) {
            throw new IllegalAccessException();
        }
        Visit visit = optVisit.get();

        AuthorizationService authorizationService = new AuthorizationService();
        authorizationService.startAuthChain(loggedUserID, null).doctorInVisit(visit).check();

        if(visit.getStatus().equals("Completed")) {
           throw new IllegalArgumentException("this visit is already completed");
        }
        if(visit.getStatus().equals("Cancelled")) {
            throw new IllegalArgumentException("this visit is already cancelled");
        }
        visit.setStatus("Completed");
        ArrayList<Code> codes = new ArrayList<>();
        for(String prescriptionCode: completionForm.getPrescriptions()) {
            codes.add(new Code(Code.CodeType.PRESCRIPTION, prescriptionCode, true));
        }
        for(String prescriptionCode: completionForm.getReferrals()) {
            codes.add(new Code(Code.CodeType.REFERRAL, prescriptionCode, true));
        }
        visit.setCodes(codes);
        visit.setNote(completionForm.getNote());
        return visitRepository.save(visit);
    }


    @Transactional
    public Visit getVisitDetails(String visitid, String loggedUserID) throws IllegalAccessException {

        Optional<Visit> optVisit = visitRepository.findById(visitid);
        if(optVisit.isEmpty()) {
            throw new IllegalAccessException();
        }
        Visit visit = optVisit.get();

        AuthorizationService authorizationService = new AuthorizationService();
        authorizationService.startAuthChain(loggedUserID, visit.getInstitution().getInstitutionId()).either()
                .patientInVisit(visit).doctorOfInstitution().employeeOfInstitution().check();

        return visit;
    }


    @Autowired
    private VisitRepository visitRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private InstitutionRepository institutionRepository;
}
