
# GamepadPair - Dual Gamepad Input Management for FTC

`GamepadPair` is a Java class designed to manage two gamepads for use in FTC (FIRST Tech Challenge) robots. It provides a robust API for tracking button presses, joystick values, rumble feedback, and more, with debouncing support for button presses to prevent unintended multiple inputs.

## Features

-   **Dual Gamepad Support**: Handles two gamepads simultaneously, making it easy to track inputs from both.
    
-   **Button Press Handling**: Detects button presses, with debounce handling to avoid multiple triggers.
    
-   **Joystick and Trigger Inputs**: Provides easy access to joystick and trigger values for controlling robot movements.
    
-   **Rumble Feedback**: Supports rumble functionality, including custom rumble patterns.
    
-   **LED Control**: Allows setting LED colors for each gamepad.
    
-   **Debouncing**: Supports custom debounce times for each button to prevent accidental multiple presses.
    


    
## Setup

1.  Clone this repository into your project directory or copy the `GamepadPair.java` file into your project's source code.
    
2.  Import the `GamepadPair` class into your FTC code.
    

```java
import org.firstinspires.ftc.teamcode.subsystems.GamepadPair;

```

3.  Initialize the `GamepadPair` class with the two gamepads from your hardware map.
    

```java
Gamepad gamepad1 = hardwareMap.get(Gamepad.class, "gamepad1");
Gamepad gamepad2 = hardwareMap.get(Gamepad.class, "gamepad2");

GamepadPair gamepadPair = new GamepadPair(gamepad1, gamepad2);

```

4. Add the copyStates method to the beginning of your loop to allow for debouncing input.
```java
gamepadPair.copyStates();
```

## Example Usage

Below is an example of how to use the `GamepadPair` class in an FTC tele-op or autonomous program:

```java
public class MyOpMode extends OpMode {
    private GamepadPair gamepadPair;

    @Override
    public void init() {
        Gamepad gamepad1 = hardwareMap.get(Gamepad.class, "gamepad1");
        Gamepad gamepad2 = hardwareMap.get(Gamepad.class, "gamepad2");

        gamepadPair = new GamepadPair(gamepad1, gamepad2);
        gamepadPair.setDebounceTime(200);  // Set debounce time for all buttons to 200ms
    }

    @Override
    public void loop() {
        // Update the button states for debouncing
        gamepadPair.copyStates();

        // Check if the 'A' button on either gamepad is pressed
        if (gamepadPair.isPressed("a")) {
            telemetry.addData("A Button Pressed", "Yes");
        }

        // Get joystick values for controlling robot movement
        float leftStickX = gamepadPair.joystickValue(1, "left", "x");
        float leftStickY = gamepadPair.joystickValue(1, "left", "y");

        // Display joystick values
        telemetry.addData("Left Stick X", leftStickX);
        telemetry.addData("Left Stick Y", leftStickY);

        telemetry.update();
    }
}

```

### Setting Custom Debounce Time

You can configure the debounce time for all buttons or for specific buttons.

```java
// Set the default debounce time for all buttons
gamepadPair.setDebounceTime(250);  // 250ms debounce time

// Set custom debounce time for specific button
gamepadPair.setDebounceTime("a", 1, 300);  // 300ms debounce time for 'A' button on Gamepad 1

```

### Rumble Feedback

You can trigger rumble effects on either or both gamepads.

```java
// Rumble for Gamepad 1 for 1 second
gamepadPair.rumble(1, 1000);

// Rumble both gamepads for 1 second
gamepadPair.rumble(1000);

// Rumble with a custom rumble effect
Gamepad.RumbleEffect effect = new Gamepad.RumbleEffect(0.5, 0.5, 500);
gamepadPair.rumble(1, effect);

```

### LED Control

You can set the LED color for either gamepad using RGB values.

```java
// Set LED color for Gamepad 1 to Red
gamepadPair.setLed(1, 1.0, 0.0, 0.0);  // Red color (max Red, no Green or Blue)

```

## Methods Overview

### Debounce Handling

-   `setDebounceTime(long milliseconds)`: Set a global debounce time for all buttons.
    
-   `setDebounceTime(String buttonStr, int gamepadNum, long milliseconds)`: Set a custom debounce time for a specific button on a specific gamepad.
    
-   `isPressed(int gamepadNum, String buttonStr)`: Checks if a button is pressed, with debounce handling.
    
-   `isPressed(String buttonStr)`: Checks if a button is pressed on either gamepad, with debounce handling.
    

### Button State

-   `isHeld(int gamepadNum, String buttonStr)`: Checks if a button is held down on a specific gamepad.
    
-   `isHeld(String buttonStr)`: Checks if a button is held down on either gamepad.
    

### Joystick & Trigger

-   `joystickValue(int gamepadNum, String joystick, String direction)`: Get the value of a joystick (X or Y axis).
    
-   `getTrigger(int gamepadNum, String trigger)`: Get the value of a trigger (left or right).
    

### Rumble Feedback

-   `rumble(int gamepadNum, int milliseconds)`: Rumble a specific gamepad for a certain duration.
    
-   `rumble(int milliseconds)`: Rumble both gamepads for a certain duration.
    
-   `rumble(int gamepadNum, Gamepad.RumbleEffect effect)`: Trigger a custom rumble effect on a specific gamepad.
    
-   `rumble(Gamepad.RumbleEffect effect)`: Trigger a custom rumble effect on both gamepads.
    

### LED Control

-   `setLed(int gamepadNum, double r, double g, double b)`: Set the LED color for a specific gamepad using RGB values.
    

## License

This project is licensed under the MIT License.
