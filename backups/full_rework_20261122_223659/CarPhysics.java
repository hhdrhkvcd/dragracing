
package com.streetdrag.game;

public final class CarPhysics {

    private static final float[] GEARS = {
        3.20f, 2.15f, 1.58f, 1.22f, 0.98f, 0.80f
    };

    private static final float FINAL_DRIVE = 3.55f;
    private static final float WHEEL_RADIUS = 0.34f;
    private static final float IDLE_RPM = 900f;
    private static final float REDLINE_RPM = 7200f;

    private final float massKg;
    private final float powerHp;
    private final float peakTorqueNm;
    private final float dragArea;
    private final boolean awd;

    private float speed;
    private float distance;
    private float rpm = IDLE_RPM;
    private float nitro = 100f;
    private float acceleration;

    private float shiftTimer;
    private float engineUpgrade = 1f;
    private float gripMultiplier = 1f;

    private boolean nitroActive;
    private int gear = 1;

    public CarPhysics(
        float massKg,
        float powerHp,
        float peakTorqueNm,
        float dragArea,
        boolean awd
    ) {
        this.massKg = Math.max(500f, massKg);
        this.powerHp = Math.max(50f, powerHp);
        this.peakTorqueNm = Math.max(100f, peakTorqueNm);
        this.dragArea = Math.max(0.2f, dragArea);
        this.awd = awd;
    }

    public void update(
        float delta,
        float throttle,
        float brake,
        boolean useNitro
    ) {
        update(delta, throttle, brake, 0f, useNitro);
    }

    public void update(
        float delta,
        float throttle,
        float brake,
        float clutch,
        boolean useNitro
    ) {
        delta = clamp(delta, 0f, 0.05f);
        throttle = clamp(throttle, 0f, 1f);
        brake = clamp(brake, 0f, 1f);
        clutch = clamp(clutch, 0f, 1f);

        if (isFinished()) {
            speed = 0f;
            acceleration = 0f;
            return;
        }

        shiftTimer = Math.max(0f, shiftTimer - delta);

        float ratio = GEARS[gear - 1];

        float wheelRpm =
            speed / WHEEL_RADIUS
                * ratio * FINAL_DRIVE
                * 60f / (2f * (float)Math.PI);

        float freeRpm = IDLE_RPM + throttle * 5000f;

        float targetRpm =
            wheelRpm * (1f - clutch)
                + freeRpm * clutch;

        if (speed < 1f) {
            targetRpm = Math.max(targetRpm, IDLE_RPM + throttle * 2500f);
        }

        rpm += (targetRpm - rpm) * Math.min(1f, delta * 9f);

        boolean limiter = rpm >= REDLINE_RPM;
        rpm = clamp(rpm, IDLE_RPM, REDLINE_RPM);

        float torque = peakTorqueNm * torqueCurve(rpm);

        nitroActive = useNitro && throttle > 0.05f && nitro > 0f;

        float nitroMultiplier = 1f;

        if (nitroActive) {
            nitroMultiplier = 1.28f;
            nitro = Math.max(0f, nitro - 28f * delta);
        }

        float wheelForce =
            torque * ratio * FINAL_DRIVE * 0.88f
                / WHEEL_RADIUS;

        float engineWatts =
            powerHp * 745.7f * engineUpgrade * nitroMultiplier;

        float powerForce =
            engineWatts * 0.90f / Math.max(speed, 4f);

        float traction =
            massKg * 9.81f
                * (awd ? 1.20f : 0.98f)
                * gripMultiplier;

        float driveForce = Math.min(
            wheelForce,
            Math.min(powerForce, traction)
        );

        if (shiftTimer > 0f) driveForce *= 0.22f;
        if (limiter) driveForce *= 0.10f;

        driveForce *= throttle * (1f - clutch);

        float airResistance =
            0.5f * 1.225f * dragArea * speed * speed;

        float rollingResistance = massKg * 9.81f * 0.013f;
        float brakingForce = brake * massKg * 10.5f;

        float netForce =
            driveForce
                - airResistance
                - rollingResistance
                - brakingForce;

        acceleration = netForce / massKg;

        speed += acceleration * delta;
        speed = clamp(speed, 0f, 110f);

        distance += speed * delta;
        distance = Math.min(distance, 402f);
    }

    private float torqueCurve(float value) {

        if (value < 1500f) return 0.68f;

        if (value < 3500f) {
            return 0.68f + (value - 1500f) / 2000f * 0.32f;
        }

        if (value < 5200f) return 1f;

        return Math.max(
            0.62f,
            1f - (value - 5200f) / 2500f * 0.38f
        );
    }

    public boolean shiftUp() {

        if (shiftTimer > 0f || gear >= GEARS.length) {
            return false;
        }

        float oldRatio = GEARS[gear - 1];

        gear++;

        float newRatio = GEARS[gear - 1];

        rpm *= newRatio / oldRatio;
        shiftTimer = 0.26f;

        return true;
    }

    public boolean shiftDown() {

        if (shiftTimer > 0f || gear <= 1) {
            return false;
        }

        float nextRpm =
            rpm * GEARS[gear - 2] / GEARS[gear - 1];

        if (nextRpm > REDLINE_RPM) return false;

        gear--;
        rpm = nextRpm;
        shiftTimer = 0.26f;

        return true;
    }

    public void reset() {
        speed = 0f;
        distance = 0f;
        rpm = IDLE_RPM;
        nitro = 100f;
        acceleration = 0f;
        shiftTimer = 0f;
        gear = 1;
        nitroActive = false;
    }

    public void setEngineUpgrade(float value) {
        engineUpgrade = clamp(value, 1f, 2.5f);
    }

    public void setGripMultiplier(float value) {
        gripMultiplier = clamp(value, 0.7f, 1.5f);
    }

    public float getSpeedKmh() {
        return speed * 3.6f;
    }

    public float getSpeedMps() {
        return speed;
    }

    public float getDistance() {
        return distance;
    }

    public float getRpm() {
        return rpm;
    }

    public float getNitro() {
        return nitro;
    }

    public int getGear() {
        return gear;
    }

    public float getAcceleration() {
        return acceleration;
    }

    public float getRaceProgress() {
        return clamp(distance / 402f, 0f, 1f);
    }

    public boolean isFinished() {
        return distance >= 402f;
    }

    public boolean isNitroActive() {
        return nitroActive;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
