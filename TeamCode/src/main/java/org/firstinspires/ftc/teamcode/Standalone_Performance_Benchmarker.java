package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import java.util.List;

/**
 * A standalone tool to measure the robot's control loop speed.
 * ... (full Javadoc header) ...
 */
@TeleOp(name = "Standalone: Performance Benchmarker", group = "Standalone Tools")
public class Standalone_Performance_Benchmarker extends LinearOpMode {

    private ElapsedTime loopTimer = new ElapsedTime();
    private int loopCount = 0;
    private double lastDisplayTime = 0;
    private double loopsPerSecond = 0;

    @Override
    public void runOpMode() throws InterruptedException {
        telemetry.addLine("Performance Benchmarker Initialized.");
        telemetry.addLine("Press START to begin.");
        telemetry.update();

        // --- Optional: Test Bulk Reads ---
        // Uncomment the following lines to see how bulk reads affect performance.
        // List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);
        // for (LynxModule hub : allHubs) {
        //     hub.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        // }

        waitForStart();

        loopTimer.reset();
        lastDisplayTime = loopTimer.seconds();

        while (opModeIsActive()) {

            // --- Optional: Test Bulk Reads ---
            // Uncomment this line if you enabled MANUAL caching mode above.
            // for (LynxModule hub : allHubs) {
            //     hub.clearBulkCache();
            // }

            loopCount++;

            // Update the display once per second to avoid flooding the telemetry
            // which would itself slow down the loop.
            double currentTime = loopTimer.seconds();
            if (currentTime - lastDisplayTime >= 1.0) {
                loopsPerSecond = loopCount / (currentTime - lastDisplayTime);
                loopCount = 0;
                lastDisplayTime = currentTime;
            }

            telemetry.clearAll();
            telemetry.addLine("--- Standalone Performance Benchmarker ---");
            telemetry.addLine("This tool measures the raw speed of the robot's control loop.");
            telemetry.addLine();
            telemetry.addData("Loop Speed (Hz)", "%.1f loops/sec", loopsPerSecond);
            telemetry.addData("Loop Time (ms)", "%.3f ms/loop", 1000.0 / loopsPerSecond);
            telemetry.addLine();
            telemetry.addLine("A higher Hz is better. Most robots run between 50-150 Hz.");

            telemetry.update();
        }
    }
}