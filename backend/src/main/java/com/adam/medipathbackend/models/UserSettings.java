package com.adam.medipathbackend.models;

public class UserSettings {
    private String language;

    private boolean systemNotifications;

    private boolean userNotifications;

    public UserSettings(String language, boolean systemNotifications, boolean userNotifications) {
        this.language = language;
        this.systemNotifications = systemNotifications;
        this.userNotifications = userNotifications;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isSystemNotifications() {
        return systemNotifications;
    }

    public void setSystemNotifications(boolean systemNotifications) {
        this.systemNotifications = systemNotifications;
    }

    public boolean isUserNotifications() {
        return userNotifications;
    }

    public void setUserNotifications(boolean userNotifications) {
        this.userNotifications = userNotifications;
    }
}
