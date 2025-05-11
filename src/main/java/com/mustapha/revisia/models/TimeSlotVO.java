package com.mustapha.revisia.models;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

// This is a Value Object class for displaying time slots in TableView
public class TimeSlotVO {

    private final String day;
    private final String startTime;
    private final String endTime;
    private final String duration;
    private final String subject;
    private final String location;
    private final Long id;

    public TimeSlotVO(TimeSlot timeSlot) {
        this.day = timeSlot.getDay().getDisplayName(TextStyle.FULL, Locale.FRENCH);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        this.startTime = timeSlot.getStartTime().format(formatter);
        this.endTime = timeSlot.getEndTime().format(formatter);

        this.duration = calculateDuration(timeSlot.getStartTime(), timeSlot.getEndTime());
        this.subject = timeSlot.getSubject().getName();
        this.location = timeSlot.getLocation() != null ? timeSlot.getLocation() : "";
        this.id = timeSlot.getId();
    }

    private String calculateDuration(LocalTime start, LocalTime end) {
        int minutes = (end.getHour() - start.getHour()) * 60 + (end.getMinute() - start.getMinute());
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;

        if (hours > 0) {
            return hours + "h" + (remainingMinutes > 0 ? " " + remainingMinutes + "min" : "");
        } else {
            return minutes + "min";
        }
    }

    public String getDay() {
        return day;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getDuration() {
        return duration;
    }

    public String getSubject() {
        return subject;
    }

    public String getLocation() {
        return location;
    }

    public Long getId() {
        return id;
    }
}