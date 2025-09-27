package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

/**
 * This is the "Smart" TeleOp, the primary driver-controlled OpMode for competition.
 *
 * It leverages our robust Robot class to provide advanced, semi-automated controls
 * that are faster, more reliable, and less prone to human error than simple
 * direct control.
 *
 * V2 FEATURES:
 * - "Slow Mode" for the drivetrain, activated by holding the right trigger.
 * - Haptic (rumble) feedback to the operator for successful preset commands.
 *
 * CORE FEATURES:
 * - Field-Centric Driving for intuitive control.
 * - Preset Mechanism Positions for fast and repeatable scoring.
 * - Real-Time Stall Protection with driver feedback.
 * - Manual Override for all automated movements.
 *
 * @author Team 13353
 */
@TeleOp(name = "Smart Mecanum TeleOp", group = "Competition")
public class Smart_Mecanum_TeleOp extends LinearOpMode {

    private Robot robot;
    private boolean isArmManual = true;

    @Override
    public void runOpMode() throws InterruptedException {
        robot = new Robot(hardwareMap);
        Config.load();

        if (!robot.init()) {
            // Handle initialization failure
            telemetry.addLine("ERROR: Hardware initialization failed. Please check configuration.");
            telemetry.update();
            while (opModeIsActive()) { sleep(20); }
            return;
        }

        telemetry.addLine("Smart TeleOp Initialized. Ready for battle!");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            robot.update();
            handleDriving();
            handleMechanisms();
            displayTelemetry();
        }
    }

    /**
     * Handles all drivetrain logic, including field-centric control,
     * IMU reset, and the new "Slow Mode".
     */
    private void handleDriving() {
        if (gamepad1.back) {
            robot.imu.resetYaw();
        }

        double forward = -gamepad1.left_stick_y;
        double strafe = gamepad1.left_stick_x;
        double turn = gamepad1.right_stick_x;

        // --- SLOW MODE ---
        // If the right trigger is held down, scale down the drive powers.
        double powerMultiplier = 1.0;
        if (gamepad1.right_trigger > 0.1) {
            powerMultiplier = Constants.DRIVE_SLOW_MODE_MULTIPLIER;
        }

        // Apply the power multiplier to the joystick inputs.
        forward *= powerMultiplier;
        strafe *= powerMultiplier;
        turn *= powerMultiplier;

        robot.driveFieldCentric(forward, strafe, turn);
    }

    /**
     * Handles all logic for the arm, wrist, and claw, including presets,
     * stall protection, manual override, and haptic feedback.
     */
    private void handleMechanisms() {
        // --- MANUAL OVERRIDE CHECK ---
        if (Math.abs(gamepad2.right_stick_y) > 0.1) {
            isArmManual = true;
        }

        // --- PRESET CONTROLS ---
        if (gamepad2.y) {
            isArmManual = false;
            robot.setArmPosition(Constants.ARM_LIFT_POSITION);
            robot.scoreWrist();
            gamepad2.rumble(250); // Haptic feedback for success
        } else if (gamepad2.a) {
            isArmManual = false;
            robot.setArmPosition(Constants.ARM_INTAKE_POSITION);
            robot.stowWrist();
            gamepad2.rumble(250);
        } else if (gamepad2.b) {
            isArmManual = false;
            robot.setArmPosition(Constants.ARM_CARRY_POSITION);
            robot.stowWrist();
            gamepad2.rumble(250);
        }

        // --- MANUAL ARM CONTROL ---
        if (isArmManual) {
            double armPower = -gamepad2.right_stick_y;
            if (robot.isArmStalled()) {
                robot.setArmPower(0);
                gamepad2.rumble(1.0, 1.0, 500); // Long rumble for stall warning
            } else {
                robot.setArmPower(armPower);
            }
        } else {
            // In automatic mode, check for stalls to switch back to manual.
            if (robot.isArmStalled()) {
                robot.setArmPower(0);

                isArmManual = true; // IMPORTANT: Give control back to driver
                gamepad2.rumble(1.0, 1.0, 500);
            }
        }

        // --- CLAW CONTROL ---
        if (gamepad2.right_bumper) {
            robot.openClaw();
        } else if (gamepad2.left_bumper) {
            robot.closeClaw();
        }
    }

    /**
     * Displays relevant data on the Driver Station for diagnostics.
     */
    private void displayTelemetry() {
        telemetry.addData("Robot Heading", "%.2f deg", robot.imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES));
        telemetry.addData("Drive Mode", gamepad1.right_trigger > 0.1 ? "SLOW" : "NORMAL");
        telemetry.addLine();
        telemetry.addData("Arm Mode", isArmManual ? "MANUAL" : "AUTOMATIC");
        telemetry.addData("Arm Position", robot.getArmPosition());
        telemetry.addData("Arm Stalled", robot.isArmStalled());
        telemetry.update();
    }
}