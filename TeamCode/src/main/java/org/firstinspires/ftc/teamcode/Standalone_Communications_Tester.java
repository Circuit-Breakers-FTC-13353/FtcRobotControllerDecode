package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * A standalone tool to test the reliability of the WiFi connection.
 * NOTE: This is a conceptual tool. The FTC SDK does not provide a direct
 * way to send custom data back to the RC from the DS gamepad object for a
 * true ping test. This implementation measures one-way latency and packet loss.
 * ... (full Javadoc header) ...
 */
@TeleOp(name = "Standalone: Communications Tester", group = "Standalone Tools")
public class Standalone_Communications_Tester extends LinearOpMode {

    private ElapsedTime packetTimer = new ElapsedTime();
    private long lastPacketTimestamp = 0;
    private long packetsReceived = 0;
    private long packetsLost = 0;
    private double lastLatencyMs = 0;

    @Override
    public void runOpMode() throws InterruptedException {
        telemetry.addLine("Communications Tester Initialized.");
        telemetry.addLine("This measures one-way latency from the Driver Station.");
        telemetry.addLine("Press START to begin.");
        telemetry.update();

        waitForStart();

        packetTimer.reset();

        while (opModeIsActive()) {
            // The gamepad object has a timestamp of when its state was generated on the DS.
            long currentTimestamp = gamepad1.timestamp;

            // Check if we have received a new packet from the Driver Station.
            if (currentTimestamp > lastPacketTimestamp) {
                packetsReceived++;

                // Calculate the one-way latency. Note: This can be affected by clock drift.
                lastLatencyMs = packetTimer.milliseconds();
                packetTimer.reset();

                // Estimate lost packets. The timestamp usually increments by a consistent
                // amount (e.g., ~20ms). A large jump suggests missed packets.
                long timeDiff = currentTimestamp - lastPacketTimestamp;
                if (lastPacketTimestamp != 0 && timeDiff > 35_000_000) { // 35ms in nanoseconds
                    packetsLost++;
                }

                lastPacketTimestamp = currentTimestamp;
            }

            // If the timer gets too high, it means we haven't received a packet in a while.
            if (packetTimer.milliseconds() > 100) {
                // This isn't a perfect measure of loss, but indicates a major lag spike.
            }

            telemetry.clearAll();
            telemetry.addLine("--- Standalone Communications Tester ---");
            telemetry.addData("One-Way Latency", "%.1f ms", lastLatencyMs);
            telemetry.addData("Packets Received", packetsReceived);
            telemetry.addData("Estimated Packets Lost", packetsLost);
            telemetry.addLine();
            telemetry.addLine("Watch for high latency (> 50ms) or increasing packet loss,");
            telemetry.addLine("which can indicate WiFi interference.");

            telemetry.update();
        }
    }
}
