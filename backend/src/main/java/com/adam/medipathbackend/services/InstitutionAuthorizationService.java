package com.adam.medipathbackend.services;

import com.adam.medipathbackend.config.Utils;
import com.adam.medipathbackend.repository.InstitutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InstitutionAuthorizationService {

    @Autowired
    private InstitutionRepository institutionRepository;

    public boolean isAdminOfInstitution(String userId, String institutionId) {
        if (!Utils.isValidMongoOID(userId)) {
            return false;
        }
        return institutionRepository.findAdminById(userId, institutionId).isPresent();
    }

    public boolean isLoggedAsEmployeeOfInstitution(String userId, String institutionId) {
        if (!Utils.isValidMongoOID(userId) || !Utils.isValidMongoOID(institutionId)) {
            return false;
        }
        return institutionRepository.findStaffById(userId, institutionId).isPresent();
    }
}
