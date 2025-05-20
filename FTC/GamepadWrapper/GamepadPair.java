package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.Gamepad;
import java.util.Objects;
import java.util.HashMap;
import java.util.Map;



public class GamepadPair {
    private final Gamepad gamepad1;
    private final Gamepad gamepad2;
    private final Gamepad currentGamepad1;
    private final Gamepad currentGamepad2;
    private final Gamepad previousGamepad1;
    private final Gamepad previousGamepad2;

    private final Map<String, Long> lastPressTime;
    private long defaultDebounceTime = 175;

    public enum Button {
        A("a", "cross"),
        B("b", "circle"),
        X("x", "square"),
        Y("y", "triangle"),
        DPAD_UP("dpad_up", "up_dpad"),
        DPAD_DOWN("dpad_down", "down_dpad"),
        DPAD_LEFT("dpad_left", "left_dpad"),
        DPAD_RIGHT("dpad_right", "right_dpad"),
        LEFT_BUMPER("left_bumper"),
        RIGHT_BUMPER("right_bumper"),
        LEFT_STICK_BUTTON("left_stick_button"),
        RIGHT_STICK_BUTTON("right_stick_button");

        private final String[] aliases;

        Button(String... aliases) {
            this.aliases = aliases;
        }

        public static Button fromString(String str) {
            for (Button button : values()) {
                for (String alias : button.aliases) {
                    if (alias.equals(str)) {
                        return button;
                    }
                }
            }
            throw new IllegalArgumentException("Unknown button: " + str);
        }
    }

    public GamepadPair(Gamepad gamepad1, Gamepad gamepad2) {
        this.gamepad1 = Objects.requireNonNull(gamepad1, "Gamepad1 cannot be null");
        this.gamepad2 = Objects.requireNonNull(gamepad2, "Gamepad2 cannot be null");
        this.currentGamepad1 = new Gamepad();
        this.currentGamepad2 = new Gamepad();
        this.previousGamepad1 = new Gamepad();
        this.previousGamepad2 = new Gamepad();
        this.lastPressTime = new HashMap<>();
        copyStates();
    }

    public void copyStates() {
        previousGamepad1.copy(currentGamepad1);
        previousGamepad2.copy(currentGamepad2);
        currentGamepad1.copy(gamepad1);
        currentGamepad2.copy(gamepad2);
    }

    public void setDebounceTime(long milliseconds) {
        if (milliseconds < 0) {
            throw new IllegalArgumentException("Debounce time cannot be negative");
        }
        this.defaultDebounceTime = milliseconds;
    }

    public void setDebounceTime(String buttonStr, int gamepadNum, long milliseconds) {
        if (milliseconds < 0) {
            throw new IllegalArgumentException("Debounce time cannot be negative");
        }
        String key = getButtonKey(buttonStr, gamepadNum);
        lastPressTime.put(key + "_debounce", milliseconds);
    }

    private String getButtonKey(String buttonStr, int gamepadNum) {
        return gamepadNum + "_" + buttonStr;
    }

    private long getDebounceTime(String buttonStr, int gamepadNum) {
        String key = getButtonKey(buttonStr, gamepadNum) + "_debounce";
        return lastPressTime.getOrDefault(key, defaultDebounceTime);
    }

    private boolean isDebounced(String buttonStr, int gamepadNum) {
        String key = getButtonKey(buttonStr, gamepadNum);
        long currentTime = System.currentTimeMillis();
        long lastPress = lastPressTime.getOrDefault(key, 0L);
        long debounceTime = getDebounceTime(buttonStr, gamepadNum);

        if (currentTime - lastPress >= debounceTime) {
            lastPressTime.put(key, currentTime);
            return true;
        }
        return false;
    }

    private Gamepad getGamepad(int gamepadNum) {
        validateGamepadNum(gamepadNum);
        return gamepadNum == 1 ? gamepad1 : gamepad2;
    }

    private Gamepad getPreviousGamepad(int gamepadNum) {
        validateGamepadNum(gamepadNum);
        return gamepadNum == 1 ? previousGamepad1 : previousGamepad2;
    }

    private void validateGamepadNum(int gamepadNum) {
        if (gamepadNum != 1 && gamepadNum != 2) {
            throw new IllegalArgumentException("Gamepad number must be 1 or 2");
        }
    }

