// Filename: TeleOp_Stall_Example.java
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/**
 * A standalone example demonstrating the real-time stall detection system.
 *
 * This OpMode provides a simple interface to control a single motor (the arm)
 * and shows how the `robot.update()` and `robot.isArmStalled()` methods work
 * together to provide feedback (controller rumble) and protect the motor.
 *
 * This serves as a clear blueprint for implementing stall protection in more
 * complex competition OpModes.
 *
 * @author Team 13353
 */
@TeleOp(name = "Example: Stall Detection", group = "Examples")
public class TeleOp_Stall_Example extends LinearOpMode {

    private Robot robot;

    @Override
    public void runOpMode() {
        // Pass the hardwareMap to the constructor.
        robot = new Robot(hardwareMap);
        Config.load();

        // Call init() with NO arguments.
        if (!robot.init()) {
            telemetry.addLine("ERROR: Robot initialization failed.");
            telemetry.update();
            while (opModeIsActive()) { sleep(20); }
            return;
        }

        telemetry.addLine("Stall Detection Example Initialized.");
        telemetry.addLine("Use the Right Stick Y to control the arm.");
        telemetry.addLine("Hold the arm physically to trigger a stall.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // *** THE CORE LOGIC ***

            // 1. ALWAYS call robot.update() at the start of your loop.
            // This updates all background systems, including the stall detector.
            robot.update();

            // 2. Get the driver's input.
            double armPower = -gamepad1.right_stick_y;

            // 3. Check the robot's state BEFORE commanding the motors.
            if (robot.isArmStalled()) {
                // The stall detector has been tripped.
                // TAKE PROTECTIVE ACTION:
                // a) Stop the motor.
                robot.setArmPower(0);
                // b) Alert the driver.
                gamepad1.rumble(1.0, 1.0, 200); // Vibrate controller for 200ms
            } else {
                // If not stalled, operate normally.
                robot.setArmPower(armPower);
            }

            // --- TELEMETRY ---
            telemetry.addData("Arm Power Command", "%.2f", armPower);
            telemetry.addData("Arm is Stalled", robot.isArmStalled());
            telemetry.addData("Stall Threshold", "%.2f A", robot.ARM_STALL_THRESHOLD_AMPS);
            telemetry.update();
        }
    }
}