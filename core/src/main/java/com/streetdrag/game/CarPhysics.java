package com.streetdrag.game;

/** Longitudinal drag-racing simulation with configurable six-speed gearing. */
public final class CarPhysics {
    private static final float[] DEFAULT_RATIOS = {3.20f, 2.15f, 1.58f, 1.22f, .98f, .80f};
    private static final float WHEEL_RADIUS = .34f;
    private static final float IDLE_RPM = 900f;
    private static final float REDLINE_RPM = 7200f;

    private final float massKg, powerHp, torqueNm, dragArea;
    private final boolean awd;
    private final float[] ratios = DEFAULT_RATIOS.clone();
    private float finalDrive = 3.55f;
    private float finishDistance = 402f;
    private float massMultiplier = 1f;
    private float speedMps, distanceM, rpm = IDLE_RPM, nitro = 100f;
    private float acceleration, shiftTimer, engineMultiplier = 1f, grip = 1f;
    private float gearboxMultiplier = 1f;
    private int gear = 1;
    private boolean nitroActive;

    public CarPhysics(float massKg, float powerHp, float torqueNm, float dragArea, boolean awd) {
        this.massKg = Math.max(500f, massKg);
        this.powerHp = Math.max(50f, powerHp);
        this.torqueNm = Math.max(100f, torqueNm);
        this.dragArea = Math.max(.2f, dragArea);
        this.awd = awd;
    }

    public void setGearRatios(float[] values) {
        if (values == null || values.length != ratios.length) return;
        for (int i = 0; i < ratios.length; i++) ratios[i] = clamp(values[i], .5f, 4.5f);
    }

    public void setFinalDrive(float value) { finalDrive = clamp(value, 2f, 5.5f); }
    public void setFinishDistance(float value) { finishDistance = value >= 804f ? 804f : 402f; }

    public void update(float delta, float throttle, float brake, float clutch, boolean useNitro) {
        delta = clamp(delta, 0f, .05f);
        throttle = clamp(throttle, 0f, 1f);
        brake = clamp(brake, 0f, 1f);
        clutch = clamp(clutch, 0f, 1f);
        if (isFinished()) { speedMps = 0f; acceleration = 0f; return; }

        shiftTimer = Math.max(0f, shiftTimer - delta);
        float ratio = ratios[gear - 1];
        float wheelRpm = speedMps / WHEEL_RADIUS * ratio * finalDrive * 60f / (2f * (float)Math.PI);
        float freeRpm = IDLE_RPM + throttle * 4800f;
        float targetRpm = wheelRpm * (1f - clutch) + freeRpm * clutch;
        if (speedMps < 1f) targetRpm = Math.max(targetRpm, IDLE_RPM + throttle * 2300f);
        rpm += (targetRpm - rpm) * Math.min(1f, delta * 10f);
        boolean limiter = rpm >= REDLINE_RPM;
        rpm = clamp(rpm, IDLE_RPM, REDLINE_RPM);

        float effectiveMass = massKg * massMultiplier;
        float torque = torqueNm * torqueCurve(rpm) * engineMultiplier;
        nitroActive = useNitro && throttle > .05f && nitro > 0f;
        float nitroMultiplier = 1f;
        if (nitroActive) {
            nitroMultiplier = 1.27f;
            nitro = Math.max(0f, nitro - 26f * delta);
        }

        float wheelForce = torque * ratio * finalDrive * .87f / WHEEL_RADIUS;
        float watts = powerHp * 745.7f * engineMultiplier * nitroMultiplier;
        float powerForce = watts * .90f / Math.max(speedMps, 4f);
        float traction = effectiveMass * 9.81f * (awd ? 1.20f : .97f) * grip;
        float driveForce = Math.min(wheelForce, Math.min(powerForce, traction));
        if (shiftTimer > 0f) driveForce *= .24f;
        if (limiter) driveForce *= .12f;
        driveForce *= throttle * (1f - clutch);

        float aero = .5f * 1.225f * dragArea * speedMps * speedMps;
        float rolling = effectiveMass * 9.81f * .013f;
        float braking = brake * effectiveMass * 10f;
        acceleration = (driveForce - aero - rolling - braking) / effectiveMass;
        speedMps = clamp(speedMps + acceleration * delta, 0f, 110f);
        distanceM = Math.min(finishDistance, distanceM + speedMps * delta);
    }

    private float torqueCurve(float r) {
        if (r < 1500f) return .68f;
        if (r < 3500f) return .68f + (r - 1500f) / 2000f * .32f;
        if (r < 5200f) return 1f;
        return Math.max(.62f, 1f - (r - 5200f) / 2500f * .38f);
    }

    public boolean shiftUp() {
        if (gear >= ratios.length || shiftTimer > 0f) return false;
        float old = ratios[gear - 1];
        gear++;
        rpm *= ratios[gear - 1] / old;
        shiftTimer = .28f / gearboxMultiplier;
        return true;
    }

    public boolean shiftDown() {
        if (gear <= 1 || shiftTimer > 0f) return false;
        float next = rpm * ratios[gear - 2] / ratios[gear - 1];
        if (next > REDLINE_RPM) return false;
        gear--;
        rpm = next;
        shiftTimer = .28f / gearboxMultiplier;
        return true;
    }

    public void reset() {
        speedMps = distanceM = acceleration = 0f;
        rpm = IDLE_RPM;
        nitro = 100f;
        shiftTimer = 0f;
        gear = 1;
        nitroActive = false;
    }

    public void setEngineMultiplier(float value) { engineMultiplier = clamp(value, 1f, 2.5f); }
    public void setGripMultiplier(float value) { grip = clamp(value, .7f, 1.6f); }
    public void setGearboxMultiplier(float value) { gearboxMultiplier = clamp(value, 1f, 2f); }
    public void setMassMultiplier(float value) { massMultiplier = clamp(value, .78f, 1f); }
    public float getTorqueAtRpm(float requestedRpm) {
        float safeRpm = clamp(requestedRpm, IDLE_RPM, REDLINE_RPM);
        return torqueNm * torqueCurve(safeRpm) * engineMultiplier;
    }
    public float getPowerHpAtRpm(float requestedRpm) {
        float safeRpm = clamp(requestedRpm, IDLE_RPM, REDLINE_RPM);
        return getTorqueAtRpm(safeRpm) * safeRpm / 7127f;
    }
    public float getSpeedKmh() { return speedMps * 3.6f; }
    public float getSpeedMps() { return speedMps; }
    public float getDistance() { return distanceM; }
    public float getRpm() { return rpm; }
    public float getNitro() { return nitro; }
    public float getAcceleration() { return acceleration; }
    public int getGear() { return gear; }
    public float getGearRatio(int index) { return ratios[Math.max(0, Math.min(index, 5))]; }
    public float getProgress() { return clamp(distanceM / finishDistance, 0f, 1f); }
    public float getFinishDistance() { return finishDistance; }
    public boolean isFinished() { return distanceM >= finishDistance; }
    public boolean isNitroActive() { return nitroActive; }
    private static float clamp(float v, float min, float max) { return Math.max(min, Math.min(max, v)); }
}
