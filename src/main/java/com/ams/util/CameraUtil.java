package com.ams.util;

import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.javacpp.BytePointer;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class CameraUtil {
    public static BufferedImage matToBufferedImage(Mat mat) {
        int type;
        if (mat.channels() == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        } else {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int width = mat.cols();
        int height = mat.rows();
        BufferedImage image = new BufferedImage(width, height, type);
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        BytePointer bp = new BytePointer(mat.data());
        bp.get(data);
        return image;
    }
}
