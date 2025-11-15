package com.adam.medipathbackend.services;

import com.adam.medipathbackend.models.*;
import com.adam.medipathbackend.repository.InstitutionRepository;
import com.adam.medipathbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class InstitutionService {

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeManagementService employeeManagementService;


    public Institution createInstitution(Institution institution, User admin) {
        validateInstitution(institution);
        checkForDuplicates(institution);

        institution.addEmployee(new StaffDigest(
                admin.getId(),
                admin.getName(),
                admin.getSurname(),
                admin.getSpecialisations(),
                admin.getRoleCode(),
                admin.getPfpimage()));

        Institution savedInstitution = institutionRepository.save(institution);

        admin.addEmployer(new InstitutionDigest(
                savedInstitution.getId(),
                savedInstitution.getName()));
        admin.setRoleCode(12);
        userRepository.save(admin);

        return savedInstitution;
    }


    public Institution updateInstitution(String institutionId, Institution newInstitution) {
        Institution existing = institutionRepository.findActiveById(institutionId)
                .orElseThrow(() -> new IllegalArgumentException("Institution not found"));

        validateInstitution(newInstitution);

        boolean addressChanged = !newInstitution.getAddress().equals(existing.getAddress());
        if (addressChanged) {
            checkForDuplicates(newInstitution);
        }

        existing.setName(newInstitution.getName());
        existing.setImage(newInstitution.getImage());
        existing.setPublic(newInstitution.isPublic());
        existing.setTypes(newInstitution.getTypes());
        existing.setAddress(newInstitution.getAddress());

        return institutionRepository.save(existing);
    }

    public Optional<Institution> getInstitution(String id) {
        return institutionRepository.findActiveById(id);
    }

    public boolean institutionExists(String id) {
        return institutionRepository.existsById(id);
    }

    private void validateInstitution(Institution institution) {
        if (institution.getAddress() == null || !institution.getAddress().isValid()) {
            throw new IllegalArgumentException("Invalid address");
        }
        if (institution.getName() == null || institution.getName().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (institution.getImage() == null) {
            institution.setImage("");
        }
        if (institution.getDescription() == null) {
            institution.setDescription("");
        }
    }

    private void checkForDuplicates(Institution institution) {
        ArrayList<Institution> possibleDuplicates = institutionRepository.findInstitutionByName(institution.getName());

        for (Institution duplicate : possibleDuplicates) {
            if (duplicate.isSimilar(institution)) {
                throw new IllegalStateException("This institution is a possible duplicate");
            }
        }
    }

    public void deactivateInstitution(String institutionId) throws IllegalAccessException {
        Optional<Institution> institutionOptional = institutionRepository.findActiveById(institutionId);
        if(institutionOptional.isEmpty()) {
            throw new IllegalAccessException();
        }
        Institution institution = institutionOptional.get();
        employeeManagementService.removeAllEmployeesFromInstitution(institution);
        institution.setActive(false);
        institutionRepository.save(institution);
    }
}
