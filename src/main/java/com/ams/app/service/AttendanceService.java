package com.ams.app.service;

import com.ams.app.model.Student;
import com.ams.app.storage.InMemoryStore;

import java.time.LocalDate;
import java.util.Set;

public class AttendanceService {
    private final InMemoryStore store;

    public AttendanceService(InMemoryStore store) {
        this.store = store;
    }

    public boolean markToday(Student student) {
        return store.markAttendance(student.getRegNumber(), LocalDate.now());
    }

    public Set<LocalDate> getStudentAttendance(Student student) {
        return store.getAttendance(student.getRegNumber());
    }
}
