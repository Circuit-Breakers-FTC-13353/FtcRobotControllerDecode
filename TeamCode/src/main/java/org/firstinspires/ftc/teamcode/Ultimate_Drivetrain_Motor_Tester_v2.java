// Filename: Ultimate_Drivetrain_Motor_Tester_v2.java
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

/**
 * This is the enhanced "Ultimate Drivetrain Motor Tester" (Version 2).
 *
 * It has been updated to provide comprehensive diagnostics for the drivetrain.
 *
 * V2 FEATURES:
 * - Displays the physical Port Number of each motor, allowing for easy
 *   verification of the robot's configuration.
 * - Displays the live Encoder Tick Count for each motor, making it an
 *   essential tool for diagnosing encoder failures before autonomous tuning.
 *
 * HOW TO USE:
 * Refer to the SOP for this tool. In summary: place robot on blocks, use the
 * D-pad to select a motor, and hold 'A' to run it while observing the telemetry
 * and physical wheel rotation.
 *
 * @author Team 13353
 */
@TeleOp(name = "Ultimate: Drivetrain Motor Tester v2", group = "1-Diagnostics")
public class Ultimate_Drivetrain_Motor_Tester_v2 extends LinearOpMode {

    private Robot robot;
    private final double TEST_POWER = 0.25;

    private enum TestOption {
        LEFT_FRONT,
        RIGHT_FRONT,
        LEFT_REAR,
        RIGHT_REAR,
        ALL
    }
    private TestOption[] testOptions = TestOption.values();
    private int selectedOptionIndex = 0;

    @Override
    public void runOpMode() {
        robot = new Robot(hardwareMap);

        if (!robot.init()) {
            telemetry.addLine("ERROR: Hardware initialization failed.");
            telemetry.update();
            while (opModeIsActive()) { sleep(20); }
            return;
        }

        // We must set the mode to RUN_USING_ENCODER to accurately read
        // encoder values while the motor is under power.
        robot.leftFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.rightFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.leftRear.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.rightRear.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        telemetry.addLine("Drivetrain Motor Tester V2 Initialized.");
        telemetry.addLine("Press START to begin.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            handleMotorSelection();

            if (gamepad1.a) {
                runSelectedTest();
            } else {
                setAllMotorVelocities(0);
            }

            displayTelemetry();
        }
    }

    private void handleMotorSelection() {
        if (gamepad1.dpad_down) {
            selectedOptionIndex = (selectedOptionIndex + 1) % testOptions.length;
            sleep(250);
        } else if (gamepad1.dpad_up) {
            selectedOptionIndex = (selectedOptionIndex - 1 + testOptions.length) % testOptions.length;
            sleep(250);
        }
    }

    private void runSelectedTest() {
        TestOption selectedOption = testOptions[selectedOptionIndex];

        // This is an arbitrary but reasonable value for max ticks/sec for many FTC motors.
        // A more advanced implementation could pull this from a Constants file.
        double maxTicksPerSecond = 2800;
        double targetVelocity = TEST_POWER * maxTicksPerSecond;

        // Cast the DcMotor objects to DcMotorEx to access the setVelocity method.
        DcMotorEx leftFrontEx = (DcMotorEx) robot.leftFront;
        DcMotorEx rightFrontEx = (DcMotorEx) robot.rightFront;
        DcMotorEx leftRearEx = (DcMotorEx) robot.leftRear;
        DcMotorEx rightRearEx = (DcMotorEx) robot.rightRear;

        switch (selectedOption) {
            case LEFT_FRONT:
                leftFrontEx.setVelocity(targetVelocity);
                break;
            case RIGHT_FRONT:
                rightFrontEx.setVelocity(targetVelocity);
                break;
            case LEFT_REAR:
                leftRearEx.setVelocity(targetVelocity);
                break;
            case RIGHT_REAR:
                rightRearEx.setVelocity(targetVelocity);
                break;
            case ALL:
                setAllMotorVelocities(targetVelocity);
                break;
        }
    }

    private void setAllMotorVelocities(double velocity) {
        ((DcMotorEx) robot.leftFront).setVelocity(velocity);
        ((DcMotorEx) robot.rightFront).setVelocity(velocity);
        ((DcMotorEx) robot.leftRear).setVelocity(velocity);
        ((DcMotorEx) robot.rightRear).setVelocity(velocity);
    }

    private void displayTelemetry() {
        telemetry.addLine("--- Drivetrain Motor Tester v2 ---");
        telemetry.addLine("Use D-pad UP/DOWN to select. Hold 'A' to run.");
        telemetry.addLine();

        TestOption selected = testOptions[selectedOptionIndex];
        telemetry.addData("--> Selected", selected);
        telemetry.addLine();

        if (selected == TestOption.ALL) {
            displayMotorInfo("LF", robot.leftFront);
            displayMotorInfo("RF", robot.rightFront);
            displayMotorInfo("LR", robot.leftRear);
            displayMotorInfo("RR", robot.rightRear);
        } else {
            displayMotorInfo("Motor", getSelectedMotor(selected));
        }

        telemetry.update();
    }

    private void displayMotorInfo(String name, DcMotor motor) {
        if (motor == null) {
            telemetry.addData(name, "ERROR: Not configured.");
            return;
        }
        telemetry.addData(name + " (Port " + motor.getPortNumber() + ") Ticks", motor.getCurrentPosition());
    }

    private DcMotor getSelectedMotor(TestOption option) {
        switch (option) {
            case LEFT_FRONT: return robot.leftFront;
            case RIGHT_FRONT: return robot.rightFront;
            case LEFT_REAR: return robot.leftRear;
            case RIGHT_REAR: return robot.rightRear;
            default: return null;
        }
    }
}