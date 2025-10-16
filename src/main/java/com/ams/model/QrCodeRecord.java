package com.ams.model;

import java.time.Instant;

public class QrCodeRecord {
    private long id;
    private long userId; // optional, can be 0 for session/date QR
    private String qrData;
    private Instant createdAt;

    public QrCodeRecord() {}

    public QrCodeRecord(long id, long userId, String qrData, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.qrData = qrData;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public String getQrData() { return qrData; }
    public void setQrData(String qrData) { this.qrData = qrData; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
