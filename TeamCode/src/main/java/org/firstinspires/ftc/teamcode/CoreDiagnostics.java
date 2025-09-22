package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.IMU;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * CoreDiagnostics - The definitive, interactive pre-match diagnostics tool.
 *
 * This OpMode provides a unified dashboard to verify the robot's core systems,
 * fulfilling the requirements of items 1.4, 1.5, and 1.6 from the roadmap with
 * an emphasis on failsafe, interactive checks.
 *
 * Key Features:
 * 1.  "Summary First" Display: A top-level system health check for a quick go/no-go
 *     decision, using clear [ OK ], [WARN], and [ERROR] tags for readability.
 * 2.  Automatic Controller Detection: Explicitly flags disconnected gamepads.
 * 3.  Interactive Liveness Checks: Provides on-screen "ACTION" prompts to guide
 *     the user in physically moving parts to verify that sensors are not just
 *     configured, but are actively working and reporting live data.
 * 4.  Comprehensive Sensor Dashboard: Checks the configuration status of all
 *     critical sensors, including every motor encoder.
 * 5.  IMU & Heading Monitor: Shows a live heading display and provides a simple,
 *     one-button reset.
 *
 * Standard Operating Procedure (SOP):
 * Run this OpMode before every match and testing session.
 *
 * 1.  Glance at the "SYSTEM HEALTH" summary at the top. If all systems are [ OK ],
 *     you are ready to proceed.
 * 2.  If any system shows [WARN], [ERROR], or [DISCONNECTED], scroll down to the
 *     detailed section for that system.
 * 3.  Follow all on-screen "ACTION" prompts. This includes:
 *     - Wiggling all controller sticks and pressing buttons.
 *     - Physically rotating the robot to verify the IMU heading changes.
 *     - Manually spinning wheels/mechanisms to verify encoder counts change.
 * 4.  Press (Y) on Gamepad 1 to reset the IMU heading to zero before starting a match.
 */

public class CoreDiagnostics extends LinearOpMode {

    private Robot robot;

    // Use a clearer enum for status reporting
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

        telemetry.addLine("Core Diagnostics V1.2 Initialized");
        telemetry.addLine("This is the final, interactive version.");
        telemetry.addLine("Press PLAY to begin checks.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            telemetry.clear();

            // Run checks and get their status
            SystemStatus controllerStatus = checkControllers();
            SystemStatus imuStatus = checkImu();
            SystemStatus sensorStatus = checkSensors();

            // Display high-level summary
            displaySummary(controllerStatus, imuStatus, sensorStatus);

            // Display detailed, interactive sections
            displayControllerDetails();
            displayImuDetails();
            displaySensorDetails();

            telemetry.update();
        }
    }

    // --- SUMMARY DISPLAY ---
    private void displaySummary(SystemStatus controllerStatus, SystemStatus imuStatus, SystemStatus sensorStatus) {
        telemetry.addLine("--- SYSTEM HEALTH (V1.2) ---");
        telemetry.addData("CONTROLLERS", controllerStatus.toString());
        telemetry.addData("IMU", imuStatus.toString());
        telemetry.addData("SENSORS", sensorStatus.toString());
        telemetry.addLine("\n----------------------------\n");
    }

    // --- CONTROLLER CHECKS (1.6) ---
    private SystemStatus checkControllers() {
        if (gamepad1.getGamepadId() == -1 || gamepad2.getGamepadId() == -1) {
            return SystemStatus.DISCONNECTED;
        }
        return SystemStatus.OK;
    }

    private void displayControllerDetails() {
        telemetry.addLine("--- Controllers ---");
        telemetry.addData("G1 Status", gamepad1.getGamepadId() == -1 ? SystemStatus.DISCONNECTED.toString() : SystemStatus.OK.toString());
        telemetry.addData("G2 Status", gamepad2.getGamepadId() == -1 ? SystemStatus.DISCONNECTED.toString() : SystemStatus.OK.toString());
        telemetry.addLine("\nACTION: Wiggle all sticks and press buttons to verify.");
        telemetry.addData("G1 Left Stick", "X: %.2f, Y: %.2f", gamepad1.left_stick_x, -gamepad1.left_stick_y);
        telemetry.addData("G1 Right Stick", "X: %.2f, Y: %.2f", gamepad1.right_stick_x, -gamepad1.right_stick_y);
    }

    // --- IMU CHECKS (1.4) ---
    private SystemStatus checkImu() {
        if (robot.imu == null) return SystemStatus.ERROR;
        return SystemStatus.OK;
    }

    private void displayImuDetails() {
        telemetry.addLine("\n--- IMU & Heading ---");
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

    // --- SENSOR CHECKS (1.5) ---
    private SystemStatus checkSensors() {
        // This is a configuration check. The liveness check is visual/manual.
        boolean hasError = robot.leftFront == null ||
                robot.rightFront == null ||
                robot.leftRear == null ||
                robot.rightRear == null ||
                robot.armMotor == null ||
                robot.frontDistanceSensor == null;
        return hasError ? SystemStatus.ERROR : SystemStatus.OK;
    }

    private void displaySensorDetails() {
        telemetry.addLine("\n--- Sensor Dashboard ---");
        telemetry.addLine("ACTION: Manually move each part. Does the value change?");

        // Drivetrain Encoders
        displayEncoderStatus("Left Front", robot.leftFront);
        displayEncoderStatus("Right Front", robot.rightFront);
        displayEncoderStatus("Left Back", robot.leftRear);
        displayEncoderStatus("Right Back", robot.rightRear);

        // Mechanism Encoders
        displayEncoderStatus("Arm Motor", robot.armMotor);

        // Distance Sensor
        if (robot.frontDistanceSensor == null) {
            telemetry.addData("Front Distance", SystemStatus.ERROR + " (Not configured)");
        } else {
            double distance = robot.frontDistanceSensor.getDistance(DistanceUnit.INCH);
            String status = (distance >= 1000 || distance <= 0) ? SystemStatus.WARNING.toString() : SystemStatus.OK.toString();
            telemetry.addData("Front Distance", status + " | Range: %.2f in", distance);
        }
    }

    /**
     * Helper method to display a single motor encoder's status.
     */
    private void displayEncoderStatus(String name, DcMotor motor) {
        String status = (motor == null) ? SystemStatus.ERROR.toString() : SystemStatus.OK.toString();
        int position = (motor == null) ? 0 : motor.getCurrentPosition();
        telemetry.addData(name + " Encoder", status + " | Pos: %d", position);
    }
}