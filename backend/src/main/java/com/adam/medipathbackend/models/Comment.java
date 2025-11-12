package com.adam.medipathbackend.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Document("Comment")
public class Comment {

    @Id
    private String id;

    private float doctorRating;

    private float institutionRating;

    private String content;

    private DoctorDigest doctorDigest;

    private InstitutionDigest institution;

    private PatientDigest author;

    private String visitId;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Europe/Warsaw")
    private final LocalDateTime createdAt;

    public Comment(float doctorRating, float institutionRating, String content, DoctorDigest doctorDigest, InstitutionDigest institution, PatientDigest author, String visitId) {
        this.doctorRating = doctorRating;
        this.institutionRating = institutionRating;
        this.content = content;
        this.doctorDigest = doctorDigest;
        this.author = author;
        this.visitId = visitId;
        this.institution = institution;
        this.createdAt = LocalDateTime.now();
    }

    public InstitutionDigest getInstitution() {
        return institution;
    }

    public void setInstitution(InstitutionDigest institution) {
        this.institution = institution;
    }

    public String getId() {
        return id;
    }


    public float getDoctorRating() {
        return doctorRating;
    }

    public void setDoctorRating(float doctorRating) {
        this.doctorRating = doctorRating;
    }

    public float getInstitutionRating() {
        return institutionRating;
    }

    public void setInstitutionRating(float institutionRating) {
        this.institutionRating = institutionRating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public DoctorDigest getDoctorDigest() {
        return doctorDigest;
    }

    public void setDoctorDigest(DoctorDigest doctorDigest) {
        this.doctorDigest = doctorDigest;
    }

    public PatientDigest getAuthor() {
        return author;
    }

    public void setAuthor(PatientDigest author) {
        this.author = author;
    }

    public String getVisitId() {
        return visitId;
    }

    public void setVisitId(String visitId) {
        this.visitId = visitId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCreatedAtString() {
        return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
