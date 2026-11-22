package com.streetdrag.game;

/** Lightweight longitudinal drag-racing physics. SI units internally. */
public final class CarPhysics {
    private static final float[] RATIOS = {3.20f, 2.15f, 1.58f, 1.22f, 0.98f, 0.80f};
    private static final float FINAL_DRIVE = 3.55f;
    private static final float WHEEL_RADIUS = 0.34f;
    private static final float IDLE_RPM = 900f;
    private static final float REDLINE_RPM = 7200f;

    private final float massKg, powerHp, torqueNm, dragArea;
    private final boolean awd;
    private float speedMps, distanceM, rpm = IDLE_RPM, nitro = 100f;
    private float acceleration, shiftTimer, engineMultiplier = 1f, grip = 1f;
    private float gearboxMultiplier = 1f;
    private int gear = 1;
    private boolean nitroActive;

    public CarPhysics(float massKg, float powerHp, float torqueNm, float dragArea, boolean awd) {
        this.massKg = Math.max(500f, massKg);
        this.powerHp = Math.max(50f, powerHp);
        this.torqueNm = Math.max(100f, torqueNm);
        this.dragArea = Math.max(0.2f, dragArea);
        this.awd = awd;
    }

    public void update(float delta, float throttle, float brake, float clutch, boolean useNitro) {
        delta = clamp(delta, 0f, 0.05f);
        throttle = clamp(throttle, 0f, 1f);
        brake = clamp(brake, 0f, 1f);
        clutch = clamp(clutch, 0f, 1f);
        if (isFinished()) { speedMps = 0f; acceleration = 0f; return; }

        shiftTimer = Math.max(0f, shiftTimer - delta);
        float ratio = RATIOS[gear - 1];
        float wheelRpm = speedMps / WHEEL_RADIUS * ratio * FINAL_DRIVE * 60f / (2f * (float)Math.PI);
        float freeRpm = IDLE_RPM + throttle * 4800f;
        float targetRpm = wheelRpm * (1f - clutch) + freeRpm * clutch;
        if (speedMps < 1f) targetRpm = Math.max(targetRpm, IDLE_RPM + throttle * 2300f);
        rpm += (targetRpm - rpm) * Math.min(1f, delta * 10f);
        boolean limiter = rpm >= REDLINE_RPM;
        rpm = clamp(rpm, IDLE_RPM, REDLINE_RPM);

        float torque = torqueNm * torqueCurve(rpm);
        nitroActive = useNitro && throttle > 0.05f && nitro > 0f;
        float nitroMultiplier = 1f;
        if (nitroActive) { nitroMultiplier = 1.27f; nitro = Math.max(0f, nitro - 26f * delta); }

        float wheelForce = torque * ratio * FINAL_DRIVE * 0.87f / WHEEL_RADIUS;
        float watts = powerHp * 745.7f * engineMultiplier * nitroMultiplier;
        float powerForce = watts * 0.90f / Math.max(speedMps, 4f);
        float traction = massKg * 9.81f * (awd ? 1.20f : 0.97f) * grip;
        float driveForce = Math.min(wheelForce, Math.min(powerForce, traction));
        if (shiftTimer > 0f) driveForce *= 0.24f;
        if (limiter) driveForce *= 0.12f;
        driveForce *= throttle * (1f - clutch);

        float aero = 0.5f * 1.225f * dragArea * speedMps * speedMps;
        float rolling = massKg * 9.81f * 0.013f;
        float braking = brake * massKg * 10.0f;
        acceleration = (driveForce - aero - rolling - braking) / massKg;
        speedMps = clamp(speedMps + acceleration * delta, 0f, 110f);
        distanceM = Math.min(402f, distanceM + speedMps * delta);
    }

    private float torqueCurve(float r) {
        if (r < 1500f) return 0.68f;
        if (r < 3500f) return 0.68f + (r - 1500f) / 2000f * 0.32f;
        if (r < 5200f) return 1f;
        return Math.max(0.62f, 1f - (r - 5200f) / 2500f * 0.38f);
    }

    public boolean shiftUp() {
        if (gear >= RATIOS.length || shiftTimer > 0f) return false;
        float old = RATIOS[gear - 1];
        gear++;
        rpm *= RATIOS[gear - 1] / old;
        shiftTimer = 0.28f / gearboxMultiplier;
        return true;
    }

    public boolean shiftDown() {
        if (gear <= 1 || shiftTimer > 0f) return false;
        float next = rpm * RATIOS[gear - 2] / RATIOS[gear - 1];
        if (next > REDLINE_RPM) return false;
        gear--;
        rpm = next;
        shiftTimer = 0.28f / gearboxMultiplier;
        return true;
    }

    public void reset() {
        speedMps = distanceM = acceleration = 0f;
        rpm = IDLE_RPM; nitro = 100f; shiftTimer = 0f; gear = 1; nitroActive = false;
    }
    public void setEngineMultiplier(float value) { engineMultiplier = clamp(value, 1f, 2.5f); }
    public void setGripMultiplier(float value) { grip = clamp(value, 0.7f, 1.6f); }
    public void setGearboxMultiplier(float value) { gearboxMultiplier = clamp(value, 1f, 2f); }
    public float getSpeedKmh() { return speedMps * 3.6f; }
    public float getSpeedMps() { return speedMps; }
    public float getDistance() { return distanceM; }
    public float getRpm() { return rpm; }
    public float getNitro() { return nitro; }
    public float getAcceleration() { return acceleration; }
    public int getGear() { return gear; }
    public float getProgress() { return clamp(distanceM / 402f, 0f, 1f); }
    public boolean isFinished() { return distanceM >= 402f; }
    public boolean isNitroActive() { return nitroActive; }
    private static float clamp(float v, float min, float max) { return Math.max(min, Math.min(max, v)); }
}
