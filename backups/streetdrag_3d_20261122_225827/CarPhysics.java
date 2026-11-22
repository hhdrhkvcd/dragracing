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
    private final float torqueNm;
    private final float dragArea;
    private final boolean awd;

    private float speed;
    private float distance;
    private float rpm = IDLE_RPM;
    private float acceleration;
    private float nitro = 100f;

    private float shiftTimer;
    private float shiftDuration = 0.26f;

    private float engineUpgrade = 1f;
    private float grip = 1f;

    private int gear = 1;
    private boolean nitroActive;

    public CarPhysics(
        float mass,
        float power,
        float torque,
        float drag,
        boolean awd
    ) {
        massKg = Math.max(500f, mass);
        powerHp = Math.max(50f, power);
        torqueNm = Math.max(100f, torque);
        dragArea = Math.max(0.2f, drag);
        this.awd = awd;
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

        float targetRpm;

        if (clutch > 0.5f) {
            targetRpm = IDLE_RPM + throttle * 4800f;
        } else {
            targetRpm = Math.max(
                IDLE_RPM + throttle * 1700f,
                wheelRpm
            );
        }

        rpm += (targetRpm - rpm) * Math.min(1f, delta * 12f);

        boolean limiter = rpm >= REDLINE_RPM;
        rpm = clamp(rpm, IDLE_RPM, REDLINE_RPM);

        float engineTorque = torqueNm * torqueCurve(rpm);

        nitroActive =
            useNitro && throttle > 0.05f && nitro > 0f;

        float nitroMultiplier = 1f;

        if (nitroActive) {
            nitroMultiplier = 1.28f;
            nitro = Math.max(0f, nitro - 28f * delta);
        }

        float wheelForce =
            engineTorque * ratio * FINAL_DRIVE * 0.88f
            / WHEEL_RADIUS;

        float engineWatts =
            powerHp * 745.7f
            * engineUpgrade
            * nitroMultiplier;

        float powerForce =
            engineWatts * 0.90f / Math.max(speed, 4f);

        float traction =
            massKg * 9.81f
            * (awd ? 1.23f : 0.98f)
            * grip;

        float driveForce = Math.min(
            wheelForce,
            Math.min(powerForce, traction)
        );

        if (shiftTimer > 0f) driveForce *= 0.25f;
        if (limiter) driveForce *= 0.10f;

        driveForce *= throttle * (1f - clutch);

        float airResistance =
            0.5f * 1.225f * dragArea * speed * speed;

        float rollingResistance =
            massKg * 9.81f * 0.013f;

        float brakingForce =
            brake * massKg * 10.5f;

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
            return 0.68f
                + (value - 1500f) / 2000f * 0.32f;
        }

        if (value < 5200f) return 1f;

        return Math.max(
            0.62f,
            1f - (value - 5200f) / 2500f * 0.38f
        );
    }

    public boolean shiftUp() {

        if (gear >= GEARS.length || shiftTimer > 0f) {
            return false;
        }

        float oldRatio = GEARS[gear - 1];

        gear++;

        rpm *= GEARS[gear - 1] / oldRatio;
        shiftTimer = shiftDuration;

        return true;
    }

    public boolean shiftDown() {

        if (gear <= 1 || shiftTimer > 0f) return false;

        float nextRpm =
            rpm * GEARS[gear - 2] / GEARS[gear - 1];

        if (nextRpm > REDLINE_RPM) return false;

        gear--;
        rpm = nextRpm;
        shiftTimer = shiftDuration;

        return true;
    }

    public void reset() {
        speed = 0f;
        distance = 0f;
        rpm = IDLE_RPM;
        acceleration = 0f;
        nitro = 100f;
        shiftTimer = 0f;
        gear = 1;
        nitroActive = false;
    }

    public void setEngineUpgrade(float value) {
        engineUpgrade = clamp(value, 1f, 2.5f);
    }

    public void setGripMultiplier(float value) {
        grip = clamp(value, 0.7f, 1.5f);
    }

    public void setGearboxUpgrade(float value) {
        shiftDuration = clamp(
            0.28f - (value - 1f) * 0.04f,
            0.12f,
            0.28f
        );
    }

    public float getSpeedKmh() { return speed * 3.6f; }
    public float getSpeedMps() { return speed; }
    public float getDistance() { return distance; }
    public float getRpm() { return rpm; }
    public float getNitro() { return nitro; }
    public float getAcceleration() { return acceleration; }
    public int getGear() { return gear; }

    public float getRaceProgress() {
        return clamp(distance / 402f, 0f, 1f);
    }

    public boolean isFinished() { return distance >= 402f; }
    public boolean isNitroActive() { return nitroActive; }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}