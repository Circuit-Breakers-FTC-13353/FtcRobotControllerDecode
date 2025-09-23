# SOP: Ultimate Diagnostics Tool

- **Version:** 1.0
- **Last Updated:** 2025-09-22
- **Author:** Lead Developer

---

### 1. Purpose

This tool is a comprehensive, interactive "pre-flight check" for the robot's entire hardware suite. Its purpose is to quickly identify any disconnected or misconfigured hardware before a match or testing session, preventing mysterious failures.

---

### 2. When to Use This Tool

- **Before every single match.** This is its primary use.
- Before starting any autonomous or TeleOp testing session.
- Any time the robot is behaving unexpectedly, to quickly rule out hardware failures.

---

### 3. Setup Requirements

- [X] Robot is turned on and connected to the Driver Station.
- [X] Both Gamepad 1 and Gamepad 2 are turned on and connected.
- [X] The robot is in a position where its wheels and mechanisms can be safely moved by hand a small amount.

---

### 4. Step-by-Step Instructions

1.  On the Driver Station, select the OpMode named "**Ultimate: Diagnostics**".
2.  Press **INIT**, then **START**.
3.  **Perform the Summary Check:** Look at the "SYSTEM HEALTH" summary at the top of the screen.
    - If `CONTROLLERS`, `IMU`, and `SENSORS` all show `[ OK ]`, your pre-flight check is complete. The robot is ready.
    - If any system shows an error, proceed to the detailed checks below.
4.  **Perform the Interactive "Liveness" Checks:** The tool is not just for checking configuration; you must interact with it. Follow all on-screen "ACTION" prompts:
    - **Controllers:** Wiggle all sticks and press a few buttons on both gamepads to verify they are responsive.
    - **IMU:** Physically rotate the robot on the floor. Watch the "Robot Heading" value on the screen and confirm that it changes. Press **(Y)** on Gamepad 1 to reset the heading to 0.
    - **Encoders:** Manually turn each wheel and move the arm by hand. Confirm that the "Pos:" value for each encoder changes on the screen.
    - **Distance Sensor:** Wave your hand in front of the distance sensor and confirm the "Range:" value changes.

---

### 5. Interpreting the Results

- **`[ OK ]`:** The component is configured correctly. For sensors, you must still perform the "liveness" check to ensure it's actively sending data.
- **`[ DISCONNECTED ]`:** A gamepad is not turned on or is not paired with the Driver Station.
- **`[ ERROR ]`:** A hardware device (motor, sensor, IMU) is listed in the code but is not found in the robot's configuration file, or it is physically disconnected.
- **`[ WARN ]` (for Distance Sensor):** The sensor is connected but is reporting a value that is out of its typical range, suggesting it might be faulty or aimed at an object that is too far away.
- **Encoder value is `0` and doesn't change when moved:** This indicates a disconnected or broken encoder cable, even if the motor itself is configured correctly. **This is a critical failure for autonomous.**

---

### 6. Troubleshooting

- **Problem:** `SENSORS` shows `[ ERROR ]`.
    - **Solution:** Look at the detailed list below the summary. The sensor with the error will be flagged. Check its physical connection and verify its name in the robot configuration file matches the name in `Robot.java`.
- **Problem:** `IMU` shows `[ ERROR ]`.
    - **Solution:** The IMU is not configured with the name "imu" in the robot configuration file, or the Control Hub is having issues.
- **Problem:** The IMU heading does not change when the robot is rotated.
    - **Solution:** This indicates an IMU fault. Restart the Robot Controller. If the problem persists, the Control Hub may need to be replaced.