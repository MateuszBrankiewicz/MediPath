package com.adam.medipathbackend.models;

import java.time.LocalDateTime;

public class VisitTime {
    private String scheduleId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    public VisitTime(String scheduleId, LocalDateTime startTime, LocalDateTime endTime) {
        this.scheduleId = scheduleId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean isValid() {
        return this.scheduleId != null && this.startTime != null && this.endTime != null && !this.scheduleId.isBlank();
    }
    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
