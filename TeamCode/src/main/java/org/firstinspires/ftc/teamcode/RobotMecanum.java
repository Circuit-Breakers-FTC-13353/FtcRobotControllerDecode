// Filename: RobotMecanum.java
package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.VoltageUnit;
import org.firstinspires.ftc.teamcode.draft.SystemHealthMonitor;
import java.util.List;

/**
 * =================================================================================================
 * ROBOT MECANUM - HARDWARE ABSTRACTION CLASS
 * =================================================================================================
 *
 * This class represents the robot's hardware and provides a centralized way to control it.
 * It follows the "Hardware Abstraction" design pattern, which means that all direct interactions
 * with hardware components (motors, sensors, etc.) are handled here. Your OpModes (like TeleOp
 * and Autonomous) should create an instance of this class to interact with the robot, rather
 * than accessing the hardware directly.
 *
 * This approach makes the code cleaner, easier to maintain, and allows you to reuse robot
 * control logic across multiple OpModes.
 *
 * Key Responsibilities:
 * - Initializing all hardware components.
 * - Providing methods to control the robot's movement (e.g., driving, stopping).
 * - Encapsulating sensor logic (e.g., IMU).
 * - Running background tasks like the System Health Monitor.
 *
 * @version 2.0 - Renamed drive() to driveRobotCentric() and added comprehensive documentation.
 * @author Team 13353
 */
public class RobotMecanum {

    // --- HARDWARE COMPONENT DECLARATIONS ---
    // Drivetrain Motors
    public DcMotor leftFront, rightFront, leftRear, rightRear;

    // Sensors
    public IMU imu;

    // --- STATE & HELPER CLASS DECLARATIONS ---
    private final HardwareMap hardwareMap;
    public SystemHealthMonitor healthMonitor;
    private List<LynxModule> allHubs; // Used for bulk data reads and health monitoring

    /**
     * The constructor for the RobotMecanum class.
     * @param hwMap The HardwareMap from the OpMode, used to map string names to hardware devices.
     */
    public RobotMecanum(HardwareMap hwMap) {
        this.hardwareMap = hwMap;
    }

    /**
     * Initializes all the hardware components on the robot.
     * This method should be called from the OpMode's `runOpMode()` method before `waitForStart()`.
     * @return True if initialization is successful, false otherwise.
     */
    public boolean init() {
        try {
            // --- HARDWARE MAPPING ---
            // Retrieve and assign hardware devices from the configuration.
            leftFront = hardwareMap.get(DcMotor.class, "leftFront");
            rightFront = hardwareMap.get(DcMotor.class, "rightFront");
            leftRear = hardwareMap.get(DcMotor.class, "leftRear");
            rightRear = hardwareMap.get(DcMotor.class, "rightRear");
            imu = hardwareMap.get(IMU.class, "imu");

            // Get all REV hubs for health monitoring and bulk reading.
            allHubs = hardwareMap.getAll(LynxModule.class);

            // ====================================================================================
            // --- DRIVETRAIN CONFIGURATION ---
            // ====================================================================================
            // Set the direction of the motors. This is one of the most common things you'll need
            // to change for your specific robot.
            //
            // HOW TO CONFIGURE:
            // 1.  Upload this code to your robot.
            // 2.  Run the TeleOp and push FORWARD on the left joystick.
            // 3.  Observe which wheels are spinning in the wrong direction.
            // 4.  For every wheel spinning backward, change its direction from FORWARD to REVERSE.
            //     For example, if the left front wheel is backward, change its line to:
            //     leftFront.setDirection(DcMotor.Direction.FORWARD);
            //
            // Standard Mecanum Configuration:
            // - Left side motors should be REVERSED.
            // - Right side motors should be FORWARD.
            leftFront.setDirection(DcMotor.Direction.REVERSE);
            leftRear.setDirection(DcMotor.Direction.REVERSE);
            rightFront.setDirection(DcMotor.Direction.FORWARD);
            rightRear.setDirection(DcMotor.Direction.FORWARD);

            // Set motor run mode. RUN_WITHOUT_ENCODER is typical for TeleOp driving.
            leftFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            rightFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            leftRear.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            rightRear.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

            // Set zero power behavior. BRAKE means the motors will actively resist movement
            // when the power is zero, which is good for stopping quickly.
            leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            leftRear.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            rightRear.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

            // ====================================================================================
            // --- IMU CONFIGURATION ---
            // ====================================================================================
            // Set the orientation of the Control Hub on your robot. This is critical for
            // field-centric driving to work correctly.
            //
            // HOW TO CONFIGURE:
            // - LogoFacingDirection: The direction the REV Robotics logo is facing on your Control Hub.
            // - UsbFacingDirection:  The direction the USB ports are facing on your Control Hub.
            //
            // Common Orientations:
            // - Hub laying flat, USB ports forward: Logo UP, USB FORWARD
            // - Hub mounted vertically, USB ports up: Logo FORWARD, USB UP
            RevHubOrientationOnRobot.LogoFacingDirection logoDirection = RevHubOrientationOnRobot.LogoFacingDirection.UP;
            RevHubOrientationOnRobot.UsbFacingDirection usbDirection = RevHubOrientationOnRobot.UsbFacingDirection.FORWARD;
            imu.initialize(new IMU.Parameters(new RevHubOrientationOnRobot(logoDirection, usbDirection)));

            // --- INITIALIZE HELPER CLASSES ---
            healthMonitor = new SystemHealthMonitor();
            healthMonitor.init(allHubs);

            return true; // Initialization successful

        } catch (Exception e) {
            // If any error occurs during initialization (e.g., a device is not configured),
            // this will prevent the OpMode from crashing.
            return false; // Initialization failed
        }
    }

