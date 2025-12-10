package com.adam.medipathbackend.services;

import com.adam.medipathbackend.models.*;
import com.adam.medipathbackend.repository.InstitutionRepository;
import com.adam.medipathbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        Institution validInstitution = validateInstitution(institution);
        checkForDuplicates(validInstitution);

        institution.addEmployee(new StaffDigest(
                admin.getId(),
                admin.getName(),
                admin.getSurname(),
                admin.getSpecialisations(),
                12,
                admin.getPfpimage()));

        Institution savedInstitution = institutionRepository.save(validInstitution);

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

        Institution validInstitution = validateInstitution(newInstitution);

        boolean addressChanged = !validInstitution.getAddress().equals(existing.getAddress());
        if (addressChanged) {
            checkForDuplicates(validInstitution);
        }

        existing.setName(validInstitution.getName());
        existing.setImage(validInstitution.getImage());
        existing.setPublic(validInstitution.isPublic());
        existing.setTypes(validInstitution.getTypes());
        existing.setAddress(validInstitution.getAddress());

        return institutionRepository.save(existing);
    }

    public Optional<Institution> getInstitution(String id) {
        return institutionRepository.findActiveById(id);
    }

    public boolean institutionExists(String id) {
        return institutionRepository.existsById(id);
    }

    private Institution validateInstitution(Institution institution) {
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
        if(institution.getTypes() == null) {
            institution.setTypes(new ArrayList<>());
        }
        return institution;
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
