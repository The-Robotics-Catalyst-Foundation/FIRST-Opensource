# RTPAxon - A Rotational Control for CRServo
## These docs are a little out of date
RTPAxon is a Java library designed for controlling a CRServo (Continuous Rotation Servo) using an analog encoder. This software provides precise control over servo rotation, supporting features like proportional control, configurable maximum power, and rotation tracking. It is optimized for use in FTC (FIRST Tech Challenge) robot systems.

## Features

- **Proportional Control**: Uses a proportional controller to adjust servo power and move the servo to the target rotation.
- **Encoder Feedback**: Tracks rotation using an encoder input, providing feedback on the servo's angle.
- **Direction Control**: Easily configure the direction of the servo rotation (forward or reverse).
- **Rotation Tracking**: Keeps track of total rotation and target rotation for precise control.
- **Adjustable Parameters**: Fine-tune parameters like maximum power, proportional gain (kP), and target rotation.


### Setup

1. Copy the `RTPAxon.java` file into your project's source code.
2. Make sure that your Axon is in **CR Mode** using the Axon Programmer.
3. Import the `RTPAxon` class into your FTC code.

```java
import org.firstinspires.ftc.teamcode.RTPAxon;
```

3. Initialize the `RTPAxon` class with the appropriate CRServo and encoder input.

```java
CRServo servo = hardwareMap.get(CRServo.class, "servoName");
AnalogInput encoder = hardwareMap.get(AnalogInput.class, "encoderName");

RTPAxon axon = new RTPAxon(servo, encoder);
```

4. Add the following to the loop in your opmode to update the position and power of the axon each loop.

```java
axon.update()
```

## Example Usage

Below is an example of how to use the `RTPAxon` class in an FTC tele-op or autonomous program:

```java
public class MyOpMode extends OpMode {
    private RTPAxon axon;

    @Override
    public void init() {
        CRServo servo = hardwareMap.get(CRServo.class, "servo");
        AnalogInput encoder = hardwareMap.get(AnalogInput.class, "encoder");

        axon = new RTPAxon(servo, encoder);
        axon.setMaxPower(0.5);  // Set the maximum power limit to 50%
        axon.setK(0.02);  // Set the proportional gain
    }

    @Override
    public void loop() {
        // Update the servo's position(this has to be done at the beginning of each loop)
        axon.update();

        // Display the current status on the driver station
        telemetry.addData("Servo Position", axon.getCurrentAngle());
        telemetry.addData("Total Rotation", axon.getTotalRotation());
        telemetry.addData("Target Rotation", axon.getTargetRotation());
        telemetry.update();
    }
}
```

### Setting Target Rotation

You can change the target rotation by calling `setTargetRotation()` or `changeTargetRotation()`:

```java
// Set target rotation to 90 degrees
axon.setTargetRotation(90);

// Increment target rotation by 45 degrees
axon.changeTargetRotation(45);
```

### Adjusting Power

You can adjust the power using the `setPower()` method:

```java
axon.setPower(0.5);  // Set the servo to 50% power
```

### Logging

For debugging or monitoring, you can log the current state of the servo using the `log()` method:

```java
String status = axon.log();
telemetry.addData("Status", status);
telemetry.update();
```

## Configuration

You can configure several parameters to fine-tune the behavior of your servo:

- **Maximum Power (`setMaxPower(double maxPower)`)**: Set the maximum power that the servo can be driven at. Default is `0.25`.
- **Proportional Gain (`setK(double k)`)**: Adjust the proportional gain for the control loop. Default is `0.015`.
- **Direction (`setDirection(Direction direction)`)**: Set the direction of rotation. Choose between `Direction.FORWARD` and `Direction.REVERSE`.
- **RTP Mode (`setRtp(boolean rtp)`)**: Enable or disable rotational tracking and proportional control.
