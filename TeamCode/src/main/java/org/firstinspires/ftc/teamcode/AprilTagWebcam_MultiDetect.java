/*
 * Copyright (c) 2024 Team 13353
 *
 * Significant modifications and enhancements have been made to the original work.
 * These modifications include, but are not limited to:
 *  - Refactoring for multi-tag detection and display.
 *  - Encapsulation of gamepad controls for improved readability.
 *  - Addition of a toggleable display mode for a cleaner user interface.
 *  - Addition of comprehensive documentation and user instructions.
 *
 * --------------------------------------------------------------------------------
 *
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
 * STANDALONE APRILTAG TESTER (MULTI-DETECT & TOGGLE VIEW)
 * =================================================================================================
 *
 * This powerful diagnostic tool allows you to test AprilTag detection while manually tuning
 * camera settings for maximum reliability. It features a toggleable display to switch between
 * a camera tuning view and a detection data view.
 *
 * Why Manual Control is Critical:
 * Auto exposure is slow and unreliable in changing competition lighting. This tool lets you
 * find and set fixed Exposure and Gain values, ensuring your vision is fast and consistent.
 *
 * How to Use:
 * 1.  Run this OpMode. The screen will start in "Tuning Mode".
 * 2.  Point the webcam at AprilTags.
 * 3.  Use the controls on Gamepad 1 to find the settings where detection is clearest.
 * 4.  Press (Y) to switch to "Detection Mode" to see detailed range and bearing data.
 * 5.  Press (Y) again to switch back to tuning.
 *
 * Gamepad 1 Controls:
 * - Y Button:          TOGGLE between Tuning Mode and Detection Mode.
 * - D-Pad Up/Down:   Increase/Decrease camera Exposure.
 * - Bumpers L/R:     Decrease/Increase camera Gain.
 * - A Button:          Toggle the camera back to Auto Exposure mode.
 *
 * @version 3.0 - Implemented toggleable display mode for improved UI.
 * @author Team 13353 (Modifications)
 * @author Phil Malone and FIRST (Original Concept)
 */
@TeleOp(name = "Standalone: AprilTag Tester (Toggle View)", group = "Standalone Tools")
public class AprilTagWebcam_MultiDetect extends LinearOpMode {

    private AprilTagProcessor aprilTag;
    private VisionPortal portal;

    // --- STATE VARIABLES for the toggleable display ---
    // This boolean tracks which view is currently active.
    private boolean isTuningMode = true;
    // This boolean handles the debounce for the toggle button.
    private boolean yWasPressed = false;

    @Override
    public void runOpMode() throws InterruptedException {
        initAprilTag();
        setManualExposure(6, 250);
        waitForStart();

        while (opModeIsActive()) {
            // --- HANDLE CONTROLS ---
            // The camera settings are adjusted in the background, regardless of the display mode.
            handleGamepadControls();

            // This logic handles toggling the display mode with the 'Y' button.
            // It uses a non-blocking debounce to ensure one press = one toggle.
            if (gamepad1.y && !yWasPressed) {
                isTuningMode = !isTuningMode; // Flip the boolean mode
            }
            yWasPressed = gamepad1.y; // Update the button state for the next loop.


            // --- UPDATE TELEMETRY BASED ON THE CURRENT MODE ---
            telemetry.clearAll(); // Clear the screen each loop to prevent flickering.

            if (isTuningMode) {
                // In Tuning Mode, show the instructions and live camera settings.
                displayTuningTelemetry();
            } else {
                // In Detection Mode, show the data for all visible AprilTags.
                telemetryAprilTag();
            }

            // Add a persistent footer to the telemetry to remind the user how to switch modes.
            telemetry.addLine("\n----------------------------------------");
            telemetry.addLine("Press (Y) to switch between Tuning and Detection views");

            telemetry.update();
            sleep(20);
        }

        portal.close();
    }

    /**
     * Initializes the AprilTag processor and the Vision Portal.
     */
    private void initAprilTag() {
        aprilTag = new AprilTagProcessor.Builder().build();
        portal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
                .addProcessor(aprilTag)
                .build();
    }

    /**
     * Displays telemetry for ALL detected AprilTags. (DETECTION MODE)
     */
    private void telemetryAprilTag() {
        telemetry.addLine("--- Detection Mode ---");
        List<AprilTagDetection> currentDetections = aprilTag.getDetections();
        telemetry.addData("Tags Detected", currentDetections.size());
        telemetry.addLine();

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
     * Displays instructions and live values for camera settings. (TUNING MODE)
     */
    private void displayTuningTelemetry() {
        telemetry.addLine("--- Camera Tuning Mode ---");
        telemetry.addLine("Use D-Pad and Bumpers to adjust settings.");
        telemetry.addLine("Press (A) to return to Auto Exposure.");
        telemetry.addLine();
        telemetry.addData("Exposure (ms)", getExposure());
        telemetry.addData("Gain", getGain());
    }

    /**
     * Handles gamepad inputs for manually adjusting camera settings.
     * This method runs in the background regardless of the display mode.
     */
    private void handleGamepadControls() {
        if (gamepad1.dpad_up) { setManualExposure(getExposure() + 1, getGain()); }
        if (gamepad1.dpad_down && getExposure() > 1) { setManualExposure(getExposure() - 1, getGain()); }
        if (gamepad1.right_bumper) { setManualExposure(getExposure(), getGain() + 10); }
        if (gamepad1.left_bumper && getGain() > 0) { setManualExposure(getExposure(), getGain() - 10); }
        if (gamepad1.a) { portal.getCameraControl(ExposureControl.class).setMode(ExposureControl.Mode.Auto); }
    }

    /**
     * Sets the camera to manual exposure mode with specific values.
     */
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

    /** Helper method to safely get the current exposure setting. */
    private long getExposure() {
        try {
            return portal.getCameraControl(ExposureControl.class).getExposure(TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return 0;
        }
    }

    /** Helper method to safely get the current gain setting. */
    private int getGain() {
        try {
            return portal.getCameraControl(GainControl.class).getGain();
        } catch (Exception e) {
            return 0;
        }
    }
}