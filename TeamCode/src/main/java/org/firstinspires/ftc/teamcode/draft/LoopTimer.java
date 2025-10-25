// Filename: LoopTimer.java
package org.firstinspires.ftc.teamcode.draft;

import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * A utility class to measure and display the performance of an OpMode's main loop.
 *
 * V2 FEATURES:
 * - Tracks not only the average loop rate (Hz) but also the PEAK loop time (ms),
 *   which is essential for identifying performance "spikes" that cause robot lag.
 *
 * USAGE:
 * 1. Create instance: `LoopTimer loopTimer = new LoopTimer();`
 * 2. Call `loopTimer.start()` before the main `while` loop.
 * 3. Call `loopTimer.update()` at the end of the `while` loop.
 * 4. Add telemetry data using `getLoopRate()` and `getPeakLoopTime()`.
 * 5. Optionally, call `loopTimer.resetPeakTimer()` with a gamepad button to
 *    measure the peak time of specific actions.
 *
 * @author Team 13353
 */
public class LoopTimer {
    private ElapsedTime loopDurationTimer = new ElapsedTime();
    private ElapsedTime displayUpdateTimer = new ElapsedTime();

    private int loopCount = 0;
    private double loopsPerSecond = 0;
    private double peakLoopTimeMs = 0;

    /**
     * Resets all timers and counters. Call once before the main loop.
     */
    public void start() {
        loopDurationTimer.reset();
        displayUpdateTimer.reset();
        loopCount = 0;
    }

    /**
     * Updates timers and calculations. Call this at the END of every loop iteration.
     */
    public void update() {
        // Track the duration of the current loop cycle
        double currentLoopTimeMs = loopDurationTimer.milliseconds();
        loopDurationTimer.reset();

        // Check if this loop is the new slowest loop we've seen
        if (currentLoopTimeMs > peakLoopTimeMs) {
            peakLoopTimeMs = currentLoopTimeMs;
        }

        loopCount++;

        // Update the average Hz display once per second
        if (displayUpdateTimer.seconds() >= 1.0) {
            loopsPerSecond = loopCount / displayUpdateTimer.seconds();
            loopCount = 0;
            displayUpdateTimer.reset();
        }
    }

    /**
     * Resets the peak loop time measurement.
     * Useful for isolating the performance impact of a specific action.
     */
    public void resetPeakTimer() {
        peakLoopTimeMs = 0;
    }

    /**
     * @return The average loop rate over the last second, in Hertz.
     */
    public double getLoopRate() {
        return loopsPerSecond;
    }

    /**
     * @return The slowest single loop cycle time since the last reset, in milliseconds.
     */
    public double getPeakLoopTime() {
        return peakLoopTimeMs;
    }
}