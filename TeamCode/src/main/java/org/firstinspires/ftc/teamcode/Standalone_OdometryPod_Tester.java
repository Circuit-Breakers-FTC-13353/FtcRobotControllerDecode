// Filename: Standalone_OdometryPod_Tester.java
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * =================================================================================================
 * STANDALONE ODOMETRY POD TESTER
 * =================================================================================================
 *
 * Purpose:
 * This OpMode is a dedicated diagnostic tool for testing the three unpowered ("dead wheel")
 * odometry pod encoders. Accurate odometry is the foundation for any high-level autonomous,
 * but it can be notoriously difficult to debug. This tool helps you verify that your hardware
 * is configured and behaving correctly *before* you write any complex localization code.
 *
 * It helps you answer the most common questions:
 * 1.  Are my encoders correctly named in the configuration?
 * 2.  Are they plugged into the correct ports and functioning?
 * 3.  Are my parallel encoders counting in the same direction when I push the robot forward?
 *
 * -------------------------------------------------------------------------------------------------
 * HOW TO USE THIS TOOL
 * -------------------------------------------------------------------------------------------------
 *
 * 1.  **CONFIGURE YOUR ENCODER NAMES:**
 *     - Scroll down to the "CONFIGURATION" section in this file.
 *     - Change the string names to match the names of your odometry encoders in the Robot
 *       Controller's configuration.
 *
 * 2.  **RUN THE OPMODE:**
 *     - Deploy the code and run "Standalone: Odometry Pod Tester" from the Driver Station.
 *
 * 3.  **PERFORM THE FORWARD TEST (CRITICAL):**
 *     - Place the robot on the floor and press (Y) on Gamepad 1 to reset the encoders to zero.
 *     - **Manually push the robot straight FORWARD about one floor tile.**
 *     - **Observe the Telemetry:** The `Left Encoder` and `Right Encoder` values should both be
 *       POSITIVE and roughly equal to each other.
 *     - **IF ONE IS NEGATIVE:** That encoder's direction is reversed! You must reverse it in
 *       your localization code (e.g., by multiplying its value by -1).
 *
 * 4.  **PERFORM THE STRAFE TEST:**
 *     - Press (Y) to reset the encoders again.
 *     - **Manually push the robot straight to the LEFT about one floor tile.**
 *     - **Observe the Telemetry:** The `Perpendicular Encoder` value should change significantly.
 *       (The direction, positive or negative, will depend on its mounting).
 *
 * By following these steps, you can be confident that your odometry hardware is ready for use.
 *
 * @version 2.0 - Added critical directionality checks, non-blocking debounce, and full documentation.
 */
@TeleOp(name = "Standalone: Odometry Pod Tester", group = "Standalone Tools")
public class Standalone_OdometryPod_Tester extends LinearOpMode {

    // --- CONFIGURATION ---
    // IMPORTANT: Change these string values to match the names of your odometry encoders
    // in the Robot Controller's configuration file.
    private final String LEFT_ENCODER_NAME = "leftFront";        // The left parallel pod
    private final String RIGHT_ENCODER_NAME = "rightFront";       // The right parallel pod
    private final String PERPENDICULAR_ENCODER_NAME = "leftRear"; // The perpendicular (strafe) pod

    private DcMotor leftEncoder, rightEncoder, perpendicularEncoder;

    // A variable to handle non-blocking debounce for the reset button.
    private boolean yWasPressed = false;

    @Override
    public void runOpMode() throws InterruptedException {
        telemetry.addLine("Odometry Pod Tester Initializing...");
        telemetry.update();

        try {
            // Retrieve the DcMotor objects from the hardware map.
            // Encoders are read through DcMotor objects.
            leftEncoder = hardwareMap.get(DcMotor.class, LEFT_ENCODER_NAME);
            rightEncoder = hardwareMap.get(DcMotor.class, RIGHT_ENCODER_NAME);
            perpendicularEncoder = hardwareMap.get(DcMotor.class, PERPENDICULAR_ENCODER_NAME);

            // This is CRITICAL. It resets the encoders and prepares them for reading.
            resetEncoders();

        } catch (Exception e) {
            // If an encoder is not found, report a clear error and stop.
            telemetry.addLine("\n!!! AN ODOMETRY ENCODER WAS NOT FOUND !!!");
            telemetry.addData("Error", e.getMessage());
            telemetry.addLine("\nPlease check the configuration names in the code and on the Robot Controller.");
            telemetry.update();
            while(opModeIsActive()) { sleep(100); } // Wait forever
            return;
        }

        telemetry.addLine("Odometry Pods Initialized and Reset.");
        telemetry.addLine("\nSee code comments for testing instructions.");
        telemetry.addLine("Press START to begin.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // --- NON-BLOCKING DEBOUNCE FOR RESET ---
            // This ensures one press of the 'Y' button resets the encoders exactly once.
            if (gamepad1.y && !yWasPressed) {
                resetEncoders();
            }
            yWasPressed = gamepad1.y; // Update the button's state for the next loop.

            // --- TELEMETRY ---
            telemetry.clearAll();
            telemetry.addLine("--- Standalone Odometry Pod Tester ---");
            telemetry.addLine("Manually push the robot and observe the values.");
            telemetry.addLine("Press (Y) on Gamepad 1 to RESET all encoders.");
            telemetry.addLine();

            // Display the live raw tick counts from each encoder.
            telemetry.addData("Left Encoder (Parallel)", leftEncoder.getCurrentPosition());
            telemetry.addData("Right Encoder (Parallel)", rightEncoder.getCurrentPosition());
            telemetry.addData("Perpendicular Encoder (Strafe)", perpendicularEncoder.getCurrentPosition());
            telemetry.addLine();

            // Display the critical instructions for the directionality check.
            telemetry.addLine("--- DIRECTIONALITY CHECK ---");
            telemetry.addLine("ACTION: Push robot FORWARD one tile.");
            telemetry.addLine("RESULT: Left and Right should be POSITIVE and roughly equal.");
            telemetry.addLine("  (If one is negative, you must REVERSE it in your code!)");
            telemetry.addLine();
            telemetry.addLine("ACTION: Push robot LEFT one tile.");
            telemetry.addLine("RESULT: Perpendicular should change significantly.");

            // Provide feedback to the user when a reset occurs.
            if (yWasPressed) {
                telemetry.addLine("\n--> Encoders Reset! <--");
            }

            telemetry.update();
        }
    }

    /**
     * A helper method to perform the full reset sequence for all three encoders.
     * This sequence is required to zero out the encoders and set them to the correct mode for reading.
     */
    private void resetEncoders() {
        // Step 1: Stop the motor's velocity and reset the tick count to zero.
        leftEncoder.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightEncoder.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        perpendicularEncoder.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        // Step 2: Set the motor to a mode that allows us to read the encoder values
        // without actually applying power to the motor.
        leftEncoder.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightEncoder.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        perpendicularEncoder.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }
}