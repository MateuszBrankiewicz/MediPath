package com.adam.medipathbackend.models;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document("Visit")
public class Visit {

    @Id
    private String id;

    private String status;

    private String note;

    private ArrayList<Code> codes;

    private PatientDigest patient;

    private DoctorDigest doctor;

    private VisitTime time;

    private InstitutionDigest institution;

    private String patientRemarks;

    private String commentId;

    public Visit(PatientDigest patient, DoctorDigest doctor, VisitTime time, InstitutionDigest institution, String patientRemarks) {
        this.status = "Upcoming";
        this.note = "";
        this.codes = new ArrayList<>();
        this.patient = patient;
        this.doctor = doctor;
        this.time = time;
        this.institution = institution;
        this.patientRemarks = patientRemarks;
        this.commentId = null;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public ArrayList<Code> getCodes() {
        return codes;
    }

    public void setCodes(ArrayList<Code> codes) {
        this.codes = codes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public PatientDigest getPatient() {
        return patient;
    }

    public void setPatient(PatientDigest patient) {
        this.patient = patient;
    }

    public DoctorDigest getDoctor() {
        return doctor;
    }

    public void setDoctor(DoctorDigest doctor) {
        this.doctor = doctor;
    }

    public VisitTime getTime() {
        return time;
    }

    public void setTime(VisitTime time) {
        this.time = time;
    }

    public InstitutionDigest getInstitution() {
        return institution;
    }

    public void setInstitution(InstitutionDigest institution) {
        this.institution = institution;
    }

    public String getPatientRemarks() {
        return patientRemarks;
    }

    public void setPatientRemarks(String patientRemarks) {
        this.patientRemarks = patientRemarks;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }
}

