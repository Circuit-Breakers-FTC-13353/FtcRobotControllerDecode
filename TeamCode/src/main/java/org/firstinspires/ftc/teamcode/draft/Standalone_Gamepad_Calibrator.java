package org.firstinspires.ftc.teamcode.draft;


import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/**
 * A standalone tool to find the precise deadzone values for a gamepad.
 * ... (full Javadoc header) ...
 */
@TeleOp(name = "Standalone: Gamepad Calibrator", group = "Standalone Tools")
public class Standalone_Gamepad_Calibrator extends LinearOpMode {

    private double leftStickX_maxDrift = 0;
    private double leftStickY_maxDrift = 0;
    private double rightStickX_maxDrift = 0;
    private double rightStickY_maxDrift = 0;

    @Override
    public void runOpMode() throws InterruptedException {
        telemetry.addLine("Gamepad Calibrator Initialized.");
        telemetry.addLine("DO NOT TOUCH THE CONTROLLER STICKS.");
        telemetry.addLine("Press START to begin calibration.");
        telemetry.update();

        waitForStart();

        // Let the system settle for a moment
        sleep(1000);

        // Continuously check for drift
        while (opModeIsActive()) {
            // Get the absolute (positive) value of the current stick positions
            double lsx = Math.abs(gamepad1.left_stick_x);
            double lsy = Math.abs(gamepad1.left_stick_y);
            double rsx = Math.abs(gamepad1.right_stick_x);
            double rsy = Math.abs(gamepad1.right_stick_y);

            // If the current drift is larger than our recorded max, update the max
            if (lsx > leftStickX_maxDrift) leftStickX_maxDrift = lsx;
            if (lsy > leftStickY_maxDrift) leftStickY_maxDrift = lsy;
            if (rsx > rightStickX_maxDrift) rightStickX_maxDrift = rsx;
            if (rsy > rightStickY_maxDrift) rightStickY_maxDrift = rsy;

            telemetry.clearAll();
            telemetry.addLine("--- Standalone Gamepad Calibrator ---");
            telemetry.addLine("Calibrating... DO NOT TOUCH THE STICKS.");
            telemetry.addLine("The values below are the max drift detected so far.");
            telemetry.addLine();
            telemetry.addLine("--- Left Stick ---");
            telemetry.addData("Max X Drift", "%.4f", leftStickX_maxDrift);
            telemetry.addData("Max Y Drift", "%.4f", leftStickY_maxDrift);
            telemetry.addLine();
            telemetry.addLine("--- Right Stick ---");
            telemetry.addData("Max X Drift", "%.4f", rightStickX_maxDrift);
            telemetry.addData("Max Y Drift", "%.4f", rightStickY_maxDrift);
            telemetry.addLine();
            telemetry.addLine("After 10-15 seconds, use the highest value and add a");
            telemetry.addLine("small buffer (e.g., +0.01) for your deadzone constant.");
            telemetry.update();
        }
    }
}