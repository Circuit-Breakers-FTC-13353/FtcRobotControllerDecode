# SOP: Ultimate Drivetrain Motor Tester v2

- **Version:** 2.0
- **Last Updated:** 2025-09-22
- **Author:** Lead Developer

---

### 1. Purpose

This tool is a comprehensive diagnostic for the drivetrain. It allows you to run each motor individually to verify three critical things:
1.  **Configuration:** That the motor's physical port matches its name in the config file.
2.  **Direction:** That the motor spins in the correct direction for forward movement.
3.  **Encoder Health:** That the motor's encoder is connected and reporting values.

---

### 2. When to Use This Tool

- **During the initial robot build:** This should be one of the first programs you run.
- **After any wiring or configuration change:** To verify the changes were successful.
- **When the robot drives incorrectly:** To quickly diagnose if the problem is mechanical, electrical, or a software configuration error.
- **Before starting autonomous tuning:** To ensure all encoders are working.

---

### 3. Setup Requirements

- [X] Robot is turned on and connected to the Driver Station.
- [X] Robot is placed securely on blocks so that all wheels can spin freely.
- [X] All drivetrain motors are physically connected to the Control Hub.

---

### 4. Step-by-Step Instructions

1.  On the Driver Station, select the OpMode named "**Ultimate: Drivetrain Motor Tester v2**".
2.  Press **INIT**, then **START**.
3.  Use the **D-pad UP/DOWN** to cycle through the test options (`leftFront`, `rightFront`, `leftRear`, `rightRear`, `ALL`).
4.  Press and hold the **A button** to run the selected motor(s).
5.  Observe the telemetry screen and the physical wheel.

---

### 5. Interpreting the Results

*While holding the 'A' button, check the following for the selected motor:*

#### **Part A: Configuration & Port Check**
- **Telemetry Shows:** `Port Number: [X]`
- **Action:** Physically trace the wire from the spinning motor back to the Control Hub.
- **Expected Result:** The wire should be plugged into the port number shown on the screen. If `leftFront` is selected and the screen says `Port 0`, the wire from the front-left motor must go to Port 0.

#### **Part B: Direction Check**
- **Action:** Observe the direction the physical wheel is spinning.
- **Expected Result:** To move the robot FORWARD:
    - `leftFront` and `leftRear` wheels must spin **COUNTER-CLOCKWISE**.
    - `rightFront` and `rightRear` wheels must spin **CLOCKWISE**.
- **If Incorrect:** Open `Robot.java` and reverse the motor's direction in the `init()` method.

#### **Part C: Encoder Health Check**
- **Telemetry Shows:** `Encoder Ticks: [Value]`
- **Action:** Observe the "Encoder Ticks" value on the screen while the motor is spinning.
- **Expected Result:** The number should be changing rapidly (either increasing or decreasing).
- **If Incorrect:** If the motor is spinning but the encoder value is stuck at `0` (or not changing), it indicates a **problem with the encoder**. Check the encoder cable at both the motor and the Control Hub. This must be fixed before attempting autonomous driving.

---

### 6. Troubleshooting

- **Problem:** The OpMode crashes on INIT.
    - **Solution:** The motor name in the robot's configuration file does not exactly match the name used in `Robot.java`.
- **Problem:** A motor does not spin when 'A' is held.
    - **Solution:** Verify the motor's physical power connection. Check that the battery is charged.