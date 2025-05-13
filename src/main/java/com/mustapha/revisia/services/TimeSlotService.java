package com.mustapha.revisia.services;

import com.mustapha.revisia.models.Subject;
import com.mustapha.revisia.models.TimeSlot;
import com.mustapha.revisia.models.User;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public interface TimeSlotService {
    void saveTimeSlot(TimeSlot timeSlot);
    void createTimeSlot(Subject subject, DayOfWeek day, LocalTime startTime, LocalTime endTime, String location, User user);
    TimeSlot getTimeSlotById(Long id);
    List<TimeSlot> getTimeSlotsByUser(User user);
    List<TimeSlot> getTimeSlotsByUserAndDay(User user, DayOfWeek day);
    List<TimeSlot> getTimeSlotsBySubject(Subject subject);
    void updateTimeSlot(TimeSlot timeSlot);
    void deleteTimeSlot(TimeSlot timeSlot);
}