// Filename: StallDetector.java
package org.firstinspires.ftc.teamcode.draft;

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

    public StallDetector(double thresholdAmps, long timeMs) {
        this.stallThresholdAmps = thresholdAmps;
        this.stallTimeThresholdMs = timeMs;
        timer.reset();
    }

    public void update(double currentAmps) {
        if (currentAmps > stallThresholdAmps) {
            if (!wasOverThresholdLastCheck) {
                timer.reset();
                wasOverThresholdLastCheck = true;
            } else {
                if (timer.milliseconds() > stallTimeThresholdMs) {
                    isStalled = true;
                }
            }
        } else {
            wasOverThresholdLastCheck = false;
            isStalled = false;
        }
    }

    public boolean isStalled() {
        return isStalled;
    }
}