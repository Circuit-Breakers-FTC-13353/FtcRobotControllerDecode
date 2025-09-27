package org.firstinspires.ftc.teamcode;


import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.ExposureControl;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.GainControl;
import org.firstinspires.ftc.vision.VisionPortal;
import java.util.concurrent.TimeUnit;

/**
 * A standalone tool to find the optimal camera settings.
 * ... (full Javadoc header) ...
 */
@TeleOp(name = "Standalone: Camera Tuner", group = "Standalone Tools")
public class Standalone_Camera_Tuner extends LinearOpMode {

    // IMPORTANT: Change this to the name of your camera in the config
    private final String WEBCAM_NAME = "Webcam 1";
    private VisionPortal visionPortal;
    private ExposureControl exposureControl;
    private GainControl gainControl;

    // --- TUNING STATE ---
    private boolean isManualExposure = false;
    private int currentExposureMS;
    private int currentGain;

    @Override
    public void runOpMode() throws InterruptedException {
        telemetry.addLine("Camera Tuner Initializing...");
        telemetry.update();

        try {
            // Create a VisionPortal to just view the camera stream.
            visionPortal = new VisionPortal.Builder()
                    .setCamera(hardwareMap.get(WebcamName.class, WEBCAM_NAME))
                    // .setCameraResolution(new Size(640, 480)) // Optional
                    .enableLiveView(true)
                    .build();
        } catch (Exception e) {
            telemetry.addLine("\n!!! WEBCAM '" + WEBCAM_NAME + "' NOT FOUND !!!");
            telemetry.addData("Error", e.getMessage());
            telemetry.update();
            while(opModeIsActive()) { sleep(100); }
            return;
        }

        // Wait for the camera to be streaming before trying to access controls
        while (visionPortal.getCameraState() != VisionPortal.CameraState.STREAMING) {
            telemetry.addLine("Waiting for camera to start streaming...");
            telemetry.update();
            sleep(20);
        }

        // Get the camera controls. This will fail if the camera does not support them.
        try {
            exposureControl = visionPortal.getCameraControl(ExposureControl.class);
            gainControl = visionPortal.getCameraControl(GainControl.class);
        } catch (Exception e) {
            telemetry.addLine("\n!!! Camera does not support Exposure/Gain control !!!");
            telemetry.update();
            sleep(3000);
        }

        telemetry.addLine("Camera Tuner Initialized.");
        telemetry.addLine("View camera stream on Driver Station.");
        telemetry.addLine("Press START to begin tuning.");
        telemetry.update();

        waitForStart();

        // Start in Auto Exposure mode
        if (exposureControl != null) {
            exposureControl.setMode(ExposureControl.Mode.Auto);
            isManualExposure = false;
        }

        while (opModeIsActive()) {
            handleGamepadInput();
            displayTelemetry();
        }
    }

    private void handleGamepadInput() {
        if (exposureControl == null || gainControl == null) return;

        // Toggle between Auto and Manual exposure mode
        if (gamepad1.x) {
            if (isManualExposure) {
                exposureControl.setMode(ExposureControl.Mode.Auto);
            } else {
                exposureControl.setMode(ExposureControl.Mode.Manual);
            }
            isManualExposure = !isManualExposure;
            sleep(250);
        }

        // If in Manual mode, allow adjustments
        if (isManualExposure) {
            // Adjust Exposure
            if (gamepad1.dpad_up) {
                long newExposure = exposureControl.getExposure(TimeUnit.MILLISECONDS) + 1;
                exposureControl.setExposure(newExposure, TimeUnit.MILLISECONDS);
                sleep(150);
            } else if (gamepad1.dpad_down) {
                long newExposure = exposureControl.getExposure(TimeUnit.MILLISECONDS) - 1;
                if (newExposure < exposureControl.getMinExposure(TimeUnit.MILLISECONDS)) {
                    newExposure = exposureControl.getMinExposure(TimeUnit.MILLISECONDS);
                }
                exposureControl.setExposure(newExposure, TimeUnit.MILLISECONDS);
                sleep(150);
            }

            // Adjust Gain
            if (gamepad1.dpad_right) {
                gainControl.setGain(gainControl.getGain() + 1);
                sleep(150);
            } else if (gamepad1.dpad_left) {
                int newGain = gainControl.getGain() - 1;
                if (newGain < gainControl.getMinGain()) newGain = gainControl.getMinGain();
                gainControl.setGain(newGain);
                sleep(150);
            }
        }
    }

    private void displayTelemetry() {
        telemetry.clearAll();
        telemetry.addLine("--- Standalone Camera Tuner ---");
        telemetry.addLine("View live feed on Driver Station.");
        telemetry.addLine();

        if (exposureControl == null || gainControl == null) {
            telemetry.addLine("This camera does not support manual controls.");
            telemetry.update();
            return;
        }

        telemetry.addData("Mode", isManualExposure ? "MANUAL" : "AUTO");
        telemetry.addLine("Press (X) to toggle Auto/Manual mode.");
        telemetry.addLine();

        if (isManualExposure) {
            telemetry.addLine("--- Manual Controls ---");
            telemetry.addLine("D-Pad U/D: Adjust Exposure");
            telemetry.addLine("D-Pad L/R: Adjust Gain");

            // Get current values
            currentExposureMS = (int) exposureControl.getExposure(TimeUnit.MILLISECONDS);
            currentGain = gainControl.getGain();

            telemetry.addData("Exposure", "%d ms", currentExposureMS);
            telemetry.addData("Gain", currentGain);
        } else {
            telemetry.addLine("In AUTO mode. Press (X) to switch to MANUAL.");
        }

        telemetry.addLine("\nRecord these values to use in your main vision code.");
        telemetry.update();
    }
}