/*
 * Copyright (c) 2024 Phil Malone and FIRST
 *
 * All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.firstinspires.ftc.teamcode;

import android.graphics.Color;
import android.util.Size;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.opencv.Circle;
import org.firstinspires.ftc.vision.opencv.ColorBlobLocatorProcessor;
import org.firstinspires.ftc.vision.opencv.ColorRange;
import org.firstinspires.ftc.vision.opencv.ImageRegion;
import org.opencv.core.RotatedRect;


import java.util.List;

@TeleOp(name = "Optimized Ball Detector w/ Distance", group = "Concept")
public class OptimizedBallDetectorWithDistance extends LinearOpMode {

    //********** CALIBRATION_CONSTANTS **********
    // YOU MUST TUNE THESE VALUES FOR YOUR ROBOT
    // 1. Place the ball at a known distance from the camera.
    // 2. Run this OpMode and observe the pixel diameter in the detailed telemetry.
    // 3. Update the constants below with your measurements.
    private static final double KNOWN_DISTANCE_INCHES = 24.0; // The distance you measured (e.g., 24 inches).
    private static final double KNOWN_DIAMETER_PIXELS = 85.0; // The pixel diameter you observed at the known distance.

    //********** TUNING CONSTANTS **********
    private static final int BLUR_SIZE = 5;
    private static final int DILATE_ERODE_SIZE = 15;
    private static final ImageRegion ROI = ImageRegion.asUnityCenterCoordinates(-0.75, 0.75, 0.75, -0.75);
    private static final double MIN_CONTOUR_AREA = 1000;
    private static final double MIN_CIRCULARITY = 0.7;

    private ColorBlobLocatorProcessor colorLocatorPurple;
    private ColorBlobLocatorProcessor colorLocatorGreen;
    private VisionPortal portal;

    @Override
    public void runOpMode() {
        // --- VISION PROCESSOR SETUP --- (Identical to previous version)
        colorLocatorPurple = new ColorBlobLocatorProcessor.Builder()
                .setTargetColorRange(ColorRange.ARTIFACT_PURPLE)
                .setContourMode(ColorBlobLocatorProcessor.ContourMode.EXTERNAL_ONLY)
                .setRoi(ROI)
                .setDrawContours(true)
                .setBoxFitColor(0)
                .setCircleFitColor(Color.rgb(255, 255, 0))
                .setBlurSize(BLUR_SIZE)
                .setDilateSize(DILATE_ERODE_SIZE)
                .setErodeSize(DILATE_ERODE_SIZE)
                .setMorphOperationType(ColorBlobLocatorProcessor.MorphOperationType.CLOSING)
                .build();

        colorLocatorGreen = new ColorBlobLocatorProcessor.Builder()
                .setTargetColorRange(ColorRange.ARTIFACT_GREEN)
                .setContourMode(ColorBlobLocatorProcessor.ContourMode.EXTERNAL_ONLY)
                .setRoi(ROI)
                .setDrawContours(true)
                .setBoxFitColor(0)
                .setCircleFitColor(Color.rgb(255, 255, 0))
                .setBlurSize(BLUR_SIZE)
                .setDilateSize(DILATE_ERODE_SIZE)
                .setErodeSize(DILATE_ERODE_SIZE)
                .setMorphOperationType(ColorBlobLocatorProcessor.MorphOperationType.CLOSING)
                .build();

        portal = new VisionPortal.Builder()
                .addProcessor(colorLocatorPurple)
                .addProcessor(colorLocatorGreen)
                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
                .setCameraResolution(new Size(320, 240))
                .build();

        telemetry.setMsTransmissionInterval(50);
        telemetry.setDisplayFormat(Telemetry.DisplayFormat.MONOSPACE);

        waitForStart();

        while (opModeIsActive()) {
            // --- DATA ACQUISITION AND FILTERING ---
            List<ColorBlobLocatorProcessor.Blob> blobsPurple = colorLocatorPurple.getBlobs();
            List<ColorBlobLocatorProcessor.Blob> blobsGreen = colorLocatorGreen.getBlobs();

            ColorBlobLocatorProcessor.Util.filterByCriteria(ColorBlobLocatorProcessor.BlobCriteria.BY_CONTOUR_AREA, MIN_CONTOUR_AREA, Double.MAX_VALUE, blobsPurple);
            ColorBlobLocatorProcessor.Util.filterByCriteria(ColorBlobLocatorProcessor.BlobCriteria.BY_CIRCULARITY, MIN_CIRCULARITY, Double.MAX_VALUE, blobsPurple);

            ColorBlobLocatorProcessor.Util.filterByCriteria(ColorBlobLocatorProcessor.BlobCriteria.BY_CONTOUR_AREA, MIN_CONTOUR_AREA, Double.MAX_VALUE, blobsGreen);
            ColorBlobLocatorProcessor.Util.filterByCriteria(ColorBlobLocatorProcessor.BlobCriteria.BY_CIRCULARITY, MIN_CIRCULARITY, Double.MAX_VALUE, blobsGreen);

            ColorBlobLocatorProcessor.Blob purpleTarget = !blobsPurple.isEmpty() ? blobsPurple.get(0) : null;
            ColorBlobLocatorProcessor.Blob greenTarget = !blobsGreen.isEmpty() ? blobsGreen.get(0) : null;

            // --- DISTANCE CALCULATION ---
            double purpleDistance = -1;
            double greenDistance = -1;

            if (purpleTarget != null) {
                Circle circleFit = purpleTarget.getCircle();
                if (circleFit != null) {
                    double currentDiameterPixels = circleFit.getRadius() * 2;
                    purpleDistance = (KNOWN_DISTANCE_INCHES * KNOWN_DIAMETER_PIXELS) / currentDiameterPixels;
                }
            }

            if (greenTarget != null) {
                Circle circleFit = greenTarget.getCircle();
                if (circleFit != null) {
                    double currentDiameterPixels = circleFit.getRadius() * 2;
                    greenDistance = (KNOWN_DISTANCE_INCHES * KNOWN_DIAMETER_PIXELS) / currentDiameterPixels;
                }
            }

            // --- TELEMETRY ---
            telemetry.addLine("--- Ball Detector w/ Distance ---");

            if (purpleTarget != null) {
                RotatedRect boxFit = purpleTarget.getBoxFit();
                telemetry.addLine(String.format("PURPLE Target: Acquired at (%3.0f, %3.0f) - Distance: %4.1f in",
                        boxFit.center.x, boxFit.center.y, purpleDistance));
            } else {
                telemetry.addLine("PURPLE Target: Not Visible");
            }

            if (greenTarget != null) {
                RotatedRect boxFit = greenTarget.getBoxFit();
                telemetry.addLine(String.format("GREEN  Target: Acquired at (%3.0f, %3.0f) - Distance: %4.1f in",
                        boxFit.center.x, boxFit.center.y, greenDistance));
            } else {
                telemetry.addLine("GREEN  Target: Not Visible");
            }

            // Optional: Add detailed data for calibration purposes
            telemetry.addLine("\n--- Detailed Data for Calibration ---");
            if (!blobsPurple.isEmpty()) {
                Circle c = blobsPurple.get(0).getCircle();
                telemetry.addLine(String.format("Largest PURPLE blob pixel diameter: %.1f", c.getRadius() * 2));
            }
            if (!blobsGreen.isEmpty()) {
                Circle c = blobsGreen.get(0).getCircle();
                telemetry.addLine(String.format("Largest GREEN blob pixel diameter: %.1f", c.getRadius() * 2));
            }

            telemetry.update();
            sleep(20);
        }
    }
}