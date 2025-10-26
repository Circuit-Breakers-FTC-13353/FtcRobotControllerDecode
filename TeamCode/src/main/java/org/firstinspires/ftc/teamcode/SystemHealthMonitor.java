// Filename: SystemHealthMonitor.java
package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.robotcore.external.navigation.VoltageUnit;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import java.io.File;


public class SystemHealthMonitor {

    private List<LynxModule> allHubs;
    private List<String> eventLog = new ArrayList<>();
    private final int MAX_LOG_SIZE = 10;

    private double minVoltage = 14.0;
    private double maxCurrentAmps = 0.0;
    private boolean isHubOk = true;

    private boolean isVoltageLow = false;
    private boolean isFirstUpdate = true;

    /**
     * Initializes the monitor with the robot's hubs.
     * @param hubs A list of all LynxModules (hubs) on the robot.
     */
    public void init(List<LynxModule> hubs) {
        this.allHubs = hubs;
    }

    /**
     * Updates all monitored values. This should be called in every loop.
     * @param matchTimer The main timer from the OpMode to use for timestamps.
     */
    public void update(ElapsedTime matchTimer) {
        if (allHubs == null || allHubs.isEmpty()) {
            if (isHubOk) logEvent("Hubs DISCONNECTED!", matchTimer);
            isHubOk = false;
            return;
        }

        double totalCurrent = 0;
        double inputVoltage = 0;
        isHubOk = true;

        for (LynxModule hub : allHubs) {
            if (hub.isNotResponding()) {
                isHubOk = false;
            }
            totalCurrent += hub.getCurrent(CurrentUnit.AMPS);
            inputVoltage = hub.getInputVoltage(VoltageUnit.VOLTS);
        }

        if (isFirstUpdate) {
            logEvent("Monitor Initialized. Battery at " + String.format("%.2f", inputVoltage) + "V");
            isFirstUpdate = false;
        }

        if (!isHubOk) logEvent("A Hub is NOT RESPONDING!", matchTimer);

        if (totalCurrent > maxCurrentAmps) maxCurrentAmps = totalCurrent;
        if (inputVoltage < minVoltage) minVoltage = inputVoltage;

        if (inputVoltage < Constants.VOLTAGE_WARNING_THRESHOLD && !isVoltageLow) {
            logEvent(String.format("Voltage Brownout: %.2fV", inputVoltage), matchTimer);
            isVoltageLow = true;
        } else if (inputVoltage >= Constants.VOLTAGE_WARNING_THRESHOLD && isVoltageLow) {
            logEvent(String.format("Voltage Recovered: %.2fV", inputVoltage), matchTimer);
            isVoltageLow = false;
        }
    }

    private void logEvent(String message, ElapsedTime timer) {
        String logEntry = String.format(Locale.US, "[ %.1fs ] %s", timer.seconds(), message);
        eventLog.add(0, logEntry);
        if (eventLog.size() > MAX_LOG_SIZE) {
            eventLog.remove(eventLog.size() - 1);
        }
    }

    private void logEvent(String message) {
        logEvent(message, new ElapsedTime(0));
    }

    public void saveLogToFile() {
        try {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(new Date());
            String filename = "MatchLog_" + timestamp + ".txt";
            File file = AppUtil.getInstance().getSettingsFile(filename);
            FileWriter writer = new FileWriter(file, false);

            writer.write("--- Match Log ---\n");
            writer.write("Summary - Min Voltage: " + String.format("%.2fV", minVoltage) + "\n");
            writer.write("Summary - Max Current: " + String.format("%.2fA", maxCurrentAmps) + "\n\n");

            for (int i = eventLog.size() - 1; i >= 0; i--) {
                writer.write(eventLog.get(i) + "\n");
            }
            writer.close();
            logEvent("Log SAVED to " + filename);
        } catch (IOException e) {
            logEvent("FAILED to save log!");
        }
    }

    public double getMinVoltage() { return minVoltage; }
    public double getMaxCurrentAmps() { return maxCurrentAmps; }
    public List<String> getEventLog() { return eventLog; }
}