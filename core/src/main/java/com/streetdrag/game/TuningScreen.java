package com.streetdrag.game;

import com.badlogic.gdx.Gdx;
import com.streetdrag.game.rendering.PbrVehicleFleetRenderer;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector2;

/** Eight upgrade systems and a per-car configurable six-speed gearbox. */
final class TuningScreen extends Base3DScreen {
    private static final String[] NAMES = {
        "ENGINE", "TURBO", "GEARBOX", "TYRES",
        "INTAKE", "EXHAUST", "CLUTCH", "WEIGHT REDUCTION"
    };

    private final VehicleModel fallbackCar;
    private final VehicleSpec spec;
    private final PbrVehicleFleetRenderer pbr;
    private String message = "";
    private float messageTime;

    TuningScreen(StreetDragGame game) {
        super(game);
        world = new World3D(true);
        spec = game.profile.car();
        pbr = new PbrVehicleFleetRenderer(camera, spec);
        fallbackCar = pbr.hasModel(spec) ? null : new VehicleModel(spec);
        if (fallbackCar != null) fallbackCar.setTransform(0f, 0f, 0f, 0f);
        pbr.setTransform(spec, 0f, 0f, 0f, 0f);

        camera.fieldOfView = 52f;
        camera.position.set(7.5f, 3f, .3f);
        camera.lookAt(0f, .8f, 0f);
        camera.update();
    }

    @Override public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override public boolean touchDown(int sx, int sy, int pointer, int button) {
                Vector2 point = touch(sx, sy);
                float x = point.x;
                float y = point.y;

                if (x < 250f && y < 115f) {
                    game.showGarage();
                    return true;
                }
                if (x > 820f && y > 575f) {
                    game.showDyno();
                    return true;
                }
                if (x >= 820f && x <= 1255f && y >= 25f && y <= 140f) {
                    if (x < 1115f) {
                        int gear = Math.min(5, Math.max(0, (int)((x - 830f) / 47f)));
                        float localX = x - (830f + gear * 47f);
                        float step = localX < 23.5f ? -.05f : .05f;
                        game.profile.setGearRatio(gear, game.profile.gearRatio(gear) + step);
                        message = "GEAR " + (gear + 1) + "  " + String.format("%.2f", game.profile.gearRatio(gear));
                    } else {
                        float step = x < 1185f ? -.05f : .05f;
                        game.profile.setFinalDrive(game.profile.finalDrive() + step);
                        message = "FINAL DRIVE  " + String.format("%.2f", game.profile.finalDrive());
                    }
                    messageTime = 2f;
                    return true;
                }

                for (int i = 0; i < NAMES.length; i++) {
                    float bx = i < 4 ? 820f : 1035f;
                    float by = 460f - (i % 4) * 100f;
                    if (hit(x, y, bx, by, 205f, 84f)) {
                        upgrade(i);
                        return true;
                    }
                }
                return true;
            }

            @Override public boolean keyDown(int key) {
                if (key == Input.Keys.ESCAPE) game.showGarage();
                if (key == Input.Keys.D) game.showDyno();
                if (key >= Input.Keys.NUM_1 && key <= Input.Keys.NUM_4) upgrade(key - Input.Keys.NUM_1);
                if (key >= Input.Keys.NUM_5 && key <= Input.Keys.NUM_8) upgrade(key - Input.Keys.NUM_5 + 4);
                return true;
            }
        });
    }

    private void upgrade(int category) {
        boolean success = game.profile.upgrade(category);
        message = success
            ? NAMES[category] + " UPGRADED"
            : "NOT ENOUGH CASH / MAX LEVEL";
        messageTime = 2.2f;
    }

    @Override void renderModels(ModelBatch batch) {
        if (fallbackCar != null) fallbackCar.render(batch, world.environment);
    }

    @Override void renderPbr(float delta) {
        pbr.render(camera, delta);
    }

    @Override public void render(float delta) {
        clear(.012f, .018f, .03f);
        renderWorld(delta);
        beginHud();

        panel(805, 0, 475, 720, new Color(.012f, .022f, .04f, .93f));
        panel(25, 22, 205, 75, new Color(.08f, .15f, .25f, 1f));
        panel(820, 585, 430, 58, new Color(.07f, .20f, .34f, 1f));

        VehicleSpec current = game.profile.car();
        int hp = (int)(current.powerHp * game.profile.engineMultiplier() * game.profile.turboMultiplier());
        int torque = (int)(current.torqueNm * game.profile.engineMultiplier() * game.profile.turboMultiplier());

        beginText();
        text("PERFORMANCE WORKSHOP", 40, 675, 1.7f, Color.WHITE);
        text(current.name, 42, 625, 1.1f, Color.CYAN);
        text("ESTIMATED POWER  " + hp + " HP", 42, 565, 1.0f, Color.WHITE);
        text("ESTIMATED TORQUE  " + torque + " NM", 42, 530, .92f, Color.LIGHT_GRAY);
        text("WEIGHT  " + (int)(current.massKg * game.profile.massMultiplier()) + " KG",
             42, 495, .92f, Color.LIGHT_GRAY);
        text("$ " + game.profile.money, 1030, 675, 1.15f, Color.GREEN);
        text("OPEN DYNO  >", 970, 620, 1.0f, Color.WHITE);

        for (int i = 0; i < NAMES.length; i++) {
            float x = i < 4 ? 820f : 1035f;
            float y = 460f - (i % 4) * 100f;
            int level = game.profile.level(i);

            panel(x, y, 205, 84, new Color(.075f, .105f, .15f, .98f));
            panel(x + 12, y + 12, 180, 8, new Color(.018f, .027f, .04f, 1f));
            panel(x + 12, y + 12, 18f * level, 8, new Color(.13f, .78f, .40f, 1f));
            text(NAMES[i], x + 12, y + 66, .78f, Color.WHITE);
            text("LV " + level + "/10", x + 12, y + 38, .75f, Color.LIGHT_GRAY);
            text("$" + game.profile.upgradeCost(i), x + 108, y + 38, .72f, Color.CYAN);
        }

        panel(820, 25, 430, 115, new Color(.055f, .085f, .13f, .98f));
        text("GEARBOX  /  LEFT -   RIGHT +", 830, 126, .65f, Color.YELLOW);
        for (int i = 0; i < 6; i++) {
            int x = 832 + i * 46;
            panel(x, 48, 42, 62, new Color(.11f, .15f, .20f, 1f));
            text("G" + (i + 1), x + 11, 96, .66f, Color.WHITE);
            text(String.format("%.2f", game.profile.gearRatio(i)), x + 2, 68, .62f, Color.CYAN);
        }
        text("FINAL", 1115, 103, .62f, Color.WHITE);
        text(String.format("%.2f", game.profile.finalDrive()), 1120, 76, .8f, Color.CYAN);
        text("-     +", 1120, 49, .72f, Color.YELLOW);

        text("BACK", 83, 70, 1.2f, Color.WHITE);
        if (messageTime > 0f) {
            messageTime -= delta;
            text(message, 825, 7, .65f, Color.YELLOW);
        }
        endText();
    }

    @Override public void dispose() {
        pbr.dispose();
        if (fallbackCar != null) fallbackCar.dispose();
        super.dispose();
    }
}
