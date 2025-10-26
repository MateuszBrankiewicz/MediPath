package com.adam.medipathbackend.services;

import com.adam.medipathbackend.config.Utils;
import com.adam.medipathbackend.models.Visit;
import com.adam.medipathbackend.repository.InstitutionRepository;
import com.adam.medipathbackend.repository.VisitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {


    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private VisitRepository visitRepository;

    private String institutionid;
    private String userId;
    private int succeses;
    private boolean allowAnyMatch;

    public AuthorizationService startAuthChain(String userId, String institutionid) {
        this.institutionid = institutionid;
        this.userId = userId;
        this.succeses = 0;
        this.allowAnyMatch = false;
        if(userId != null && !Utils.isValidMongoOID(userId)) {
            throw new IllegalStateException();
        }
        if(institutionid != null && !Utils.isValidMongoOID(institutionid)) {
            throw new IllegalStateException();
        }
        return this;
    }

    public AuthorizationService matchAnyPermission() {
        this.allowAnyMatch = true;
        return this;
    }

    public void check() throws IllegalAccessException {
        if(allowAnyMatch && succeses < 1) {
            throw new IllegalAccessException();
        }
    }


    public AuthorizationService adminOfInstitution() throws IllegalAccessException {
        if (institutionRepository.findAdminById(userId, institutionid).isEmpty()) {
            if (!allowAnyMatch) {
                throw new IllegalAccessException();
            }
        } else {
            succeses += 1;
        }
        return this;

    }

    public AuthorizationService employeeOfInstitution() throws IllegalAccessException {
        if (institutionRepository.findStaffById(userId, institutionid).isEmpty()) {
            if (!allowAnyMatch) {
                throw new IllegalAccessException();
            }
        } else {
            succeses += 1;
        }
        return this;
    }

    public AuthorizationService doctorOfInstitution() throws IllegalAccessException {
        if (institutionRepository.findDoctorById(userId, institutionid).isEmpty()) {
            if (!allowAnyMatch) {
                throw new IllegalAccessException();
            }

        } else {
            succeses += 1;
        }
        return this;
    }

    public AuthorizationService patientInVisit(Visit visit) throws IllegalAccessException {
        if(!visit.getPatient().getUserId().equals(userId)) {
            if (!allowAnyMatch) {
                throw new IllegalAccessException();
            }
        } else {
            succeses += 1;
        }
        return this;
    }

    public AuthorizationService doctorInVisit(Visit visit) throws IllegalAccessException {
        if(!visit.getPatient().getUserId().equals(userId)) {
            if (!allowAnyMatch) {
                throw new IllegalAccessException();
            }
        } else {
            succeses += 1;
        }
        return this;
    }

    public AuthorizationService doctorServedPatient(String patientId) throws IllegalAccessException {
        if(!Utils.isValidMongoOID(patientId) || visitRepository.getAllVisitsForPatientWithDoctor(patientId, userId).isEmpty()) {
            if (!allowAnyMatch) {
                throw new IllegalAccessException();
            }
        } else {
            succeses += 1;
        }
        return this;
    }
}
