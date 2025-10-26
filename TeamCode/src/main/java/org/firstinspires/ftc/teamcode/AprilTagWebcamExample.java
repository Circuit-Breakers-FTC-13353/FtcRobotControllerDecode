package org.firstinspires.ftc.teamcode;

import android.util.Size;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;

/**
 * An example OpMode that demonstrates how to use the improved AprilTagWebcam class.
 * This OpMode initializes the webcam, continuously looks for AprilTags,
 * and displays telemetry for a specific tag ID (e.g., ID 20).
 * It follows FTC best practices for OpMode structure and resource management.
 *
 * Original code attribution: Coach Brogan M. Pratt
 * Improvements and documentation by the user and an AI assistant.
 */
@Autonomous(name = "AprilTag Webcam Example", group = "Examples")
public class AprilTagWebcamExample extends OpMode {

    /** An instance of our AprilTagWebcam helper class. */
    private final AprilTagWebcam aprilTagWebcam = new AprilTagWebcam();

    // Define constants for easy configuration
    private static final String WEBCAM_NAME = "Webcam 1";
    private static final int TARGET_TAG_ID = 20;

    /**
     * This method is run once when the "INIT" button is pressed on the Driver Station.
     * It initializes the AprilTag webcam by calling our helper class.
     */
    @Override
    public void init() {
        telemetry.addLine("Initializing AprilTag Webcam...");
        telemetry.update();

        // Initialize the AprilTag webcam helper class with specific parameters.
        aprilTagWebcam.init(hardwareMap, telemetry, WEBCAM_NAME, new Size(640, 480));
    }

    /**
     * This method is run repeatedly after "INIT" is pressed but before "START" is pressed.
     * It can be used for tasks like displaying initialization status.
     */
    @Override
    public void init_loop() {
        // You can add telemetry here to confirm initialization is complete
        // The init() method in AprilTagWebcam already provides status telemetry.
    }

    /**
     * This method is run once when the "START" button is pressed.
     */
    @Override
    public void start() {
        // Any code that needs to run once at the beginning of the autonomous period can go here.
    }

    /**
     * This method is run repeatedly after the "START" button is pressed until the OpMode is stopped.
     * It continuously updates AprilTag detections and displays relevant telemetry.
     */
    @Override
    public void loop() {
        // Get the latest detections from the camera.
        aprilTagWebcam.update();

        // Try to find the AprilTag with our target ID.
        AprilTagDetection targetTag = aprilTagWebcam.getTagBySpecificId(TARGET_TAG_ID);

        // Check if the target tag was found before trying to display its data.
        if (targetTag != null) {
            telemetry.addData("Status", "Target Tag (ID " + TARGET_TAG_ID + ") Found!");

            // Display the detailed telemetry for the detected tag.
            aprilTagWebcam.displayDetectionTelemetry(targetTag);

            // Here you could add robot logic based on the tag's position, e.g.:
            // double range = targetTag.ftcPose.range;
            // double bearing = targetTag.ftcPose.bearing;
            // driveTowardsTag(range, bearing);

        } else {
            // Provide feedback that the target tag is not visible.
            telemetry.addData("Status", "Target Tag (ID " + TARGET_TAG_ID + ") is not visible.");

            // Also display the total number of tags currently visible.
            telemetry.addData("Detected Tags", aprilTagWebcam.getDetectedTags().size());
        }

        // Update the telemetry on the Driver Station screen. This is crucial!
        telemetry.update();
    }


    /**
     * This method is run once when the OpMode is stopped (e.g., by pressing the stop button).
     * It's a critical place to release hardware resources.
     */
    @Override
    public void stop() {
        // Stop the vision portal to release camera resources.
        aprilTagWebcam.stop();
        telemetry.addLine("Webcam stopped.");
        telemetry.update();
    }
}