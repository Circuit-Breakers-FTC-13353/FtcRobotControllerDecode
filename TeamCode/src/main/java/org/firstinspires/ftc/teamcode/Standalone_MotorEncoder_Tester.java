package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

/**
 * A standalone tool to test a single motor's encoder.
 * ... (full Javadoc header) ...
 */
@TeleOp(name = "Standalone: Motor Encoder Tester", group = "Standalone Tools")
public class Standalone_MotorEncoder_Tester extends LinearOpMode {

    // *** IMPORTANT: CHANGE THESE VALUES FOR THE MOTOR YOU ARE TESTING ***
    private final String MOTOR_NAME = "armMotor"; // The name in your config file
    private final DcMotorSimple.Direction MOTOR_DIRECTION = DcMotorSimple.Direction.FORWARD;
    private final double TEST_POWER = 0.20; // Run at a slow, constant power

    private DcMotor motor;

    @Override
    public void runOpMode() throws InterruptedException {
        telemetry.addLine("Motor Encoder Tester Initializing...");
        telemetry.update();

        try {
            motor = hardwareMap.get(DcMotor.class, MOTOR_NAME);
            motor.setDirection(MOTOR_DIRECTION);
            motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

            // Reset the encoder to 0. This also sets the mode.
            motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            // We want to run with power, not velocity, so we use this mode.
            motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        } catch (Exception e) {
            telemetry.addLine("\n!!! MOTOR '" + MOTOR_NAME + "' NOT FOUND !!!");
            telemetry.addData("Error", e.getMessage());
            telemetry.update();
            while(opModeIsActive()) { sleep(100); }
            return;
        }

        telemetry.addLine("Motor '" + MOTOR_NAME + "' Initialized.");
        telemetry.addLine("Encoder has been reset to 0.");
        telemetry.addLine("Press START to begin.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            telemetry.clearAll();
            telemetry.addLine("--- Standalone Motor Encoder Tester ---");
            telemetry.addLine("Press (A) to run FORWARD | Press (B) to run REVERSE");
            telemetry.addLine("Press (Y) to RESET encoder to 0.");
            telemetry.addLine();

            double power = 0;
            if (gamepad1.a) {
                power = TEST_POWER;
            } else if (gamepad1.b) {
                power = -TEST_POWER;
            }
            motor.setPower(power);

            if (gamepad1.y) {
                motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            }

            telemetry.addData("Motor", MOTOR_NAME);
            telemetry.addData("Applied Power", "%.2f", power);
            telemetry.addData("Encoder Ticks", motor.getCurrentPosition());
            telemetry.addLine();
            telemetry.addLine("ACTION: Manually rotate motor. Does tick count change?");
            telemetry.addLine("ACTION: Run motor. Does tick count change rapidly?");

            telemetry.update();
        }
    }
}