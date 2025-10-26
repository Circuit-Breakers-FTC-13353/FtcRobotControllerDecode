# Software Architecture Overview

This document explains the high-level design of our robot's software. Understanding these principles is essential for all programmers on the team.

---

### 1. Core Principles

Our architecture is built on two core principles:

-   **Hardware Abstraction ("The Black Box"):** All low-level hardware interactions (motor power, servo positions, sensor readings) are handled exclusively by a single class: `Robot.java`. All other programs (TeleOp, Autonomous, Tools) **never** talk to hardware directly. They only make high-level requests to the `Robot` class (e.g., `robot.drive(...)`, `robot.openClaw()`). This keeps our code clean, modular, and easy to maintain.

-   **Configuration Management ("Hybrid System"):** All tuning values (constants, positions, PID gains) are managed through a two-tiered system. This allows for both developer convenience and rapid, on-the-fly tuning without recompiling code.

---

### 2. The Key Files

| File | Purpose |
| :--- | :--- |
| **`Robot.java`** | The Hardware Abstraction Class. The "engine" of the robot. |
| **`Constants.java`** | The in-code list of safe, default tuning values. The ultimate fallback. |
| **`Config.java`** | A utility that reads the external config file. |
| **`robot_config.properties`** | A text file on the Robot Hub for overriding the default constants. |

---

### 3. The Hybrid Configuration System

When the robot initializes, it finds a value by following this order:

1.  **Check First:** The `Config.java` utility looks for the value in the `robot_config.properties` file on the Robot Controller's storage. If found, this value is used.
2.  **Fallback:** If the file or the specific value is not found, the system uses the safe, default value from `Constants.java`.

This provides the perfect balance of reliability and flexibility.

---

### The Golden Rule

**OpModes talk to `Robot.java`, not to hardware.** Adhering to this principle is the key to our entire architecture.