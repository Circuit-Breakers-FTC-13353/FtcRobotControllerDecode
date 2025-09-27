// Filename: Constants.java
package org.firstinspires.ftc.teamcode;

/**
 * This class holds all the default constants and tuning values for the robot.
 *
 * =================================================================================
 *
 *                             *** IMPORTANT ***
 *
 * This file serves as the "source of truth" for the programmer and the ultimate
 * fallback for the robot. It contains the SAFE, DEFAULT values.
 *
 * The `robot_config.properties` file on the Robot Controller's storage is used
 * to OVERRIDE these values for on-the-fly tuning.
 *
 * **TEAM PROCESS:**
 * At the end of a tuning session, the final, perfected values from the
 * `robot_config.properties` file should be copied back into this `Constants.java`
 * file and committed to version control. This ensures that our code always
 * has the most up-to-date "good" values as its default.
 *
 * =================================================================================
 * @author Team 13353
 */
public class Constants {

    // =============================================================================================
    //                                     DRIVETRAIN CONSTANTS
    // =============================================================================================
    public static final double DRIVE_POWER_MULTIPLIER = 1.0;


    // =============================================================================================
    //                                     MECHANISM CONSTANTS
    // =============================================================================================

    // --- Claw Servo Constants ---
    public static final double CLAW_OPEN_POSITION = 0.8;
    public static final double CLAW_CLOSED_POSITION = 0.25;

    // --- Wrist Servo Constants ---
    public static final double WRIST_STOW_POSITION = 0.05;
    public static final double WRIST_SCORE_POSITION = 0.75;

    // --- Arm Constants ---
    public static final int ARM_LIFT_POSITION = 1200;
    public static final int ARM_CARRY_POSITION = 400;
    public static final int ARM_INTAKE_POSITION = 50;
    public static final double ARM_POWER_LIMIT = 0.7;
    public static final double ARM_MANUAL_POWER_MULTIPLIER = 0.4;

    // --- Arm PIDF Coefficients ---
    public static final double ARM_P = 10.0;
    public static final double ARM_I = 0.0;
    public static final double ARM_D = 1.0;
    public static final double ARM_F = 0.5;

    // --- Arm Current Profile & Stall Protection ---
    public static final double ARM_FREE_SPIN_CURRENT_AMPS = 1.5;
    public static final double ARM_STALL_CURRENT_AMPS = 10.0;
    public static final double ARM_STALL_THRESHOLD_AMPS = 9.0;


    // =============================================================================================
    //                                     DIAGNOSTIC TOOL CONSTANTS
    // =============================================================================================

    // --- Servo Tuner Constants ---
    public static final double SERVO_TUNER_SAFE_MIN = 0.05;
    public static final double SERVO_TUNER_SAFE_MAX = 0.95;

    // This multiplier is used when "Slow Mode" is active in the Smart TeleOp.
    public static final double DRIVE_SLOW_MODE_MULTIPLIER = 0.5; // 50% of normal speed
}