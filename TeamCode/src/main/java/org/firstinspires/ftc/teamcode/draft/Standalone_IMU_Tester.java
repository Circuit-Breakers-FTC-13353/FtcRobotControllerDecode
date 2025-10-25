package org.firstinspires.ftc.teamcode.draft;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.IMU;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

/**
 * A standalone tool to test and calibrate the Inertial Measurement Unit (IMU).
 * ... (full Javadoc header) ...
 */
@TeleOp(name = "Standalone: IMU Tester", group = "Standalone Tools")
public class Standalone_IMU_Tester extends LinearOpMode {

    private IMU imu;

    @Override
    public void runOpMode() throws InterruptedException {
        telemetry.addLine("IMU Tester Initializing...");
        telemetry.update();

        try {
            imu = hardwareMap.get(IMU.class, "imu");

            // IMPORTANT: This is where you define the physical orientation of YOUR Control Hub.
            // For example, if the logo is facing UP and the USB ports are facing FORWARD:
            RevHubOrientationOnRobot.LogoFacingDirection logoDirection = RevHubOrientationOnRobot.LogoFacingDirection.UP;
            RevHubOrientationOnRobot.UsbFacingDirection usbDirection = RevHubOrientationOnRobot.UsbFacingDirection.FORWARD;
            RevHubOrientationOnRobot orientationOnRobot = new RevHubOrientationOnRobot(logoDirection, usbDirection);

            imu.initialize(new IMU.Parameters(orientationOnRobot));

        } catch (Exception e) {
            telemetry.addLine("\n!!! IMU NOT FOUND IN CONFIGURATION !!!");
            telemetry.addData("Error", e.getMessage());
            telemetry.update();
            // Wait forever, since the tool is useless without the IMU
            while(opModeIsActive()) { sleep(100); }
            return;
        }

        telemetry.addLine("IMU Initialized Successfully.");
        telemetry.addLine("Press START to begin.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // Get the current orientation angles from the IMU
            YawPitchRollAngles orientation = imu.getRobotYawPitchRollAngles();

            telemetry.clearAll();
            telemetry.addLine("--- Standalone IMU Tester ---");
            telemetry.addLine("Physically move the robot/hub to see values change.");
            telemetry.addLine("Press (Y) on Gamepad 1 to reset Yaw.");
            telemetry.addLine();
            telemetry.addData("Yaw (Heading)", "%.2f degrees", orientation.getYaw(AngleUnit.DEGREES));
            telemetry.addData("Pitch", "%.2f degrees", orientation.getPitch(AngleUnit.DEGREES));
            telemetry.addData("Roll", "%.2f degrees", orientation.getRoll(AngleUnit.DEGREES));
            telemetry.update();

            // Allow the user to reset the Yaw angle
            if (gamepad1.y) {
                imu.resetYaw();
                telemetry.addLine("\n--> YAW RESET! <--");
                telemetry.update();
                sleep(500); // Debounce
            }
        }
    }
}