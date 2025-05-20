package org.firstinspires.ftc.teamcode.subsystems;

import android.annotation.SuppressLint;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.util.ElapsedTime;

public class RTPAxon {
    private final AnalogInput servoEncoder;
    private final CRServo servo;
    private boolean rtp;
    private double power;
    private double maxPower;
    private Direction direction;
    private double previousAngle;
    private double totalRotation;
    private double targetRotation;

    // PID controller parameters
    private double kP;
    private double kI;
    private double kD;
    private double integralSum;
    private double lastError;
    private double maxIntegralSum; // Anti-windup limit
    private ElapsedTime pidTimer;

    public double STARTPOS;
    public int ntry = 0;
    public int cliffs = 0;
    public double homeAngle;

    public enum Direction {
        FORWARD,
        REVERSE
    }

    // region constructors
    public RTPAxon(CRServo servo, AnalogInput encoder) {
        rtp = true;
        this.servo = servo;
        servoEncoder = encoder;
        direction = Direction.FORWARD;
        initialize();
    }

    public RTPAxon(CRServo servo, AnalogInput encoder, Direction direction) {
        this(servo, encoder);
        this.direction = direction;
        initialize();
    }

    private void initialize() {
        servo.setPower(0);
        try {
            Thread.sleep(50);
        } catch (InterruptedException ignored) {
        }

        do {
            STARTPOS = getCurrentAngle();
            if (Math.abs(STARTPOS) > 1) {
                previousAngle = getCurrentAngle();
            } else {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {
                }
            }
            ntry++;
        } while (Math.abs(previousAngle) < 0.2 && (ntry < 50));

        totalRotation = 0;
        homeAngle = previousAngle;

        // PID controller initialization
        kP = 0.015;   // Default P gain
        kI = 0.0005;  // Default I gain
        kD = 0.0025;  // Default D gain
        integralSum = 0.0;
        lastError = 0.0;
        maxIntegralSum = 100.0;  // Default anti-windup limit
        pidTimer = new ElapsedTime();
        pidTimer.reset();

        maxPower = 0.25;
        cliffs = 0;   // This could be wrong if init is not at home position
    }
    // endregion

    // region getters & setters
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setPower(double power) {
        this.power = Math.max(-maxPower, Math.min(maxPower, power));
        servo.setPower(this.power * (direction == Direction.REVERSE ? -1 : 1));
    }

    public double getPower() {
        return power;
    }

    public void setMaxPower(double maxPower) {
        this.maxPower = maxPower;
    }

    public double getMaxPower() {
        return maxPower;
    }

    public void setRtp(boolean rtp) {
        this.rtp = rtp;
        // Reset PID controller when turning RTP on
        if (rtp) {
            resetPID();
        }
    }

    public boolean getRtp() {
        return rtp;
    }

    // PID coefficient setters
    public void setKP(double kP) {
        this.kP = kP;
    }

    public void setKI(double kI) {
        this.kI = kI;
        resetIntegral(); // Reset integral when changing I gain
    }

    public void setKD(double kD) {
        this.kD = kD;
    }

    public void setPidCoeffs(double kP, double kI, double kD){
        setKP(kP);
        setKI(kI);
        setKD(kD);
    }

    // PID coefficient getters
    public double getKP() {
        return kP;
    }

    public double getKI() {
        return kI;
    }

    public double getKD() {
        return kD;
    }

    // For backward compatibility
    public void setK(double k) {
        setKP(k);
    }

    public double getK() {
        return getKP();
    }

    // Anti-windup control
    public void setMaxIntegralSum(double maxIntegralSum) {
        this.maxIntegralSum = maxIntegralSum;
    }

    public double getMaxIntegralSum() {
        return maxIntegralSum;
    }

    public double getTotalRotation() {
        return totalRotation;
    }

    public double getTargetRotation() {
        return targetRotation;
    }

    public void changeTargetRotation(double change) {
        targetRotation += change;
    }

    public void setTargetRotation(double target) {
        targetRotation = target;
        // Reset PID controller when setting a new target
        resetPID();
    }

    public double getCurrentAngle() {
        if (servoEncoder == null) return 0;
        return (servoEncoder.getVoltage() / 3.3) * (direction.equals(Direction.REVERSE) ? -360 : 360);
    }

    public boolean isAtTarget() {
        return isAtTarget(5);
    }

    public boolean isAtTarget(double tolerance) {
        return Math.abs(targetRotation - totalRotation) < tolerance;
    }

    public void forceResetTotalRotation() {
        totalRotation = 0;
        previousAngle = getCurrentAngle();
        resetPID();
    }

    // Reset PID controller
    public void resetPID() {
        resetIntegral();
        lastError = 0;
        pidTimer.reset();
    }

    // Just reset the integral component
    public void resetIntegral() {
        integralSum = 0;
    }
    // endregion

