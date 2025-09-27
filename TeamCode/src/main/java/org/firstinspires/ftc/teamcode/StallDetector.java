// Filename: StallDetector.java
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * A helper class to detect when a motor has stalled.
 *
 * This class monitors the motor's current draw. If the current exceeds a specified
 * threshold for a specific, continuous duration, the motor is considered stalled. This is crucial
 * for preventing motor burnout and providing actionable feedback to the OpMode.
 *
 * The logic is designed to ignore brief, normal current spikes and only trigger on
 * true, sustained stall events.
 *
 * @author Team 13353
 */
public class StallDetector {
    private double stallThresholdAmps;
    private long stallTimeThresholdMs;
    private ElapsedTime timer = new ElapsedTime();
    private boolean isStalled = false;
    private boolean wasOverThresholdLastCheck = false;

    /**
     * Constructor for the StallDetector.
     * @param thresholdAmps The current draw (in Amps) that is considered a stall.
     * @param timeMs The continuous duration (in milliseconds) the current must exceed the
     *               threshold for a stall to be detected.
     */
    public StallDetector(double thresholdAmps, long timeMs) {
        this.stallThresholdAmps = thresholdAmps;
        this.stallTimeThresholdMs = timeMs;
        timer.reset();
    }

    /**
     * Updates the stall detection logic. This method must be called in every
     * loop of the OpMode.
     * @param currentAmps The current current draw of the motor in Amps.
     */
    public void update(double currentAmps) {
        // Check if the current is above our threshold
        if (currentAmps > stallThresholdAmps) {
            // If the current just now spiked above the threshold...
            if (!wasOverThresholdLastCheck) {
                // ...reset the timer and flag that we are now over the threshold.
                timer.reset();
                wasOverThresholdLastCheck = true;
            } else {
                // If the current has been continuously over the threshold,
                // check if the timer has exceeded our time limit.
                if (timer.milliseconds() > stallTimeThresholdMs) {
                    isStalled = true;
                }
            }
        } else {
            // If the current is below the threshold, it's not stalled.
            // Reset the flag so the timer will restart on the next spike.
            wasOverThresholdLastCheck = false;
            isStalled = false;
        }
    }

    /**
     * @return true if a stall is currently detected, false otherwise.
     */
    public boolean isStalled() {
        return isStalled;
    }
}