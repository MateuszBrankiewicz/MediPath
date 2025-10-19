package com.adam.medipathbackend.services;


import com.adam.medipathbackend.models.Code;
import com.adam.medipathbackend.models.Visit;
import com.adam.medipathbackend.repository.VisitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
public class CodeService {


    @Autowired
    private VisitRepository visitRepository;

    @Transactional
    public boolean updateCode(Code code, String loggedUserID) {

        if(code.getCode() == null || code.getCode().isBlank()) {
            throw new IllegalArgumentException("missing code");
        }
        if(code.getCodeType() == null) {
            throw new IllegalArgumentException("missing codeType");
        }

        ArrayList<Visit> visits = visitRepository.getAllVisitsForPatient(loggedUserID);
        boolean found = false;
        for(Visit visit: visits) {
            ArrayList<Code> codes = visit.getCodes();
            for(int i = 0; i < codes.size(); i++) {
                if(codes.get(i).getCodeType() == code.getCodeType() && codes.get(i).getCode().equals(code.getCode())) {
                    codes.set(i, new Code(code.getCodeType(), code.getCode(), false));
                    found = true;
                    break;
                }
            }
            if(found) {
                visit.setCodes(codes);
                visitRepository.save(visit);
                break;
            }
        }
        return found;
    }


    @Transactional
    public boolean deleteCode(Code code, String loggedUserID) {

        ArrayList<String> missingfields = new ArrayList<>();
        if(code.getCode() == null || code.getCode().isBlank()) {
            throw new IllegalArgumentException("code");
        }
        if(code.getCodeType() == null) {
            throw new IllegalArgumentException("codeType");
        }

        ArrayList<Visit> visits = visitRepository.getAllVisitsForPatient(loggedUserID);
        boolean found = false;
        for(Visit visit: visits) {
            ArrayList<Code> codes = visit.getCodes();
            for(int i = 0; i < codes.size(); i++) {
                if(codes.get(i).getCodeType() == code.getCodeType() && codes.get(i).getCode().equals(code.getCode())) {
                    codes.remove(i);
                    found = true;
                    break;
                }
            }
            if(found) {
                visit.setCodes(codes);
                visitRepository.save(visit);
                break;
            }
        }
        return found;
    }
}
