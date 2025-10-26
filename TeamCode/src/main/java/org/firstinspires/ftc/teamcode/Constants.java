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
    // DRIVETRAIN
    public static final double DRIVE_POWER_MULTIPLIER = 1.0;
    public static final double DRIVE_SLOW_MODE_MULTIPLIER = 0.5;
    // MECHANISMS - ARM PIDF
    public static final double ARM_P = 10.0;
    public static final double ARM_I = 0.0;
    public static final double ARM_D = 1.0;
    public static final double ARM_F = 0.5;

    // =============================================================================================
    //                                     POWER & BATTERY CONSTANTS
    // =============================================================================================

    // The voltage level below which a warning should be shown. A typical FTC battery is 12.6V when full.
    public static final double VOLTAGE_WARNING_THRESHOLD = 12.0;

    // The maximum acceptable voltage drop during a stress test. A drop greater than this
    // indicates a poor battery or bad wiring. 1.0V is a reasonable starting point.
    public static final double VOLTAGE_DROP_CRITICAL_THRESHOLD = 1.0;

    // The power to apply to the drivetrain motors during the stress test.
    // 50% is a significant, sustained load.
    public static final double STRESS_TEST_POWER = 0.5;


}
