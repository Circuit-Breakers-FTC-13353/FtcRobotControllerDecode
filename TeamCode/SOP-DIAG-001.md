# Standard Operating Procedure: Pre-Run System Diagnostics

**SOP ID:** SOP-DIAG-001
**Title:** Pre-Match & Pre-Test System Diagnostics
**Purpose:** To provide a standardized, failsafe procedure for verifying the health of all core robot systems (controllers, sensors, and IMU) before any run. This procedure is designed to catch hardware, configuration, and connectivity issues in under 60 seconds.
**Scope:** This procedure is **mandatory** and must be performed:
*   Before queuing for every competition match.
*   Before starting any autonomous or TeleOp testing session.
    **Required Tool:** `Core Diagnostics (V1.2 FINAL)` OpMode

---

### Procedure Steps

**1. Initiation**
*   Place the robot on a flat, stable surface with room to rotate.
*   Connect the Driver Station and initialize the robot.
*   Select the **`Core Diagnostics (V1.2 FINAL)`** OpMode from the TeleOp list.
*   Press **`INIT`**, then press **`PLAY`**.

**2. The 3-Second Health Check**
*   Look immediately at the **`--- SYSTEM HEALTH ---`** summary at the top of the screen.
*   **If all systems show `[ OK ]`:** Proceed to Step 3.
*   **If any system shows `[WARN]`, `[ERROR]`, or `[DISCONNECTED]`:** Stop. Go directly to the **Troubleshooting** section below. Do not proceed until all systems report `[ OK ]`.

**3. Interactive Liveness Checks (Mandatory)**
*   This step verifies that the sensors are not just connected, but are actively working. Follow the on-screen **`ACTION`** prompts.
*   **Controllers:**
    *   **ACTION:** Wiggle all sticks and press various buttons on both gamepads.
    *   **CONFIRM:** The values on the screen change in response to your inputs.
*   **IMU & Heading:**
    *   **ACTION:** Physically rotate the robot at least 90 degrees.
    *   **CONFIRM:** The "Robot Heading" value changes smoothly as the robot turns.
*   **Sensors & Encoders:**
    *   **ACTION:** For each motor listed (drivetrain and mechanisms), manually spin its corresponding wheel or move the mechanism.
    *   **CONFIRM:** The "Pos:" value for that encoder changes. It must not be stuck at 0 or any other number.

**4. Finalization**
*   Once all liveness checks are confirmed, face the robot in its starting direction.
*   Press the **`(Y)`** button on Gamepad 1.
*   **CONFIRM:** The message **`--> HEADING RESET! <--`** appears and the "Robot Heading" is at or near `0.00`.
*   The diagnostic is complete. Stop the OpMode and proceed with your match or test.

---

### Troubleshooting Guide

*If the Health Check (Step 2) fails, find the error below and perform the first action.*

| Problem Displayed | Likely Cause(s) | First Action(s) to Take |
| :--- | :--- | :--- |
| `CONTROLLERS: [DISCONNECTED]` | 1. Controller is off.<br>2. Controller not paired. | 1. Check controller power light.<br>2. Re-pair the controller (Start + A/B).<br>3. Check the Driver Station phone's USB hub. |
| `IMU: [ERROR]` | Configuration name mismatch. | 1. Open `Robot.java` and verify the IMU name matches the robot configuration file (e.g., "imu"). |
| `SENSORS: [ERROR]` | Configuration name mismatch. | 1. Find the specific sensor reporting `[ERROR]` in the details below the summary.<br>2. Verify its name in `Robot.java` matches the robot configuration file. |
| Heading value is stuck / does not change when rotating. | IMU has frozen. | 1. **Restart the Robot.** (Control Hub power cycle). This is the most common fix. |
| An encoder "Pos:" value is stuck / does not change when moving the part. | Encoder cable is disconnected. | 1. **Check the encoder cable connection at the motor.**<br>2. Trace the cable and check its connection at the Control/Expansion Hub port. |
| `Front Distance: [WARN]` | 1. Something is too close.<br>2. Cable is loose. | 1. Ensure the path in front of the sensor is clear.<br>2. Check the sensor's cable connection at both ends. |

---

> ### **Lead's Summary: A Culture of Reliability**
> "This SOP is not just a document; it's a core part of our team's commitment to reliability. By making this quick, simple check a non-negotiable habit, we eliminate an entire class of unforced errors. This process ensures that when we face a problem on the field, it's a strategic challenge, not a simple hardware failure that should have been caught in the pit. This completes items 1.4, 1.5, and 1.6 of our roadmap. Our Core Diagnostics Suite is officially deployed."