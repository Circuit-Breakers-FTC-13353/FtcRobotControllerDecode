package org.firstinspires.ftc.teamcode;

import android.util.Size;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages an AprilTag detection pipeline using a webcam for a FIRST Tech Challenge robot.
 * This class encapsulates the initialization of the VisionPortal and AprilTagProcessor,
 * processes detections in a thread-safe manner, and provides methods to access detection data.
 * It is designed to be robust, reusable, and easy to integrate into any OpMode.
 *
 * Original code attribution: Coach Brogan M. Pratt
 * Improvements and documentation by the user and an AI assistant.
 */
public class AprilTagWebcam {

    /** The main processor for detecting AprilTags. */
    private AprilTagProcessor aprilTagProcessor;

    /** The portal that manages the camera feed and processors. */
    private VisionPortal visionPortal;

    /** A list to store the most recently detected AprilTags. Marked as volatile for thread safety. */
    private volatile List<AprilTagDetection> detectedTags = new ArrayList<>();

    /** The telemetry object for displaying data on the Driver Station. */
    private Telemetry telemetry;

    /**
     * Initializes the AprilTag detector and the camera with specified parameters.
     * This method is designed to be flexible, allowing for different webcam names and resolutions.
     * Includes error handling to prevent the OpMode from crashing if the webcam is not found.
     *
     * @param hwMap The HardwareMap from the OpMode, used to get the webcam.
     * @param telemetry The Telemetry object from the OpMode for output.
     * @param webcamName The name of the webcam as configured in the robot's configuration file.
     * @param resolution The desired camera resolution (e.g., new Size(640, 480)).
     */
    public void init(HardwareMap hwMap, Telemetry telemetry, String webcamName, Size resolution) {
        this.telemetry = telemetry;

        try {
            // Create the AprilTag processor with desired settings.
            aprilTagProcessor = new AprilTagProcessor.Builder()
                    .setDrawTagID(true)             // Draw the tag ID on the camera stream
                    .setDrawTagOutline(true)        // Draw a bounding box around the tag
                    .setDrawAxes(true)              // Draw the X, Y, and Z axes of the tag
                    .setDrawCubeProjection(true)    // Draw a 3D cube projecting from the tag
                    .setOutputUnits(DistanceUnit.INCH, AngleUnit.DEGREES) // Set units for pose estimation
                    .build();

            // Create the VisionPortal to manage the camera and processor.
            VisionPortal.Builder builder = new VisionPortal.Builder();
            builder.setCamera(hwMap.get(WebcamName.class, webcamName)); // Specify the webcam by name
            builder.setCameraResolution(resolution); // Set the camera resolution
            builder.addProcessor(aprilTagProcessor); // Add the AprilTag processor to the portal

            // Build the VisionPortal and start the camera stream.
            visionPortal = builder.build();
            telemetry.addData("Status", "AprilTagWebcam initialized successfully.");

        } catch (Exception e) {
            // Handle the case where the webcam is not configured or found.
            telemetry.addData("Error", "Could not initialize webcam '" + webcamName + "'. Check configuration.");
            telemetry.update(); // Display the error message immediately
            visionPortal = null; // Ensure visionPortal is null if initialization fails
        }
    }

    /**
     * Updates the list of detected AprilTags with the latest data from the processor.
     * This method should be called repeatedly in the main loop of an OpMode.
     * It will do nothing if the vision portal failed to initialize.
     */
    public void update() {
        if (visionPortal != null) {
            // Get the current list of AprilTag detections.
            // This call is thread-safe and returns an empty list if no tags are found.
            List<AprilTagDetection> currentDetections = aprilTagProcessor.getDetections();
            if (currentDetections != null) {
                this.detectedTags = currentDetections;
            } else {
                this.detectedTags = Collections.emptyList(); // Ensure list is never null
            }
        }
    }

    /**
     * Returns a copy of the list of all currently detected AprilTags.
     *
     * @return A List of {@link AprilTagDetection} objects. The list will be empty if no tags are detected.
     */
    public List<AprilTagDetection> getDetectedTags() {
        return new ArrayList<>(detectedTags); // Return a copy to prevent concurrent modification issues
    }

    /**
     * Displays detailed telemetry data for a specific AprilTag detection.
     * This includes the tag's ID, name (if available), and its pose (position and orientation).
     *
     * @param detection The AprilTagDetection object to display telemetry for. If null, a message is shown.
     */
    public void displayDetectionTelemetry(AprilTagDetection detection) {
        // Provide a summary of how many tags are currently detected.
        telemetry.addData("Detected Tags", detectedTags.size());

        // Robustly handle the case where the requested tag was not found.
        if (detection == null) {
            telemetry.addLine("The requested AprilTag was not detected.");
            return; // Exit the method early
        }

        // Check if the tag's metadata (name, etc.) is available.
        if (detection.metadata != null) {
            telemetry.addLine(String.format("\n==== (ID %d) %s", detection.id, detection.metadata.name));
            telemetry.addLine(String.format("XYZ %6.1f %6.1f %6.1f  (inch)", detection.ftcPose.x, detection.ftcPose.y, detection.ftcPose.z));
            telemetry.addLine(String.format("PRY %6.1f %6.1f %6.1f  (deg)", detection.ftcPose.pitch, detection.ftcPose.roll, detection.ftcPose.yaw));
            telemetry.addLine(String.format("RBE %6.1f %6.1f %6.1f  (inch, deg, deg)", detection.ftcPose.range, detection.ftcPose.bearing, detection.ftcPose.elevation));
        } else {
            // Display basic information if metadata is not available.
            telemetry.addLine(String.format("\n==== (ID %d) Unknown", detection.id));
            telemetry.addLine(String.format("Center %6.0f %6.0f   (pixels)", detection.center.x, detection.center.y));
        }
    }

    /**
     * Searches the list of detected tags for a tag with a specific ID.
     *
     * @param id The ID of the AprilTag to search for.
     * @return The {@link AprilTagDetection} object if found, otherwise null.
     */
    public AprilTagDetection getTagBySpecificId(int id) {
        // Iterate through the list of detected tags.
        for (AprilTagDetection tag : detectedTags) {
            if (tag.id == id) {
                // Return the matching tag.
                return tag;
            }
        }
        // Return null if no tag with the specified ID is found.
        return null;
    }

    /**
     * Stops the vision portal and releases camera resources.
     * This is crucial to call at the end of an OpMode to ensure the camera is freed.
     */
    public void stop() {
        if (visionPortal != null) {
            visionPortal.close();
            visionPortal = null; // Set to null to indicate it's been closed
        }
    }
}