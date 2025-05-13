package com.mustapha.revisia.services;

import com.mustapha.revisia.dao.TimeSlotDAO;
import com.mustapha.revisia.dao.TimeSlotDAOImpl;
import com.mustapha.revisia.models.Subject;
import com.mustapha.revisia.models.TimeSlot;
import com.mustapha.revisia.models.User;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public class TimeSlotServiceImpl implements TimeSlotService {

    private final TimeSlotDAO timeSlotDAO = new TimeSlotDAOImpl();

    @Override
    public void saveTimeSlot(TimeSlot timeSlot) {
        timeSlotDAO.saveTimeSlot(timeSlot);
    }

    @Override
    public void createTimeSlot(Subject subject, DayOfWeek day, LocalTime startTime, LocalTime endTime, String location, User user) {
        TimeSlot timeSlot = new TimeSlot(subject, day, startTime, endTime, location, user);
        saveTimeSlot(timeSlot);
    }

    @Override
    public TimeSlot getTimeSlotById(Long id) {
        return timeSlotDAO.getTimeSlotById(id);
    }

    @Override
    public List<TimeSlot> getTimeSlotsByUser(User user) {
        return timeSlotDAO.getTimeSlotsByUser(user);
    }

    @Override
    public List<TimeSlot> getTimeSlotsByUserAndDay(User user, DayOfWeek day) {
        return timeSlotDAO.getTimeSlotsByUserAndDay(user, day);
    }

    @Override
    public List<TimeSlot> getTimeSlotsBySubject(Subject subject) {
        return timeSlotDAO.getTimeSlotsBySubject(subject);
    }

    @Override
    public void updateTimeSlot(TimeSlot timeSlot) {
        timeSlotDAO.updateTimeSlot(timeSlot);
    }

    @Override
    public void deleteTimeSlot(TimeSlot timeSlot) {
        timeSlotDAO.deleteTimeSlot(timeSlot);
    }
}