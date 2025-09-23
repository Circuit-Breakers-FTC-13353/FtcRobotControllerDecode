// Filename: Robot.java
package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * The Robot Hardware Abstraction Class.
 *
 * This class serves as a centralized hub for all robot hardware components and their control logic.
 * It follows the "Hardware Abstraction" design pattern, meaning that all direct interactions with
 * hardware (e.g., `hardwareMap.get()`, `motor.setPower()`) are encapsulated here.
 *
 * OpModes (both TeleOp and Autonomous) should interact with this class to command the robot,
 * rather than accessing hardware directly. This modular approach makes the main OpMode code
 * cleaner, easier to read, and significantly easier to maintain.
 *
 * This class also integrates our hybrid configuration system, loading its constants from
 * the `Config` class, which in turn reads from `Constants.java` and `robot_config.properties`.
 *
 * @author 13353
 */

public class Robot {

    // --- HARDWARE DECLARATIONS ---
    public DcMotor leftFront, rightFront, leftRear, rightRear;
    public IMU imu;
    public Servo clawServo, wristServo;
    public DcMotor armMotor;
    public DistanceSensor frontDistanceSensor; // <-- Correctly included

    // --- CONSTANTS ---
    public double CLAW_OPEN_POSITION, CLAW_CLOSED_POSITION;
    public double WRIST_STOW_POSITION, WRIST_SCORE_POSITION;
    public double ARM_MANUAL_POWER_MULTIPLIER, ARM_POWER_LIMIT;

    private HardwareMap hardwareMap;

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

            // --- DRIVETRAIN INIT ---
            leftFront = hardwareMap.get(DcMotor.class, "leftFront");
            rightFront = hardwareMap.get(DcMotor.class, "rightFront");
            leftRear = hardwareMap.get(DcMotor.class, "leftRear");
            rightRear = hardwareMap.get(DcMotor.class, "rightRear");
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

            // --- IMU INIT ---
            imu = hardwareMap.get(IMU.class, "imu");
            RevHubOrientationOnRobot.LogoFacingDirection logoDirection = RevHubOrientationOnRobot.LogoFacingDirection.UP;
            RevHubOrientationOnRobot.UsbFacingDirection usbDirection = RevHubOrientationOnRobot.UsbFacingDirection.FORWARD;
            imu.initialize(new IMU.Parameters(new RevHubOrientationOnRobot(logoDirection, usbDirection)));

            // --- MECHANISM INIT ---
            clawServo = hardwareMap.get(Servo.class, "clawServo");
            wristServo = hardwareMap.get(Servo.class, "wristServo");
            armMotor = hardwareMap.get(DcMotor.class, "armMotor");
            frontDistanceSensor = hardwareMap.get(DistanceSensor.class, "frontDistanceSensor");

            armMotor.setDirection(DcMotorSimple.Direction.FORWARD);
            armMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            resetArmEncoder();
            armMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

            // Set initial positions
            closeClaw();
            stowWrist();

            return true;

        } catch (Exception e) {
            return false;
        }
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
        leftFront.setPower(0);
        rightFront.setPower(0);
        leftRear.setPower(0);
        rightRear.setPower(0);
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
}