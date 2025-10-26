// Filename: Config.java
package org.firstinspires.ftc.teamcode.draft;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * This is the Configuration Manager class.
 *
 * It reads and writes to a .properties file on the robot's file system to manage
 * constants and tuning values. This allows for quick adjustments without recompiling code.
 *
 * To use, call Config.load() once at the beginning of an OpMode.
 * Then, access values using the static getter methods, which provide a default
 * value as a fallback from the Constants.java class.
 *
 * @author Team 13353
 */
public class ConflgDraft {
    private static Properties properties = new Properties();

    public static void load() {
        try {
            File file = AppUtil.getInstance().getSettingsFile("robot_config.properties");
            FileReader reader = new FileReader(file);
            properties.load(reader);
            reader.close();
        } catch (IOException e) {
            // File not found or unreadable, defaults will be used.
        }
    }

    public static String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static double getDouble(String key, double defaultValue) {
        try {
            return Double.parseDouble(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static void save(String key, Object value) {
        properties.setProperty(key, String.valueOf(value));
        try {
            File file = AppUtil.getInstance().getSettingsFile("robot_config.properties");
            FileWriter writer = new FileWriter(file, false);
            properties.store(writer, "Robot Configuration - Updated by an Ultimate Tool");
            writer.close();
        } catch (IOException e) {
            // Handle error
        }
    }
}