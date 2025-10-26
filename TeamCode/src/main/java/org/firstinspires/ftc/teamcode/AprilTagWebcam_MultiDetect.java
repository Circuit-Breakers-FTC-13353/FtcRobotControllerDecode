/*
 * Copyright (c) 2024 Phil Malone and FIRST
 *
 * All rights reserved.
 * ... (copyright header remains the same) ...
 */

package org.firstinspires.ftc.teamcode;

// *** ALL REQUIRED IMPORTS ARE NOW INCLUDED ***
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
 * STANDALONE APRILTAG TESTER (MULTI-DETECT VERSION)
 * =================================================================================================
 *
 * This powerful diagnostic tool allows you to test AprilTag detection while manually tuning
 * camera settings for maximum reliability.
 *
 * Why Manual Control is Critical:
 * Auto exposure is slow and unreliable in changing competition lighting. This tool lets you
 * find and set fixed Exposure and Gain values, ensuring your vision is fast and consistent.
 *
 * How to Use:
 * 1. Run this OpMode.
 * 2. Point the webcam at AprilTags.
 * 3. Use the D-Pad and bumpers on Gamepad 1 to find the settings where detection is clearest
 *    and most stable.
 * 4. Note these values and use them in your Autonomous code.
 *
 * @version 2.2 - Corrected all missing import statements for camera controls.
 */
@TeleOp(name = "Standalone: AprilTag Tester (Multi-Detect)", group = "Standalone Tools")
public class AprilTagWebcam_MultiDetect extends LinearOpMode {

    private AprilTagProcessor aprilTag;
    private VisionPortal portal;

    @Override
    public void runOpMode() throws InterruptedException {
        initAprilTag();

        // It is recommended to set a starting manual exposure for more reliable performance.
        setManualExposure(6, 250); // Use low exposure time to reduce motion blur

        waitForStart();

        while (opModeIsActive()) {
            telemetryAprilTag(); // Display telemetry for all detected tags
            handleGamepadControls(); // Allow user to change camera settings
            telemetry.update();
            sleep(20);
        }

        portal.close(); // Make sure to close the vision portal
    }

    /**
     * Initializes the AprilTag processor and Vision Portal.
     */
    private void initAprilTag() {
        aprilTag = new AprilTagProcessor.Builder().build();
        portal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
                .addProcessor(aprilTag)
                .build();
    }

    /**
     * Displays telemetry for ALL detected AprilTags.
     */
    private void telemetryAprilTag() {
        List<AprilTagDetection> currentDetections = aprilTag.getDetections();
        telemetry.addData("Tags Detected", currentDetections.size());
        telemetry.addLine("----------------------------------------");

        for (AprilTagDetection detection : currentDetections) {
            if (detection.metadata != null) {
                telemetry.addLine(String.format(">> ID %d (%s)", detection.id, detection.metadata.name));
                telemetry.addLine(String.format("   Range: %5.1f inches", detection.ftcPose.range));
                telemetry.addLine(String.format("   Bearing: %3.0f degrees", detection.ftcPose.bearing));
                telemetry.addLine(String.format("   Elevation: %3.0f degrees", detection.ftcPose.elevation));
            }
        }
    }

    /**
     * Handles gamepad inputs for manually adjusting camera settings.
     */
    private void handleGamepadControls() {
        // Exposure controls
        if (gamepad1.dpad_up) { setManualExposure(getExposure() + 1, getGain()); }
        if (gamepad1.dpad_down && getExposure() > 1) { setManualExposure(getExposure() - 1, getGain()); }

        // Gain controls
        if (gamepad1.right_bumper) { setManualExposure(getExposure(), getGain() + 10); }
        if (gamepad1.left_bumper && getGain() > 0) { setManualExposure(getExposure(), getGain() - 10); }

        // Auto mode toggle
        if (gamepad1.a) { portal.getCameraControl(ExposureControl.class).setMode(ExposureControl.Mode.Auto); }

        telemetry.addLine("\n--- Camera Controls ---");
        telemetry.addLine("D-Pad Up/Down: Change Exposure");
        telemetry.addLine("Bumpers: Change Gain");
        telemetry.addLine("A Button: Set to Auto Exposure");
        telemetry.addData("Exposure (ms)", getExposure());
        telemetry.addData("Gain", getGain());
    }

    /**
     * Sets the camera to manual exposure mode with specific values.
     */
    private void setManualExposure(long exposureMS, int gain) {
        if (portal == null || portal.getCameraState() != VisionPortal.CameraState.STREAMING) return;

        ExposureControl exposureControl = portal.getCameraControl(ExposureControl.class);
        if (exposureControl.getMode() != ExposureControl.Mode.Manual) {
            exposureControl.setMode(ExposureControl.Mode.Manual);
            sleep(50); // Give the camera time to switch modes
        }
        exposureControl.setExposure(exposureMS, TimeUnit.MILLISECONDS);
        sleep(20);
        GainControl gainControl = portal.getCameraControl(GainControl.class);
        gainControl.setGain(gain);
        sleep(20);
    }

    /** Helper method to get the current exposure setting. */
    private long getExposure() {
        try {
            return portal.getCameraControl(ExposureControl.class).getExposure(TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return 0; // Return a default value if control is not available
        }
    }

    /** Helper method to get the current gain setting. */
    private int getGain() {
        try {
            return portal.getCameraControl(GainControl.class).getGain();
        } catch (Exception e) {
            return 0; // Return a default value if control is not available
        }
    }
}