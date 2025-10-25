//Filename: Ultimate_Diagnostics.java
package org.firstinspires.ftc.teamcode.draft;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * CoreDiagnostics - The definitive, interactive pre-match diagnostics tool.
 *
 * This OpMode provides a unified dashboard to verify the robot's core systems,
 * fulfilling the requirements of items 1.4, 1.5, and 1.6 from the roadmap. It
 * emphasizes failsafe, interactive checks to ensure all hardware is not only
 * configured but also actively working.
 *
 * Standard Operating Procedure (SOP):
 * Run this OpMode before every match.
 * 1. Glance at the "SYSTEM HEALTH" summary. If all is [ OK ], you are ready.
 * 2. If any system shows an error, scroll to its detailed section.
 * 3. Follow all on-screen "ACTION" prompts (wiggle sticks, move robot, etc.)
 *    to perform liveness checks.
 * 4. Press (Y) on Gamepad 1 to reset the IMU heading to zero before starting.
 *
 * @author Team 13353
 */
@TeleOp(name = "Ultimate: Diagnostics", group = "1-Diagnostics")
public class Ultimate_Diagnostics extends LinearOpMode {

    private Robot robot;

    // This enum is correctly defined inside the class that uses it.
    private enum SystemStatus {
        OK("[ OK ]"),
        WARNING("[WARN]"),
        ERROR("[ERROR]"),
        DISCONNECTED("[DISCONNECTED]");

        private final String representation;
        SystemStatus(String representation) { this.representation = representation; }
        @Override public String toString() { return representation; }
    }

    @Override
    public void runOpMode() {
        robot = new Robot(hardwareMap);

        telemetry.addLine("Core Diagnostics Initialized.");
        telemetry.addLine("Press START to run hardware initialization.");
        telemetry.update();

        waitForStart();

        boolean initSuccess = robot.init();

        while (opModeIsActive()) {
            telemetry.clearAll();
            if (!initSuccess) {
                telemetry.addLine("!!! HARDWARE INITIALIZATION FAILED !!!");
                telemetry.addLine("Check robot configuration and physical connections.");
                telemetry.update();
                continue;
            }

            SystemStatus controllerStatus = checkControllers();
            SystemStatus imuStatus = checkImu();
            SystemStatus sensorStatus = checkSensors();

            displaySummary(controllerStatus, imuStatus, sensorStatus);
            displayControllerDetails();
            displayImuDetails();
            displaySensorDetails();
            telemetry.update();
        }
    }

    private void displaySummary(SystemStatus controllerStatus, SystemStatus imuStatus, SystemStatus sensorStatus) {
        telemetry.addLine("--- SYSTEM HEALTH ---");
        telemetry.addData("CONTROLLERS", controllerStatus.toString());
        telemetry.addData("IMU", imuStatus.toString());
        telemetry.addData("SENSORS", sensorStatus.toString());
        telemetry.addLine("\n----------------------------\n");
    }

    private SystemStatus checkControllers() {
        return (gamepad1.getGamepadId() == -1 || gamepad2.getGamepadId() == -1) ? SystemStatus.DISCONNECTED : SystemStatus.OK;
    }

    private void displayControllerDetails() {
        telemetry.addLine("--- Controllers (1.6) ---");
        telemetry.addData("G1 Status", gamepad1.getGamepadId() == -1 ? SystemStatus.DISCONNECTED.toString() : SystemStatus.OK.toString());
        telemetry.addData("G2 Status", gamepad2.getGamepadId() == -1 ? SystemStatus.DISCONNECTED.toString() : SystemStatus.OK.toString());
        telemetry.addLine("\nACTION: Wiggle all sticks and press buttons to verify.");
        telemetry.addData("G1 Left Stick", "X: %.2f, Y: %.2f", gamepad1.left_stick_x, -gamepad1.left_stick_y);
    }

    private SystemStatus checkImu() {
        return (robot.imu == null) ? SystemStatus.ERROR : SystemStatus.OK;
    }

    private void displayImuDetails() {
        telemetry.addLine("\n--- IMU & Heading (1.4) ---");
        if (checkImu() == SystemStatus.ERROR) {
            telemetry.addData("IMU Status", SystemStatus.ERROR + " (Not configured in Robot.java)");
            return;
        }

        telemetry.addData("IMU Status", SystemStatus.OK.toString());
        telemetry.addData("Robot Heading", "%.2f degrees", robot.imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES));
        telemetry.addLine("\nACTION: Physically rotate the robot. Does the heading value change?");
        telemetry.addLine("ACTION: Press (Y) on Gamepad 1 to reset heading.");
        if (gamepad1.y) {
            robot.imu.resetYaw();
            telemetry.addLine("--> HEADING RESET! <--");
        }
    }

    private SystemStatus checkSensors() {
        boolean hasError = robot.leftFront == null ||
                robot.rightFront == null ||
                robot.leftRear == null ||
                robot.rightRear == null ||
                robot.armMotor == null ||
                robot.clawServo == null ||
                robot.wristServo == null ||
                robot.frontDistanceSensor == null;
        // The compile error was because the enum was not correctly included in the
        // file you sent. In this complete file, the enum is present and this line is valid.
        return hasError ? SystemStatus.ERROR : SystemStatus.OK;
    }

    private void displaySensorDetails() {
        telemetry.addLine("\n--- Sensor Dashboard (1.5) ---");
        telemetry.addLine("ACTION: Manually move each part. Does the value change?");

        displayEncoderStatus("Left Front", robot.leftFront);
        displayEncoderStatus("Right Front", robot.rightFront);
        displayEncoderStatus("Left Rear", robot.leftRear);
        displayEncoderStatus("Right Rear", robot.rightRear);
        displayEncoderStatus("Arm Motor", robot.armMotor);

        displayServoStatus("Claw Servo", robot.clawServo);
        displayServoStatus("Wrist Servo", robot.wristServo);

        if (robot.frontDistanceSensor == null) {
            telemetry.addData("Front Distance", SystemStatus.ERROR + " (Not configured)");
        } else {
            double distance = robot.getFrontDistance(DistanceUnit.INCH);
            String status = (distance >= 1000 || distance <= 0) ? SystemStatus.WARNING.toString() : SystemStatus.OK.toString();
            telemetry.addData("Front Distance", status + " | Range: %.2f in", distance);
        }
    }

    private void displayEncoderStatus(String name, DcMotor motor) {
        String status = (motor == null) ? SystemStatus.ERROR.toString() : SystemStatus.OK.toString();
        int position = (motor == null) ? 0 : motor.getCurrentPosition();
        telemetry.addData(name + " Encoder", status + " | Pos: %d", position);
    }

    private void displayServoStatus(String name, Servo servo) {
        String status = (servo == null) ? SystemStatus.ERROR.toString() : SystemStatus.OK.toString();
        telemetry.addData(name, status);
    }
}