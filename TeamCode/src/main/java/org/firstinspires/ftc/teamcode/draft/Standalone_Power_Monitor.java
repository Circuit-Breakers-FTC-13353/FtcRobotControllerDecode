package org.firstinspires.ftc.teamcode.draft;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.robotcore.external.navigation.VoltageUnit;

import java.util.List;

/**
 * A standalone tool to diagnose complex electrical issues.
 * ... (full Javadoc header) ...
 */
@TeleOp(name = "Standalone: Power Monitor", group = "Standalone Tools")
public class Standalone_Power_Monitor extends LinearOpMode {

    // A list to hold all the hubs on the robot (Control and Expansion)
    private List<LynxModule> allHubs;

    // The voltage at which we'll show a warning.
    private final double VOLTAGE_WARNING_THRESHOLD = 12.0;

    @Override
    public void runOpMode() throws InterruptedException {
        telemetry.addLine("Power Monitor Initializing...");
        telemetry.update();

        try {
            // Get a list of all hubs on the robot. This is the key to this tool.
            allHubs = hardwareMap.getAll(LynxModule.class);
        } catch (Exception e) {
            telemetry.addLine("\n!!! LYNX MODULE (HUB) NOT FOUND !!!");
            telemetry.addData("Error", e.getMessage());
            telemetry.update();
            while(opModeIsActive()) { sleep(100); }
            return;
        }

        telemetry.addLine("Hubs Detected: " + allHubs.size());
        telemetry.addLine("Press START to begin monitoring.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            telemetry.clearAll();
            telemetry.addLine("--- Standalone Power Monitor ---");
            telemetry.addLine("Run your other OpModes (like TeleOp) while this is active");
            telemetry.addLine("in the background to see live power draw.");
            telemetry.addLine();

            double totalCurrentAmps = 0;
            double inputVoltage = 0;

            // Loop through all the hubs on the robot
            for (LynxModule hub : allHubs) {
                // Get the total current draw for this specific hub
                double hubCurrentAmps = hub.getCurrent(CurrentUnit.AMPS);
                totalCurrentAmps += hubCurrentAmps;

                // Get the input voltage. This will be the same for all hubs,
                // so we can just grab the last one.
                inputVoltage = hub.getInputVoltage(VoltageUnit.VOLTS);

                String hubName = hub.isParent() ? "Control Hub" : "Expansion Hub";
                telemetry.addData(hubName + " Current", "%.3f Amps", hubCurrentAmps);
            }

            telemetry.addLine();
            telemetry.addLine("--- Robot Totals ---");
            telemetry.addData("Total Current Draw", "%.3f Amps", totalCurrentAmps);

            // Add a warning if the voltage is getting low
            String voltageStatus = (inputVoltage < VOLTAGE_WARNING_THRESHOLD) ? "  <-- LOW VOLTAGE!" : "";
            telemetry.addData("Input Voltage", "%.2f Volts" + voltageStatus, inputVoltage);

            telemetry.update();
        }
    }
}