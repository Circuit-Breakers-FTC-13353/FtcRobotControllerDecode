// Filename: StallDetector.java
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * =================================================================================================
 * MOTOR STALL DETECTOR UTILITY
 * =================================================================================================
 *
 * This helper class provides a robust mechanism to detect when a DC motor has stalled.
 *
 * A motor stalls when it is commanded to move but is physically prevented from doing so.
 * This causes a sharp spike in the electrical current it draws, which can quickly overheat
 * and permanently damage the motor, as well as rapidly drain the robot's battery.
 *
 * This class solves the problem by monitoring two factors:
 * 1.  **Current Magnitude:** Is the motor drawing more current than a defined threshold?
 * 2.  **Time Duration:** Has the high current been sustained for a continuous period?
 *
 * By requiring the high current to persist for a set duration (e.g., 250 milliseconds),
 * this detector intelligently ignores normal, brief current spikes that occur when a motor
 * starts up or is under a heavy but acceptable load. It only triggers a "stall" condition
 * when the motor is truly stuck, providing a reliable way for an OpMode to react by cutting
 * power to the motor and preventing damage.
 *
 * @author Team 13353
 */
public class StallDetector {

    // --- CONFIGURATION ---
    /** The current threshold in Amperes. If the motor draws more current than this,
     * it is considered to be in a potential stall condition. */
    private final double stallThresholdAmps;

    /** The time threshold in milliseconds. The motor's current must remain above
     * 'stallThresholdAmps' continuously for this duration to be confirmed as stalled. */
    private final long stallTimeThresholdMs;

    // --- STATE VARIABLES ---
    /** A timer (stopwatch) that measures how long the current has been continuously high. */
    private final ElapsedTime timer = new ElapsedTime();

    /** The final output state. True if the motor is considered stalled, otherwise false. */
    private boolean isStalled = false;

    /** A memory flag. It tracks whether the current was over the threshold during the *previous*
     *  call to update(). This is the key to distinguishing a new stall event from a continuing one. */
    private boolean wasOverThresholdLastCheck = false;

    /**
     * Constructor for the StallDetector.
     * @param thresholdAmps The current draw in Amperes that signifies a potential stall.
     * @param timeMs The continuous duration in milliseconds the current must exceed the
     *               threshold to confirm a stall.
     */
    public StallDetector(double thresholdAmps, long timeMs) {
        this.stallThresholdAmps = thresholdAmps;
        this.stallTimeThresholdMs = timeMs;
        timer.reset();
    }

    /**
     * Updates the stall detector's state.
     * This method should be called repeatedly in the main loop of an OpMode.
     * @param currentAmps The motor's current electrical draw in Amperes.
     */
    public void update(double currentAmps) {
        // STEP 1: Check if the motor's current draw is above our defined danger level.
        if (currentAmps > stallThresholdAmps) {
            // The current is high. Now we need to know for how long.

            // STEP 2: Check if this is a *new* high-current event.
            if (!wasOverThresholdLastCheck) {
                // This is the very first moment the current spiked above the threshold.
                // We need to start the timer to see how long it lasts.
                timer.reset();
                // Set the memory flag to true so that on the next loop, we know the stall is continuing.
                wasOverThresholdLastCheck = true;
            } else {
                // The current was already high on the previous check, so the stall is continuing.
                // Now we check the timer to see if it has been stalled long enough.
                if (timer.milliseconds() > stallTimeThresholdMs) {
                    // The timer has exceeded our time threshold. This is a confirmed stall.
                    isStalled = true;
                }
            }
        } else {
            // The current is at a safe, normal level.
            // This means any potential stall event is over. Reset everything to the default state.
            wasOverThresholdLastCheck = false;
            isStalled = false;
        }
    }

    /**
     * Returns whether a stall condition is currently detected.
     * @return True if the motor is stalled, otherwise false.
     */
    public boolean isStalled() {
        return isStalled;
    }
}