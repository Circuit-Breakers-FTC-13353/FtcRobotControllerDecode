# Advanced FTC Java Codebase

Welcome to our advanced FTC robotics codebase! This repository is more than just a basic template; it's a collection of robust, reusable, and competition-tested Java classes designed to accelerate development and enhance robot performance.

This codebase is built on the principles of **hardware abstraction**, **modular utilities**, and **powerful diagnostics**. Whether you are looking to implement reliable field-centric driving, protect your motors from burnout, or calibrate your sensors with confidence, these tools will provide a solid foundation.

## ✨ Core Features

*   **Competition-Ready TeleOp:** A feature-rich driver-controlled program with dual-drive modes (Field-Centric and Robot-Centric).
*   **Robust Sensor Utilities:** Standalone testers for critical sensors like the IMU and advanced pipelines for vision processing.
*   **Hardware Protection:** A smart `StallDetector` utility to prevent motor burnout and provide driver feedback.
*   **Clear and Self-Documenting:** All tools are heavily commented and provide clear instructions on the Driver Station, making them easy to use for the whole team.

---

## 📁 Codebase Components

Here's a breakdown of the key files included in this repository.

### 📄 `Smart_Mecanum_TeleOp.java`

This is the primary driver-controlled program, designed for competitive play with a mecanum drivetrain.

*   **Purpose:** To provide a versatile and intuitive driving experience.
*   **Key Features:**
    *   **Dual Drive Modes:** Toggle between Field-Centric and Robot-Centric control on the fly.
    *   **Slow Mode:** A precision mode for fine-tuned maneuvering.
    *   **Interactive Controls:** Reset IMU Yaw and switch modes with dedicated gamepad buttons.
    *   **Hardware Abstraction:** All hardware calls are routed through the `RobotMecanum.java` class for clean, maintainable code.
*   **How to Use (Gamepad 1 - Driver):**
    *   **Left Stick:** Drive (Forward/Backward) and Strafe (Left/Right)
    *   **Right Stick:** Turn (Left/Right)
    *   **Right Trigger:** Hold for Slow Mode
    *   **X Button:** Toggle between Field-Centric and Robot-Centric drive modes.
    *   **Back Button:** Reset the IMU's Yaw angle (re-calibrates "forward").

### 🔧 `Standalone_IMU_Tester.java`

A crucial diagnostic tool for testing and calibrating the robot's Inertial Measurement Unit (IMU).

*   **Purpose:** To verify that the IMU is configured correctly before using it for field-centric drive or autonomous turns.
*   **Key Features:**
    *   **Live Telemetry:** Displays real-time Yaw, Pitch, and Roll angles.
    *   **Visual Heading Indicator:** Shows a simple text-based compass (e.g., `(Forward)`, `(Left)`) for intuitive feedback.
    *   **Interactive Yaw Reset:** Use the gamepad to reset the robot's heading to zero.
    *   **Clear Error Handling:** Explicitly warns you if the IMU is not found in your robot's configuration.
*   **How to Use (The "Calibration Dance"):**
    1.  **Configure:** Open the file and set the `logoDirection` and `usbDirection` to match your Hub's physical orientation.
    2.  **Run:** Execute the OpMode from the Driver Station.
    3.  **Test Yaw:** Place the robot facing forward, press (Y) to reset. Turn the robot left and right to verify the angle changes correctly (+90° left, -90° right).
    4.  **Test Pitch & Roll:** Lift the front and tilt the sides of the robot to see the other values change.

### 🛡️ `StallDetector.java`

A smart utility class that protects your motors from burning out.

*   **Purpose:** To reliably detect when a motor is stalled (blocked but still receiving power) and allow the program to react.
*   **Key Features:**
    *   **Smart Detection:** A stall is only triggered if the motor's current draw exceeds a defined threshold for a continuous duration, ignoring normal startup spikes.
    *   **Highly Configurable:** Easily set the current limit (Amps) and time limit (milliseconds).
    *   **Lightweight & Reusable:** Can be instantiated for any motor on your robot (arm, intake, etc.).
*   **How to Use (Example):**
    ```java
    // 1. In your Robot class, create an instance
    StallDetector armStallDetector = new StallDetector(8.0, 250); // 8 Amps, 250ms

    // 2. In your main loop, update it with the motor's current
    armStallDetector.update(armMotor.getCurrent(CurrentUnit.AMPS));

    // 3. React to the stall condition
    if (armStallDetector.isStalled()) {
        armMotor.setPower(0);
        gamepad2.rumble(500); // Stop the motor & alert the driver!
    }
    ```

### 👁️ `AprilTagWebcam.java`

*(Note: This is a template description. Functionality may vary based on the actual code.)*

A standalone OpMode for testing and calibrating AprilTag detection with a webcam.

*   **Purpose:** To verify that the webcam is configured correctly and that the AprilTag pipeline is detecting tags.
*   **Key Features:**
    *   **Live Telemetry:** Displays detailed information for any detected AprilTags, including their ID, range, bearing, and elevation.
    *   **Easy Configuration:** Set the webcam name directly in the code.
*   **How to Use:**
    1.  Run the OpMode from the Driver Station.
    2.  Point the robot's webcam at an AprilTag from the current season's game field.
    3.  Observe the telemetry to confirm that the tag's data is being displayed correctly.

### ⚾ `OptimizedBallDetectorWithDistance.java`

This file is a high-quality, standalone vision utility for detecting colored game objects ("balls") and calculating their distance from the robot using only a webcam. It is an exemplary tool for testing, tuning, and calibration.

*   **Purpose:** To provide a reliable way to detect objects and estimate their distance. This logic is crucial for autonomous tasks like navigating to an object or determining which of three randomized positions an object is in.
*   **Key Features:**
    *   **Vision-Based Distance Calculation:** Cleverly calculates an object's distance based on its apparent size in pixels. This requires a one-time calibration but does not require a hardware distance sensor.
    *   **Advanced Filtering:** Uses a combination of color, contour area, and **circularity** to ensure that detections are both the right color and the right shape, dramatically reducing false positives.
    *   **Built for Calibration:** The code is designed to be user-friendly, with clear instructions in the comments and specialized telemetry output to help you perform the necessary calibration.
    *   **Optimized Pipeline:** Built on the FTC SDK's `ColorBlobLocatorProcessor` for efficient and reliable performance.
*   **How to Use:**
    1.  **Calibrate:** This is a required first step. Place a ball at a known distance (e.g., 24 inches) from your camera.
    2.  Run this OpMode and look at the "Detailed Data" in the telemetry to see the observed "pixel diameter."
    3.  Update the `KNOWN_DISTANCE_INCHES` and `KNOWN_DIAMETER_PIXELS` constants at the top of the file with your measurements.
    4.  **Tune:** Adjust other constants like `MIN_CONTOUR_AREA` and `MIN_CIRCULARITY` to get the most stable detection in your environment.
    5.  **Integrate:** To use this logic in an Autonomous program, you will need to refactor it into a separate helper class.

## 🚀 Getting Started

1.  **Clone the Repository:** Clone this project into your `TeamCode` folder.
2.  **Open in Android Studio:** Open your FTC project in Android Studio.
3.  **Configure Hardware:** This is the most important step! Open your central hardware class (e.g., `RobotMecanum.java`) and change the string names for your motors, servos, and sensors to match what you have in your Robot Controller configuration file. For example, change `"leftFront"` to `"front_left_motor"` if that is what you named it.
4.  **Deploy and Run:** Deploy the code to your Robot Controller and enjoy