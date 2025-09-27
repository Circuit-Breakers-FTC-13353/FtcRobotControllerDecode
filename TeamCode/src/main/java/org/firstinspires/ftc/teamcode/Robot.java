// Filename: Robot.java
package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;


/**
 * The Robot Hardware Abstraction Class.
 *
 * This class serves as a centralized hub for all robot hardware components and their control logic.
 * It follows the "Hardware Abstraction" design pattern, meaning that all direct interactions with
 * hardware are encapsulated here. OpModes should interact with this class to command the robot.
 * This modular approach makes the main OpMode code cleaner, easier to read, and easier to maintain.
 *
 * It also integrates our hybrid configuration system and includes a stall detector for the arm motor.
 *
 * @version 1.4 Master - Definitive, globally consistent, and bug-free version.
 * @author Team 13353
 */
public class Robot {

    // --- HARDWARE DECLARATIONS ---
    public DcMotor leftFront, rightFront, leftRear, rightRear;
    public IMU imu;
    public Servo clawServo, wristServo;
    public DcMotor armMotor; // Declared as standard DcMotor for abstraction
    public DistanceSensor frontDistanceSensor;

    // --- STATE & HELPERS ---
    private HardwareMap hardwareMap;
    private StallDetector armStallDetector;

    // --- CONFIGURATION CONSTANTS ---
    public double CLAW_OPEN_POSITION, CLAW_CLOSED_POSITION;
    public double WRIST_STOW_POSITION, WRIST_SCORE_POSITION;
    public double ARM_MANUAL_POWER_MULTIPLIER, ARM_POWER_LIMIT;
    public double ARM_STALL_THRESHOLD_AMPS;

    public Robot(HardwareMap hwMap) {
        this.hardwareMap = hwMap;
    }

