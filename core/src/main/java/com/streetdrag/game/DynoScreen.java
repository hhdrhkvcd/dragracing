package com.streetdrag.game;

import com.badlogic.gdx.Gdx;
import com.streetdrag.game.rendering.PbrVehicleFleetRenderer;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector2;

/** Offline chassis dyno using the same torque curve and upgrades as the race physics. */
final class DynoScreen extends Base3DScreen {
    private static final float[] SAMPLE_RPMS = {1000f, 2000f, 3000f, 4000f, 5000f, 6000f, 7000f};

    private final VehicleSpec spec;
    private final CarPhysics dyno;
    private final PbrVehicleFleetRenderer pbr;
    private final VehicleModel fallback;
    private boolean running;
    private float pullTime;
    private float rpm = 1000f;
    private float measuredHp;
    private float measuredTorque;

    DynoScreen(StreetDragGame game) {
        super(game);
        world = new World3D(true);
        spec = game.profile.car();
        dyno = spec.newPhysics();
        dyno.setEngineMultiplier(game.profile.engineMultiplier() * game.profile.turboMultiplier());
        pbr = new PbrVehicleFleetRenderer(camera, spec);
        fallback = pbr.hasModel(spec) ? null : new VehicleModel(spec);
        if (fallback != null) fallback.setTransform(0f, 0f, 0f, 0f);
        pbr.setTransform(spec, 0f, 0f, 0f, 0f);

        camera.fieldOfView = 51f;
        camera.position.set(7.8f, 3.1f, .2f);
        camera.lookAt(0f, .9f, 0f);
        camera.far = 110f;
        camera.update();
    }

    @Override public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override public boolean touchDown(int sx, int sy, int pointer, int button) {
                Vector2 point = touch(sx, sy);
                if (point.x < 240f && point.y < 145f) {
                    game.showTuning();
                } else if (point.x > 900f && point.y < 155f) {
                    startPull();
                } else if (point.y > 205f && point.y < 530f) {
                    int sample = Math.max(0, Math.min(6, (int)((point.x - 58f) / 94f)));
                    rpm = SAMPLE_RPMS[sample];
                    updateReading();
                }
                return true;
            }

            @Override public boolean keyDown(int key) {
                if (key == Input.Keys.ESCAPE) game.showTuning();
                if (key == Input.Keys.SPACE || key == Input.Keys.ENTER) startPull();
                if (key == Input.Keys.R) {
                    running = false;
                    pullTime = 0f;
                    rpm = 1000f;
                    updateReading();
                }
                return true;
            }
        });
    }

    private void startPull() {
        running = true;
        pullTime = 0f;
        rpm = 1000f;
        updateReading();
    }

    private void updateReading() {
        measuredTorque = dyno.getTorqueAtRpm(rpm);
        measuredHp = dyno.getPowerHpAtRpm(rpm);
    }

    private float maximumPower() {
        float max = 1f;
        for (float sample : SAMPLE_RPMS) max = Math.max(max, dyno.getPowerHpAtRpm(sample));
        return max;
    }

    @Override void renderModels(ModelBatch batch) {
        if (fallback != null) fallback.render(batch, world.environment);
    }

    @Override void renderPbr(float delta) {
        pbr.render(camera, delta);
    }

    @Override public void render(float delta) {
        if (running) {
            pullTime += Math.min(delta, .05f);
            rpm = Math.min(7200f, 1000f + pullTime * 1850f);
            updateReading();
            if (rpm >= 7200f) running = false;
        }

        clear(.008f, .014f, .027f);
        renderWorld(delta);
        beginHud();

        panel(0, 0, 1280, 190, new Color(.008f, .014f, .027f, .97f));
        panel(25, 22, 205, 78, new Color(.08f, .15f, .25f, 1f));
        panel(825, 345, 425, 300, new Color(.012f, .024f, .043f, .95f));
        panel(825, 205, 425, 120, new Color(.025f, .055f, .075f, .97f));
        panel(75, 210, 700, 290, new Color(.015f, .025f, .04f, .94f));
        panel(75, 210, 700, 2, new Color(.26f, .36f, .48f, 1f));

        float maxHp = maximumPower();
        for (int i = 0; i < SAMPLE_RPMS.length; i++) {
            float hp = dyno.getPowerHpAtRpm(SAMPLE_RPMS[i]);
            float barHeight = 205f * hp / maxHp;
            float x = 105f + i * 94f;
            Color bar = Math.abs(rpm - SAMPLE_RPMS[i]) < 500f
                ? new Color(.15f, .88f, .50f, 1f)
                : new Color(.08f, .42f, .72f, 1f);
            panel(x, 245f, 55f, barHeight, bar);
        }

        panel(25, 22, 205, 78, new Color(.08f, .15f, .25f, 1f));
        panel(850, 22, 390, 125, running
            ? new Color(.07f, .28f, .17f, 1f)
            : new Color(.08f, .17f, .28f, 1f));

        beginText();
        text("CHASSIS DYNO", 42, 675, 1.85f, Color.WHITE);
        text(spec.name, 45, 630, 1.05f, Color.CYAN);
        text("ENGINE UPGRADES INCLUDED", 45, 595, .80f, Color.LIGHT_GRAY);
        text("RPM", 855, 600, .82f, Color.LIGHT_GRAY);
        text(String.valueOf((int)rpm), 855, 550, 2.0f, Color.WHITE);
        text("HORSEPOWER", 855, 505, .82f, Color.LIGHT_GRAY);
        text(String.format("%.1f HP", measuredHp), 855, 465, 1.65f, Color.GREEN);
        text("TORQUE", 855, 420, .82f, Color.LIGHT_GRAY);
        text(String.format("%.1f NM", measuredTorque), 855, 380, 1.35f, Color.CYAN);
        text("POWER CURVE  /  SIMULATED ENGINE OUTPUT", 95, 535, .88f, Color.YELLOW);
        for (int i = 0; i < SAMPLE_RPMS.length; i++) {
            text(String.valueOf((int)(SAMPLE_RPMS[i] / 1000f)) + "k",
                 115f + i * 94f, 225f, .72f, Color.LIGHT_GRAY);
        }
        text(running ? "PULL IN PROGRESS..." : "TAP A BAR TO INSPECT RPM",
             855, 285, .82f, running ? Color.GREEN : Color.LIGHT_GRAY);
        text("BACK TO TUNING", 48, 72, 1.0f, Color.WHITE);
        text(running ? "RUNNING..." : "RUN DYNO PULL", 930, 95, 1.12f, Color.WHITE);
        text("SPACE / ENTER: RUN PULL     R: RESET", 855, 54, .68f, Color.LIGHT_GRAY);
        endText();
    }

    @Override public void dispose() {
        pbr.dispose();
        if (fallback != null) fallback.dispose();
        super.dispose();
    }
}
