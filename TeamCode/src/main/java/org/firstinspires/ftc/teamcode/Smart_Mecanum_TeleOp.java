// Filename: Smart_Mecanum_TeleOp.java
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.draft.Config;
import org.firstinspires.ftc.teamcode.draft.Constants;

/**
 * =================================================================================================
 * SMART MECANUM TELEOP - COMPETITION DRIVER-CONTROLLED PROGRAM (DUAL DRIVE MODE)
 * =================================================================================================
 *
 * This OpMode is the primary driver-controlled program for a mecanum drive robot,
 * designed with advanced features for competitive play in the FIRST Tech Challenge (FTC).
 *
 * It features a toggleable drive mode, allowing the driver to switch between Field-Centric
 * and Robot-Centric control on the fly. This provides strategic versatility and a crucial
 * failsafe in case of IMU sensor failure.
 *
 * Key Features:
 * - Dual Drive Modes: Switch between Field-Centric and Robot-Centric driving.
 *   - Field-Centric: Intuitive control relative to the field. Best for navigation and scoring.
 *   - Robot-Centric: Direct control relative to the robot. Best for defense and pushing.
 * - Health Monitoring System: Logs important events and saves them for later analysis.
 * - Slow Mode: A precision driving mode for fine-tuned adjustments.
 * - Hardware Abstraction: Uses a `RobotMecanum` class for cleaner, more maintainable code.
 *
 * -------------------------------------------------------------------------------------------------
 * INSTRUCTIONS FOR HARDWARE CONFIGURATION
 * -------------------------------------------------------------------------------------------------
 *
 * To use this code, you MUST configure your `RobotMecanum.java` file correctly.
 *
 * 1.  **Motor Names:**
 *     - In `RobotMecanum.java`, change the string names (e.g., "frontLeft") in the
 *       `hardwareMap.get()` calls to match your robot's configuration.
 *
 * 2.  **IMU (Inertial Measurement Unit) Name:**
 *     - The IMU is CRITICAL for Field-Centric driving.
 *     - In `RobotMecanum.java`, ensure the name in `hardwareMap.get(IMU.class, "imu")`
 *       matches your configuration.
 *
 * 3.  **Robot-Centric Drive Method:**
 *     - This code assumes your `RobotMecanum.java` class has a method for robot-centric driving.
 *       Here, it is called `driveRobotCentric()`. If your method is named differently (e.g., `drive()`),
 *       you must change the method call in the `handleDriving()` section below.
 *
 * -------------------------------------------------------------------------------------------------
 * CONTROLLER LAYOUT (GAMEPAD 1 - DRIVER)
 * -------------------------------------------------------------------------------------------------
 *
 * - Left Stick (Y-axis):    Drive Forward / Backward
 * - Left Stick (X-axis):    Strafe Left / Right
 * - Right Stick (X-axis):   Turn Left / Right
 * - Right Trigger:          Hold for Slow Mode (precision driving)
 * - Back Button:            Reset IMU yaw (re-calibrates "forward" for Field-Centric mode)
 * - X Button:               TOGGLE between Field-Centric and Robot-Centric drive modes
 *
 * @version 3.0 - Added toggleable Field-Centric and Robot-Centric drive modes.
 * @author Team 13353
 */
@TeleOp(name = "Smart Mecanum TeleOp (Dual Drive)", group = "Competition")
public class Smart_Mecanum_TeleOp extends LinearOpMode {

    // The robot hardware abstraction object.
    private RobotMecanum robot;
    private final ElapsedTime matchTimer = new ElapsedTime();

    // --- VARIABLES FOR DUAL-MODE DRIVING ---
    // State variable to track the current drive mode. Defaults to Field-Centric.
    private boolean isFieldCentric = true;

    // State variable to ensure the drive mode toggles only once per button press.
    private boolean xWasPressed = false;

    // State variable that was intended for switching between manual and automatic arm control.
    // It remains here for future implementation.
    private boolean isArmManual = true;

