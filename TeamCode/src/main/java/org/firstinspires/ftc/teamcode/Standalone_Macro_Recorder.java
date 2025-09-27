package org.firstinspires.ftc.teamcode;


import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A standalone tool to create simple autonomous paths by recording driver movements.
 * NOTE: This is a conceptual, time-based recorder. A more advanced version
 * would use encoder-based recordings.
 * ... (full Javadoc header) ...
 */
@TeleOp(name = "Standalone: Macro Recorder", group = "Standalone Tools")
public class Standalone_Macro_Recorder extends LinearOpMode {

    // A list to store our recorded commands
    private ArrayList<String> recordedCommands = new ArrayList<>();
    private ElapsedTime timer = new ElapsedTime();
    private boolean isRecording = false;

    // We will use a simplified Robot class for this example
    private Robot robot;

    @Override
    public void runOpMode() throws InterruptedException {
        robot = new Robot(hardwareMap);
        robot.init(); // Assuming a basic init

        telemetry.addLine("Macro Recorder Initialized.");
        telemetry.addLine("Press (A) to START/STOP recording.");
        telemetry.addLine("Press (B) to SAVE the recording.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // --- Driving ---
            double forward = -gamepad1.left_stick_y;
            double strafe = gamepad1.left_stick_x;
            double turn = gamepad1.right_stick_x;
            robot.drive(forward, strafe, turn);

            // --- Recording Logic ---
            if (gamepad1.a) {
                if (!isRecording) {
                    // Start a new recording
                    recordedCommands.clear();
                    isRecording = true;
                    timer.reset();
                } else {
                    // Stop the current recording
                    isRecording = false;
                }
                sleep(500); // Debounce
            }

            if (isRecording) {
                // Add a drive command to our list
                // Format: "drive,forward,strafe,turn,duration"
                String command = String.format("drive,%.2f,%.2f,%.2f,%.3f",
                        forward, strafe, turn, timer.seconds());
                recordedCommands.add(command);
                timer.reset(); // Reset timer for the next command's duration
            }

            // --- Saving Logic ---
            if (gamepad1.b && !isRecording && !recordedCommands.isEmpty()) {
                saveRecording("my_macro.txt");
                recordedCommands.clear(); // Clear after saving
                sleep(500); // Debounce
            }

            // --- Telemetry ---
            telemetry.clearAll();
            telemetry.addLine("--- Standalone Macro Recorder ---");
            telemetry.addData("Recording Status", isRecording ? "RECORDING..." : "Stopped");
            telemetry.addData("Commands Recorded", recordedCommands.size());
            telemetry.addLine();
            telemetry.addLine("Press (A) to Start/Stop Recording.");
            telemetry.addLine("Press (B) to Save Recording.");
            telemetry.update();
        }
    }

    private void saveRecording(String filename) {
        File file = AppUtil.getInstance().getSettingsFile(filename);
        try {
            FileWriter writer = new FileWriter(file, false);
            for (String command : recordedCommands) {
                writer.write(command + "\n");
            }
            writer.close();
            telemetry.addLine("--> Recording SAVED to " + filename + " <--");
            telemetry.update();
        } catch (IOException e) {
            telemetry.addLine("--> FAILED to save recording! <--");
            telemetry.update();
        }
    }
}
