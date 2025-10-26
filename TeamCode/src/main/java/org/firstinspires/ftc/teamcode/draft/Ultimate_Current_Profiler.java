// Filename: Ultimate_Current_Profiler.java
package org.firstinspires.ftc.teamcode.draft;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.Config;
import org.firstinspires.ftc.teamcode.Constants;

/**
 * This is the "Ultimate Current Profiler" from the FTC Ultimate Toolkit.
 *
 * It is a diagnostic tool designed to safely measure and record two critical
 * diagnostic metrics for a motor: its Free-Spin Current and its Stall Current.
 *
 * - Free-Spin Current: Serves as a baseline for mechanical health.
 * - Stall Current: Provides the threshold needed to implement automated stall detection.
 *
 * This tool uses preset power levels for consistent, repeatable tests and displays
 * the peak current to get a reliable measurement.
 *
 * @author Team 13353
 */
@TeleOp(name = "Ultimate: Current Profiler", group = "3-Performance")
public class Ultimate_Current_Profiler extends LinearOpMode {

    private Robot robot;
    private double peakCurrentAmps = 0.0;

    // Use preset power levels for consistent testing.
    // Low power is crucial for safely stalling a motor.
    private final double[] powerPresets = {0.20, 0.30, 0.40, 0.50, 0.75, 1.00};
    private int presetIndex = 1; // Start at 30% power

    @Override
    public void runOpMode() throws InterruptedException {
        robot = new Robot(hardwareMap);
        Config.load();

        if (!robot.init()) {
            telemetry.addLine("ERROR: Hardware initialization failed.");
            telemetry.update();
            while (opModeIsActive()) { sleep(20); }
            return;
        }

        telemetry.addLine("Current Profiler Initialized.");
        telemetry.addLine("Press START to begin.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // --- USER CONTROL ---
            // D-pad up/down to select a power level
            if (gamepad1.dpad_up) {
                presetIndex = Math.min(powerPresets.length - 1, presetIndex + 1);
                sleep(250);
            }
            if (gamepad1.dpad_down) {
                presetIndex = Math.max(0, presetIndex - 1);
                sleep(250);
            }

            // Hold 'A' to apply the selected power in the FORWARD direction.
            // Hold 'Y' to apply the selected power in the REVERSE direction.
            double motorPower = 0;
            if (gamepad1.a) {
                motorPower = powerPresets[presetIndex];
            } else if (gamepad1.y) {
                motorPower = -powerPresets[presetIndex];
            }
            // We use the generic setArmPower, which has built-in safety limits.
            robot.setArmPower(motorPower);

            // --- CURRENT MONITORING ---
            double liveCurrent = robot.getArmCurrent(CurrentUnit.AMPS);
            if (gamepad1.a || gamepad1.y) { // Only update peak when running a test
                if (liveCurrent > peakCurrentAmps) {
                    peakCurrentAmps = liveCurrent;
                }
            }
            if (gamepad1.b) { // Reset peak for a new test
                peakCurrentAmps = 0.0;
            }

            // --- TELEMETRY ---
            displayTelemetry(motorPower, liveCurrent);
        }
    }

    private void displayTelemetry(double power, double current) {
        telemetry.addLine("--- Ultimate Current Profiler ---");
        telemetry.addLine("D-Pad U/D: Select Power Preset");
        telemetry.addLine("Hold (A) Fwd / (Y) Rev to Run Test");
        telemetry.addLine("Press (B) to Reset Peak");
        telemetry.addLine();
        telemetry.addData("Selected Power", "%.0f %%", powerPresets[presetIndex] * 100);
        telemetry.addData("Applied Power", "%.2f", power);
        telemetry.addLine();
        telemetry.addLine("--- Measurements ---");
        telemetry.addData("Live Current", "%.3f Amps", current);
        telemetry.addData("PEAK Current", "%.3f Amps", peakCurrentAmps);
        telemetry.addLine();

        telemetry.addLine("--- Baseline Health Profile (from Constants.java) ---");
        telemetry.addData("Expected Free-Spin", "< %.2f A", Constants.ARM_FREE_SPIN_CURRENT_AMPS);
        telemetry.addData("Stall Threshold", "%.2f A", Constants.ARM_STALL_THRESHOLD_AMPS);

        telemetry.update();
    }
}