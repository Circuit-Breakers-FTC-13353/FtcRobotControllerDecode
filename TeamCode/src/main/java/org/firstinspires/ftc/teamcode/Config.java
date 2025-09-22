// Filename: Config.java
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.util.ReadWriteFile;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * This is the Configuration Manager class.
 *
 * It reads a .properties file from the robot's file system to load constants
 * and tuning values. This allows for quick adjustments without recompiling code.
 *
 * To use, call Config.load() once at the beginning of an OpMode.
 * Then, access values using the static getter methods, which provide a default
 * value as a fallback.
 */
public class Config {

    private static Properties properties = new Properties();

    /**
     * Loads the configuration file. This should be called once, typically in the
     * init() phase of your OpMode.
     */
    public static void load() {
        try {
            // Get the full path to the configuration file.
            File file = AppUtil.getInstance().getSettingsFile("robot_config.properties");
            FileReader reader = new FileReader(file);
            properties.load(reader);
            reader.close();
        } catch (IOException e) {
            // If the file doesn't exist or can't be read, the properties object
            // will remain empty. The getter methods will then rely on their defaults.
        }
    }

    /**
     * Gets a string value from the loaded properties.
     * @param key The key of the value to retrieve.
     * @param defaultValue The value to return if the key is not found.
     * @return The retrieved value or the default value.
     */
    public static String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Gets a double value from the loaded properties.
     * @param key The key of the value to retrieve.
     * @param defaultValue The value to return if the key is not found or is not a valid double.
     * @return The retrieved value or the default value.
     */
    public static double getDouble(String key, double defaultValue) {
        try {
            String value = properties.getProperty(key);
            if (value != null) {
                return Double.parseDouble(value);
            }
        } catch (NumberFormatException e) {
            // Value was not a valid double, fall through to return default.
        }
        return defaultValue;
    }

    /**
     * Gets an integer value from the loaded properties.
     * @param key The key of the value to retrieve.
     * @param defaultValue The value to return if the key is not found or is not a valid integer.
     * @return The retrieved value or the default value.
     */
    public static int getInt(String key, int defaultValue) {
        try {
            String value = properties.getProperty(key);
            if (value != null) {
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            // Value was not a valid integer, fall through to return default.
        }
        return defaultValue;
    }
}