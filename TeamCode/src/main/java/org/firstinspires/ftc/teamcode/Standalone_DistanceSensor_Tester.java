package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * A standalone tool to test and visualize the range of a distance sensor.
 * ... (full Javadoc header) ...
 */
@TeleOp(name = "Standalone: Distance Sensor Tester", group = "Standalone Tools")
public class Standalone_DistanceSensor_Tester extends LinearOpMode {

    // IMPORTANT: Change this to the name of your distance sensor in the config
    private final String SENSOR_NAME = "frontDistanceSensor";
    private DistanceSensor distanceSensor;

    @Override
    public void runOpMode() throws InterruptedException {
        telemetry.addLine("Distance Sensor Tester Initializing...");
        telemetry.update();

        try {
            distanceSensor = hardwareMap.get(DistanceSensor.class, SENSOR_NAME);
        } catch (Exception e) {
            telemetry.addLine("\n!!! SENSOR '" + SENSOR_NAME + "' NOT FOUND !!!");
            telemetry.addData("Error", e.getMessage());
            telemetry.update();
            while(opModeIsActive()) { sleep(100); }
            return;
        }

        telemetry.addLine("Sensor Initialized Successfully.");
        telemetry.addLine("Press START to begin.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            telemetry.clearAll();
            telemetry.addLine("--- Standalone Distance Sensor Tester ---");
            telemetry.addLine("Wave an object in front of the sensor.");
            telemetry.addLine();

            double dist_in = distanceSensor.getDistance(DistanceUnit.INCH);
            double dist_cm = distanceSensor.getDistance(DistanceUnit.CM);
            double dist_mm = distanceSensor.getDistance(DistanceUnit.MM);

            telemetry.addData("Sensor Name", SENSOR_NAME);
            telemetry.addData("Range (in)", "%.2f", dist_in);
            telemetry.addData("Range (cm)", "%.2f", dist_cm);
            telemetry.addData("Range (mm)", "%.2f", dist_mm);
            telemetry.addLine();

            // Create a simple text-based bar graph
            int barLength = 30;
            int filledLength = (int) (dist_in / 2.0); // Scale the graph (e.g., 1 char per 2 inches)
            if (filledLength > barLength) filledLength = barLength;
            if (filledLength < 0) filledLength = 0;

            String bar = "[";
            for (int i = 0; i < filledLength; i++) bar += "=";
            for (int i = 0; i < barLength - filledLength; i++) bar += " ";
            bar += "]";
            telemetry.addLine("Visual Range:");
            telemetry.addLine(bar);

            telemetry.update();
        }
    }
}