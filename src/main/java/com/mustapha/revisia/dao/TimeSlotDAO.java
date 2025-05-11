package com.mustapha.revisia.dao;

import com.mustapha.revisia.models.Subject;
import com.mustapha.revisia.models.TimeSlot;
import com.mustapha.revisia.models.User;
import java.time.DayOfWeek;
import java.util.List;

public interface TimeSlotDAO {
    void saveTimeSlot(TimeSlot timeSlot);
    TimeSlot getTimeSlotById(Long id);
    List<TimeSlot> getTimeSlotsByUser(User user);
    List<TimeSlot> getTimeSlotsByUserAndDay(User user, DayOfWeek day);
    List<TimeSlot> getTimeSlotsBySubject(Subject subject);
    void updateTimeSlot(TimeSlot timeSlot);
    void deleteTimeSlot(TimeSlot timeSlot);
}