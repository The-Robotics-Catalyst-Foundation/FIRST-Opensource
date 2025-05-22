# RTPAxon - A Rotational Control for CRServo

RTPAxon is a Java library for FTC robots that enables precise control of a Continuous Rotation Servo (CRServo) using an analog encoder. It provides PID-based run-to-position (RTP) control, direction management, and rotation tracking, making it easy to use a CRServo as a positional actuator.

## Features

- **PID Control**: Uses a PID controller (with configurable kP, kI, kD) to move the servo to a target rotation.
- **Encoder Feedback**: Tracks rotation using an analog encoder for accurate feedback.
- **Direction Control**: Supports both forward and reverse directions.
- **Rotation Tracking**: Maintains total and target rotation, including wraparound handling.
- **Adjustable Parameters**: Easily tune max power, PID coefficients, and integral windup limits.
- **Manual and RTP Modes**: Switch between direct power control and run-to-position mode.
- **Telemetry/Logging**: Built-in logging for debugging and dashboard integration.
- **TeleOp Test Mode**: Includes a TeleOp class for live tuning and testing.

## Setup

1. **Copy the Source File**  
   Copy `RTPAxon.java` into your project's source code (e.g., `org.firstinspires.ftc.teamcode.subsystems`).

2. **Configure Your Axon**  
   Ensure your Axon is set to **CR Mode** using the Axon Programmer.

3. **Import the Class**

   ```java
   import org.firstinspires.ftc.teamcode.subsystems.RTPAxon;
   ```

4. **Initialize in Your OpMode**

   ```java
   CRServo servo = hardwareMap.get(CRServo.class, "servoName");
   AnalogInput encoder = hardwareMap.get(AnalogInput.class, "encoderName");
   RTPAxon axon = new RTPAxon(servo, encoder);
   ```

5. **Update in Your Loop**  
   Call `axon.update();` at the start of each loop to update the servo's position and apply PID control.

## Example Usage

```java
public class MyOpMode extends OpMode {
    private RTPAxon axon;

    @Override
    public void init() {
        CRServo servo = hardwareMap.get(CRServo.class, "servo");
        AnalogInput encoder = hardwareMap.get(AnalogInput.class, "encoder");
        axon = new RTPAxon(servo, encoder);
        axon.setMaxPower(0.5);  // Limit max power to 50%
        axon.setPidCoeffs(0.02, 0.0005, 0.0025);  // Set PID coefficients
    }

    @Override
    public void loop() {
        axon.update(); // Must be called every loop

        telemetry.addData("Servo Position", axon.getCurrentAngle());
        telemetry.addData("Total Rotation", axon.getTotalRotation());
        telemetry.addData("Target Rotation", axon.getTargetRotation());
        telemetry.update();
    }
}
```

## Setting Target Rotation

Set or increment the target rotation (in degrees):

```java
axon.setTargetRotation(90);    // Move to 90 degrees absolute
axon.changeTargetRotation(45); // Move 45 degrees from current position
```

## Manual Power Control

You can directly set the servo power (bypassing PID) by disabling RTP mode:

```java
axon.setRtp(false);    // Disable run-to-position mode
axon.setPower(0.5);    // Set servo to 50% power
```

## PID Tuning

Adjust PID coefficients for your mechanism:

```java
axon.setPidCoeffs(0.02, 0.0005, 0.0025); // kP, kI, kD
axon.setMaxIntegralSum(50);              // Optional: limit integral windup
```

## Direction Control

Reverse the servo direction if needed:

```java
axon.setDirection(RTPAxon.Direction.REVERSE);
```

## Logging and Telemetry

For debugging, use the built-in log method:

```java
telemetry.addLine(axon.log());
telemetry.update();
```

## TeleOp Test Mode

A ready-to-use TeleOp (`CRAxonTest`) is included for live tuning and testing.  
It supports gamepad controls for changing target rotation and PID coefficients.

## Configuration Options

- **setMaxPower(double maxPower)**: Set the maximum output power (default: 0.25).
- **setPidCoeffs(double kP, double kI, double kD)**: Set PID coefficients.
- **setMaxIntegralSum(double maxIntegralSum)**: Limit integral windup.
- **setDirection(Direction direction)**: Set servo direction (FORWARD/REVERSE).
- **setRtp(boolean rtp)**: Enable/disable run-to-position mode.
- **setTargetRotation(double target)**: Set absolute target rotation (degrees).
- **changeTargetRotation(double delta)**: Increment target rotation (degrees).
- **setPower(double power)**: Directly set servo power (when RTP is off).

## Notes

- Always call `axon.update()` at the start of each loop for correct operation.
- The encoder must be connected and mapped correctly in your hardware configuration.
- For best results, tune PID coefficients for your specific mechanism.

---

For questions or improvements, open an issue or PR!
