// Filename: Ultimate_Drivetrain_Motor_Tester.java

package org.firstinspires.ftc.teamcode.draft;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/**
 * This OpMode is the "Ultimate Drivetrain Motor Tester" from the FTC Ultimate Toolkit.
 *
 * It's designed to be a simple, interactive tool for diagnosing and verifying the
 * functionality of a Mecanum drivetrain.
 *
 * FEATURES:
 * - Uses the Robot Hardware Abstraction Class (`Robot.java`) for all hardware access.
 * - Allows the user to select individual motors (leftFront, rightFront, etc.) or all motors
 *   at once using the D-pad.
 * - Runs the selected motor(s) at a low, constant power when a button is held.
 * - Provides clear telemetry instructions and feedback.
 *
 * HOW TO USE:
 * 1.  Place the robot on blocks so the wheels can spin freely.
 * 2.  Select this OpMode on the Driver Station.
 * 3.  INIT and START the OpMode.
 * 4.  Use D-pad UP and DOWN to cycle through the test options.
 * 5.  Press and hold the 'A' button to run the selected motor(s).
 * 6.  Refer to the associated SOP to interpret the results and correct motor directions.
 * @author Team 13353 with Gemini
 */
@TeleOp(name = "Ultimate: Drivetrain Motor Tester", group = "1-Diagnostics")
public class Ultimate_Drivetrain_Motor_Tester extends LinearOpMode {

    // The robot object, which contains all hardware access.
    private Robot robot;

    // The power level to apply to the motors during the test.
    private final double TEST_POWER = 0.25;

    // An array to hold the different test options.
    // This makes the selection logic clean and scalable.
    private enum TestOption {
        LEFT_FRONT,
        RIGHT_FRONT,
        LEFT_REAR,
        RIGHT_REAR,
        ALL
    }
    private TestOption[] testOptions = TestOption.values();

    // The index of the currently selected test option.
    private int selectedOptionIndex = 0;

    @Override
    public void runOpMode() {
        // Instantiate our robot class.
        robot = new Robot(hardwareMap);

        // Initialize the robot's hardware.
        if (!robot.init()) {
            telemetry.addLine("ERROR: Hardware initialization failed.");
            telemetry.addLine("Please check your configuration and connections.");
            telemetry.update();
            // Wait until the OpMode is stopped.
            while (opModeIsActive()) {
                sleep(20);
            }
            return;
        }

        telemetry.addLine("Drivetrain Motor Tester Initialized.");
        telemetry.addLine("Press START to begin.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // --- USER INPUT ---
            // Check for D-pad input to change the selected motor.
            handleMotorSelection();

            // --- MOTOR ACTIVATION ---
            // If the 'A' button is held down, run the selected motor test.
            // Otherwise, make sure all motors are stopped.
            if (gamepad1.a) {
                runSelectedTest();
            } else {
                robot.stop();
            }

            // --- TELEMETRY ---
            // Display instructions and the current state.
            displayTelemetry();
        }
    }

    /**
     * Handles the logic for selecting which motor to test using the D-pad.
     */
    private void handleMotorSelection() {
        if (gamepad1.dpad_down) {
            selectedOptionIndex++;
            if (selectedOptionIndex >= testOptions.length) {
                selectedOptionIndex = 0; // Wrap around to the beginning.
            }
            sleep(250); // Debounce to prevent rapid cycling.
        } else if (gamepad1.dpad_up) {
            selectedOptionIndex--;
            if (selectedOptionIndex < 0) {
                selectedOptionIndex = testOptions.length - 1; // Wrap around to the end.
            }
            sleep(250); // Debounce.
        }
    }

    /**
     * Runs the currently selected motor test by setting power to the appropriate motor(s).
     */
    private void runSelectedTest() {
        TestOption selectedOption = testOptions[selectedOptionIndex];

        // We use a switch statement to cleanly handle each test case.
        // Note that we are only setting the power. The directions are handled
        // within the Robot class, which is exactly what our architecture is for.
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
                // We could call robot.drive() here, but for a simple test,
                // setting individual powers is clearer.
                robot.leftFront.setPower(TEST_POWER);
                robot.rightFront.setPower(TEST_POWER);
                robot.leftRear.setPower(TEST_POWER);
                robot.rightRear.setPower(TEST_POWER);
                break;
        }
    }

    /**
     * Displays clear instructions and the current status on the Driver Station.
     */
    private void displayTelemetry() {
        telemetry.addLine("--- Drivetrain Motor Tester ---");
        telemetry.addLine();
        telemetry.addLine("Use D-pad UP/DOWN to select a motor.");
        telemetry.addLine("Press and hold 'A' to run the motor.");
        telemetry.addLine();

        // Display the currently selected option.
        telemetry.addData("--> Selected", testOptions[selectedOptionIndex]);
        telemetry.addData("    Power", gamepad1.a ? TEST_POWER : "0.0");
        telemetry.addLine();
        telemetry.addLine("Refer to the SOP for expected wheel directions.");
        telemetry.update();
    }
}