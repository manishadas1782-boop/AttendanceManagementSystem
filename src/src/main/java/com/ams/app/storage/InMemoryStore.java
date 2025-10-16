package com.ams.app.storage;

import com.ams.app.model.Student;

import java.time.LocalDate;
import java.util.*;

public class InMemoryStore {
    private final Map<String, Student> studentsByReg = new HashMap<>();
    private final Map<String, Set<LocalDate>> attendanceByReg = new HashMap<>();

    public Optional<Student> findStudent(String reg) {
        return Optional.ofNullable(studentsByReg.get(reg));
    }

    public Student addStudent(Student s) {
        studentsByReg.put(s.getRegNumber(), s);
        attendanceByReg.putIfAbsent(s.getRegNumber(), new HashSet<>());
        return s;
    }

    public boolean markAttendance(String reg, LocalDate date) {
        attendanceByReg.putIfAbsent(reg, new HashSet<>());
        Set<LocalDate> dates = attendanceByReg.get(reg);
        if (dates.contains(date)) return false;
        dates.add(date);
        return true;
    }

    public Set<LocalDate> getAttendance(String reg) {
        return Collections.unmodifiableSet(attendanceByReg.getOrDefault(reg, Collections.emptySet()));
    }
}
