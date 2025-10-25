// Filename: BasicMecanumTeleOp.java
package org.firstinspires.ftc.teamcode.draft;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

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
    private ElapsedTime matchTimer = new ElapsedTime(); // <-- TIMER ADDED

    @Override
    public void runOpMode() {
        robot = new Robot(hardwareMap);
        Config.load();

        if (!robot.init()) {
            telemetry.addLine("ERROR: Hardware initialization failed.");
            telemetry.update();
            while (opModeIsActive()) { sleep(20); }
            return;
        }

        telemetry.addLine("Robot initialized successfully. Ready to start.");
        telemetry.update();

        waitForStart();

        matchTimer.reset(); // Reset timer when match starts

        while (opModeIsActive()) {
            // *** THE CRITICAL FIX ***
            // ALWAYS call robot.update() at the start of your loop,
            // passing it the match timer.
            robot.update(matchTimer);

            // --- DRIVETRAIN CONTROLS ---
            double forward = -gamepad1.left_stick_y;
            double strafe = gamepad1.left_stick_x;
            double turn = gamepad1.right_stick_x;
            robot.drive(forward, strafe, turn);

            // --- MECHANISM CONTROLS (EXAMPLE) ---
            double armPower = -gamepad1.right_stick_y;

            if (robot.isArmStalled()) {
                robot.setArmPower(0);
                gamepad1.rumble(500);
            } else {
                robot.setArmPower(armPower);
            }

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