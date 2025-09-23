// Filename: BasicMecanumTeleOp.java
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/**
 * A basic, driver-controlled TeleOp for a mecanum drive robot.
 *
 * This OpMode follows the hardware abstraction model by using the 'Robot' class
 * to manage all hardware interactions. It reads joystick values from Gamepad 1
 * and translates them into drive commands (forward, strafe, turn), which are
 * then passed to the `robot.drive()` method. This keeps the TeleOp code clean
 * and focused on high-level logic.
 *
 * It also includes a robust initialization check to ensure all hardware is
 * configured correctly before the match starts.
 *
 * CONTROLS:
 * - Gamepad 1 Left Stick (Y-axis): Drive Forward/Backward
 * - Gamepad 1 Left Stick (X-axis): Strafe Left/Right
 * - Gamepad 1 Right Stick (X-axis): Turn Left/Right
 *
 * @author Team 13353
 */

@TeleOp(name = "Basic Mecanum TeleOp", group = "Competition")
public class BasicMecanumTeleOp extends LinearOpMode {

    // Create an instance of our Robot class.
    private Robot robot;

    @Override
    public void runOpMode() {
        // Instantiate the robot by passing it the hardwareMap from this OpMode.
        robot = new Robot(hardwareMap);

        // Initialize the robot's hardware.
        // The init() method returns a boolean, so we can check if it was successful.
        if (!robot.init()) {
            telemetry.addLine("ERROR: Hardware initialization failed. Please check configuration.");
            telemetry.update();
            // Wait for the OpMode to be stopped.
            while (opModeIsActive()) {
                sleep(20);
            }
            return; // Exit the OpMode
        }

        telemetry.addLine("Robot initialized successfully. Ready to start.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // Get joystick inputs. Note the negative sign on the y-axis, as
            // pushing forward on the stick gives a negative value.
            double forward = -gamepad1.left_stick_y;
            double strafe = gamepad1.left_stick_x;
            double turn = gamepad1.right_stick_x;

            // Call the high-level drive method on our robot object.
            robot.drive(forward, strafe, turn);

            // Add any other TeleOp logic here (e.g., controlling an arm).
            // telemetry.addData("Heading", robot.imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES));
            // telemetry.update();
        }
    }
}