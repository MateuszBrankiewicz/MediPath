package com.adam.medipathbackend.forms;

import java.util.ArrayList;

public class CompleteVisitForm {
    private ArrayList<String> prescriptions;
    private ArrayList<String> referrals;
    private String note;

    public CompleteVisitForm(ArrayList<String> prescriptions, ArrayList<String> referrals, String note) {
        this.prescriptions = prescriptions;
        this.referrals = referrals;
        this.note = note;
    }

    public ArrayList<String> getPrescriptions() {
        return prescriptions;
    }

    public void setPrescriptions(ArrayList<String> prescriptions) {
        this.prescriptions = prescriptions;
    }

    public ArrayList<String> getReferrals() {
        return referrals;
    }

    public void setReferrals(ArrayList<String> referrals) {
        this.referrals = referrals;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
