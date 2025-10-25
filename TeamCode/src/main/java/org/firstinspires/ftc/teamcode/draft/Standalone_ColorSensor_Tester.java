package org.firstinspires.ftc.teamcode.draft;

import android.graphics.Color;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;

/**
 * A standalone tool to find the precise color values of game elements.
 * ... (full Javadoc header) ...
 */
@TeleOp(name = "Standalone: Color Sensor Tester", group = "Standalone Tools")
public class Standalone_ColorSensor_Tester extends LinearOpMode {

    // IMPORTANT: Change this to the name of your color sensor in the config
    private final String SENSOR_NAME = "colorSensor";
    private ColorSensor colorSensor;

    @Override
    public void runOpMode() throws InterruptedException {
        telemetry.addLine("Color Sensor Tester Initializing...");
        telemetry.update();

        try {
            colorSensor = hardwareMap.get(ColorSensor.class, SENSOR_NAME);
            colorSensor.enableLed(true); // Turn on the LED for consistent readings
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

        // Array to hold HSV values
        float[] hsvValues = new float[3];

        while (opModeIsActive()) {
            telemetry.clearAll();
            telemetry.addLine("--- Standalone Color Sensor Tester ---");
            telemetry.addLine("Place a game element in front of the sensor.");
            telemetry.addLine();

            // Get RGB values
            int red = colorSensor.red();
            int green = colorSensor.green();
            int blue = colorSensor.blue();
            int alpha = colorSensor.alpha(); // Proximity/light intensity

            // Convert RGB to HSV for more reliable color detection
            Color.RGBToHSV(red, green, blue, hsvValues);
            float hue = hsvValues[0];
            float saturation = hsvValues[1];
            float value = hsvValues[2];

            telemetry.addData("Sensor Name", SENSOR_NAME);
            telemetry.addLine();
            telemetry.addLine("--- RGB Values ---");
            telemetry.addData("Red", red);
            telemetry.addData("Green", green);
            telemetry.addData("Blue", blue);
            telemetry.addData("Alpha (Brightness)", alpha);
            telemetry.addLine();
            telemetry.addLine("--- HSV Values (RECOMMENDED) ---");
            telemetry.addData("Hue (0-360)", "%.1f", hue);
            telemetry.addData("Saturation (0-1)", "%.3f", saturation);
            telemetry.addData("Value (0-1)", "%.3f", value);

            telemetry.update();
        }
    }
}