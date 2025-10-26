package org.firstinspires.ftc.teamcode.draft;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Config;
import org.firstinspires.ftc.teamcode.Constants;

/**
 * This is the "Ultimate Power & Battery Monitor" from the FTC Ultimate Toolkit.
 *
 * It is a diagnostic tool designed to help identify failing batteries and poor
 * wiring connections by measuring voltage drop under a controlled load.
 *
 * A significant voltage drop (> 1.0V) under load is a strong indicator of a
 * weak battery or a high-resistance connection (e.g., a loose wire).
 *
 * @author Team 13353
 */
@TeleOp(name = "Ultimate: Power & Battery Monitor", group = "3-Performance")
public class Ultimate_Power_Monitor extends LinearOpMode {

    private Robot robot;

    // --- TEST STATE ---
    private double restingVoltage = 0.0;
    private double minVoltageUnderTest = 14.0; // Start high
    private double voltageDrop = 0.0;
    private boolean isTesting = false;

    @Override
    public void runOpMode() throws InterruptedException {
        robot = new Robot(hardwareMap);
        Config.load(); // Though not used, it's good practice

        if (!robot.init()) {
            telemetry.addLine("ERROR: Hardware initialization failed.");
            telemetry.update();
            while (opModeIsActive()) { sleep(20); }
            return;
        }

        telemetry.addLine("Power & Battery Monitor Initialized.");
        telemetry.addLine("Press START to begin.");
        telemetry.update();

        waitForStart();

        // Get the initial resting voltage
        restingVoltage = robot.getVoltage();
        minVoltageUnderTest = restingVoltage;

        while (opModeIsActive()) {
            // --- USER CONTROL ---
            // Hold the 'A' button to run the stress test
            if (gamepad1.a) {
                if (!isTesting) {
                    isTesting = true;
                    minVoltageUnderTest = robot.getVoltage();
                    gamepad1.rumble(300); // <-- ADDED: Haptic feedback on START
                }
                robot.performStressTest(Constants.STRESS_TEST_POWER);
            } else {
                if (isTesting) {
                    isTesting = false;
                    robot.stop();
                    robot.setArmPower(0);
                    // Return servos to a known state after test
                    robot.closeClaw();
                    robot.stowWrist();
                    gamepad1.rumble(0.7, 0.7, 300); // <-- ADDED: Different rumble on STOP
                }
                restingVoltage = robot.getVoltage();
            }

            // --- DATA ANALYSIS ---
            double liveVoltage = robot.getVoltage();
            if (isTesting) {
                // If we are testing, check if this is a new minimum
                if (liveVoltage < minVoltageUnderTest) {
                    minVoltageUnderTest = liveVoltage;
                }
                // Calculate the live voltage drop
                voltageDrop = restingVoltage - minVoltageUnderTest;
            }

            // --- TELEMETRY ---
            displayTelemetry(liveVoltage);
        }
    }

    private void displayTelemetry(double liveVoltage) {
        telemetry.addLine("--- Ultimate Power & Battery Monitor ---");
        telemetry.addLine("Hold (A) to run a stress test on all motors.");
        telemetry.addLine("The robot's wheels and arm will move!");
        telemetry.addLine();

        telemetry.addLine("--- Measurements ---");
        telemetry.addData("Live Voltage", "%.2f V", liveVoltage);
        telemetry.addData("Resting Voltage", "%.2f V", restingVoltage);
        telemetry.addData("Min Voltage (under load)", "%.2f V", minVoltageUnderTest);
        telemetry.addLine();

        // Provide a clear, actionable analysis of the voltage drop
        String status;
        if (voltageDrop > Constants.VOLTAGE_DROP_CRITICAL_THRESHOLD) {
            status = "[CRITICAL] - Check battery and wiring!";
        } else if (voltageDrop > 0.5) {
            status = "[ WARNING ] - Showing signs of weakness.";
        } else {
            status = "[  OK  ] - System appears healthy.";
        }

        telemetry.addData("Voltage Drop", "%.2f V", voltageDrop);
        telemetry.addData("System Health", status);
        telemetry.update();
    }
}