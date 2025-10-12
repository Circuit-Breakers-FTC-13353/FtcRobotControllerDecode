// Filename: Robot.java
package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.VoltageUnit;

import java.util.List;

/**
 * The Robot Hardware Abstraction Class.
 *
 * This class serves as a centralized hub for all robot hardware components and their control logic.
 * It follows the "Hardware Abstraction" design pattern, meaning that all direct interactions with
 * hardware are encapsulated here. OpModes should interact with this class to command the robot.
 *
 * It also integrates our hybrid configuration system, a stall detector for the arm motor,
 * and the "Black Box" System Health Monitor.
 *
 * @version 1.5 Master - Definitive, globally consistent, and bug-free version.
 * @author Team 13353
 */
public class Robot {

    // --- HARDWARE DECLARATIONS ---
    public DcMotor leftFront, rightFront, leftRear, rightRear;
    public IMU imu;
    public Servo clawServo, wristServo;
    public DcMotor armMotor;
    public DistanceSensor frontDistanceSensor;

    // --- STATE & HELPERS ---
    private HardwareMap hardwareMap;
    private StallDetector armStallDetector;
    public SystemHealthMonitor healthMonitor;
    private List<LynxModule> allHubs;

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
            allHubs = hardwareMap.getAll(LynxModule.class);

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
            healthMonitor = new SystemHealthMonitor();
            healthMonitor.init(allHubs);

            // Set initial mechanism positions
            closeClaw();
            stowWrist();

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    public void update(ElapsedTime matchTimer) {
        armStallDetector.update(getArmCurrent(CurrentUnit.AMPS));
        healthMonitor.update(matchTimer);
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
    public void driveFieldCentric(double forward, double strafe, double turn) {
        double heading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);
        double rotX = strafe * Math.cos(-heading) - forward * Math.sin(-heading);
        double rotY = strafe * Math.sin(-heading) + forward * Math.cos(-heading);
        double denominator = Math.max(Math.abs(rotY) + Math.abs(rotX) + Math.abs(turn), 1.0);
        leftFront.setPower((rotY + rotX + turn) / denominator);
        rightFront.setPower((rotY - rotX - turn) / denominator);
        leftRear.setPower((rotY - rotX + turn) / denominator);
        rightRear.setPower((rotY + rotX - turn) / denominator);
    }
    public void stop() {
        leftFront.setPower(0);
        rightFront.setPower(0);
        leftRear.setPower(0);
        rightRear.setPower(0);
    }

    // --- Claw & Wrist Methods ---
    public void openClaw() { clawServo.setPosition(CLAW_OPEN_POSITION); }
    public void closeClaw() { clawServo.setPosition(CLAW_CLOSED_POSITION); }
    public void setClawPosition(double position) { clawServo.setPosition(position); }
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
    public boolean isArmStalled() { return armStallDetector.isStalled(); }
    public double getArmCurrent(CurrentUnit unit) {
        if (armMotor instanceof DcMotorEx) {
            return ((DcMotorEx) armMotor).getCurrent(unit);
        }
        return 0;
    }

    // --- System & Sensor Methods ---
    public double getFrontDistance(DistanceUnit unit) {
        if (frontDistanceSensor != null) {
            return frontDistanceSensor.getDistance(unit);
        }
        return Double.MAX_VALUE;
    }

    public double getVoltage() {
        if (allHubs != null && !allHubs.isEmpty()) {
            return allHubs.get(0).getInputVoltage(VoltageUnit.VOLTS);
        }
        return 0;
    }

    public void performStressTest(double power) {
        // Run DC Motors
        leftFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        leftRear.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightRear.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        armMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        leftFront.setPower(power);
        rightFront.setPower(power);
        leftRear.setPower(power);
        rightRear.setPower(power);
        armMotor.setPower(power);

        // Actuate Servos
        double servoPosition = (Math.sin(System.currentTimeMillis() / 500.0) + 1.0) / 2.0;
        setClawPosition(servoPosition);
        setWristPosition(servoPosition);
    }
}