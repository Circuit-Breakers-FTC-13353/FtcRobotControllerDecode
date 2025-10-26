// Filename: TeleOp_Stall_Example.java
package org.firstinspires.ftc.teamcode.draft;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Config;

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
    private ElapsedTime matchTimer = new ElapsedTime(); // <-- TIMER ADDED

    @Override
    public void runOpMode() {
        robot = new Robot(hardwareMap);
        Config.load();

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

        matchTimer.reset(); // Reset timer when match starts

        while (opModeIsActive()) {
            // *** THE CRITICAL FIX ***
            // 1. ALWAYS call robot.update() at the start of your loop,
            //    passing it the match timer.
            robot.update(matchTimer);

            // 2. Get the driver's input.
            double armPower = -gamepad1.right_stick_y;

            // 3. Check the robot's state BEFORE commanding the motors.
            if (robot.isArmStalled()) {
                robot.setArmPower(0);
                gamepad1.rumble(1.0, 1.0, 200);
            } else {
                robot.setArmPower(armPower);
            }

            // --- TELEMETRY ---
            telemetry.addData("Match Time", "%.1f s", matchTimer.seconds());
            telemetry.addData("Arm Power Command", "%.2f", armPower);
            telemetry.addData("Arm is Stalled", robot.isArmStalled());
            telemetry.addData("Stall Threshold", "%.2f A", robot.ARM_STALL_THRESHOLD_AMPS);
            telemetry.update();
        }
    }
}