package com.adam.medipathbackend.forms;

public class AddComboForm {
    private RegistrationForm userDetails;
    private DoctorUpdateForm doctorDetails;
    private int roleCode;

    public AddComboForm(RegistrationForm userDetails, DoctorUpdateForm doctorDetails, int roleCode) {
        this.roleCode = roleCode;
        this.userDetails = userDetails;
        this.doctorDetails = doctorDetails;
    }

    public RegistrationForm getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(RegistrationForm userDetails) {
        this.userDetails = userDetails;
    }


    public DoctorUpdateForm getDoctorDetails() {
        return doctorDetails;
    }

    public void setDoctorDetails(DoctorUpdateForm doctorDetails) {
        this.doctorDetails = doctorDetails;
    }

    public int getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(int roleCode) {
        this.roleCode = roleCode;
    }
}
