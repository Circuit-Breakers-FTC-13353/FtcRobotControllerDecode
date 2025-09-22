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
 */

public class Constants {

    // =============================================================================================
    //                                     DRIVETRAIN CONSTANTS
    // =============================================================================================

    // This multiplier can be used to scale down the drive power for more precise control.
    // A value of 1.0 means full power, while 0.5 would cap the max power at 50%.
    public static final double DRIVE_POWER_MULTIPLIER = 1.0;


    // =============================================================================================
    //                                     MECHANISM CONSTANTS
    // =============================================================================================

    // --- Claw Servo Constants ---
    // These are the servo positions for the claw mechanism.
    // 0.0 is one extreme of the servo's range, 1.0 is the other.
    public static final double CLAW_OPEN_POSITION = 0.8;    // The position for a fully open claw.
    public static final double CLAW_CLOSED_POSITION = 0.25; // The position for a closed claw gripping an object.


    // --- Arm Constants (Example) ---
    // These are example encoder positions for an arm mechanism.
    public static final int ARM_LIFT_POSITION = 1200;       // Encoder ticks for the arm in scoring position.
    public static final int ARM_CARRY_POSITION = 400;       // Encoder ticks for the arm in a safe carrying position.
    public static final int ARM_INTAKE_POSITION = 50;         // Encoder ticks for the arm in intake position.
    public static final double ARM_POWER_LIMIT = 0.7;       // The maximum power to apply to the arm motor.

    // --- Arm Constants ---
    // The maximum power to apply to the arm motor during manual control.
    // This prevents the mechanism from moving too quickly and causing damage.
    public static final double ARM_MANUAL_POWER_MULTIPLIER = 0.4;


    // =============================================================================================
    //                                     AUTONOMOUS CONSTANTS
    // =============================================================================================

    // Add constants for PID controllers, odometry, etc. here in the future.
    // public static final double ODOMETRY_TICKS_PER_INCH = 2450.0;
}