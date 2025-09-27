// Filename: Ultimate_PID_Tuner_v3.java
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.util.ElapsedTime;

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

    // --- LIVE TUNING STATE ---
    private double p_live, i_live, d_live, f_live;
    // --- SAVED STATE ---
    private double p_saved, i_saved, d_saved, f_saved;

    private int targetPosition = 0;
    private enum Coeff { P, I, D, F }
    private Coeff selectedCoeff = Coeff.P;
    private double stepSize = 0.1;

    // --- RUN STATS ---
    private int overshoot = 0;
    private double timeToSettle = 0;
    private ElapsedTime timer = new ElapsedTime();
    private String lastSaveStatus = "None";

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

        // Initialize both live and saved variables from Constants first.
        p_live = p_saved = Constants.ARM_P;
        i_live = i_saved = Constants.ARM_I;
        d_live = d_saved = Constants.ARM_D;
        f_live = f_saved = Constants.ARM_F;

        // Then, load any overrides from the config file to update "saved" values.
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

    private void handleGamepadInput() {
        // Coefficient Selection
        if (gamepad1.dpad_right) {
            selectedCoeff = Coeff.values()[(selectedCoeff.ordinal() + 1) % Coeff.values().length];
            sleep(200);
        } else if (gamepad1.dpad_left) {
            selectedCoeff = Coeff.values()[(selectedCoeff.ordinal() - 1 + Coeff.values().length) % Coeff.values().length];
            sleep(200);
        }

        // Step Size Adjustment
        if (gamepad1.right_bumper) {
            stepSize *= 10;
            sleep(200);
        } else if (gamepad1.left_bumper) {
            stepSize /= 10;
            if (stepSize < 0.0001) stepSize = 0.0001;
            sleep(200);
        }

        // Coefficient Value Adjustment
        if (gamepad1.dpad_up) {
            changeCoeff(selectedCoeff, stepSize);
            sleep(150);
        } else if (gamepad1.dpad_down) {
            changeCoeff(selectedCoeff, -stepSize);
            sleep(150);
        }

        // Bidirectional Target Control
        if (gamepad1.y) {
            targetPosition = (targetPosition == Constants.ARM_LIFT_POSITION) ? Constants.ARM_INTAKE_POSITION : Constants.ARM_LIFT_POSITION;
            executeTuningRun(targetPosition);
        } else if (gamepad1.a) {
            targetPosition = (targetPosition == Constants.ARM_CARRY_POSITION) ? Constants.ARM_INTAKE_POSITION : Constants.ARM_CARRY_POSITION;
            executeTuningRun(targetPosition);
        }

        // Save to Config
        if (gamepad1.start) {
            saveCoefficients();
            sleep(500);
        }

        // In-OpMode Encoder Reset
        if (gamepad1.back) {
            resetArmAndStats();
            sleep(500);
        }
    }

    private void executeTuningRun(int newTarget) {
        overshoot = 0;
        timeToSettle = 0;
        boolean hasSettled = false;

        DcMotorEx armMotorEx = (DcMotorEx) robot.armMotor;
        PIDFCoefficients newCoeffs = new PIDFCoefficients(p_live, i_live, d_live, f_live);
        armMotorEx.setPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION, newCoeffs);
        armMotorEx.setTargetPositionTolerance(10);

        robot.setArmPosition(newTarget);
        timer.reset();

        while (opModeIsActive() && robot.isArmBusy()) {
            int currentPosition = robot.getArmPosition();
            int error = newTarget - currentPosition;

            if (newTarget > currentPosition && error < -overshoot) {
                overshoot = Math.abs(error);
            } else if (newTarget < currentPosition && error > overshoot) {
                overshoot = error;
            }

            if (!hasSettled && Math.abs(error) < 10) {
                timeToSettle = timer.milliseconds();
                hasSettled = true;
            }

            displayTelemetry(currentPosition);
        }

        if (!hasSettled) timeToSettle = timer.milliseconds();
        displayTelemetry(robot.getArmPosition());
    }

    private void loadCoefficientsFromConfig() {
        p_saved = Config.getDouble("ARM_P", p_saved);
        i_saved = Config.getDouble("ARM_I", i_saved);
        d_saved = Config.getDouble("ARM_D", d_saved);
        f_saved = Config.getDouble("ARM_F", f_saved);

        p_live = p_saved;
        i_live = i_saved;
        d_live = d_saved;
        f_live = f_saved;
    }

    private void saveCoefficients() {
        try {
            Config.save("ARM_P", String.format("%.4f", p_live));
            Config.save("ARM_I", String.format("%.4f", i_live));
            Config.save("ARM_D", String.format("%.4f", d_live));
            Config.save("ARM_F", String.format("%.4f", f_live));

            p_saved = p_live; i_saved = i_live; d_saved = d_live; f_saved = f_live;
            lastSaveStatus = "Success!";
        } catch (Exception e) {
            lastSaveStatus = "FAILED!";
        }
    }

    private void resetArmAndStats() {
        robot.resetArmEncoder();
        robot.armMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        targetPosition = 0;
        overshoot = 0;
        timeToSettle = 0;
        lastSaveStatus = "None";
    }

    private void changeCoeff(Coeff coeff, double amount) {
        if (coeff == Coeff.P) p_live += amount;
        if (coeff == Coeff.I) i_live += amount;
        if (coeff == Coeff.D) d_live += amount;
        if (coeff == Coeff.F) f_live += amount;
    }

    private void displayTelemetry() { displayTelemetry(robot.getArmPosition()); }

    private void displayTelemetry(int currentPos) {
        telemetry.addLine("--- Ultimate PID Tuner v3 ---");
        telemetry.addLine("D-Pad: Select/Adjust | Bumpers: Change Step");
        telemetry.addLine("(Y) Toggles Lift | (A) Toggles Carry");
        telemetry.addLine("START: Save to Config | BACK: Re-Zero Encoder");
        telemetry.addLine();

        telemetry.addData((selectedCoeff == Coeff.P ? ">> P <<" : " P"), "Live: %.4f (Saved: %.4f)", p_live, p_saved);
        telemetry.addData((selectedCoeff == Coeff.I ? ">> I <<" : " I"), "Live: %.4f (Saved: %.4f)", i_live, i_saved);
        telemetry.addData((selectedCoeff == Coeff.D ? ">> D <<" : " D"), "Live: %.4f (Saved: %.4f)", d_live, d_saved);
        telemetry.addData((selectedCoeff == Coeff.F ? ">> F <<" : " F"), "Live: %.4f (Saved: %.4f)", f_live, f_saved);
        telemetry.addData("   Step", "%.4f", stepSize);
        telemetry.addLine();

        telemetry.addLine("--- Run Stats ---");
        telemetry.addData("Target", targetPosition);
        telemetry.addData("Current", currentPos);
        telemetry.addData("Overshoot", "%d ticks", overshoot);
        telemetry.addData("Time to Settle", "%.0f ms", timeToSettle);
        telemetry.addData("Last Save", lastSaveStatus);
        telemetry.update();
    }
}