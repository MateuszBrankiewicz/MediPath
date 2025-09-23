package com.adam.medipathbackend.models;

public class AddCommentForm {
    private String visitID;
    private float institutionRating;
    private float doctorRating;
    private String comment;

    public AddCommentForm(String visitID, float institutionRating, float doctorRating, String comment) {
        this.visitID = visitID;
        this.institutionRating = institutionRating;
        this.doctorRating = doctorRating;
        this.comment = comment;
    }

    public String getVisitID() {
        return visitID;
    }

    public void setVisitID(String visitID) {
        this.visitID = visitID;
    }

    public float getInstitutionRating() {
        return institutionRating;
    }

    public void setInstitutionRating(float institutionRating) {
        this.institutionRating = institutionRating;
    }

    public float getDoctorRating() {
        return doctorRating;
    }

    public void setDoctorRating(float doctorRating) {
        this.doctorRating = doctorRating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
