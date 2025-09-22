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
 * This is the Robot Hardware Abstraction Class.
 *
 * It provides a single point of access to all robot hardware and encapsulates all hardware-specific logic.
 * OpModes should interact with the robot through this class only.
 * This class uses the Config and Constants classes to initialize its values, implementing
 * the hybrid configuration system.
 *
 * Version 1.2: Added armMotor, its control methods, and fully integrated its constants.
 */
public class Robot {

    // =============================================================================================
    //                                     HARDWARE DECLARATIONS
    // =============================================================================================
    // --- Drivetrain ---
    public DcMotor leftFront, rightFront, leftRear, rightRear;
    public IMU imu;
    // --- Mechanisms ---
    public Servo clawServo, wristServo;
    public DcMotor armMotor;
    public DistanceSensor frontDistanceSensor;

    // =============================================================================================
    //                                     CONFIGURATION CONSTANTS
    // =============================================================================================
    public double CLAW_OPEN_POSITION, CLAW_CLOSED_POSITION;
    public double WRIST_STOW_POSITION, WRIST_SCORE_POSITION;
    public double ARM_MANUAL_POWER_MULTIPLIER, ARM_POWER_LIMIT;

    // =============================================================================================
    //                                     CLASS MEMBERS
    // =============================================================================================
    private HardwareMap hardwareMap;

    // =============================================================================================
    //                                     CONSTRUCTOR & INITIALIZATION
    // =============================================================================================
    public Robot(HardwareMap hwMap) {
        this.hardwareMap = hwMap;
    }

    public boolean init() {
        try {
            // --- LOAD CONSTANTS FROM HYBRID CONFIGURATION ---
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
            armMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            armMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            resetArmEncoder();

            // Set initial positions
            closeClaw();
            stowWrist();

            return true;

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
    public void setClawPosition(double position) {
        clawServo.setPosition(position);
    }

    // --- Wrist Methods ---
    public void stowWrist() {
        wristServo.setPosition(WRIST_STOW_POSITION);
    }
    public void scoreWrist() {
        wristServo.setPosition(WRIST_SCORE_POSITION);
    }
    public void setWristPosition(double position) {
        wristServo.setPosition(position);
    }

    // --- Arm Methods ---
    public void setArmPower(double power) {
        armMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        armMotor.setPower(power * ARM_MANUAL_POWER_MULTIPLIER);
    }
    public int getArmPosition() {
        return armMotor.getCurrentPosition();
    }
    public void resetArmEncoder() {
        armMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    }
    public void setArmPosition(int position) {
        armMotor.setTargetPosition(position);
        armMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        armMotor.setPower(ARM_POWER_LIMIT);
    }
    public boolean isArmBusy() {
        return armMotor.isBusy();
    }

    //Diagnostic
    public String getSensorDiagnostics() {
        StringBuilder sb = new StringBuilder();
        // Arm Encoder Check
        sb.append(String.format("Arm Encoder: OK | Pos: %d\n", armMotor.getCurrentPosition()));
        // Distance Sensor Check
        sb.append(String.format("Distance Sensor: OK | %.2f in\n", frontDistanceSensor.getDistance(DistanceUnit.INCH)));
        // ...and so on for every sensor
        return sb.toString();
    }
}