    private boolean getButtonState(Gamepad gamepad, Button button) {
        switch (button) {
            case A: return gamepad.a;
            case B: return gamepad.b;
            case X: return gamepad.x;
            case Y: return gamepad.y;
            case DPAD_UP: return gamepad.dpad_up;
            case DPAD_DOWN: return gamepad.dpad_down;
            case DPAD_LEFT: return gamepad.dpad_left;
            case DPAD_RIGHT: return gamepad.dpad_right;
            case LEFT_BUMPER: return gamepad.left_bumper;
            case RIGHT_BUMPER: return gamepad.right_bumper;
            case LEFT_STICK_BUTTON: return gamepad.left_stick_button;
            case RIGHT_STICK_BUTTON: return gamepad.right_stick_button;
            default: throw new IllegalArgumentException("Unhandled button type: " + button);
        }
    }

    public boolean isPressed(int gamepadNum, String buttonStr) {

        Button button = Button.fromString(buttonStr);
        boolean isButtonPressed = false;

        if (gamepadNum == 1 || gamepadNum == 2) {
            Gamepad current = getGamepad(gamepadNum);
            Gamepad previous = getPreviousGamepad(gamepadNum);
            isButtonPressed = getButtonState(current, button) && !getButtonState(previous, button);
        } else {
            isButtonPressed = (getButtonState(gamepad1, button) && !getButtonState(previousGamepad1, button)) ||
                    (getButtonState(gamepad2, button) && !getButtonState(previousGamepad2, button));

            if(getButtonState(gamepad1, button)) gamepadNum = 1;
            else if(getButtonState(gamepad2, button)) gamepadNum = 2;
            else return false;
        }

        return isButtonPressed && isDebounced(buttonStr, gamepadNum);
    }

    public boolean isPressed(String buttonStr) {
        return isPressed(1, buttonStr) || isPressed(2, buttonStr);
    }


    public boolean isHeld(int gamepadNum, String buttonStr) {

        Button button = Button.fromString(buttonStr);
        if (gamepadNum == 1 || gamepadNum == 2) {
            return getButtonState(getGamepad(gamepadNum), button);
        }

        return getButtonState(gamepad1, button) ||
                getButtonState(gamepad2, button);
    }

    public boolean isHeld(String buttonStr){
        Button button = Button.fromString(buttonStr);
        return getButtonState(gamepad1, button) ||
                getButtonState(gamepad2, button);
    }

    public float joystickValue(int gamepadNum, String joystick, String direction) {
        validateGamepadNum(gamepadNum);

        Gamepad currentGamepad = getGamepad(gamepadNum);
        switch (joystick.toLowerCase()) {
            case "left":
                if ("x".equals(direction)) {
                    return currentGamepad.left_stick_x;
                } else if ("y".equals(direction)) {
                    return currentGamepad.left_stick_y;
                }
                break;
            case "right":
                if ("x".equals(direction)) {
                    return currentGamepad.right_stick_x;
                } else if ("y".equals(direction)) {
                    return currentGamepad.right_stick_y;
                }
                break;
        }
        throw new IllegalArgumentException("Invalid joystick/direction combination: " + joystick + "/" + direction);
    }

    public double getTrigger(int gamepadNum, String trigger) {
        validateGamepadNum(gamepadNum);

        Gamepad currentGamepad = getGamepad(gamepadNum);
        switch (trigger) {
            case "left_trigger": return currentGamepad.left_trigger;
            case "right_trigger": return currentGamepad.right_trigger;
            default: throw new IllegalArgumentException("Unknown trigger: " + trigger);
        }
    }

    public void setLed(int gamepadNum, double r, double g, double b) {
        if (gamepadNum == 1 || gamepadNum == 2) {
            getGamepad(gamepadNum).setLedColor(r, g, b, Gamepad.LED_DURATION_CONTINUOUS);
        }
    }

    public void blipRumble(int gamepadNum, int blips) {
        if (gamepadNum == 1) {
            gamepad1.rumbleBlips(blips);
        } else {
            gamepad2.rumbleBlips(blips);
        }
    }

    public void blipRumble(int blips){
        gamepad1.rumbleBlips(blips);
        gamepad2.rumbleBlips(blips);
    }

    public void rumble(int gamepadNum, int milliseconds) {
        if (gamepadNum == 1) {
            gamepad1.rumble(milliseconds);
        } else if (gamepadNum == 2) {
            gamepad2.rumble(milliseconds);
        }
    }

    public void rumble(int milliseconds){
        gamepad1.rumble(milliseconds);
        gamepad2.rumble(milliseconds);
    }

    public void rumble(int gamepadNum, Gamepad.RumbleEffect effect) {
        if (gamepadNum == 1) {
            gamepad1.runRumbleEffect(effect);
        } else if (gamepadNum == 2) {
            gamepad2.runRumbleEffect(effect);
        }
    }

    public void rumble(Gamepad.RumbleEffect effect){
        gamepad1.runRumbleEffect(effect);
        gamepad2.runRumbleEffect(effect);
    }
}
