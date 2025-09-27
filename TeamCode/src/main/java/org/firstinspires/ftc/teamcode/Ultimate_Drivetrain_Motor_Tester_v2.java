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

        // We set the mode here for the specific needs of this tool.
        // This overrides the default from init() but does not affect other OpModes.
        robot.leftFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        robot.rightFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        robot.leftRear.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        robot.rightRear.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        telemetry.addLine("Drivetrain Motor Tester V2 Initialized.");
        telemetry.addLine("Press START to begin.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            handleMotorSelection();

            if (gamepad1.a) {
                runSelectedTest();
            } else {
                robot.stop();
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
        switch (selectedOption) {
            case LEFT_FRONT:
                robot.leftFront.setPower(TEST_POWER);
                break;
            case RIGHT_FRONT:
                robot.rightFront.setPower(TEST_POWER);
                break;
            case LEFT_REAR:
                robot.leftRear.setPower(TEST_POWER);
                break;
            case RIGHT_REAR:
                robot.rightRear.setPower(TEST_POWER);
                break;
            case ALL:
                robot.leftFront.setPower(TEST_POWER);
                robot.rightFront.setPower(TEST_POWER);
                robot.leftRear.setPower(TEST_POWER);
                robot.rightRear.setPower(TEST_POWER);
                break;
        }
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

    /** Helper method to display port and encoder info for a single motor. */
    private void displayMotorInfo(String name, DcMotor motor) {
        if (motor == null) {
            telemetry.addData(name, "ERROR: Not configured.");
            return;
        }
        telemetry.addData(name + " (Port " + motor.getPortNumber() + ") Ticks", motor.getCurrentPosition());
    }

    /** Helper method to get the motor object corresponding to the selected option. */
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