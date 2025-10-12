// Filename: Smart_Mecanum_TeleOp.java
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime; // <-- IMPORT ADDED
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

/**
 * This is the "Smart" TeleOp, the primary driver-controlled OpMode for competition.
 * ... (full Javadoc header) ...
 * @version 2.1 - Corrected `update()` call to include match timer.
 * @author Team 13353
 */
@TeleOp(name = "Smart Mecanum TeleOp", group = "Competition")
public class Smart_Mecanum_TeleOp extends LinearOpMode {

    private Robot robot;
    private boolean isArmManual = true;

    // A timer to track the match duration for logging purposes.
    private ElapsedTime matchTimer = new ElapsedTime();

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

        // Reset the timer as soon as the match starts
        matchTimer.reset();

        while (opModeIsActive()) {
            // *** THIS IS THE CRITICAL FIX ***
            // Pass the match timer to the update() method.
            robot.update(matchTimer);

            handleDriving();
            handleMechanisms();
            displayTelemetry();
        }

        // At the end of the match, save the log from the health monitor.
        if (robot.healthMonitor != null) {
            robot.healthMonitor.saveLogToFile();
        }
    }

    /**
     * Handles all the drivetrain logic.
     */
    private void handleDriving() {
        if (gamepad1.back) {
            robot.imu.resetYaw();
        }

        double forward = -gamepad1.left_stick_y;
        double strafe = gamepad1.left_stick_x;
        double turn = gamepad1.right_stick_x;

        double powerMultiplier = 1.0;
        if (gamepad1.right_trigger > 0.1) {
            powerMultiplier = Constants.DRIVE_SLOW_MODE_MULTIPLIER;
        }

        robot.driveFieldCentric(forward * powerMultiplier, strafe * powerMultiplier, turn * powerMultiplier);
    }

    /**
     * Handles all the logic for mechanisms.
     */
    private void handleMechanisms() {
        if (Math.abs(gamepad2.right_stick_y) > 0.1) {
            isArmManual = true;
        }

        if (gamepad2.y) {
            isArmManual = false;
            robot.setArmPosition(Constants.ARM_LIFT_POSITION);
            robot.scoreWrist();
            gamepad2.rumble(250);
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

        if (isArmManual) {
            double armPower = -gamepad2.right_stick_y;
            if (robot.isArmStalled()) {
                robot.setArmPower(0);
                gamepad2.rumble(1.0, 1.0, 500);
            } else {
                robot.setArmPower(armPower);
            }
        } else {
            if (robot.isArmStalled()) {
                robot.setArmPower(0);
                isArmManual = true;
                gamepad2.rumble(1.0, 1.0, 500);
            }
        }

        if (gamepad2.right_bumper) {
            robot.openClaw();
        } else if (gamepad2.left_bumper) {
            robot.closeClaw();
        }
    }

    /**
     * Displays relevant data on the Driver Station.
     */
    private void displayTelemetry() {
        telemetry.addData("Robot Heading", "%.2f deg", robot.imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES));
        telemetry.addData("Drive Mode", gamepad1.right_trigger > 0.1 ? "SLOW" : "NORMAL");
        telemetry.addLine();
        telemetry.addData("Arm Mode", isArmManual ? "MANUAL" : "AUTOMATIC");
        telemetry.addData("Arm Position", robot.getArmPosition());
        telemetry.addData("Arm Stalled", robot.isArmStalled());

        // Add the health monitor log to the telemetry
        telemetry.addLine();
        telemetry.addLine("--- System Health Log ---");
        if (robot.healthMonitor != null) {
            for (String logEntry : robot.healthMonitor.getEventLog()) {
                telemetry.addLine(logEntry);
            }
        }

        telemetry.update();
    }
}