    public synchronized void update() {
        // Calculate when the axon has wrapped around its 360 degree encoder limit
        double currentAngle = getCurrentAngle();
        double angleDifference = currentAngle - previousAngle;

        if (angleDifference > 180) {
            angleDifference -= 360;
            cliffs--;
        } else if (angleDifference < -180) {
            angleDifference += 360;
            cliffs++;
        }

        totalRotation = currentAngle - homeAngle + cliffs * 360;
        previousAngle = currentAngle;

        // PID controller to move the servo to the target rotation
        if (!rtp) return;

        // Calculate time delta for derivative and integral components
        double dt = pidTimer.seconds();
        pidTimer.reset();

        // Skip this update if time delta is too small or unreasonably large
        if (dt < 0.001 || dt > 1.0) {
            return;
        }

        // Calculate error
        double error = targetRotation - totalRotation;

        // Calculate the integral term with anti-windup
        integralSum += error * dt;
        // Anti-windup: limit the integral term
        integralSum = Math.max(-maxIntegralSum, Math.min(maxIntegralSum, integralSum));

        // If we're very close to target, don't accumulate more integral
        final double INTEGRAL_DEADZONE = 2.0;
        if (Math.abs(error) < INTEGRAL_DEADZONE) {
            // Slowly decay integral term when in deadzone to prevent oscillation
            integralSum *= 0.95;
        }

        // Calculate derivative (rate of change of error)
        double derivative = (error - lastError) / dt;
        lastError = error;

        // PID formula
        double pTerm = kP * error;
        double iTerm = kI * integralSum;
        double dTerm = kD * derivative;

        // Sum the terms to get the output
        double output = pTerm + iTerm + dTerm;

        // Apply motor power with a small deadzone to prevent jitter
        final double DEADZONE = 0.5;
        if (Math.abs(error) > DEADZONE) {
            double power = Math.min(maxPower, Math.abs(output)) * Math.signum(output);
            setPower(power);
        } else {
            // Inside deadzone, gradually reduce power to prevent abrupt stops
            setPower(0);
        }
    }

    @SuppressLint("DefaultLocale")
    public String log() {
        return String.format(
                "Current Volts: %.3f\n" +
                        "Current Angle: %.2f\n" +
                        "Total Rotation: %.2f\n" +
                        "Target Rotation: %.2f\n" +
                        "Current Power: %.3f\n" +
                        "PID Values: P=%.3f I=%.3f D=%.3f\n" +
                        "PID Terms: Error=%.2f Integral=%.2f",
                servoEncoder.getVoltage(),
                getCurrentAngle(),
                totalRotation,
                targetRotation,
                power,
                kP, kI, kD,
                targetRotation - totalRotation,
                integralSum
        );
    }

    @TeleOp(name = "Cont. Rotation Axon Test", group = "test")
    public static class CRAxonTest extends LinearOpMode {

        @Override
        public void runOpMode() throws InterruptedException {
            telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
            CRServo crservo = hardwareMap.crservo.get("rightHorizSlide");
            AnalogInput encoder = hardwareMap.get(AnalogInput.class, "rightHorizSlideEncoder");
            GamepadPair gamepads = new GamepadPair(gamepad1, gamepad2);
            RTPAxon servo = new RTPAxon(crservo, encoder);

            waitForStart();

            while (!isStopRequested()) {
                gamepads.copyStates();
                servo.update();

                if (gamepads.isPressed(-1, "dpad_up")) {
                    servo.changeTargetRotation(15);
                }
                if (gamepads.isPressed(-1, "dpad_down")) {
                    servo.changeTargetRotation(-15);
                }
                if (gamepads.isPressed(-1, "cross")) {
                    servo.setTargetRotation(0);
                }

                // Additional controls for tuning PID parameters
                if (gamepads.isPressed(-1, "triangle")) {
                    // Increase P gain
                    servo.setKP(servo.getKP() + 0.001);
                }
                if (gamepads.isPressed(-1, "square")) {
                    // Decrease P gain
                    servo.setKP(Math.max(0, servo.getKP() - 0.001));
                }

                if (gamepads.isPressed(-1, "right_bumper")) {
                    // Increase I gain
                    servo.setKI(servo.getKI() + 0.0001);
                }
                if (gamepads.isPressed(-1, "left_bumper")) {
                    // Decrease I gain
                    servo.setKI(Math.max(0, servo.getKI() - 0.0001));
                }

                if (gamepads.isPressed(-1, "touchpad")) {
                    // Reset PID values to defaults
                    servo.setKP(0.015);
                    servo.setKI(0.0005);
                    servo.setKD(0.0025);
                    servo.resetPID();
                }

                telemetry.addData("Starting angle", servo.STARTPOS);
                telemetry.addLine(servo.log());
                telemetry.addData("NTRY", servo.ntry);
                telemetry.update();
            }
        }
    }
}
