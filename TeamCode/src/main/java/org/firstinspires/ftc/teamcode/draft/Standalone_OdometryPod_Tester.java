package org.firstinspires.ftc.teamcode.draft;


import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * A standalone tool to test the three unpowered odometry pod encoders.
 * ... (full Javadoc header) ...
 */
@TeleOp(name = "Standalone: Odometry Pod Tester", group = "Standalone Tools")
public class Standalone_OdometryPod_Tester extends LinearOpMode {

    // IMPORTANT: Change these to the names of your odometry encoders in the config.
    // These must be configured as DcMotor objects.
    private final String LEFT_ENCODER_NAME = "leftFront"; // Parallel pod
    private final String RIGHT_ENCODER_NAME = "rightFront"; // Parallel pod
    private final String PERPENDICULAR_ENCODER_NAME = "leftRear"; // Perpendicular/Strafe pod

    private DcMotor leftEncoder, rightEncoder, perpendicularEncoder;

    @Override
    public void runOpMode() throws InterruptedException {
        telemetry.addLine("Odometry Pod Tester Initializing...");
        telemetry.update();

        try {
            leftEncoder = hardwareMap.get(DcMotor.class, LEFT_ENCODER_NAME);
            rightEncoder = hardwareMap.get(DcMotor.class, RIGHT_ENCODER_NAME);
            perpendicularEncoder = hardwareMap.get(DcMotor.class, PERPENDICULAR_ENCODER_NAME);

            // It is CRITICAL to reset the encoders at the start.
            leftEncoder.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            rightEncoder.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            perpendicularEncoder.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

            // This is the standard mode for reading encoders without powering the motor.
            leftEncoder.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            rightEncoder.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            perpendicularEncoder.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        } catch (Exception e) {
            telemetry.addLine("\n!!! AN ODOMETRY ENCODER WAS NOT FOUND !!!");
            telemetry.addData("Error", e.getMessage());
            telemetry.update();
            while(opModeIsActive()) { sleep(100); }
            return;
        }

        telemetry.addLine("Odometry Pods Initialized and Reset.");
        telemetry.addLine("Press START to begin.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            telemetry.clearAll();
            telemetry.addLine("--- Standalone Odometry Pod Tester ---");
            telemetry.addLine("Manually push the robot to see values change.");
            telemetry.addLine("Press (Y) on Gamepad 1 to RESET all encoders.");
            telemetry.addLine();
            telemetry.addData("Left Encoder (Parallel)", leftEncoder.getCurrentPosition());
            telemetry.addData("Right Encoder (Parallel)", rightEncoder.getCurrentPosition());
            telemetry.addData("Perpendicular Encoder (Strafe)", perpendicularEncoder.getCurrentPosition());
            telemetry.addLine();
            telemetry.addLine("ACTION: Push robot FORWARD. Left and Right should change.");
            telemetry.addLine("ACTION: Push robot SIDEWAYS. Perpendicular should change.");

            if (gamepad1.y) {
                leftEncoder.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                rightEncoder.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                perpendicularEncoder.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

                leftEncoder.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                rightEncoder.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                perpendicularEncoder.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

                telemetry.addLine("\n--> Encoders Reset! <--");
            }

            telemetry.update();
        }
    }
}