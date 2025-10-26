// Filename: Ultimate_Mechanism_Range_Finder_v2.java
package org.firstinspires.ftc.teamcode.draft;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.Config;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the enhanced "Ultimate Mechanism Range Finder" (Version 2).
 *
 * It is a powerful and interactive tool for safely finding and recording the encoder limits
 * and key positions for any mechanism on the robot (e.g., arms, lifts, wrists).
 * This tool is designed to be reusable and future-proof.
 *
 * =================================================================================
 *                                 HOW TO USE
 * =================================================================================
 *
 * 1.  **INITIAL SETUP:**
 *     - Physically move the mechanism(s) you want to test to a known "zero" or "home"
 *       position (e.g., fully retracted at the bottom).
 *
 * 2.  **INITIALIZE THE OPMODE:**
 *     - Select this OpMode ("Ultimate: Mechanism Range Finder v2") on the Driver Station.
 *     - Press **INIT**. This will reset the encoders for all configured mechanisms to 0.
 *
 * 3.  **CONTROL AND FIND POSITIONS:**
 *     - Press **START**.
 *     - Use the **Left and Right Bumpers** to select which mechanism you want to control.
 *       The telemetry will show which mechanism is currently active.
 *     - Use the **Left Stick (Up/Down)** for large, fast movements.
 *     - Use the **D-pad (Up/Down)** for slow, precise "fine-tuning" adjustments.
 *     - If at any point you need to re-zero the encoder, physically move the mechanism
 *       back to its home position and press the **BACK button**.
 *
 * 4.  **SAVE KEY POSITIONS:**
 *     - When the mechanism is in a desired position, press one of the designated buttons
 *       to save its current encoder value:
 *         - **(A) Button:** Saves the `INTAKE` position.
 *         - **(B) Button:** Saves the `CARRY` position.
 *         - **(Y) Button:** Saves the `SCORING` position.
 *         - **(X) Button:** Saves the `MAX_LIMIT` position.
 *
 * 5.  **RECORD THE VALUES:**
 *     - The telemetry screen will display all the saved encoder values for the selected mechanism.
 *     - Carefully copy these integer values into your `Constants.java` file (or your
 *       `robot_config.properties` file) for use in your competition OpModes.
 *
 * =================================================================================
 * @author Team 13353
 */
@TeleOp(name = "Ultimate: Mechanism Range Finder v2", group = "1-Diagnostics")
public class Ultimate_Mechanism_Range_Finder_v2 extends LinearOpMode {

    private enum ControllableMechanism {
        ARM
        // Add other motors like LIFT here in the future
    }
    private ControllableMechanism[] mechanisms = ControllableMechanism.values();
    private int selectedMechanismIndex = 0;

    private Map<String, Map<String, Integer>> savedPositions = new HashMap<>();

    private Robot robot;

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

        for (ControllableMechanism mechanism : mechanisms) {
            savedPositions.put(mechanism.name(), new HashMap<>());
        }

        // Note: The robot.init() already resets the encoder. This is a redundant
        // safety check for clarity.
        robot.resetArmEncoder();

        telemetry.addLine("Mechanism Range Finder V2 Initialized.");
        telemetry.addLine("Ensure all mechanisms are in their 'zero' positions.");
        telemetry.addLine("Press START to begin.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            handleMechanismSelection();
            handleMotorControl();
            handlePositionSaving();
            handleEncoderReset();
            displayTelemetry();
        }
    }

    private void handleMechanismSelection() {
        if (gamepad1.right_bumper) {
            selectedMechanismIndex = (selectedMechanismIndex + 1) % mechanisms.length;
            sleep(250);
        }
        if (gamepad1.left_bumper) {
            selectedMechanismIndex = (selectedMechanismIndex - 1 + mechanisms.length) % mechanisms.length;
            sleep(250);
        }
    }

    private void handleMotorControl() {
        ControllableMechanism selected = mechanisms[selectedMechanismIndex];
        double power = 0;

        if (gamepad1.dpad_up) {
            power = 0.15; // Fine-tune up
        } else if (gamepad1.dpad_down) {
            power = -0.15; // Fine-tune down
        } else {
            power = -gamepad1.left_stick_y; // Gross control
        }

        switch (selected) {
            case ARM:
                robot.setArmPower(power);
                break;
        }
    }

    private void handlePositionSaving() {
        ControllableMechanism selected = mechanisms[selectedMechanismIndex];
        String mechanismName = selected.name();
        int currentPosition = 0;

        switch (selected) {
            case ARM:
                currentPosition = robot.getArmPosition();
                break;
        }

        if (gamepad1.a) savedPositions.get(mechanismName).put("INTAKE", currentPosition);
        if (gamepad1.b) savedPositions.get(mechanismName).put("CARRY", currentPosition);
        if (gamepad1.y) savedPositions.get(mechanismName).put("SCORING", currentPosition);
        if (gamepad1.x) savedPositions.get(mechanismName).put("MAX_LIMIT", currentPosition);
    }

    private void handleEncoderReset() {
        if (gamepad1.back) {
            ControllableMechanism selected = mechanisms[selectedMechanismIndex];
            switch (selected) {
                case ARM:
                    robot.resetArmEncoder();
                    robot.armMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER); // Re-set mode after reset
                    break;
            }
            savedPositions.get(selected.name()).clear();
            sleep(250);
        }
    }

    private void displayTelemetry() {
        ControllableMechanism selected = mechanisms[selectedMechanismIndex];
        String mechanismName = selected.name();
        int currentPosition = 0;

        switch (selected) {
            case ARM:
                currentPosition = robot.getArmPosition();
                break;
        }

        telemetry.addLine("--- Mechanism Range Finder v2 ---");
        telemetry.addLine("Bumpers: Switch Mechanism | BACK: Re-Zero");
        telemetry.addLine("Left Stick: Gross Control | D-pad: Fine Control");
        telemetry.addLine("Buttons: (A) Intake | (B) Carry | (Y) Scoring | (X) Max");
        telemetry.addLine();
        telemetry.addData("--> CONTROLLING", mechanismName);
        telemetry.addData("    LIVE Ticks", currentPosition);
        telemetry.addLine();
        telemetry.addLine("--- Saved Values for " + mechanismName + " ---");

        Map<String, Integer> positions = savedPositions.get(mechanismName);
        telemetry.addData(mechanismName + "_INTAKE", positions.get("INTAKE") != null ? positions.get("INTAKE") : "Not Saved");
        telemetry.addData(mechanismName + "_CARRY", positions.get("CARRY") != null ? positions.get("CARRY") : "Not Saved");
        telemetry.addData(mechanismName + "_SCORING", positions.get("SCORING") != null ? positions.get("SCORING") : "Not Saved");
        telemetry.addData(mechanismName + "_MAX_LIMIT", positions.get("MAX_LIMIT") != null ? positions.get("MAX_LIMIT") : "Not Saved");

        telemetry.update();
    }
}