# SOP: Ultimate PID Tuner v3

- **Version:** 3.0
- **Last Updated:** 2025-09-22
- **Author:** Lead Developer

---

### 1. Purpose

This tool is an interactive dashboard for finding the optimal PIDF (Proportional, Integral, Derivative, Feedforward) coefficients for an encoder-based motor, such as an arm. The goal is to tune the motor to move to a target position quickly, smoothly, and with minimal error.

---

### 2. When to Use This Tool

- **Initial Tuning:** When a new mechanism is built, to find its baseline PIDF values.
- **Re-Tuning:** After changing the weight or geometry of a mechanism.
- **Performance Optimization:** To improve the speed and smoothness of autonomous movements.

---

### 3. Setup Requirements

- [X] Robot is turned on and connected to the Driver Station.
- [X] The robot is in a safe, stationary position.
- [X] The mechanism to be tuned (e.g., the arm) is in its physical **"zero" or "home" position.**

---

### 4. Step-by-Step Tuning Process

1.  On the Driver Station, select the OpMode named "**Ultimate: PID Tuner v3**".
2.  Press **INIT**, then **START**. The arm's encoder will be reset to `0`. The screen will display the last saved PIDF values.

3.  **Select a Coefficient:** Use the **D-pad Left/Right** to select which coefficient (`P`, `I`, `D`, or `F`) you want to edit. It will be highlighted with `>> ... <<`.

4.  **Adjust the Value:**
    - Use the **D-pad Up/Down** to change the selected value.
    - Use the **Left/Right Bumpers** to change the step size (the amount each D-pad press changes the value).

5.  **Run a Test:**
    - Press the **(Y) button** to command the arm to move to its `LIFT` position. The screen will update with performance stats (`Overshoot`, `Time to Settle`).
    - Press the **(Y) button again** to command the arm back to its `INTAKE` position. This allows for repeated, bidirectional testing.
    - The **(A) button** can be used to test movement to the `CARRY` position.

6.  **Analyze and Repeat:** Observe the arm's physical movement and the on-screen stats. Repeat steps 4 and 5, adjusting the P, I, D, and F values until the performance is optimal (fast, smooth, and accurate).

7.  **Save Your Work:** Once you are satisfied with the tune, press the **START button**. The screen will confirm that the new values have been successfully saved to the `robot_config.properties` file.

---

### 5. Special Functions

- **Re-Zeroing the Encoder:** If the arm slips or you need to restart the tuning process, physically move the arm back to its "zero" position and press the **BACK button**. This will reset the encoder to `0` and clear any run stats without losing your tuned PIDF values.

---

### 6. Guide to PIDF Coefficients

- **`P` (Proportional):** The primary workhorse. A higher `P` provides more power to correct errors, making the mechanism move faster. Too high, and it will overshoot and oscillate violently. Start by increasing `P` until the mechanism moves quickly and starts to overshoot slightly.
- **`D` (Derivative):** The "brakes." `D` counteracts overshoot by damping the movement as it approaches the target. After setting `P`, increase `D` to reduce oscillations and stop smoothly.
- **`F` (Feedforward):** The "gravity helper." For an arm, `F` provides a constant amount of power to counteract the force of gravity. A good `F` value will allow the arm to hold its position even with a low `P`.
- **`I` (Integral):** The "error corrector." `I` slowly builds up power if there is a small, persistent error (e.g., the arm "sags" a few ticks below its target). It's often not needed for FTC arms if `F` is tuned well. Start with `I` at `0`.