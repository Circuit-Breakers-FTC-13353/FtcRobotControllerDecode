// Filename: BasicMecanumTeleOp.java
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/**
 * A basic, driver-controlled TeleOp for a mecanum drive robot.
 *
 * This OpMode follows the hardware abstraction model by using the 'Robot' class
 * to manage all hardware interactions. It reads joystick values from Gamepad 1
 * and translates them into drive commands.
 *
 * It also demonstrates the proper use of the `robot.update()` method, which is
 * required in every loop to run background tasks like stall detection.
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

    private Robot robot;

    @Override
    public void runOpMode() {
        // Instantiate the robot by passing it the hardwareMap from this OpMode.
        robot = new Robot(hardwareMap);
        Config.load(); // Load any overrides from the config file.

        // Initialize the robot's hardware.
        if (!robot.init()) {
            telemetry.addLine("ERROR: Hardware initialization failed. Please check configuration.");
            telemetry.update();
            while (opModeIsActive()) { sleep(20); }
            return;
        }

        telemetry.addLine("Robot initialized successfully. Ready to start.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // *** CRITICAL ***
            // ALWAYS call robot.update() at the start of your loop.
            // This updates all background systems, including the stall detector.
            robot.update();

            // --- DRIVETRAIN CONTROLS ---
            // Get joystick inputs. Note the negative sign on the y-axis.
            double forward = -gamepad1.left_stick_y;
            double strafe = gamepad1.left_stick_x;
            double turn = gamepad1.right_stick_x;

            // Call the high-level drive method on our robot object.
            robot.drive(forward, strafe, turn);


            // --- MECHANISM CONTROLS (EXAMPLE) ---
            // Example of using the stall detector for the arm.
            double armPower = -gamepad1.right_stick_y;

            if (robot.isArmStalled()) {
                // If the arm is stalled, stop the motor and alert the driver.
                robot.setArmPower(0);
                gamepad1.rumble(500); // Rumble for half a second.
            } else {
                // If not stalled, apply power normally.
                robot.setArmPower(armPower);
            }

            // Example of simple claw control.
            if (gamepad1.right_bumper) {
                robot.openClaw();
            } else if (gamepad1.left_bumper) {
                robot.closeClaw();
            }

            // --- TELEMETRY ---
            telemetry.addData("Arm Stalled", robot.isArmStalled());
            telemetry.update();
        }
    }
}