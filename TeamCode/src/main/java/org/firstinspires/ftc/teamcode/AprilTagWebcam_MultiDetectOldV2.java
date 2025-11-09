/*
 * Copyright (c) 2024 Team 13353
 *
 * Significant modifications and enhancements have been made to the original work.
 * These modifications include, but are not limited to:
 *  - Refactoring for multi-tag detection and display.
 *  - Encapsulation of gamepad controls for improved readability.
 *  - Addition of a toggleable display mode for a cleaner user interface.
 *  - Implementation of a Known vs. Apparent size comparison feature for diagnostics.
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
 * STANDALONE APRILTAG TESTER V2 (with Size Comparison & Toggle View)
 * =================================================================================================
 *
 * This powerful diagnostic tool allows you to test AprilTag detection while manually tuning
 * camera settings for maximum reliability. It features a toggleable display to switch between
 * a camera tuning view and a detailed detection data view.
 *
 * Why Manual Control is Critical:
 * Auto exposure is slow and unreliable in changing competition lighting. This tool lets you
 * find and set fixed Exposure and Gain values, ensuring your vision is fast and consistent.
 *
 * Key Diagnostic Feature:
 * In "Detection Mode," this tool displays the tag's known physical size alongside its apparent
 * size in pixels. A stable pixel width directly translates to a stable and accurate distance calculation.
 *
 * Gamepad 1 Controls:
 * - Y Button:          TOGGLE between Tuning Mode and Detection Mode.
 * - D-Pad Up/Down:   Increase/Decrease camera Exposure.
 * - Bumpers L/R:     Decrease/Increase camera Gain.
 * - A Button:          Toggle the camera back to Auto Exposure mode.
 *
 * @version 3.2 - Corrected method for getting known tag size.
 * @author Team 13353 (Modifications)
 * @author Phil Malone and FIRST (Original Concept)
 */
@TeleOp(name = "AprilTag-MultiView+ConfigV2", group = "Standalone Tools")
public class AprilTagWebcam_MultiDetectOldV2 extends LinearOpMode {

    private AprilTagProcessor aprilTag;
    private VisionPortal portal;

    // State variables for the toggleable display
    private boolean isTuningMode = true;
    private boolean yWasPressed = false;

    @Override
    public void runOpMode() throws InterruptedException {
        initAprilTag();
        setManualExposure(6, 250); // Set a sane starting exposure
        waitForStart();

        while (opModeIsActive()) {
            // Handle all gamepad inputs in the background
            handleGamepadControls();

            // Handle the display mode toggle logic
            if (gamepad1.y && !yWasPressed) {
                isTuningMode = !isTuningMode; // Flip the mode
            }
            yWasPressed = gamepad1.y;

            // Update telemetry based on the current mode
            telemetry.clearAll();
            if (isTuningMode) {
                displayTuningTelemetry();
            } else {
                telemetryAprilTag();
            }

            // Add a persistent footer to remind the user how to switch modes
            telemetry.addLine("\n----------------------------------------");
            telemetry.addLine("Press (Y) to switch between Tuning and Detection views");
            telemetry.update();
            sleep(20);
        }

        portal.close(); // Release camera resources
    }

    /**
     * Initializes the AprilTag processor and the Vision Portal.
     */
    private void initAprilTag() {
        aprilTag = new AprilTagProcessor.Builder()
                .build(); // Automatically loads the CenterStage tag library

        portal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
                .addProcessor(aprilTag)
                .build();
    }

    /**
     * Displays telemetry for ALL detected AprilTags, including size comparison.
     */
    private void telemetryAprilTag() {
        telemetry.addLine("--- Detection Mode ---");
        List<AprilTagDetection> currentDetections = aprilTag.getDetections();
        telemetry.addData("Tags Detected", currentDetections.size());
        telemetry.addLine();

        for (AprilTagDetection detection : currentDetections) {
            if (detection.metadata != null) {
                // Get the known physical size of the tag by looking it up with our helper method.
                double knownSize = getKnownTagSize(detection.id);
                // Calculate the apparent width of the tag on the camera sensor in pixels.
                double pixelWidth = Math.abs(detection.corners[1].x - detection.corners[0].x);

                telemetry.addLine(String.format(">> ID %d (%s)", detection.id, detection.metadata.name));
                telemetry.addLine(String.format("   Known Size: %5.2f in", knownSize));
                telemetry.addLine(String.format("   Pixel Width: %5.1f px", pixelWidth));
                telemetry.addLine(String.format("   Range: %5.1f in", detection.ftcPose.range));
                telemetry.addLine(String.format("   Bearing: %3.0f deg", detection.ftcPose.bearing));
            }
        }
    }

    /**
     * Returns the known physical size of an AprilTag based on its ID for the CenterStage game.
     * @param tagId The ID of the AprilTag.
     * @return The physical size of the tag in inches.
     */
    private double getKnownTagSize(int tagId) {
        // For the CenterStage game (2023-2024), tags 1-6 are small (2 inches)
        // and tags 7-10 are large (5 inches).
        if (tagId >= 1 && tagId <= 6) {
            return 2.0;
        } else if (tagId >= 7 && tagId <= 10) {
            return 5.0;
        }
        return 0.0; // Return 0 for any unrecognized ID
    }

    /**
     * Displays instructions and live values for camera settings in Tuning Mode.
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