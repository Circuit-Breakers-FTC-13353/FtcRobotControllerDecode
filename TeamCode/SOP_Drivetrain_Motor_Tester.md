# SOP: Ultimate Drivetrain Motor Tester

- **Version:** 1.0
- **Last Updated:** 2025-09-22
- **Author:** Lead Developer

---

### 1. Purpose

This tool allows you to run each drivetrain motor individually to verify its direction and configuration.

---

### 2. When to Use This Tool

- During the initial robot build to confirm all wiring is correct.
- After making a change to the robot's configuration file.
- When the robot is driving or strafing incorrectly during practice.

---

### 3. Setup Requirements

- [X] Robot is turned on and connected to the Driver Station.
- [X] Robot is placed on blocks so the wheels can spin freely without touching the ground.
- [X] All drivetrain motors are physically connected to the Control Hub.

---

### 4. Step-by-Step Instructions

1.  On the Driver Station, select the OpMode named "**Ultimate: Drivetrain Motor Tester**".
2.  Press **INIT**, then **START**.
3.  The telemetry screen will show the currently selected motor. Use the **D-pad UP/DOWN** to cycle through the motors (e.g., `leftFront`, `rightFront`, `leftRear`, `rightRear`, `ALL`).
4.  Press and hold the **A button** to run the selected motor(s) at 25% positive power.
5.  Observe the wheel's rotation direction.

---

### 5. Interpreting the Results

- **Expected Result:** When holding 'A' (positive power), wheels should spin to move the robot **FORWARD**:
    - `leftFront` and `leftRear` wheels should spin **COUNTER-CLOCKWISE**.
    - `rightFront` and `rightRear` wheels should spin **CLOCKWISE**.
- **Action:** If a wheel spins in the wrong direction, open `Robot.java` and reverse its direction in the `init()` method (e.g., change `DcMotor.Direction.FORWARD` to `REVERSE`).

---

### 6. Troubleshooting

- **Problem:** The OpMode crashes on INIT.
    - **Solution:** Check that the motor names in the robot's configuration file (`leftFront`, `rightFront`, etc.) exactly match the names used in `Robot.java`.
- **Problem:** No motors spin when holding 'A'.
    - **Solution:** Verify the motor is physically connected and the robot battery is fully charged.
    - 