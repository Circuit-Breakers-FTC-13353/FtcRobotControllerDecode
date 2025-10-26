# SOP: Ultimate Servo Tuner v2

- **Version:** 2.0
- **Last Updated:** 2025-09-22
- **Author:** Lead Developer

---

### 1. Purpose

This tool provides a safe, fast, and precise way to find the exact position values (from 0.0 to 1.0) for any servo on the robot. It is essential for calibrating mechanisms like claws, wrists, and latches.

---

### 2. When to Use This Tool

- **Initial Calibration:** When a new servo-driven mechanism is added to the robot.
- **Re-Tuning:** After a physical change that might affect a servo's range of motion.
- **Diagnostics:** To verify that a servo is connected, configured correctly, and functioning smoothly.

---

### 3. Setup Requirements

- [X] Robot is turned on and connected to the Driver Station.
- [X] The robot is in a safe position where all servos can move without hitting anything.
- [X] The `Constants.java` file has been updated with `SERVO_TUNER_SAFE_MIN` and `SERVO_TUNER_SAFE_MAX` values to prevent hardware damage.

---

### 4. Step-by-Step Instructions

1.  On the Driver Station, select the OpMode named "**Ultimate: Servo Tuner v2**".
2.  Press **INIT**, then **START**.
3.  **Select Servo:** Use the **Left/Right Bumpers** to cycle through the available servos (e.g., `CLAW`, `WRIST`). The currently selected servo is shown on the telemetry.
4.  **Adjust Position:** Use the multi-speed controls to move the servo:
    - **Rapid Movement:** Use the **Left Joystick (Left/Right)** for fast sweeps.
    - **Coarse Adjustment:** Use the **Left/Right Triggers** for medium steps.
    - **Fine Adjustment:** Use the **D-pad (Left/Right)** for very small, precise steps.
5.  **Save Key Positions:** When the servo is in the correct physical location, press a button to save its position:
    - **(Y) Button:** Saves "Position 1" (e.g., OPEN or SCORE).
    - **(A) Button:** Saves "Position 2" (e.g., CLOSED or STOW).
6.  **Record Values:** The telemetry screen will display the saved position values. Copy these numbers (e.g., `0.755`) into your `Constants.java` file or `robot_config.properties` file.

---

### 5. Interpreting the Results

- **`Live Position`:** This shows the current position value being sent to the servo. It will always be clamped between the `SERVO_TUNER_SAFE_MIN` and `SERVO_TUNER_SAFE_MAX` values from your `Constants.java` file.
- **`Saved Values`:** The telemetry will use helpful labels (like `OPEN_POSITION` or `STOW_POSITION`) for the saved values to make recording them less error-prone.

---

### 6. Troubleshooting

- **Problem:** The OpMode crashes on INIT.
    - **Solution:** Check that the servo name in the robot's configuration file (e.g., "wristServo") exactly matches the name used in `Robot.java`.
- **Problem:** The servo does not move.
    - **Solution:** Verify the servo's physical connection to the Control Hub. Check that the battery is charged.
- **Problem:** The servo is buzzing or straining at one end of its travel.
    - **Solution:** The `SERVO_TUNER_SAFE_MIN/MAX` values in `Constants.java` are too wide. Make them less extreme (e.g., change `0.05` to `0.10`) to provide a larger safety margin.