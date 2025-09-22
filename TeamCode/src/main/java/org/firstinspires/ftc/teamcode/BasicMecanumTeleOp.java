// Filename: BasicMecanumTeleOp.java
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Basic Mecanum TeleOp", group = "Competition")
public class BasicMecanumTeleOp extends LinearOpMode {

    // Create an instance of our Robot class.
    private Robot robot;

    @Override
    public void runOpMode() {
        // Instantiate the robot by passing it the hardwareMap from this OpMode.
        robot = new Robot(hardwareMap);

        // Initialize the robot's hardware.
        // The init() method returns a boolean, so we can check if it was successful.
        if (!robot.init()) {
            telemetry.addLine("ERROR: Hardware initialization failed. Please check configuration.");
            telemetry.update();
            // Wait for the OpMode to be stopped.
            while (opModeIsActive()) {
                sleep(20);
            }
            return; // Exit the OpMode
        }

        telemetry.addLine("Robot initialized successfully. Ready to start.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // Get joystick inputs. Note the negative sign on the y-axis, as
            // pushing forward on the stick gives a negative value.
            double forward = -gamepad1.left_stick_y;
            double strafe = gamepad1.left_stick_x;
            double turn = gamepad1.right_stick_x;

            // Call the high-level drive method on our robot object.
            robot.drive(forward, strafe, turn);

            // Add any other TeleOp logic here (e.g., controlling an arm).
            // telemetry.addData("Heading", robot.imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES));
            // telemetry.update();
        }
    }
}