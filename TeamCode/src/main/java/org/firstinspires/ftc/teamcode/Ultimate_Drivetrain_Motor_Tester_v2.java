// Filename: Ultimate_Drivetrain_Motor_Tester_v2.java
//@author Team 13353 with Gemini
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx; // Import the extended motor interface

/**
 * This is the enhanced "Ultimate Drivetrain Motor Tester".
 *
 * It has been updated to address two key gaps:
 * 1.  It now displays the physical Port Number of each motor, allowing for easy
 *     verification of the robot's configuration.
 * 2.  It now displays the live Encoder Tick Count for each motor, making it an
 *     essential tool for diagnosing encoder failures before autonomous tuning.
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

        // IMPORTANT: We are setting the motor mode to RUN_USING_ENCODER here.
        // This is necessary to read the encoder values, but it means the motor
        // will try to maintain a constant velocity. For a simple power test,
        // this is perfectly fine.
        robot.leftFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.rightFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.leftRear.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.rightRear.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        telemetry.addLine("Drivetrain Motor Tester Initialized.");
        telemetry.addLine("Press START to begin.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            handleMotorSelection();

            if (gamepad1.a) {
                runSelectedTest();
            } else {
                // When not pressing A, we must set power to 0, not use robot.stop()
                // because robot.stop() doesn't account for RUN_USING_ENCODER mode.
                // For this mode, we need to set velocity to 0.
                setAllMotorVelocities(0);
            }

            displayTelemetry();
        }
    }

    private void handleMotorSelection() {
        if (gamepad1.dpad_down) {
            selectedOptionIndex++;
            if (selectedOptionIndex >= testOptions.length) {
                selectedOptionIndex = 0;
            }
            sleep(250);
        } else if (gamepad1.dpad_up) {
            selectedOptionIndex--;
            if (selectedOptionIndex < 0) {
                selectedOptionIndex = testOptions.length - 1;
            }
            sleep(250);
        }
    }

    /**
     * This method now sets motor VELOCITY instead of power, because the motors
     * are in RUN_USING_ENCODER mode. We calculate a target velocity based on our
     * test power and the motor's max ticks per second.
     */
    private void runSelectedTest() {
        TestOption selectedOption = testOptions[selectedOptionIndex];

        // This is an arbitrary but reasonable value for max ticks/sec for many FTC motors.
        // For a more advanced implementation, this could be a constant in your Robot class.
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

    /** A helper method to set all motor velocities at once. */
    private void setAllMotorVelocities(double velocity) {
        ((DcMotorEx) robot.leftFront).setVelocity(velocity);
        ((DcMotorEx) robot.rightFront).setVelocity(velocity);
        ((DcMotorEx) robot.leftRear).setVelocity(velocity);
        ((DcMotorEx) robot.rightRear).setVelocity(velocity);
    }

    /**
     * Displays the enhanced telemetry, including Port Numbers and Encoder Ticks.
     */
    private void displayTelemetry() {
        telemetry.addLine("--- Drivetrain Motor Tester v2 ---");
        telemetry.addLine("Use D-pad UP/DOWN to select. Hold 'A' to run.");
        telemetry.addLine();

        TestOption selected = testOptions[selectedOptionIndex];
        telemetry.addData("--> Selected", selected);

        if (selected == TestOption.ALL) {
            telemetry.addLine();
            telemetry.addData("LF (Port " + robot.leftFront.getPortNumber() + ") Ticks", robot.leftFront.getCurrentPosition());
            telemetry.addData("RF (Port " + robot.rightFront.getPortNumber() + ") Ticks", robot.rightFront.getCurrentPosition());
            telemetry.addData("LR (Port " + robot.leftRear.getPortNumber() + ") Ticks", robot.leftRear.getCurrentPosition());
            telemetry.addData("RR (Port " + robot.rightRear.getPortNumber() + ") Ticks", robot.rightRear.getCurrentPosition());
        } else {
            DcMotor motor = getSelectedMotor(selected);
            if (motor != null) {
                telemetry.addData("    Port Number", motor.getPortNumber());
                telemetry.addData("    Encoder Ticks", motor.getCurrentPosition());
            }
        }

        telemetry.update();
    }

    /** A helper method to get the motor object corresponding to the selected option. */
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