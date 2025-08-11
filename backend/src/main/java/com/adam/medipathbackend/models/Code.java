package com.adam.medipathbackend.models;



public class Code {

    public enum CodeType {
        PRESCRIPTION,
        REFERRAL
    }

    private CodeType codeType;

    private String code;

    private boolean isActive;

    public Code(CodeType codeType, String code, boolean isActive) {
        this.codeType = codeType;
        this.code = code;
        this.isActive = isActive;
    }

    public CodeType getCodeType() {
        return codeType;
    }

    public void setCodeType(CodeType codeType) {
        this.codeType = codeType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
