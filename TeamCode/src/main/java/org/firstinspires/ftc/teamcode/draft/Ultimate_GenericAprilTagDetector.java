
/*
 * MODIFICATION NOTICE:
 *
 * This file is an enhanced version of the original FTC SDK sample code.
 * It has been extensively modified and commented by [Your Team Name or Number]
 * on [Date - e.g., September 2025] to create a robust, interactive, and
 * educational tool for the FTC community.
 *
 * The original copyright and license from Dryw Wade and FIRST are preserved
 * below and govern the use of this code.
 */
/* Copyright (c) 2024 Dryw Wade. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode.draft;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.hardware.camera.BuiltinCameraDirection;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;

@TeleOp(name = "Ultimate: Generic AprilTag Detector", group = "Concept")
//@Disabled
public class Ultimate_GenericAprilTagDetector extends LinearOpMode {

    // =============================================================================================
    // SECTION 1: CONFIGURATION & VARIABLES
    // =============================================================================================

    // --- HARDWARE & TARGETING ---
    private static final boolean USE_WEBCAM = true;
    private static final String WEBCAM_NAME = "Webcam 1"; // Match this to your config file
    private int targetTagId = 20; // The default ID of the tag we want to track.

    // --- DECLARED VARIABLES ---
    private AprilTagProcessor aprilTag;
    private VisionPortal visionPortal;
    private AprilTagDetection targetTag = null;
    private boolean cameraInitialized = false;
    private ElapsedTime lastSeenTimer = new ElapsedTime();

    // =============================================================================================
    // SECTION 2: CAMERA CALIBRATION (CRITICAL - UPDATE THESE VALUES)
    // =============================================================================================

    /**
     * CAMERA POSITION ON THE ROBOT
     *
     * This describes the physical location of the camera lens, measured from the center
     * of the robot. The "center" of the robot is typically the point around which it turns.
     *
     * The measurements are in the order of (X, Y, Z).
     *
     * To get these values, follow these steps:
     *
     * 1. X value (Left/Right):
     *    - Imagine looking down on your robot from above.
     *    - Measure the distance from the center of the robot to the center of the camera lens.
     *    - If the camera is to the RIGHT of center, the value is POSITIVE.
     *    - If the camera is to the LEFT of center, the value is NEGATIVE.
     *
     * 2. Y value (Forward/Backward):
     *    - Measure the distance from the center of the robot to the center of the camera lens.
     *    - If the camera is FORWARD of center, the value is POSITIVE.
     *    - If the camera is BACKWARD of center, the value is NEGATIVE.
     *
     * 3. Z value (Up/Down):
     *    - Measure the distance from the center of the robot's turning axis to the camera lens.
     *    - If the camera is ABOVE the center, the value is POSITIVE.
     *    - If the camera is BELOW the center, the value is NEGATIVE.
     *
     * EXAMPLE: A camera that is 2 inches right, 5 inches forward, and 8 inches up would be:
     *          new Position(DistanceUnit.INCH, 2, 5, 8, 0)
     */

    private Position cameraPosition = new Position(DistanceUnit.INCH, 0, 0, 6.5, 0);

    /**
     * CAMERA ORIENTATION ON THE ROBOT
     *
     * This describes how the camera is rotated. Think of it like turning, tilting,
     * or rolling your head.
     *
     * The values are in the order of (Yaw, Pitch, Roll).
     *
     * 1. Yaw (Turning Left/Right):
     *    - This is like shaking your head "no".
     *    - A camera pointing straight forward from the robot has a Yaw of 0.
     *    - A camera pointing 45 degrees to the left would have a Yaw of +45.
     *    - A camera pointing 45 degrees to the right would have a Yaw of -45.
     *
     * 2. Pitch (Tilting Up/Down):
     *    - This is like nodding your head "yes".
     *    - A camera pointing straight up at the ceiling has a Pitch of 0.
     *    - A camera pointing horizontally forward has a Pitch of -90. (This is very common!)
     *    - A camera pointing straight down at the floor has a Pitch of -180 or +180.
     *
     * 3. Roll (Tilting Side-to-Side):
     *    - This is like tilting your head to your shoulder.
     *    - A camera that is perfectly level has a Roll of 0.
     *    - If the camera is tilted 15 degrees to the left, it has a Roll of +15.
     *    - If it's tilted 15 degrees to the right, it has a Roll of -15.
     *
     * EXAMPLE: A common webcam, pointing straight forward and perfectly level:
     *          new YawPitchRollAngles(AngleUnit.DEGREES, 0, -90, 0, 0)
     */
    private YawPitchRollAngles cameraOrientation = new YawPitchRollAngles(AngleUnit.DEGREES, 0, -90, 0, 0);

    // =============================================================================================
    // SECTION 3: OPMODE LOGIC
    // =============================================================================================

    @Override
    public void runOpMode() {
        initAprilTag();

        telemetry.addData(">", "Press START to begin");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {
            // If the camera failed to initialize, just display the error and do nothing else.
            if (!cameraInitialized) {
                telemetry.update();
                sleep(20);
                continue;
            }

            // Gamepad controls to change the target ID
            handleGamepadInput();

            // Main detection loop
            findTargetTag();

            // Display telemetry
            telemetryAprilTag();
            telemetry.update();

            sleep(20);
        }
        visionPortal.close();
    }

    /**
     * Initializes the AprilTag processor and the camera with robust error handling.
     */
    private void initAprilTag() {
        try {
            // Create the AprilTag processor with our desired settings.
            aprilTag = new AprilTagProcessor.Builder()
                    .setTagFamily(AprilTagProcessor.TagFamily.TAG_36h11)
                    .setCameraPose(cameraPosition, cameraOrientation)
                    .build();

            // Create the Vision Portal, which handles camera streaming.
            VisionPortal.Builder builder = new VisionPortal.Builder();
            if (USE_WEBCAM) {
                builder.setCamera(hardwareMap.get(WebcamName.class, WEBCAM_NAME));
            } else {
                builder.setCamera(BuiltinCameraDirection.BACK);
            }
            builder.addProcessor(aprilTag);
            visionPortal = builder.build();

            // Set a flag to indicate that initialization was successful.
            cameraInitialized = true;
            telemetry.addData("Status", "Camera Initialized Successfully");

        } catch (Exception e) {
            // If any error occurs, catch it and provide a clear message.
            cameraInitialized = false;
            telemetry.addLine("ERROR: Could not initialize camera");
            telemetry.addData("Reason", e.getMessage());
            telemetry.addLine("Please check your configuration and camera connection.");
        }
    }

    /**
     * Handles gamepad input for changing the target AprilTag ID.
     */
    private void handleGamepadInput() {
        if (gamepad1.right_bumper) {
            targetTagId++;
            sleep(250); // Debounce to prevent rapid changes
        }
        if (gamepad1.left_bumper) {
            targetTagId = Math.max(0, targetTagId - 1); // Prevent negative IDs
            sleep(250); // Debounce
        }
    }

    /**
     * Loops through all detections and stores the data for our target tag.
     */
    private void findTargetTag() {
        targetTag = null; // Reset at the start of each loop
        List<AprilTagDetection> currentDetections = aprilTag.getDetections();

        for (AprilTagDetection detection : currentDetections) {
            if (detection.id == targetTagId) {
                targetTag = detection; // Found it!
                lastSeenTimer.reset();   // Reset the timer since we just saw it.
                break;                   // Exit the loop early.
            }
        }
    }

    /**
     * Displays all relevant information on the Driver Station's telemetry screen.
     */
    private void telemetryAprilTag() {
        telemetry.addLine("--- Camera and Controls ---");
        telemetry.addData("Camera Status", visionPortal.getCameraState());
        telemetry.addData("Target ID", "Use Bumpers to change (Current: %d)", targetTagId);
        telemetry.addLine();

        telemetry.addLine("--- Target Info ---");
        if (targetTag != null) {
            // If the target is visible, display its data.
            telemetry.addLine("TARGET VISIBLE!");
            // The ftcPose data gives us position and orientation relative to the camera.
            telemetry.addData("Position (X, Y, Z)", String.format(java.util.Locale.US, "%.2f, %.2f, %.2f (in)",
                    targetTag.ftcPose.x, targetTag.ftcPose.y, targetTag.ftcPose.z));
            telemetry.addData("Range", "%.2f in", targetTag.ftcPose.range);
            telemetry.addData("Bearing", "%.1f deg", targetTag.ftcPose.bearing); // Bearing is the left/right angle
            telemetry.addData("Yaw", "%.1f deg", targetTag.ftcPose.yaw);       // Yaw is the tag's rotation
        } else {
            // If the target is not visible, report it.
            telemetry.addLine("Target not visible.");
            // Also, report how long it's been since we last saw it.
            telemetry.addData("Last Seen", "%.1f seconds ago", lastSeenTimer.seconds());
        }
    }
}