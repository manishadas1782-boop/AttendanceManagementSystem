package com.ams.service;

import java.nio.file.Path;

public class FaceServiceMock {
    
    // Mock face verification - always returns true for demo
    public boolean verifyFace(Path referenceImage, Path capturedImage) {
        // In real implementation, this would use OpenCV for face matching
        // For demo purposes, we'll simulate a successful match
        return true;
    }
    
    // Mock face detection - always returns true for demo
    public boolean detectFace(Path capturedImage) {
        // In real implementation, this would detect if a face is present
        // For demo purposes, we'll simulate face detection
        return true;
    }
}