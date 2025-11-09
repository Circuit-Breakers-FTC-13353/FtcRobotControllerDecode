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
 * STANDALONE APRILTAG TESTER V4 (Final Version with Autonomous Guide)
 * =================================================================================================
 *
 * This is the ultimate diagnostic tool for AprilTag vision. It includes all previous features
 * plus a guide on how to use the output data in your own Autonomous programs.
 *
 * How to Use:
 * 1.  Run this OpMode to find the optimal manual camera settings for your environment.
 * 2.  Use "Detection Mode" to understand the relationship between Slant Range, Ground Distance,
 *     and Bearing. The Ground Distance and Bearing are the key values for navigation.
 * 3.  Use the example below to build your own high-precision autonomous routines.
 *
 * -------------------------------------------------------------------------------------------------
 * FROM DIAGNOSTICS TO AUTONOMOUS - A CONCEPTUAL GUIDE
 * -------------------------------------------------------------------------------------------------
 *
 * This tool provides the two critical values you need for navigation:
 *  - `Ground Distance`: The true forward/backward distance on the floor to the tag.
 *  - `Bearing`: The left/right angle to the tag.
 *
 * Here is a pseudo-code example of how you would use this data in an Autonomous OpMode to
 * drive the robot to a precise position (e.g., 12 inches away from Tag #5).
 *
 *   // --- Start of Autonomous Pseudo-Code ---
 *
 *   // 1. Define Your Targets
 *   double TARGET_GROUND_DISTANCE = 12.0; // inches
 *   int TARGET_TAG_ID = 5;
 *   double TURN_SPEED = 0.4;
 *   double DRIVE_SPEED = 0.5;
 *
 *   // 2. Find the target tag
 *   AprilTagDetection myTag = findTag(TARGET_TAG_ID);
 *
 *   // 3. Align the Robot (Correct for Bearing)
 *   // Turn the robot until the bearing is close to 0 degrees.
 *   while (myTag.ftcPose.bearing > 1.0) {
 *       turnRight(TURN_SPEED);
 *       myTag = findTag(TARGET_TAG_ID); // Get updated data
 *   }
 *   while (myTag.ftcPose.bearing < -1.0) {
 *       turnLeft(TURN_SPEED);
 *       myTag = findTag(TARGET_TAG_ID); // Get updated data
 *   }
 *
 *   // 4. Drive to the Target Distance (Correct for Ground Distance)
 *   // Calculate the current ground distance using the formula from this tool.
 *   double currentGroundDistance = calculateGroundDistance(myTag);
 *
 *   while (currentGroundDistance > TARGET_GROUND_DISTANCE) {
 *       driveForward(DRIVE_SPEED);
 *       myTag = findTag(TARGET_TAG_ID); // Get updated data
 *       currentGroundDistance = calculateGroundDistance(myTag); // Update the distance
 *   }
 *
 *   // 5. Stop the robot
 *   stop();
 *
 *   // --- End of Autonomous Pseudo-Code ---
 *
 *
 * @version 4.0 - Added Autonomous implementation guide to the header.
 * @author Team 13353 (Modifications)
 * @author Phil Malone and FIRST (Original Concept)
 */
@TeleOp(name = "AprilTag-MultiView+Config - Ground Dist", group = "Standalone Tools")
public class AprilTagWebcam_MultiDetect extends LinearOpMode {

    private AprilTagProcessor aprilTag;
    private VisionPortal portal;

    // State variables for the toggleable display
    private boolean isTuningMode = true;
    private boolean yWasPressed = false;

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
     * Displays telemetry for ALL detected AprilTags, including Ground Distance.
     */
    private void telemetryAprilTag() {
        telemetry.addLine("--- Detection Mode ---");
        List<AprilTagDetection> currentDetections = aprilTag.getDetections();
        telemetry.addData("Tags Detected", currentDetections.size());
        telemetry.addLine();

        for (AprilTagDetection detection : currentDetections) {
            if (detection.metadata != null) {
                double slantRange = detection.ftcPose.range;
                double elevation = detection.ftcPose.elevation;
                double elevationRadians = Math.toRadians(elevation);
                double groundDistance = slantRange * Math.cos(elevationRadians);

                telemetry.addLine(String.format(">> ID %d (%s)", detection.id, detection.metadata.name));
                telemetry.addLine(String.format("   Slant Range: %5.1f in", slantRange));
                telemetry.addLine(String.format("   Ground Distance: %5.1f in", groundDistance));
                telemetry.addLine(String.format("   Bearing: %3.0f deg", detection.ftcPose.bearing));
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