    public boolean init() {
        try {
            // --- LOAD CONSTANTS ---
            CLAW_OPEN_POSITION = Config.getDouble("CLAW_OPEN_POSITION", Constants.CLAW_OPEN_POSITION);
            CLAW_CLOSED_POSITION = Config.getDouble("CLAW_CLOSED_POSITION", Constants.CLAW_CLOSED_POSITION);
            WRIST_STOW_POSITION = Config.getDouble("WRIST_STOW_POSITION", Constants.WRIST_STOW_POSITION);
            WRIST_SCORE_POSITION = Config.getDouble("WRIST_SCORE_POSITION", Constants.WRIST_SCORE_POSITION);
            ARM_MANUAL_POWER_MULTIPLIER = Config.getDouble("ARM_MANUAL_POWER_MULTIPLIER", Constants.ARM_MANUAL_POWER_MULTIPLIER);
            ARM_POWER_LIMIT = Config.getDouble("ARM_POWER_LIMIT", Constants.ARM_POWER_LIMIT);
            ARM_STALL_THRESHOLD_AMPS = Config.getDouble("ARM_STALL_THRESHOLD_AMPS", Constants.ARM_STALL_THRESHOLD_AMPS);

            // --- HARDWARE INIT ---
            leftFront = hardwareMap.get(DcMotor.class, "leftFront");
            rightFront = hardwareMap.get(DcMotor.class, "rightFront");
            leftRear = hardwareMap.get(DcMotor.class, "leftRear");
            rightRear = hardwareMap.get(DcMotor.class, "rightRear");
            imu = hardwareMap.get(IMU.class, "imu");
            clawServo = hardwareMap.get(Servo.class, "clawServo");
            wristServo = hardwareMap.get(Servo.class, "wristServo");
            armMotor = hardwareMap.get(DcMotor.class, "armMotor");
            frontDistanceSensor = hardwareMap.get(DistanceSensor.class, "frontDistanceSensor");

            // --- DRIVETRAIN CONFIG ---
            leftFront.setDirection(DcMotor.Direction.REVERSE);
            leftRear.setDirection(DcMotor.Direction.REVERSE);
            rightFront.setDirection(DcMotor.Direction.FORWARD);
            rightRear.setDirection(DcMotor.Direction.FORWARD);
            leftFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            rightFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            leftRear.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            rightRear.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            leftRear.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            rightRear.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

            // --- IMU CONFIG ---
            RevHubOrientationOnRobot.LogoFacingDirection logoDirection = RevHubOrientationOnRobot.LogoFacingDirection.UP;
            RevHubOrientationOnRobot.UsbFacingDirection usbDirection = RevHubOrientationOnRobot.UsbFacingDirection.FORWARD;
            imu.initialize(new IMU.Parameters(new RevHubOrientationOnRobot(logoDirection, usbDirection)));

            // --- ARM CONFIG ---
            armMotor.setDirection(DcMotorSimple.Direction.FORWARD);
            armMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            resetArmEncoder();
            armMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

            // --- CREATE HELPERS ---
            armStallDetector = new StallDetector(ARM_STALL_THRESHOLD_AMPS, 500);

            // Set initial mechanism positions
            closeClaw();
            stowWrist();

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * This method MUST be called in every loop of an OpMode.
     * It handles background tasks like updating the stall detector.
     */
    public void update() {
        armStallDetector.update(getArmCurrent(CurrentUnit.AMPS));
    }

    // --- Drivetrain Methods ---
    public void drive(double forward, double strafe, double turn) {
        double leftFrontPower = forward + strafe + turn;
        double rightFrontPower = forward - strafe - turn;
        double leftRearPower = forward - strafe + turn;
        double rightRearPower = forward + strafe - turn;
        double denominator = Math.max(Math.abs(forward) + Math.abs(strafe) + Math.abs(turn), 1.0);
        leftFront.setPower(leftFrontPower / denominator);
        rightFront.setPower(rightFrontPower / denominator);
        leftRear.setPower(leftRearPower / denominator);
        rightRear.setPower(rightRearPower / denominator);
    }
    public void stop() {
        leftFront.setPower(0); rightFront.setPower(0); leftRear.setPower(0); rightRear.setPower(0);
    }

    // --- Claw Methods ---
    public void openClaw() { clawServo.setPosition(CLAW_OPEN_POSITION); }
    public void closeClaw() { clawServo.setPosition(CLAW_CLOSED_POSITION); }
    public void setClawPosition(double position) { clawServo.setPosition(position); }

    // --- Wrist Methods ---
    public void stowWrist() { wristServo.setPosition(WRIST_STOW_POSITION); }
    public void scoreWrist() { wristServo.setPosition(WRIST_SCORE_POSITION); }
    public void setWristPosition(double position) { wristServo.setPosition(position); }

    // --- Arm Methods ---
    public void setArmPower(double power) {
        // Stall protection is handled by the OpMode, which checks isArmStalled().
        // This method just passes power through.
        armMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        armMotor.setPower(power * ARM_MANUAL_POWER_MULTIPLIER);
    }
    public int getArmPosition() { return armMotor.getCurrentPosition(); }
    public void resetArmEncoder() { armMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER); }
    public void setArmPosition(int position) {
        armMotor.setTargetPosition(position);
        armMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        armMotor.setPower(ARM_POWER_LIMIT);
    }
    public boolean isArmBusy() { return armMotor.isBusy(); }
    public boolean isArmStalled() { return armStallDetector.isStalled(); }
    public double getArmCurrent(CurrentUnit unit) {
        // Cast to DcMotorEx to get current. This is the one place we allow this
        // specific type for a critical diagnostic feature.
        if (armMotor instanceof DcMotorEx) {
            return ((DcMotorEx) armMotor).getCurrent(unit);
        }
        return 0;
    }

    // --- Sensor Methods ---
    public double getFrontDistance(DistanceUnit unit) {
        if (frontDistanceSensor != null) {
            return frontDistanceSensor.getDistance(unit);
        }
        return Double.MAX_VALUE;
    }

    /**
     * Drives the robot using field-centric control.
     * This method uses the IMU to account for the robot's rotation, making the
     * controls intuitive for the driver regardless of which way the robot is facing.
     * "Forward" on the joystick will always move the robot away from the driver.
     *
     * @param forward  The power for forward/backward movement [-1.0, 1.0].
     * @param strafe   The power for left/right strafing movement [-1.0, 1.0].
     * @param turn     The power for rotational movement [-1.0, 1.0].
     */
    public void driveFieldCentric(double forward, double strafe, double turn) {
        // Get the robot's current heading in radians from the IMU.
        double heading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);

        // Rotate the joystick inputs by the negative of the robot's heading.
        // This effectively translates the driver's "forward" command to be relative
        // to the field, not the robot.
        double rotX = strafe * Math.cos(-heading) - forward * Math.sin(-heading);
        double rotY = strafe * Math.sin(-heading) + forward * Math.cos(-heading);

        // Denominator is the largest motor power (absolute value) or 1
        // This ensures all the powers maintain the same ratio, but only when
        // at least one is out of the range [-1, 1]
        double denominator = Math.max(Math.abs(rotY) + Math.abs(rotX) + Math.abs(turn), 1.0);

        // Apply the rotated powers to the mecanum drive kinematics.
        double leftFrontPower = (rotY + rotX + turn) / denominator;
        double rightFrontPower = (rotY - rotX - turn) / denominator;
        double leftRearPower = (rotY - rotX + turn) / denominator;
        double rightRearPower = (rotY + rotX - turn) / denominator;

        leftFront.setPower(leftFrontPower);
        rightFront.setPower(rightFrontPower);
        leftRear.setPower(leftRearPower);
        rightRear.setPower(rightRearPower);
    }
}