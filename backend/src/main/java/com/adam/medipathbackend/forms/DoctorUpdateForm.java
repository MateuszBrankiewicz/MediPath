package com.adam.medipathbackend.forms;

import java.util.ArrayList;

public class DoctorUpdateForm {
    private String licenceNumber;
    private ArrayList<String> specialisations;

    public DoctorUpdateForm(String licenceNumber, ArrayList<String> specialisations) {
        this.licenceNumber = licenceNumber;
        this.specialisations = specialisations;
    }

    public String getLicenceNumber() {
        return licenceNumber;
    }

    public void setLicenceNumber(String licenceNumber) {
        this.licenceNumber = licenceNumber;
    }

    public ArrayList<String> getSpecialisations() {
        return specialisations;
    }

    public void setSpecialisations(ArrayList<String> specialisations) {
        this.specialisations = specialisations;
    }
}
