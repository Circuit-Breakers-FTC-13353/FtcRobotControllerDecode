// Filename: Robot.java
package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * This is the Robot Hardware Abstraction Class.
 *
 * It provides a single point of access to all robot hardware and encapsulates all hardware-specific logic.
 * OpModes should interact with the robot through this class only.
 * This class uses the Config and Constants classes to initialize its values, implementing
 * the hybrid configuration system.
 *
 * Version 1.1: Added armMotor and its control methods.
 */
public class Robot {

    // --- HARDWARE DECLARATIONS ---
    // Drivetrain
    public DcMotor leftFront;
    public DcMotor rightFront;
    public DcMotor leftRear;
    public DcMotor rightRear;
    public IMU imu;

    // Mechanisms
    public Servo clawServo;
    public DcMotor armMotor;

    // --- CONSTANTS ---
    // These variables hold the final configured values. They are populated by the init() method.
    public double CLAW_OPEN_POSITION;
    public double CLAW_CLOSED_POSITION;
    public double ARM_MANUAL_POWER_MULTIPLIER;

    private HardwareMap hardwareMap;

    /**
     * The constructor for the Robot class.
     * @param hwMap The hardware map from the OpMode, used to initialize hardware.
     */
    public Robot(HardwareMap hwMap) {
        this.hardwareMap = hwMap;
    }

    /**
     * Initializes all the robot's hardware components and loads configuration values.
     * @return true if initialization is successful, false otherwise.
     */
    public boolean init() {
        try {
            // --- LOAD CONSTANTS FROM HYBRID CONFIGURATION ---
            CLAW_OPEN_POSITION = Config.getDouble("CLAW_OPEN_POSITION", Constants.CLAW_OPEN_POSITION);
            CLAW_CLOSED_POSITION = Config.getDouble("CLAW_CLOSED_POSITION", Constants.CLAW_CLOSED_POSITION);
            ARM_MANUAL_POWER_MULTIPLIER = Config.getDouble("ARM_MANUAL_POWER_MULTIPLIER", Constants.ARM_MANUAL_POWER_MULTIPLIER);

            // --- DRIVETRAIN INITIALIZATION ---
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

            // --- IMU INITIALIZATION ---
            imu = hardwareMap.get(IMU.class, "imu");
            RevHubOrientationOnRobot.LogoFacingDirection logoDirection = RevHubOrientationOnRobot.LogoFacingDirection.UP;
            RevHubOrientationOnRobot.UsbFacingDirection usbDirection = RevHubOrientationOnRobot.UsbFacingDirection.FORWARD;
            RevHubOrientationOnRobot orientationOnRobot = new RevHubOrientationOnRobot(logoDirection, usbDirection);
            imu.initialize(new IMU.Parameters(orientationOnRobot));

            // --- MECHANISM INITIALIZATION ---
            clawServo = hardwareMap.get(Servo.class, "clawServo");
            armMotor = hardwareMap.get(DcMotor.class, "armMotor");

            armMotor.setDirection(DcMotorSimple.Direction.FORWARD);
            armMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            // NOTE: We do not set the RunMode for the arm here because it changes
            // depending on the task (e.g., manual control vs. autonomous).

            // Set initial positions
            closeClaw();

            return true; // Initialization was successful

        } catch (Exception e) {
            return false;
        }
    }

    // =============================================================================================
    //                                     HIGH-LEVEL CONTROL METHODS
    // =============================================================================================

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
    public void openClaw() {
        clawServo.setPosition(CLAW_OPEN_POSITION);
    }

    public void closeClaw() {
        clawServo.setPosition(CLAW_CLOSED_POSITION);
    }

    // --- Arm Methods ---
    public void setArmPower(double power) {
        // Apply the power limit from our constants to prevent damage.
        armMotor.setPower(power * ARM_MANUAL_POWER_MULTIPLIER);
    }

    public int getArmPosition() {
        return armMotor.getCurrentPosition();
    }

    public void resetArmEncoder() {
        // This sequence is the proper way to reset an encoder.
        armMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        // The calling OpMode is responsible for setting the new RunMode afterward.
    }
}