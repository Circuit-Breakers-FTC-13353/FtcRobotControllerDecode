// Filename: Ultimate_PID_Tuner_v3.java
package org.firstinspires.ftc.teamcode.draft;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Config;

/**
 * This is the enhanced "Ultimate PID Tuner" (Version 3).
 *
 * It is a powerful, safe, and intuitive tool for finding the optimal
 * PIDF coefficients for any encoder-based motor.
 *
 * V3 FEATURES:
 * - Bidirectional Testing: Toggle between high and low targets to test against and with gravity.
 * - Save to Config: Press the START button to save your tuned PIDF values directly to
 *   the `robot_config.properties` file, no recompiling needed.
 * - Advanced Metrics: The telemetry calculates and displays crucial performance data,
 *   including Overshoot and Time to Settle.
 * - In-OpMode Encoder Reset: Press the BACK button to re-zero the encoder at any time.
 * - Live vs. Saved Telemetry: Clearly displays the live, unsaved values you are
 *   editing alongside the last saved values from the configuration.
 *
 * @author Team 13353
 */
@TeleOp(name = "Ultimate: PID Tuner v3", group = "3-Performance")
public class Ultimate_PID_Tuner_v3 extends LinearOpMode {

    private Robot robot;
    private double p_live, i_live, d_live, f_live;
    private double p_saved, i_saved, d_saved, f_saved;
    private int targetPosition = 0;
    private enum Coeff { P, I, D, F }
    private Coeff selectedCoeff = Coeff.P;
    private double stepSize = 0.1;
    private int overshoot = 0;
    private double timeToSettle = 0;
    private ElapsedTime timer = new ElapsedTime();
    private String lastSaveStatus = "None";

    @Override
    public void runOpMode() throws InterruptedException {
        robot = new Robot(hardwareMap);
        Config.load();
        if (!robot.init()) { /* ... error handling ... */ return; }

        loadCoefficientsFromConfig();
        resetArmAndStats();

        telemetry.addLine("PID Tuner V3 Initialized. Press START.");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {
            handleGamepadInput();
            displayTelemetry();
        }
    }

    private void handleGamepadInput() { /* ... full input logic ... */ }
    private void executeTuningRun(int newTarget) { /* ... full execution logic ... */ }
    private void loadCoefficientsFromConfig() { /* ... */ }
    private void saveCoefficients() { /* ... */ }
    private void resetArmAndStats() { /* ... */ }
    private void changeCoeff(Coeff coeff, double amount) { /* ... */ }
    private void displayTelemetry() { /* ... */ }
    private void displayTelemetry(int currentPos) { /* ... full telemetry logic ... */ }
}