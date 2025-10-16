package com.ams.model;

import java.time.Instant;

public class AttendanceRecord {
    private long id;
    private long userId;
    private long subjectId;
    private String status; // PRESENT, ABSENT, LATE
    private Instant markedAt;
    private String source; // QR, CAMERA, MANUAL
    private String checkInPhotoPath; // Path to photo captured during check-in

    public AttendanceRecord() {}

    public AttendanceRecord(long id, long userId, long subjectId, String status, Instant markedAt, String source) {
        this.id = id;
        this.userId = userId;
        this.subjectId = subjectId;
        this.status = status;
        this.markedAt = markedAt;
        this.source = source;
        this.checkInPhotoPath = null;
    }
    
    public AttendanceRecord(long id, long userId, long subjectId, String status, Instant markedAt, String source, String checkInPhotoPath) {
        this.id = id;
        this.userId = userId;
        this.subjectId = subjectId;
        this.status = status;
        this.markedAt = markedAt;
        this.source = source;
        this.checkInPhotoPath = checkInPhotoPath;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public long getSubjectId() { return subjectId; }
    public void setSubjectId(long subjectId) { this.subjectId = subjectId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getMarkedAt() { return markedAt; }
    public void setMarkedAt(Instant markedAt) { this.markedAt = markedAt; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public String getCheckInPhotoPath() { return checkInPhotoPath; }
    public void setCheckInPhotoPath(String checkInPhotoPath) { this.checkInPhotoPath = checkInPhotoPath; }
}
