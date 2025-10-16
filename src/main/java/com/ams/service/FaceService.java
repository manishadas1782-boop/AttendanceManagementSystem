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
    private static final String FACE_CASCADE_PATH = "/haarcascade_frontalface_alt.xml";
    private CascadeClassifier faceCascade;
    
    public FaceService() {
        try {
            // Load cascade from resources
            java.io.InputStream cascadeStream = getClass().getResourceAsStream(FACE_CASCADE_PATH);
            if (cascadeStream != null) {
                // Create temporary file from resource
                java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("cascade", ".xml");
                java.nio.file.Files.copy(cascadeStream, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                cascadeStream.close();
                
                faceCascade = new CascadeClassifier(tempFile.toString());
                
                // Clean up temp file
                tempFile.toFile().deleteOnExit();
                
                if (faceCascade.empty()) {
                    System.err.println("Warning: Loaded cascade is empty, using alternative detection");
                    faceCascade = null;
                }
            } else {
                System.err.println("Warning: Could not find cascade file in resources, using alternative detection");
                faceCascade = null;
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not initialize face cascade classifier: " + e.getMessage());
            faceCascade = null;
        }
    }

    // Compares two images by extracting only the face regions and comparing facial biometric features
    public boolean verifyFace(Path referenceImage, Path capturedImage) {
        try {
            Mat img1 = opencv_imgcodecs.imread(referenceImage.toString());
            Mat img2 = opencv_imgcodecs.imread(capturedImage.toString());
            if (img1 == null || img1.empty() || img2 == null || img2.empty()) {
                System.out.println("Face verification failed: Could not load images");
                return false;
            }

            // Extract face regions from both images
            Mat face1 = extractFaceRegion(img1);
            Mat face2 = extractFaceRegion(img2);
            
            if (face1 == null || face2 == null || face1.empty() || face2.empty()) {
                System.out.println("Face verification failed: Could not extract face regions");
                // If face extraction fails, use alternative comparison with stricter thresholds
                return verifyFaceAlternative(img1, img2);
            }
            
            // Normalize face regions for consistent lighting
            Mat normalizedFace1 = normalizeFace(face1);
            Mat normalizedFace2 = normalizeFace(face2);
            
            // Compare facial biometric features using multiple methods
            boolean templateMatch = compareFacesTemplate(normalizedFace1, normalizedFace2);
            boolean featureMatch = compareFacesFeatures(normalizedFace1, normalizedFace2);
            boolean histogramMatch = compareFacesHistogram(normalizedFace1, normalizedFace2);
            
            // STRICT VERIFICATION: Prevent any false positives
            // Only verify if we have STRONG evidence this is the same person
            boolean result = false;
            
            // PRIMARY RULE: Must have both template AND feature match for high security
            if (templateMatch && featureMatch) {
                result = true;
                System.out.println("STRONG MATCH: Both template and features verified - HIGH CONFIDENCE");
            }
            // FALLBACK RULE: Allow ONLY if template match is very strong (for passport photos)
            else if (templateMatch && !featureMatch) {
                // Get the actual template score for additional verification
                Mat tempResult = new Mat();
                opencv_imgproc.matchTemplate(normalizedFace1, normalizedFace2, tempResult, opencv_imgproc.TM_CCOEFF_NORMED);
                org.bytedeco.opencv.opencv_core.Point minLoc = new org.bytedeco.opencv.opencv_core.Point();
                org.bytedeco.opencv.opencv_core.Point maxLoc = new org.bytedeco.opencv.opencv_core.Point();
                double[] minVal = new double[1];
                double[] maxVal = new double[1];
                opencv_core.minMaxLoc(tempResult, minVal, maxVal, minLoc, maxLoc, new Mat());
                double actualScore = maxVal[0];
                
                // Only allow if template score is VERY HIGH (indicating strong similarity)
                if (actualScore > 0.65) {
                    result = true;
                    System.out.println("STRONG TEMPLATE MATCH: Score " + String.format("%.4f", actualScore) + " - MEDIUM CONFIDENCE");
                } else {
                    result = false;
                    System.out.println("WEAK TEMPLATE MATCH: Score " + String.format("%.4f", actualScore) + " - REJECTED FOR SECURITY");
                }
            }
            // REJECT: Any other combination is not secure enough
            else {
                result = false;
                System.out.println("INSUFFICIENT MATCH: Rejecting to prevent false positive");
            }
            System.out.println("Face verification result: " + result + " (template: " + templateMatch + 
                             ", features: " + featureMatch + ", histogram: " + histogramMatch + ")");
            
            // Additional security check: if result is true, do a cross-validation
            if (result) {
                return performSecurityCheck(normalizedFace1, normalizedFace2);
            }
            
            return result;
            
        } catch (Exception e) {
            System.err.println("Error in face verification: " + e.getMessage());
            return false;
        }
    }
    
    // Extract the largest face region from the image
    private Mat extractFaceRegion(Mat image) {
        try {
            Mat gray = new Mat();
            opencv_imgproc.cvtColor(image, gray, opencv_imgproc.COLOR_BGR2GRAY);
            
            if (faceCascade != null && !faceCascade.isNull()) {
                RectVector faces = new RectVector();
                // More aggressive detection for small/passport photos
                faceCascade.detectMultiScale(gray, faces, 1.05, 2, 0, 
                    new org.bytedeco.opencv.opencv_core.Size(30, 30),  // Smaller minimum size
                    new org.bytedeco.opencv.opencv_core.Size());
                
                if (faces.size() > 0) {
                    // Find the largest face
                    Rect largestFace = null;
                    int maxArea = 0;
                    
                    for (long i = 0; i < faces.size(); i++) {
                        Rect face = faces.get(i);
                        int area = face.width() * face.height();
                        if (area > maxArea) {
                            maxArea = area;
                            largestFace = face;
                        }
                    }
                    
                    if (largestFace != null) {
                        // Expand face region slightly to include more facial features
                        int padding = Math.max(10, Math.min(largestFace.width(), largestFace.height()) / 10);
                        int x = Math.max(0, largestFace.x() - padding);
                        int y = Math.max(0, largestFace.y() - padding);
                        int width = Math.min(gray.cols() - x, largestFace.width() + 2 * padding);
                        int height = Math.min(gray.rows() - y, largestFace.height() + 2 * padding);
                        
                        Rect expandedFace = new Rect(x, y, width, height);
                        return new Mat(gray, expandedFace);
                    }
                }
            }
            
            // If no face detected, return the center portion of the image
            int centerX = gray.cols() / 4;
            int centerY = gray.rows() / 4;
            int centerWidth = gray.cols() / 2;
            int centerHeight = gray.rows() / 2;
            
            if (centerX + centerWidth <= gray.cols() && centerY + centerHeight <= gray.rows()) {
                Rect centerRect = new Rect(centerX, centerY, centerWidth, centerHeight);
                return new Mat(gray, centerRect);
            }
            
            return gray;
            
        } catch (Exception e) {
            System.err.println("Error extracting face region: " + e.getMessage());
            return null;
        }
    }
    
    // Normalize face for consistent lighting and contrast
    private Mat normalizeFace(Mat face) {
        try {
            Mat normalized = new Mat();
            
            // Apply histogram equalization for consistent lighting
            opencv_imgproc.equalizeHist(face, normalized);
            
            // Apply Gaussian blur to reduce noise
            Mat blurred = new Mat();
            opencv_imgproc.GaussianBlur(normalized, blurred, 
                new org.bytedeco.opencv.opencv_core.Size(3, 3), 0);
            
            // Resize to standard size for comparison
            Mat resized = new Mat();
            opencv_imgproc.resize(blurred, resized, 
                new org.bytedeco.opencv.opencv_core.Size(128, 128));
            
            return resized;
            
        } catch (Exception e) {
            System.err.println("Error normalizing face: " + e.getMessage());
            return face;
        }
    }
    
    // Compare faces using template matching
    private boolean compareFacesTemplate(Mat face1, Mat face2) {
        try {
            Mat result = new Mat();
            opencv_imgproc.matchTemplate(face1, face2, result, opencv_imgproc.TM_CCOEFF_NORMED);
            
            org.bytedeco.opencv.opencv_core.Point minLoc = new org.bytedeco.opencv.opencv_core.Point();
            org.bytedeco.opencv.opencv_core.Point maxLoc = new org.bytedeco.opencv.opencv_core.Point();
            double[] minVal = new double[1];
            double[] maxVal = new double[1];
            
            opencv_core.minMaxLoc(result, minVal, maxVal, minLoc, maxLoc, new Mat());
            
            double matchScore = maxVal[0];
            System.out.println("Template match score: " + String.format("%.4f", matchScore));
            
            // Strong threshold to prevent false positives
            // Only pass if there's strong facial similarity
            return matchScore > 0.55;  // Increased to 0.55 for high security
            
        } catch (Exception e) {
            System.err.println("Error in template matching: " + e.getMessage());
            return false;
        }
    }
    
    // Compare faces using feature matching (ORB)
    private boolean compareFacesFeatures(Mat face1, Mat face2) {
        try {
            ORB orb = ORB.create(500, 1.2f, 8, 31, 0, 2, ORB.HARRIS_SCORE, 31, 20);
            KeyPointVector kps1 = new KeyPointVector();
            KeyPointVector kps2 = new KeyPointVector();
            Mat desc1 = new Mat();
            Mat desc2 = new Mat();
            
            orb.detectAndCompute(face1, new Mat(), kps1, desc1);
            orb.detectAndCompute(face2, new Mat(), kps2, desc2);
            
            if (desc1.empty() || desc2.empty() || kps1.size() < 10 || kps2.size() < 10) {
                System.out.println("Feature matching: Insufficient features detected");
                return false;
            }
            
            BFMatcher matcher = new BFMatcher(opencv_core.NORM_HAMMING, true);
            DMatchVector matches = new DMatchVector();
            matcher.match(desc1, desc2, matches);
            
            if (matches.size() < 10) {
                System.out.println("Feature matching: Too few matches (" + matches.size() + ")");
                return false;
            }
            
            // Calculate good matches (with low distance)
            int goodMatches = 0;
            double totalDistance = 0;
            
            for (long i = 0; i < matches.size(); i++) {
                double distance = matches.get(i).distance();
                totalDistance += distance;
                if (distance < 50.0) {
                    goodMatches++;
                }
            }
            
            double avgDistance = totalDistance / matches.size();
            double goodMatchRatio = (double) goodMatches / matches.size();
            
            System.out.println("Feature matching: " + goodMatches + "/" + matches.size() + 
                             " good matches, avg distance: " + String.format("%.2f", avgDistance) + 
                             ", ratio: " + String.format("%.2f", goodMatchRatio));
            
            // STRICT feature matching to prevent false positives
            // Require strong feature correlation to verify identity
            return goodMatches >= 15 && avgDistance < 55.0 && goodMatchRatio > 0.35;  // Very strict requirements
            
        } catch (Exception e) {
            System.err.println("Error in feature matching: " + e.getMessage());
            return false;
        }
    }
    
    // Compare faces using histogram correlation
    private boolean compareFacesHistogram(Mat face1, Mat face2) {
        try {
            // Calculate histograms for both faces using proper OpenCV Java binding
            Mat hist1 = new Mat();
            Mat hist2 = new Mat();
            
            // Create MatVector for the images
            org.bytedeco.opencv.opencv_core.MatVector images1 = new org.bytedeco.opencv.opencv_core.MatVector(1);
            org.bytedeco.opencv.opencv_core.MatVector images2 = new org.bytedeco.opencv.opencv_core.MatVector(1);
            images1.put(0, face1);
            images2.put(0, face2);
            
            // Create proper parameter objects
            org.bytedeco.javacpp.IntPointer channels = new org.bytedeco.javacpp.IntPointer(0);
            org.bytedeco.javacpp.IntPointer histSize = new org.bytedeco.javacpp.IntPointer(256);
            org.bytedeco.javacpp.FloatPointer ranges = new org.bytedeco.javacpp.FloatPointer(0f, 256f);
            
            opencv_imgproc.calcHist(images1, channels, new Mat(), hist1, histSize, ranges);
            opencv_imgproc.calcHist(images2, channels, new Mat(), hist2, histSize, ranges);
            
            // Normalize histograms
            opencv_core.normalize(hist1, hist1, 0, 1, opencv_core.NORM_MINMAX, -1, new Mat());
            opencv_core.normalize(hist2, hist2, 0, 1, opencv_core.NORM_MINMAX, -1, new Mat());
            
            // Calculate correlation
            double correlation = opencv_imgproc.compareHist(hist1, hist2, opencv_imgproc.CV_COMP_CORREL);
            
            System.out.println("Histogram correlation: " + String.format("%.4f", correlation));
            
            // Relaxed histogram correlation threshold for passport photos
            return correlation > 0.5;  // Reduced from 0.7 to 0.5
            
        } catch (Exception e) {
            System.err.println("Error in histogram comparison: " + e.getMessage());
            return false;
        }
    }
    
    // Additional security check to prevent false positives
    private boolean performSecurityCheck(Mat face1, Mat face2) {
        try {
            // Perform a stricter template matching as final security check
            Mat result = new Mat();
            opencv_imgproc.matchTemplate(face1, face2, result, opencv_imgproc.TM_CCOEFF_NORMED);
            
            org.bytedeco.opencv.opencv_core.Point minLoc = new org.bytedeco.opencv.opencv_core.Point();
            org.bytedeco.opencv.opencv_core.Point maxLoc = new org.bytedeco.opencv.opencv_core.Point();
            double[] minVal = new double[1];
            double[] maxVal = new double[1];
            
            opencv_core.minMaxLoc(result, minVal, maxVal, minLoc, maxLoc, new Mat());
            
            double securityScore = maxVal[0];
            System.out.println("Security check score: " + String.format("%.4f", securityScore));
            
            // MAXIMUM security threshold - must have very strong correlation
            boolean securityPass = securityScore > 0.60;
            
            if (!securityPass) {
                System.out.println("SECURITY CHECK FAILED - Rejecting verification to prevent false positive");
            } else {
                System.out.println("Security check passed - Verification confirmed");
            }
            
            return securityPass;
            
        } catch (Exception e) {
            System.err.println("Error in security check: " + e.getMessage());
            return false; // Fail secure
        }
    }
    
    // Alternative face verification method when face extraction fails
    private boolean verifyFaceAlternative(Mat img1, Mat img2) {
        try {
            System.out.println("Using alternative face verification method");
            
            // Convert to grayscale and normalize
            Mat gray1 = new Mat();
            Mat gray2 = new Mat();
            opencv_imgproc.cvtColor(img1, gray1, opencv_imgproc.COLOR_BGR2GRAY);
            opencv_imgproc.cvtColor(img2, gray2, opencv_imgproc.COLOR_BGR2GRAY);
            
            Mat norm1 = new Mat();
            Mat norm2 = new Mat();
            opencv_imgproc.equalizeHist(gray1, norm1);
            opencv_imgproc.equalizeHist(gray2, norm2);
            
            // Resize to same dimensions
            int targetSize = 200;
            Mat resized1 = new Mat();
            Mat resized2 = new Mat();
            opencv_imgproc.resize(norm1, resized1, 
                new org.bytedeco.opencv.opencv_core.Size(targetSize, targetSize));
            opencv_imgproc.resize(norm2, resized2, 
                new org.bytedeco.opencv.opencv_core.Size(targetSize, targetSize));
            
            // Use very strict template matching for entire image
            Mat result = new Mat();
            opencv_imgproc.matchTemplate(resized1, resized2, result, opencv_imgproc.TM_CCOEFF_NORMED);
            
            org.bytedeco.opencv.opencv_core.Point minLoc = new org.bytedeco.opencv.opencv_core.Point();
            org.bytedeco.opencv.opencv_core.Point maxLoc = new org.bytedeco.opencv.opencv_core.Point();
            double[] minVal = new double[1];
            double[] maxVal = new double[1];
            
            opencv_core.minMaxLoc(result, minVal, maxVal, minLoc, maxLoc, new Mat());
            
            double matchScore = maxVal[0];
            System.out.println("Alternative verification score: " + String.format("%.4f", matchScore));
            
            // STRICT threshold for alternative method to prevent false positives
            return matchScore > 0.75;  // High threshold for security
            
        } catch (Exception e) {
            System.err.println("Error in alternative face verification: " + e.getMessage());
            return false;
        }
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
