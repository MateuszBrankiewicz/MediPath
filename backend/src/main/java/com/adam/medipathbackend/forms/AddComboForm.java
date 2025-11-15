package com.adam.medipathbackend.forms;

public class AddComboForm {
    private RegistrationForm userDetails;
    private String licenceNumber;
    private AddEmployeeForm employeeDetails;

    public AddComboForm(RegistrationForm userDetails, String licenceNumber, int roleCode, AddEmployeeForm employeeForm) {
        this.licenceNumber = licenceNumber;
        this.userDetails = userDetails;
        this.employeeDetails = employeeForm;
    }

    public RegistrationForm getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(RegistrationForm userDetails) {
        this.userDetails = userDetails;
    }

    public String getLicenceNumber() {
        return licenceNumber;
    }

    public void setLicenceNumber(String licenceNumber) {
        this.licenceNumber = licenceNumber;
    }

    public AddEmployeeForm getEmployeeDetails() {
        return employeeDetails;
    }

    public void setEmployeeDetails(AddEmployeeForm employeeDetails) {
        this.employeeDetails = employeeDetails;
    }
}
