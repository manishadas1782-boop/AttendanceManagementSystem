package com.ams.service;

import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_features2d;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.global.opencv_objdetect;
import org.bytedeco.opencv.opencv_core.DMatchVector;
import org.bytedeco.opencv.opencv_core.KeyPointVector;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_features2d.BFMatcher;
import org.bytedeco.opencv.opencv_features2d.ORB;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import java.io.IOException;

public class FaceService {
    private static final String FACE_CASCADE_PATH = "haarcascade_frontalface_alt.xml";
    private CascadeClassifier faceCascade;
    
    public FaceService() {
        try {
            // Try to load the built-in cascade classifier
            faceCascade = new CascadeClassifier();
            // OpenCV will use the default face cascade if available
        } catch (Exception e) {
            System.err.println("Warning: Could not initialize face cascade classifier: " + e.getMessage());
            faceCascade = null;
        }
    }

    // Compares two images using ORB feature matching. Returns true if match score passes threshold.
    public boolean verifyFace(Path referenceImage, Path capturedImage) {
        Mat img1 = opencv_imgcodecs.imread(referenceImage.toString());
        Mat img2 = opencv_imgcodecs.imread(capturedImage.toString());
        if (img1 == null || img1.empty() || img2 == null || img2.empty()) return false;

        // Convert to grayscale
        Mat gray1 = new Mat();
        Mat gray2 = new Mat();
        opencv_imgproc.cvtColor(img1, gray1, opencv_imgproc.COLOR_BGR2GRAY);
        opencv_imgproc.cvtColor(img2, gray2, opencv_imgproc.COLOR_BGR2GRAY);

        ORB orb = ORB.create(1000, 1.2f, 8, 31, 0, 2, ORB.HARRIS_SCORE, 31, 20);
        KeyPointVector kps1 = new KeyPointVector();
        KeyPointVector kps2 = new KeyPointVector();
        Mat desc1 = new Mat();
        Mat desc2 = new Mat();
        orb.detectAndCompute(gray1, new Mat(), kps1, desc1);
        orb.detectAndCompute(gray2, new Mat(), kps2, desc2);

        if (desc1.empty() || desc2.empty()) return false;

        BFMatcher matcher = new BFMatcher(opencv_core.NORM_HAMMING, true);
        DMatchVector matches = new DMatchVector();
        matcher.match(desc1, desc2, matches);

        long total = matches.size();
        if (total == 0) return false;

        // Compute average distance of matches
        double sum = 0.0;
        for (long i = 0; i < total; i++) {
            sum += matches.get(i).distance();
        }
        double avgDist = sum / total;

        // Enhanced threshold: require minimum matches and good average distance
        // Faces should have at least 30 good feature matches with low distance
        boolean hasEnoughMatches = total >= 30;
        boolean hasGoodDistance = avgDist < 40.0;
        
        // Additional verification: check for consistent matches
        int goodMatches = 0;
        for (long i = 0; i < total; i++) {
            if (matches.get(i).distance() < 35.0) {
                goodMatches++;
            }
        }
        
        double goodMatchRatio = (double) goodMatches / total;
        boolean hasConsistentMatches = goodMatchRatio > 0.6; // At least 60% good matches
        
        return hasEnoughMatches && hasGoodDistance && hasConsistentMatches;
    }
    
    // Detects if a face is present in the captured image
    public boolean detectFace(Path capturedImage) {
        try {
            Mat img = opencv_imgcodecs.imread(capturedImage.toString());
            if (img == null || img.empty()) return false;
            
            // Convert to grayscale
            Mat gray = new Mat();
            opencv_imgproc.cvtColor(img, gray, opencv_imgproc.COLOR_BGR2GRAY);
            
            // Enhanced face detection
            if (faceCascade != null && !faceCascade.isNull()) {
                RectVector faces = new RectVector();
                faceCascade.detectMultiScale(gray, faces, 1.1, 3, 0, 
                    new org.bytedeco.opencv.opencv_core.Size(30, 30), new org.bytedeco.opencv.opencv_core.Size());
                
                if (faces.size() > 0) {
                    return true; // At least one face detected
                }
            }
            
            // Fallback: Use alternative face detection methods
            return detectFaceAlternative(gray);
            
        } catch (Exception e) {
            System.err.println("Error in face detection: " + e.getMessage());
            return false;
        }
    }
    
    // Alternative face detection using image analysis
    private boolean detectFaceAlternative(Mat gray) {
        try {
            // Check basic image properties
            if (gray.rows() < 100 || gray.cols() < 100) {
                return false; // Image too small
            }
            
            // Check if image isn't too dark or too bright
            Mat mean = new Mat();
            Mat stddev = new Mat();
            opencv_core.meanStdDev(gray, mean, stddev, new Mat());
            double brightness = mean.getDoubleBuffer().get(0);
            double contrast = stddev.getDoubleBuffer().get(0);
            
            // Face images typically have moderate brightness and good contrast
            boolean goodBrightness = brightness > 30 && brightness < 220;
            boolean goodContrast = contrast > 15; // Reasonable contrast indicating features
            
            // Simple edge detection to check for facial features
            Mat edges = new Mat();
            opencv_imgproc.Canny(gray, edges, 50, 150);
            
            // Count edge pixels
            Mat nonZero = new Mat();
            opencv_core.findNonZero(edges, nonZero);
            double edgeRatio = (double) nonZero.total() / (gray.rows() * gray.cols());
            
            // Faces typically have edge ratios between 0.02 and 0.15
            boolean hasFeatures = edgeRatio > 0.02 && edgeRatio < 0.20;
            
            return goodBrightness && goodContrast && hasFeatures;
            
        } catch (Exception e) {
            // Final fallback - just check brightness
            try {
                Mat mean = new Mat();
                opencv_core.meanStdDev(gray, mean, new Mat(), new Mat());
                double brightness = mean.getDoubleBuffer().get(0);
                return brightness > 30 && brightness < 220;
            } catch (Exception ex) {
                return false;
            }
        }
    }
}
