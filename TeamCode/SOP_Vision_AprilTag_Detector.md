# SOP: Ultimate Generic AprilTag Detector

- **Version:** 1.0
- **Last Updated:** 2025-09-22
- **Author:** Lead Developer

---

### 1. Purpose

This tool allows you to verify camera functionality, get data from any AprilTag, and provides the necessary feedback to calibrate the camera's physical position in the code.

---

### 2. When to Use This Tool

- **Initial Setup:** To confirm the camera is connected and working.
- **Critical Calibration:** To fine-tune the `cameraPosition` and `cameraOrientation` values in `Robot.java`.
- **Debugging:** When autonomous is failing to see or align to a tag.
- **Diagnostics:** To identify the ID number of an unknown AprilTag during practice.

---

### 3. Setup Requirements

- [X] Robot is turned on and connected to the Driver Station.
- [X] The robot's camera is physically connected and not obstructed.
- [X] You have at least one AprilTag from the **36h11 family** available to show the camera. (This is the standard FTC family).

---

### 4. Step-by-Step Instructions

1.  Place the robot in a well-lit area where it has a clear view of an AprilTag.
2.  On the Driver Station, select the OpMode named "**Ultimate: Generic AprilTag Detector**".
3.  Press **INIT**, then **START**.
4.  The telemetry screen will display the live camera status and target info.
5.  Point the camera at an AprilTag. If it is detected, its data will appear.
6.  Use the **Right Bumper** and **Left Bumper** on the gamepad to increase or decrease the "Target ID" you are looking for.

---

### 5. Interpreting the Results

- **If the screen says "TARGET VISIBLE!":**
    - **`Range`:** The direct, straight-line distance from the camera lens to the tag (in inches).
    - **`Bearing`:** The left/right angle to the tag. A negative value means the tag is to the **right** of center; a positive value means it's to the **left**.
    - **`Yaw`:** The tag's own rotation. If the tag is flat against a wall, this tells you if you are looking at it from an angle.
- **For Calibration:** Place the robot a known distance (e.g., 24 inches) straight in front of a tag. The telemetry `Range` should read `24.0 in`, and the `Bearing` should be close to `0.0 deg`. If not, adjust the values in `Robot.java`.
- **If the screen says "Target not visible":**
    - **`Last Seen`:** This tells you how many seconds ago the tag was visible. If this number is small, it might mean your connection is flickering due to motion blur or poor lighting.

---

### 6. Troubleshooting

- **Problem:** The OpMode crashes on INIT.
    - **Solution:** The camera name in your robot configuration (e.g., "Webcam 1") does not match the `WEBCAM_NAME` in the code. Or, the camera is disconnected.
- **Problem:** No tags are detected even when one is in front of the camera.
    - **Solution:**
        1. Check for poor lighting or glare on the tag.
        2. Make sure the tag is from the 36h11 family.
        3. The tag may be too far away or too close for the camera to focus.