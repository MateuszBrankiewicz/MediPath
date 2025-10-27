package com.adam.medipathbackend.services;

import com.adam.medipathbackend.models.DoctorDigest;
import com.adam.medipathbackend.models.MedicalHistory;
import com.adam.medipathbackend.models.User;
import com.adam.medipathbackend.repository.MedicalHistoryRepository;
import com.adam.medipathbackend.repository.UserRepository;
import com.adam.medipathbackend.repository.VisitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class MedicalHistoryService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    MedicalHistoryRepository medicalHistoryRepository;
    @Autowired
    VisitRepository visitRepository;

    @Autowired
    private AuthorizationService authorizationService;

    public void addMedicalHistory(MedicalHistory medicalHistory, String loggedUserID) throws IllegalArgumentException, IllegalAccessException {

        ArrayList<String> missingFields = new ArrayList<>();
        if (medicalHistory.getDate() == null) missingFields.add("date");
        if (medicalHistory.getNote() == null || medicalHistory.getNote().isBlank()) missingFields.add("note");
        if (medicalHistory.getTitle() == null || medicalHistory.getTitle().isBlank()) missingFields.add("title");

        if (!missingFields.isEmpty()) throw new IllegalArgumentException("Missing fields: " + missingFields);

        Optional<User> userOpt;

        if (medicalHistory.getUserId() == null || medicalHistory.getUserId().isBlank()) {

            userOpt = userRepository.findById(loggedUserID);
            if (userOpt.isEmpty()) throw new IllegalAccessException("User not found");

            medicalHistory.setDoctor(null);
            medicalHistory.setUserId(loggedUserID);

        } else {
            if (medicalHistory.getDoctor() == null || medicalHistory.getDoctor().getUserId() == null || medicalHistory.getDoctor().getUserId().isBlank()) throw new IllegalArgumentException("Doctor digest must be included when adding for a patient");
            userOpt = userRepository.findById(medicalHistory.getUserId());

            if (userOpt.isEmpty()) throw new IllegalAccessException("Patient not found");


            if (!medicalHistory.getDoctor().getUserId().equals(loggedUserID)) throw new IllegalAccessException();


            authorizationService.startAuthChain(loggedUserID, null).doctorServedPatient(medicalHistory.getUserId());

            Optional<User> doctorOpt = userRepository.findById(medicalHistory.getDoctor().getUserId());
            if (doctorOpt.isEmpty()) throw new IllegalAccessException("Doctor not found");

            User doctor = doctorOpt.get();
            medicalHistory.setDoctor(new DoctorDigest(doctor.getId(), doctor.getName(), doctor.getSurname(), doctor.getSpecialisations()));
        }

        User patient = userOpt.get();
        patient.addLatestMedicalHistory(medicalHistory);

        medicalHistoryRepository.save(medicalHistory);
        userRepository.save(patient);
    }

    public void modifyMedHisEntry(String med_his_id, MedicalHistory medicalHistory, String loggedUserID) throws IllegalArgumentException, IllegalAccessException {

        Optional<User> userOpt = userRepository.findById(loggedUserID);
        if (userOpt.isEmpty()) throw new IllegalAccessException("User not found");

        Optional<MedicalHistory> oldMedHisOpt = medicalHistoryRepository.findById(med_his_id);
        if (oldMedHisOpt.isEmpty()) throw new IllegalAccessException("Medical history entry not found");

        MedicalHistory oldMedicalHistory = oldMedHisOpt.get();
        if (!oldMedicalHistory.getUserId().equals(loggedUserID)) throw new IllegalAccessException("User not authorized");
        if (oldMedicalHistory.getDoctor() != null) throw new IllegalAccessException("Entries added by doctors cannot be changed");

        oldMedicalHistory.setDate(medicalHistory.getDate());
        oldMedicalHistory.setNote(medicalHistory.getNote());
        oldMedicalHistory.setTitle(medicalHistory.getTitle());

        User user = userOpt.get();
        LinkedList<MedicalHistory> histories = user.getLatestMedicalHistory();
        boolean foundInLatest = false;

        for (int i = 0; i < histories.size(); i++) {

            if (histories.get(i).getId().equals(med_his_id)) {
                histories.set(i, oldMedicalHistory);
                foundInLatest = true;
            }

        }

        medicalHistoryRepository.save(oldMedicalHistory);
        if (foundInLatest) userRepository.save(user);

    }

    public void deleteMedHisEntry(String med_his_id, String loggedUserID) throws IllegalArgumentException, IllegalAccessException {

        Optional<User> userOpt = userRepository.findById(loggedUserID);
        if (userOpt.isEmpty()) throw new IllegalAccessException("User not found");

        Optional<MedicalHistory> oldMedHisOpt = medicalHistoryRepository.findById(med_his_id);
        if (oldMedHisOpt.isEmpty()) throw new IllegalAccessException("Medical history entry not found");

        MedicalHistory oldMedicalHistory = oldMedHisOpt.get();
        if (!oldMedicalHistory.getUserId().equals(loggedUserID)) throw new IllegalAccessException("User not authorized");

        if (oldMedicalHistory.getDoctor() != null)
            throw new IllegalAccessException("Entries added by doctors cannot be deleted");

        User user = userOpt.get();
        LinkedList<MedicalHistory> histories = user.getLatestMedicalHistory();

        boolean foundInLatest = histories.removeIf(history -> history.getId().equals(med_his_id));

        medicalHistoryRepository.deleteById(med_his_id);
        if (foundInLatest) userRepository.save(user);
    }
}