    @Override
    public void runOpMode() throws InterruptedException {
        // Initialize the robot hardware object.
        robot = new RobotMecanum(hardwareMap);
        Config.load();

        // Attempt to initialize the robot's hardware. If it fails, report an error and stop.
        if (!robot.init()) {
            telemetry.addLine("ERROR: Hardware initialization failed. Please check configuration.");
            telemetry.update();
            while (opModeIsActive()) { sleep(20); }
            return;
        }

        // Signal to the drivers that initialization was successful.
        telemetry.addLine("Smart TeleOp Initialized. Ready for battle!");
        telemetry.update();

        waitForStart();
        matchTimer.reset();

        // The main OpMode loop.
        while (opModeIsActive()) {
            // This critical method call handles background tasks in the RobotMecanum class.
            robot.update(matchTimer);

            // Handle all driver-related controls.
            handleDriving();

            // Handle all operator-related controls for mechanisms.
            handleMechanisms();

            // Display relevant information on the Driver Station.
            displayTelemetry();
        }

        // After the match, save the health monitor log.
        if (robot.healthMonitor != null) {
            robot.healthMonitor.saveLogToFile();
        }
    }

    /**
     * Handles all the drivetrain logic, including the mode-switching functionality.
     */
    private void handleDriving() {
        // --- DRIVE MODE TOGGLE LOGIC ---
        // This "debounce" logic ensures that one press of the 'X' button toggles the mode
        // exactly once, preventing flickering if the button is held down.
        if (gamepad1.x && !xWasPressed) {
            isFieldCentric = !isFieldCentric; // Flip the boolean state.
        }
        xWasPressed = gamepad1.x; // Update the button's state for the next loop iteration.

        // Reset the IMU's yaw angle if the 'back' button is pressed. This is crucial
        // for maintaining an accurate "forward" direction in Field-Centric mode.
        if (gamepad1.back) {
            robot.imu.resetYaw();
        }

        // Read joystick values. The Y-axis is inverted.
        double forward = -gamepad1.left_stick_y;
        double strafe = gamepad1.left_stick_x;
        double turn = gamepad1.right_stick_x;

        // Implement a "slow mode" for precision driving.
        double powerMultiplier = 1.0; // Full power by default.
        if (gamepad1.right_trigger > 0.1) {
            powerMultiplier = Constants.DRIVE_SLOW_MODE_MULTIPLIER;
        }

        // --- CONDITIONAL DRIVE METHOD CALL ---
        // Based on the 'isFieldCentric' variable, call the appropriate drive method from the RobotMecanum class.
        if (isFieldCentric) {
            robot.driveFieldCentric(forward * powerMultiplier, strafe * powerMultiplier, turn * powerMultiplier);
        } else {
            // Assumes your RobotMecanum class has a method named 'driveRobotCentric'.
            // If your method has a different name (like 'drive'), change it here.
            robot.driveRobotCentric(forward * powerMultiplier, strafe * powerMultiplier, turn * powerMultiplier);
        }
    }

    /**
     * Handles all the logic for mechanisms.
     * This section is a placeholder for future development of mechanism controls.
     */
    private void handleMechanisms() {
        if (Math.abs(gamepad2.right_stick_y) > 0.1) {
            isArmManual = true;
        }
        // TODO: Add all controls for Gamepad 2 (the operator controller) here.
    }

    /**
     * Displays relevant data on the Driver Station's telemetry screen.
     */
    private void displayTelemetry() {
        // Display the current drive mode. Crucial feedback for the driver.
        telemetry.addData("DRIVE MODE", isFieldCentric ? "FIELD-CENTRIC" : "ROBOT-CENTRIC");

        // Display the robot's current heading from the IMU.
        telemetry.addData("Robot Heading", "%.2f deg", robot.imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES));

        // Show the current speed mode (Normal or Slow).
        telemetry.addData("Speed Mode", gamepad1.right_trigger > 0.1 ? "SLOW" : "NORMAL");
        telemetry.addLine();

        // Display the live log from the health monitor.
        telemetry.addLine("--- System Health Log ---");
        if (robot.healthMonitor != null) {
            for (String logEntry : robot.healthMonitor.getEventLog()) {
                telemetry.addLine(logEntry);
            }
        }

        // Pushes all the data to the Driver Station screen.
        telemetry.update();
    }
}