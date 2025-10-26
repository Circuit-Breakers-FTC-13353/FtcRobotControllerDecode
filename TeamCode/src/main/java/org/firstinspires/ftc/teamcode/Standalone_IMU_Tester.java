// Filename: Standalone_IMU_Tester.java
package org.firstinspires.ftc.teamcode.draft;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.IMU;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

/**
 * =================================================================================================
 * STANDALONE IMU TESTER UTILITY
 * =================================================================================================
 *
 * Purpose:
 * This OpMode is a dedicated diagnostic tool for testing and calibrating the robot's
 * Inertial Measurement Unit (IMU). An accurately configured IMU is essential for features like
 * field-centric driving and precise autonomous turns. This tool helps you quickly verify that
 * your IMU is working correctly.
 *
 * Key Features:
 * - Live Telemetry: Displays real-time Yaw, Pitch, and Roll angles.
 * - Visual Heading Indicator: Shows a simple text-based compass direction for Yaw.
 * - Interactive Yaw Reset: Allows you to reset the robot's heading using a gamepad button.
 * - Robust Error Handling: Clearly reports if the IMU is not found in the configuration.
 * - Self-Documenting: All instructions are provided on the Driver Station screen.
 *
 * -------------------------------------------------------------------------------------------------
 * HOW TO USE THIS TOOL - THE "CALIBRATION DANCE"
 * -------------------------------------------------------------------------------------------------
 *
 * 1.  **CONFIGURE THE HUB ORIENTATION:**
 *     - Open this file and scroll down to the "CONFIGURE YOUR HUB ORIENTATION HERE" section.
 *     - Change the `logoDirection` and `usbDirection` to match how your Control/Expansion Hub
 *       is physically mounted on the robot. This is the most important step.
 *
 * 2.  **RUN THE OPMODE:**
 *     - Deploy the code and run "Standalone: IMU Tester" from the Driver Station.
 *
 * 3.  **VERIFY YAW (HEADING):**
 *     - Place the robot on the floor, facing what you consider "forward."
 *     - Press (Y) on Gamepad 1 to reset the Yaw. The Yaw should now read ~0.0 degrees and "(Forward)".
 *     - **Turn the robot 90 degrees LEFT.** The Yaw should read approximately **+90** degrees.
 *     - **Turn the robot 90 degrees RIGHT** (from the start). The Yaw should read approximately **-90** degrees.
 *     - If your left/right values are inverted, your hub orientation in the code is likely incorrect.
 *
 * 4.  **VERIFY PITCH:**
 *     - From a level start, lift the **front** of your robot up. The Pitch value should change.
 *
 * 5.  **VERIFY ROLL:**
 *     - From a level start, tilt the robot to its **left** side. The Roll value should change.
 *
 * By following these steps, you can be confident that your IMU is configured correctly for
 * both TeleOp and Autonomous modes.
 *
 * @version 2.0 - Added visual heading indicator, non-blocking debounce, and full documentation.
 */
@TeleOp(name = "Standalone: IMU Tester", group = "Standalone Tools")
public class Standalone_IMU_Tester extends LinearOpMode {

    private IMU imu;

    // A variable to handle non-blocking debounce for the Yaw reset button.
    private boolean yWasPressed = false;

    @Override
    public void runOpMode() throws InterruptedException {
        telemetry.addLine("IMU Tester Initializing...");
        telemetry.update();

        try {
            // Retrieve the IMU from the hardware map.
            imu = hardwareMap.get(IMU.class, "imu");

            // ====================================================================================
            // --- CONFIGURE YOUR HUB ORIENTATION HERE ---
            // ====================================================================================
            // This is the most important step. Change these values to match the physical
            // orientation of your Control Hub on the robot.
            //
            // For example, if the REV logo is facing UP and the USB ports are facing FORWARD:
            RevHubOrientationOnRobot.LogoFacingDirection logoDirection = RevHubOrientationOnRobot.LogoFacingDirection.UP;
            RevHubOrientationOnRobot.UsbFacingDirection usbDirection = RevHubOrientationOnRobot.UsbFacingDirection.FORWARD;
            // ------------------------------------------------------------------------------------

            RevHubOrientationOnRobot orientationOnRobot = new RevHubOrientationOnRobot(logoDirection, usbDirection);
            imu.initialize(new IMU.Parameters(orientationOnRobot));

        } catch (Exception e) {
            // If the IMU is not found, report a clear error message and stop.
            telemetry.addLine();
            telemetry.addLine("!!! IMU NOT FOUND IN CONFIGURATION !!!");
            telemetry.addData("Error", e.getMessage());
            telemetry.update();

            // Wait forever, since the tool is useless without the IMU.
            while(opModeIsActive()) { sleep(100); }
            return;
        }

        telemetry.addLine("IMU Initialized Successfully.");
        telemetry.addLine("\nPerform the 'Calibration Dance' as described in the code comments.");
        telemetry.addLine("Press START to begin.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // --- YAW RESET LOGIC ---
            // This is a non-blocking debounce. It ensures that one press of the 'Y' button
            // resets the Yaw exactly once, without freezing the OpMode with a `sleep()` call.
            if (gamepad1.y && !yWasPressed) {
                imu.resetYaw();
            }
            yWasPressed = gamepad1.y; // Update the button's state for the next loop iteration.


            // --- TELEMETRY DISPLAY ---
            // Get the current orientation angles from the IMU.
            YawPitchRollAngles orientation = imu.getRobotYawPitchRollAngles();
            double yawDegrees = orientation.getYaw(AngleUnit.DEGREES);

            // Clear the screen to prevent clutter.
            telemetry.clearAll();
            telemetry.addLine("--- Standalone IMU Tester ---");
            telemetry.addLine();
            telemetry.addLine("Physically move the robot/hub to see values change.");
            telemetry.addLine("Press (Y) on Gamepad 1 to reset Yaw to 0.");
            telemetry.addLine();

            // Display the Yaw, Pitch, and Roll with a visual indicator for Yaw.
            telemetry.addData("Yaw (Heading)", "%.2f degrees %s", yawDegrees, getYawDirection(yawDegrees));
            telemetry.addData("Pitch", "%.2f degrees", orientation.getPitch(AngleUnit.DEGREES));
            telemetry.addData("Roll", "%.2f degrees", orientation.getRoll(AngleUnit.DEGREES));

            // Provide feedback after a Yaw reset.
            if (yWasPressed) {
                telemetry.addLine("\n--> YAW RESET! <--");
            }

            // Update the telemetry on the Driver Station.
            telemetry.update();
        }
    }

    /**
     * A helper method to provide a simple, text-based visual indicator for the Yaw angle.
     * @param yawDegrees The robot's current Yaw (heading) in degrees.
     * @return A string representing the general direction (e.g., "(Forward)", "(Right)").
     */
    private String getYawDirection(double yawDegrees) {
        if (yawDegrees >= -22.5 && yawDegrees < 22.5) return "(Forward)";
        if (yawDegrees >= 22.5 && yawDegrees < 67.5) return "(Forward-Left)";
        if (yawDegrees >= 67.5 && yawDegrees < 112.5) return "(Left)";
        if (yawDegrees >= 112.5 && yawDegrees < 157.5) return "(Back-Left)";
        if (yawDegrees >= 157.5 || yawDegrees < -157.5) return "(Back)";
        if (yawDegrees >= -157.5 && yawDegrees < -112.5) return "(Back-Right)";
        if (yawDegrees >= -112.5 && yawDegrees < -67.5) return "(Right)";
        if (yawDegrees >= -67.5 && yawDegrees < -22.5) return "(Forward-Right)";
        return "(Unknown)";
    }
}