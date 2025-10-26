# FTC Vision OpMode: Optimized Ball Detector w/ Distance

This repository contains a complete, optimized `LinearOpMode` for the FIRST Tech Challenge that detects the purple and green perforated game balls and estimates their distance from the camera.

## Features
- **Dual Color Detection:** Simultaneously tracks both purple and green blobs.
- **Robust Filtering:** Uses a combination of Contour Area and Circularity filters to reject noise and non-ball objects.
- **Morphological Operations:** Intelligently fills the holes in the perforated balls to see them as solid objects, dramatically improving detection stability.
- **Distance Estimation:** Calculates the real-world distance to the detected ball in inches.

## How It Works
The code uses the FTC SDK's `ColorBlobLocatorProcessor` with a series of advanced OpenCV settings. By performing a "closing" morphological operation (Dilate followed by Erode), it creates a solid mask of the perforated balls, which allows for accurate geometric analysis.

## Required Calibration (IMPORTANT)
To get accurate distance readings, you **must** perform this simple, one-time calibration for your robot.

**Step 1: Physical Setup**
- Place your robot on the floor.
- Using a tape measure, place one of the game balls **exactly 24 inches** away from your robot's camera lens.

**Step 2: Capture Pixel Value**
- Deploy and run the `OptimizedBallDetectorWithDistance` OpMode in **INIT** mode on your Driver Station.
- Look at the telemetry data at the bottom of the screen. You will see a line:
  `Largest PURPLE blob pixel diameter: XX.X`
- Write down the number value you see.

**Step 3: Update Code**
- Open the `.java` file.
- Find the `CALIBRATION_CONSTANTS` section at the top.
- Update the value of `KNOWN_DIAMETER_PIXELS` to the number you wrote down in Step 2.
- Re-deploy the code.

Your robot is now calibrated!

## Usage
After calibration, you can run the OpMode normally or integrate the processor and distance calculation logic into your own autonomous programs.