    /**
     * The main update loop for the robot.
     * This method should be called in every iteration of the OpMode's main loop. It's used for
     * running background tasks like updating the health monitor.
     * @param matchTimer The OpMode's ElapsedTime timer.
     */
    public void update(ElapsedTime matchTimer) {
        healthMonitor.update(matchTimer);
    }

    // ============================================================================================
    // --- DRIVETRAIN CONTROL METHODS ---
    // ============================================================================================

    /**
     * Drives the robot using robot-centric controls.
     * "Forward" is always the direction the front of the robot is facing.
     * @param forward The power for moving forward and backward (-1.0 to 1.0).
     * @param strafe The power for strafing left and right (-1.0 to 1.0).
     * @param turn The power for turning left and right (-1.0 to 1.0).
     */
    public void driveRobotCentric(double forward, double strafe, double turn) {
        // Standard mecanum drive formulas
        double leftFrontPower = forward + strafe + turn;
        double rightFrontPower = forward - strafe - turn;
        double leftRearPower = forward - strafe + turn;
        double rightRearPower = forward + strafe - turn;

        // Normalize the motor powers to ensure no value exceeds 1.0 while maintaining proportions.
        double denominator = Math.max(Math.abs(forward) + Math.abs(strafe) + Math.abs(turn), 1.0);
        leftFront.setPower(leftFrontPower / denominator);
        rightFront.setPower(rightFrontPower / denominator);
        leftRear.setPower(leftRearPower / denominator);
        rightRear.setPower(rightRearPower / denominator);
    }

    /**
     * Drives the robot using field-centric controls.
     * "Forward" is always the same direction on the field, regardless of the robot's orientation.
     * @param forward The power for moving away from the driver (-1.0 to 1.0).
     * @param strafe The power for moving left and right relative to the driver (-1.0 to 1.0).
     * @param turn The power for turning left and right (-1.0 to 1.0).
     */
    public void driveFieldCentric(double forward, double strafe, double turn) {
        // Get the robot's current heading in radians from the IMU.
        double heading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);

        // This is the core of field-centric driving. It rotates the joystick inputs
        // by the negative of the robot's heading, effectively making the controls
        // relative to the field instead of the robot.
        double rotX = strafe * Math.cos(-heading) - forward * Math.sin(-heading);
        double rotY = strafe * Math.sin(-heading) + forward * Math.cos(-heading);

        // Use the rotated values in the standard mecanum drive formulas.
        double leftFrontPower = rotY + rotX + turn;
        double rightFrontPower = rotY - rotX - turn;
        double leftRearPower = rotY - rotX + turn;
        double rightRearPower = rotY + rotX - turn;

        // Normalize the motor powers.
        double denominator = Math.max(Math.abs(rotY) + Math.abs(rotX) + Math.abs(turn), 1.0);
        leftFront.setPower(leftFrontPower / denominator);
        rightFront.setPower(rightFrontPower / denominator);
        leftRear.setPower(leftRearPower / denominator);
        rightRear.setPower(rightRearPower / denominator);
    }

    /**
     * Stops all drivetrain motors.
     */
    public void stop() {
        leftFront.setPower(0);
        rightFront.setPower(0);
        leftRear.setPower(0);
        rightRear.setPower(0);
    }

    // ============================================================================================
    // --- UTILITY AND DEBUGGING METHODS ---
    // ============================================================================================

    /**
     * Gets the current voltage of the robot's main battery.
     * @return The battery voltage in Volts. Returns 0 if unable to read.
     */
    public double getVoltage() {
        if (allHubs != null && !allHubs.isEmpty()) {
            return allHubs.get(0).getInputVoltage(VoltageUnit.VOLTS);
        }
        return 0;
    }

    /**
     * A simple stress test method to run all motors at a given power.
     * Useful for testing battery performance and checking motor directions.
     * @param power The power to apply to the motors (-1.0 to 1.0).
     */
    public void performStressTest(double power) {
        leftFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        leftRear.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightRear.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        leftFront.setPower(power);
        rightFront.setPower(power);
        leftRear.setPower(power);
        rightRear.setPower(power);
    }
}