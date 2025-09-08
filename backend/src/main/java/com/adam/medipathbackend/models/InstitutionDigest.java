package com.adam.medipathbackend.models;

public class InstitutionDigest {
    private String institutionId;

    private String institutionName;

    public InstitutionDigest(String institutionId, String institutionName) {
        this.institutionId = institutionId;
        this.institutionName = institutionName;
    }

    public String getInstitutionId() {
        return institutionId;
    }

    public boolean isValid() {
        return this.institutionId != null && this.institutionName != null && !(this.institutionId.isBlank() || this.institutionName.isBlank());
    }

    public void setInstitutionId(String institutionId) {
        this.institutionId = institutionId;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }
}
