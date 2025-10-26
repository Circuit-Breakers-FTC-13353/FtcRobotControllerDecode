# SOP: Ultimate Mechanism Range Finder

- **Version:** 2.0
- **Last Updated:** 2025-09-22
- **Author:** Lead Developer

---

### 1. Purpose

This tool allows you to safely and precisely find the key encoder positions for any articulated mechanism on the robot, such as an arm or a lift. The values it provides are used in both Autonomous and TeleOp to move mechanisms to exact, repeatable locations.

---

### 2. When to Use This Tool

- **Initial Calibration:** The first time you set up a new mechanism.
- **Re-Tuning:** After making a physical change to a mechanism (e.g., changing gear ratios or chain length).
- **Diagnostics:** To verify that a mechanism's encoder is working and its range of motion is correct.

---

### 3. Setup Requirements

- [X] Robot is turned on and connected to the Driver Station.
- [X] The mechanism you want to test (e.g., the arm) is in its **physical "zero" or "home" position** (e.g., fully retracted and down). This is a critical starting step.

---

### 4. Step-by-Step Instructions

1.  On the Driver Station, select the OpMode named "**Ultimate: Mechanism Range Finder v2**".
2.  Press **INIT**. The screen will confirm initialization and remind you to start in the zero position. The mechanism's encoder is now reset to `0`.
3.  Press **START**.
4.  **Select Mechanism (if applicable):** Use the **Left/Right Bumpers** to cycle through the available mechanisms (e.g., `ARM`). The currently controlled mechanism is shown at the top of the telemetry.
5.  **Move the Mechanism:**
    - For large, fast movements, use the **Left Joystick (Up/Down)**.
    - For small, precise adjustments, use the **D-pad (Up/Down)**.
6.  **Save Key Positions:** When the mechanism is in the correct physical location, press a button to save its current encoder value:
    - **(A) Button:** Saves the `INTAKE` position.
    - **(B) Button:** Saves the `CARRY` position.
    - **(Y) Button:** Saves the `SCORING` position.
    - **(X) Button:** Saves the `MAX_LIMIT` (the highest safe point).
7.  **Record Values:** The telemetry screen will display all the saved integer values. Copy these numbers into your `Constants.java` file or `robot_config.properties` file.

---

### 5. Special Functions

- **Re-Zeroing the Encoder:** If the starting position was wrong or the mechanism skipped a gear, you can reset the encoder at any time.
    1.  Physically move the mechanism back to its "zero" or "home" position.
    2.  Press the **BACK button** on the gamepad.
    3.  The encoder for the currently selected mechanism will be reset to `0`, and all its previously saved positions will be cleared.

---

### 6. Troubleshooting

- **Problem:** The OpMode crashes on INIT.
    - **Solution:** Check that the mechanism's motor name in the robot's configuration file (e.g., "armMotor") exactly matches the name used in `Robot.java`.
- **Problem:** The mechanism moves in the wrong direction when I push the joystick up.
    - **Solution:** Open `Robot.java` and reverse the motor's direction in the `init()` method (e.g., change `DcMotor.Direction.FORWARD` to `REVERSE`).
- **Problem:** The saved values seem incorrect or inconsistent.
    - **Solution:** The encoder was likely not zeroed correctly at the start. Use the **Re-Zeroing** procedure described in Section 5.