package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.ExposureControl;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.GainControl;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * =================================================================================================
 *  Standalone AprilTag Detection and Camera Tuning Utility
 * =================================================================================================
 *
 * This OpMode is an all-in-one diagnostic tool for AprilTag vision. It provides a real-time
 * feed of all detected tags and allows for manual camera tuning to optimize performance for
 * specific lighting conditions.
 *
 * How to Use:
 * 1.  Run this OpMode and point the robot's camera at an AprilTag.
 * 2.  Press the (Y) button on the gamepad to switch between two views:
 *     - Detection Mode: Displays detailed telemetry for each visible tag, including ID,
 *       calculated Ground Distance, Bearing, and Elevation.
 *     - Tuning Mode: Allows you to manually adjust the camera's Exposure and Gain using
 *       the D-pad and bumpers to achieve a clear, stable, and noise-free image.
 * 3.  The optimal Exposure and Gain values found in Tuning Mode should be recorded and used
 *     to manually configure the camera in your autonomous programs for reliable performance.
 *
 * @author Team 13353 (Modifications)
 * @author Phil Malone and FIRST (Original Concept)
 */
@TeleOp(name = "AprilTag-MultiView+Config - Ground Dist", group = "Standalone Tools")
public class AprilTagWebcam_MultiDetectV3 extends LinearOpMode {

    private AprilTagProcessor aprilTag;
    private VisionPortal portal;

    // State variables for the toggleable display
    private boolean isTuningMode = true;
    private boolean yWasPressed = false;

    /**
     * This is a calibration correction factor to account for camera lens inaccuracies.
     * It was determined by comparing calculated ground distances to actual measured distances.
     * (e.g., Calculated 93.8" vs. Actual 91.0")
     */
    final double GROUND_DISTANCE_CORRECTION_FACTOR = 0.975;

    @Override
    public void runOpMode() throws InterruptedException {
        initAprilTag();
        setManualExposure(6, 250);
        waitForStart();

        while (opModeIsActive()) {
            handleGamepadControls();

            if (gamepad1.y && !yWasPressed) {
                isTuningMode = !isTuningMode;
            }
            yWasPressed = gamepad1.y;

            telemetry.clearAll();
            if (isTuningMode) {
                displayTuningTelemetry();
            } else {
                telemetryAprilTag();
            }

            telemetry.addLine("\n----------------------------------------");
            telemetry.addLine("Press (Y) to switch between Tuning and Detection views");
            telemetry.update();
            sleep(20);
        }

        portal.close();
    }

    private void initAprilTag() {
        aprilTag = new AprilTagProcessor.Builder().build();
        portal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
                .addProcessor(aprilTag)
                .build();
    }

    /**
     * Displays telemetry for ALL detected AprilTags, including the corrected Ground Distance.
     */
    private void telemetryAprilTag() {
        telemetry.addLine("--- Detection Mode ---");
        List<AprilTagDetection> currentDetections = aprilTag.getDetections();
        telemetry.addData("Tags Detected", currentDetections.size());
        telemetry.addLine();

        for (AprilTagDetection detection : currentDetections) {
            if (detection.metadata != null) {
                // Get the raw data from the detection
                double slantRange = detection.ftcPose.range;
                double elevation = detection.ftcPose.elevation;
                double bearing = detection.ftcPose.bearing;

                // Perform the trigonometric calculation to get the uncorrected ground distance
                double elevationRadians = Math.toRadians(elevation);
                double groundDistance = slantRange * Math.cos(elevationRadians);

                // Apply the empirical correction factor
                double correctedGroundDistance = groundDistance * GROUND_DISTANCE_CORRECTION_FACTOR;

                // Display all the relevant information
                telemetry.addLine(String.format(">> ID %d (%s)", detection.id, detection.metadata.name));
                telemetry.addLine(String.format("   Slant Range: %5.1f in", slantRange));
                telemetry.addLine(String.format("   Ground Distance (Calc): %5.1f in", groundDistance));
                telemetry.addLine(String.format("   Ground Distance (Corrected): %5.1f in", correctedGroundDistance));
                telemetry.addLine(String.format("   Bearing: %3.0f deg", bearing));
                telemetry.addLine(String.format("   Elevation: %3.0f deg", elevation));
            }
        }
    }

    private void displayTuningTelemetry() {
        telemetry.addLine("--- Camera Tuning Mode ---");
        telemetry.addLine("Use D-Pad and Bumpers to adjust settings.");
        telemetry.addLine("Press (A) to return to Auto Exposure.");
        telemetry.addLine();
        telemetry.addData("Exposure (ms)", getExposure());
        telemetry.addData("Gain", getGain());
    }

    private void handleGamepadControls() {
        if (gamepad1.dpad_up) { setManualExposure(getExposure() + 1, getGain()); }
        if (gamepad1.dpad_down && getExposure() > 1) { setManualExposure(getExposure() - 1, getGain()); }
        if (gamepad1.right_bumper) { setManualExposure(getExposure(), getGain() + 10); }
        if (gamepad1.left_bumper && getGain() > 0) { setManualExposure(getExposure(), getGain() - 10); }
        if (gamepad1.a) { portal.getCameraControl(ExposureControl.class).setMode(ExposureControl.Mode.Auto); }
    }

    private void setManualExposure(long exposureMS, int gain) {
        if (portal == null || portal.getCameraState() != VisionPortal.CameraState.STREAMING) return;
        ExposureControl exposureControl = portal.getCameraControl(ExposureControl.class);
        if (exposureControl.getMode() != ExposureControl.Mode.Manual) {
            exposureControl.setMode(ExposureControl.Mode.Manual);
            sleep(50);
        }
        exposureControl.setExposure(exposureMS, TimeUnit.MILLISECONDS);
        sleep(20);
        GainControl gainControl = portal.getCameraControl(GainControl.class);
        gainControl.setGain(gain);
        sleep(20);
    }

    private long getExposure() {
        try { return portal.getCameraControl(ExposureControl.class).getExposure(TimeUnit.MILLISECONDS); }
        catch (Exception e) { return 0; }
    }

    private int getGain() {
        try { return portal.getCameraControl(GainControl.class).getGain(); }
        catch (Exception e) { return 0; }
    }
}