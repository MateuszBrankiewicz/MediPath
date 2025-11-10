package com.adam.medipathbackend.services;

import com.adam.medipathbackend.forms.AddScheduleForm;
import com.adam.medipathbackend.forms.ManySchedulesUpdateForm;
import com.adam.medipathbackend.models.*;
import com.adam.medipathbackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ScheduleService {
    @Autowired
    ScheduleRepository scheduleRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    InstitutionRepository institutionRepository;
    @Autowired
    AuthorizationService authorizationService;

     
    public Schedule addSchedule(AddScheduleForm schedule, String loggedUserID) throws IllegalArgumentException, IllegalAccessException, IllegalStateException {

        ArrayList<String> missingFields = getMissingFields(schedule);

        if (!missingFields.isEmpty()) throw new IllegalArgumentException("Missing fields: " + missingFields);

        authorizationService.startAuthChain(loggedUserID, schedule.getInstitutionID()).employeeOfInstitution();
        authorizationService.startAuthChain(schedule.getDoctorID(), schedule.getInstitutionID()).doctorOfInstitution();

        Optional<User> optUser = userRepository.findActiveById(schedule.getDoctorID());

        if (optUser.isEmpty()) throw new IllegalArgumentException("Doctor not found");
        Optional<Institution> optInst = institutionRepository.findActiveById(schedule.getInstitutionID());

        if (optInst.isEmpty()) throw new IllegalArgumentException("Institution not found");
        if (isScheduleOverlapping(schedule.getStartHour(), schedule.getEndHour(), schedule.getDoctorID(), null)) throw new IllegalStateException("Doctor is booked at this hour");

        User doctor = optUser.get();
        Schedule newSchedule = new Schedule(schedule.getStartHour(), schedule.getEndHour(), new DoctorDigest(schedule.getDoctorID(), doctor.getName(), doctor.getSurname(), doctor.getSpecialisations()), new InstitutionDigest(schedule.getInstitutionID(), optInst.get().getName()));

        return scheduleRepository.save(newSchedule);
    }

     
    public void addManySchedules(AddScheduleForm schedule, String loggedUserID) throws IllegalArgumentException, IllegalAccessException, IllegalStateException {

        ArrayList<String> missingFields = getMissingFields(schedule);

        if (schedule.getInterval() == null) missingFields.add("interval");
        if (!missingFields.isEmpty()) throw new IllegalArgumentException("Missing fields: " + missingFields);

        authorizationService.startAuthChain(loggedUserID, schedule.getInstitutionID()).employeeOfInstitution();
        authorizationService.startAuthChain(schedule.getDoctorID(), schedule.getInstitutionID()).doctorOfInstitution();

        Optional<User> optUser = userRepository.findActiveById(schedule.getDoctorID());
        if (optUser.isEmpty()) throw new IllegalArgumentException("Doctor not found");

        Optional<Institution> optInst = institutionRepository.findActiveById(schedule.getInstitutionID());
        if (optInst.isEmpty()) throw new IllegalArgumentException("Institution not found");

        LocalDateTime start = schedule.getStartHour();
        ArrayList<Schedule> newSchedules = new ArrayList<>();

        User doctor = optUser.get();
        if (isScheduleOverlapping(schedule.getStartHour(), schedule.getEndHour(), schedule.getDoctorID(), null))
            throw new IllegalStateException("Doctor is booked in this time frame");

        while (start.plusSeconds(schedule.getInterval().toSecondOfDay()).isBefore(schedule.getEndHour()) || start.plusSeconds(schedule.getInterval().toSecondOfDay()).isEqual(schedule.getEndHour())) {
            newSchedules.add(new Schedule(start, start.plusSeconds(schedule.getInterval().toSecondOfDay()), new DoctorDigest(schedule.getDoctorID(), doctor.getName(), doctor.getSurname(), doctor.getSpecialisations()), new InstitutionDigest(schedule.getInstitutionID(), optInst.get().getName())));
            start = start.plusSeconds(schedule.getInterval().toSecondOfDay());
        }

        scheduleRepository.saveAll(newSchedules);
    }

     
    public void updateManySchedules(ManySchedulesUpdateForm newSchedule, String loggedUserID) throws IllegalArgumentException,
            IllegalAccessException, IllegalStateException {

        ArrayList<String> missingFields = getMissingUpdateFields(newSchedule);
        if (!missingFields.isEmpty()) throw new IllegalArgumentException("Missing fields: " + missingFields);

        authorizationService.startAuthChain(loggedUserID, newSchedule.getInstitutionID()).employeeOfInstitution();
        authorizationService.startAuthChain(newSchedule.getDoctorID(), newSchedule.getInstitutionID()).doctorOfInstitution();

        Optional<User> optUser = userRepository.findActiveById(newSchedule.getDoctorID());
        if (optUser.isEmpty()) throw new IllegalArgumentException("Doctor not found");

        Optional<Institution> optInst = institutionRepository.findActiveById(newSchedule.getInstitutionID());
        if (optInst.isEmpty()) throw new IllegalArgumentException("Institution not found");

        ArrayList<Schedule> schedulesToReset = scheduleRepository.getSchedulesBetween(newSchedule.getDoctorID(),
                newSchedule.getStartHour(), newSchedule.getEndHour());
        for (Schedule schedule : schedulesToReset) {
            if (!schedule.getInstitution().getInstitutionId().equals(newSchedule.getInstitutionID()))
                throw new IllegalArgumentException("Schedule overlaps with another institution's schedule");
        }

        if (isNotInsideOrEqual(newSchedule.getNewStartHour(), newSchedule.getNewEndHour(), newSchedule.getStartHour(), newSchedule.getEndHour())) {
            if (newSchedule.getNewStartHour().isBefore(newSchedule.getStartHour())) {
                if (isScheduleOverlapping(newSchedule.getNewStartHour(), newSchedule.getStartHour(), newSchedule.getDoctorID(), ""))
                    throw new IllegalStateException("Schedule overlaps on start");
            }
            if (newSchedule.getNewEndHour().isAfter(newSchedule.getEndHour())) {
                if (isScheduleOverlapping(newSchedule.getEndHour(), newSchedule.getNewEndHour(), newSchedule.getDoctorID(), ""))
                    throw new IllegalStateException("Schedule overlaps on end");
            }
        }

        LocalDateTime start = newSchedule.getNewStartHour();
        ArrayList<Schedule> newSchedules = new ArrayList<>();
        User doctor = optUser.get();

        scheduleRepository.deleteAll(schedulesToReset);
        while (start.plusSeconds(newSchedule.getNewInterval().toSecondOfDay()).isBefore(newSchedule.getNewEndHour()) || start.plusSeconds(newSchedule.getNewInterval().toSecondOfDay()).isEqual(newSchedule.getNewEndHour())) {
            newSchedules.add(new Schedule(start, start.plusSeconds(newSchedule.getNewInterval().toSecondOfDay()), new DoctorDigest(newSchedule.getDoctorID(), doctor.getName(), doctor.getSurname(), doctor.getSpecialisations()), new InstitutionDigest(newSchedule.getInstitutionID(), optInst.get().getName())));
            start = start.plusSeconds(newSchedule.getNewInterval().toSecondOfDay());
        }

        scheduleRepository.saveAll(newSchedules);
    }

     
    public void updateSchedule(String scheduleid, AddScheduleForm newSchedule, String loggedUserID) throws IllegalArgumentException, IllegalAccessException, IllegalStateException {

        Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleid);
        if (scheduleOpt.isEmpty()) throw new IllegalArgumentException("Schedule not found");

        Schedule schedule = scheduleOpt.get();
        authorizationService.startAuthChain(loggedUserID, schedule.getInstitution().getInstitutionId()).employeeOfInstitution();

        ArrayList<String> missingFields = new ArrayList<>();
        if (newSchedule.getEndHour() == null) missingFields.add("endHour");
        if (newSchedule.getStartHour() == null) missingFields.add("startHour");
        if (!missingFields.isEmpty()) throw new IllegalArgumentException("Missing fields: " + missingFields);

        if (schedule.isBooked()) throw new IllegalArgumentException("Schedule is already booked");
        if (isScheduleOverlapping(newSchedule.getStartHour(), newSchedule.getEndHour(), schedule.getDoctor().getUserId(), schedule.getId()))
            throw new IllegalStateException("Doctor is booked at this hour");

        schedule.setStartHour(newSchedule.getStartHour());
        schedule.setEndHour(newSchedule.getEndHour());
        scheduleRepository.save(schedule);
    }

     
    public void deleteSchedule(String scheduleid, String loggedUserID) throws IllegalArgumentException, IllegalAccessException, IllegalStateException {

        Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleid);
        if (scheduleOpt.isEmpty()) throw new IllegalArgumentException("Schedule not found");

        Schedule schedule = scheduleOpt.get();
        authorizationService.startAuthChain(loggedUserID, schedule.getInstitution().getInstitutionId()).employeeOfInstitution();
        if (schedule.isBooked()) throw new IllegalArgumentException("Schedule is already booked");

        scheduleRepository.delete(schedule);
    }

    private static ArrayList<String> getMissingFields(AddScheduleForm schedule) {
        ArrayList<String> missingFields = new ArrayList<>();

        if (schedule.getEndHour() == null)
            missingFields.add("endHour");

        if (schedule.getDoctorID() == null || schedule.getDoctorID().isBlank())
            missingFields.add("doctor");

        if (schedule.getInstitutionID() == null || schedule.getInstitutionID().isBlank())
            missingFields.add("institution");

        return missingFields;
    }
    private static ArrayList<String> getMissingUpdateFields(ManySchedulesUpdateForm schedule) {
        ArrayList<String> missingFields = new ArrayList<>();

        if (schedule.getEndHour() == null)
            missingFields.add("endHour");

        if (schedule.getDoctorID() == null || schedule.getDoctorID().isBlank())
            missingFields.add("doctor");

        if (schedule.getInstitutionID() == null || schedule.getInstitutionID().isBlank())
            missingFields.add("institution");

        if (schedule.getStartHour() == null)
            missingFields.add("startHour");

        if (schedule.getNewInterval() == null)
            missingFields.add("newInterval");

        if (schedule.getNewEndHour() == null)
            missingFields.add("newEndHour");

        if (schedule.getNewStartHour() == null)
            missingFields.add("newStartHour");

        return missingFields;
    }

    private boolean isNotInsideOrEqual(LocalDateTime insideStart, LocalDateTime insideEnd, LocalDateTime outsideStart, LocalDateTime outsideEnd) {
        return !insideStart.isBefore(outsideStart) && !insideEnd.isAfter(outsideEnd);
    }

    private boolean isScheduleOverlapping(LocalDateTime start, LocalDateTime end, String userId, String omitID) {
        ArrayList<Schedule> schedules = scheduleRepository.getSchedulesByDoctor(userId);

        for (Schedule schedule : schedules) {
            if (schedule.getId().equals(omitID)) continue;
            if (!schedule.getStartHour().toLocalDate().isEqual(start.toLocalDate())) continue;

            boolean startOrEndSame = schedule.getStartHour().isEqual(start) || schedule.getEndHour().isEqual(end);
            boolean startBetween = start.isAfter(schedule.getStartHour()) && start.isBefore(schedule.getEndHour());
            boolean endBetween = end.isBefore(schedule.getEndHour()) && end.isAfter(schedule.getStartHour());
            boolean completeOverlap = start.isBefore(schedule.getStartHour()) && end.isAfter(schedule.getEndHour());

            if (startOrEndSame || startBetween || endBetween || completeOverlap) return true;
        }

        return false;
    }
}
