package com.streetdrag.game;

import com.badlogic.gdx.Gdx;
import com.streetdrag.game.rendering.PbrVehicleFleetRenderer;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector2;

/** Offline workshop: engine upgrades and per-car adjustable six-speed gearbox. */
final class TuningScreen extends Base3DScreen {
    private final VehicleModel fallbackCar;
    private final VehicleSpec spec;
    private final PbrVehicleFleetRenderer pbr;
    private final String[] names = {"ENGINE", "TURBO", "GEARBOX", "TIRES"};
    private String message = "";
    private float messageTime;

    TuningScreen(StreetDragGame game) {
        super(game);
        world = new World3D(true);
        spec = game.profile.car();
        pbr = new PbrVehicleFleetRenderer(camera, spec);
        fallbackCar = pbr.hasModel(spec) ? null : new VehicleModel(spec);
        pbr.setTransform(spec, 0f, 0f, 0f, 0f);
        camera.fieldOfView = 52f;
        camera.position.set(7.5f, 3f, .3f);
        camera.lookAt(0f, .8f, 0f);
        camera.update();
    }

    @Override public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override public boolean touchDown(int sx, int sy, int pointer, int button) {
                Vector2 p = touch(sx, sy);
                if (p.x < 250f && p.y < 115f) { game.showGarage(); return true; }

                if (p.x >= 830f && p.x < 1255f && p.y >= 95f && p.y <= 205f) {
                    int cell = Math.min(5, (int)((p.x - 830f) / 70f));
                    float value = game.profile.gearRatio(cell);
                    float step = p.x - (830f + cell * 70f) < 35f ? -.05f : .05f;
                    game.profile.setGearRatio(cell, value + step);
                    message = "GEAR " + (cell + 1) + " RATIO  " + String.format("%.2f", game.profile.gearRatio(cell));
                    messageTime = 2f;
                    return true;
                }

                if (p.x >= 830f && p.x < 1255f && p.y < 85f) {
                    float step = p.x < 1042f ? -.05f : .05f;
                    game.profile.setFinalDrive(game.profile.finalDrive() + step);
                    message = "FINAL DRIVE  " + String.format("%.2f", game.profile.finalDrive());
                    messageTime = 2f;
                    return true;
                }

                for (int i = 0; i < 4; i++) {
                    if (hit(p.x, p.y, 830f, 520f - i * 98f, 420f, 82f)) upgrade(i);
                }
                return true;
            }

            @Override public boolean keyDown(int key) {
                if (key == Input.Keys.ESCAPE) game.showGarage();
                if (key >= Input.Keys.NUM_1 && key <= Input.Keys.NUM_4) upgrade(key - Input.Keys.NUM_1);
                return true;
            }
        });
    }

    private void upgrade(int category) {
        boolean ok = game.profile.upgrade(category);
        message = ok ? names[category] + " UPGRADED" : "NOT ENOUGH CASH / MAX LEVEL";
        messageTime = 2.2f;
    }

    @Override void renderModels(ModelBatch batch) {
        if (fallbackCar != null) fallbackCar.render(batch, world.environment);
    }

    @Override void renderPbr(float delta) { pbr.render(camera, delta); }

    @Override public void render(float delta) {
        clear(.012f, .018f, .03f);
        renderWorld(delta);
        beginHud();
        panel(805, 0, 475, 720, new Color(.012f, .022f, .04f, .91f));
        panel(25, 22, 205, 75, new Color(.08f, .15f, .25f, 1));
        VehicleSpec current = game.profile.car();

        beginText();
        text("PERFORMANCE TUNING", 40, 675, 1.8f, Color.WHITE);
        text(current.name, 42, 625, 1.15f, Color.CYAN);
        text("$ " + game.profile.money, 1020, 675, 1.25f, Color.GREEN);

        for (int i = 0; i < 4; i++) {
            int y = 520 - i * 98;
            panel(830, y, 420, 82, new Color(.08f, .11f, .16f, .96f));
            panel(1010, y + 25, 170, 13, new Color(.02f, .03f, .05f, 1));
            panel(1010, y + 25, 17 * game.profile.level(i), 13, new Color(.12f, .78f, .38f, 1));
            text(names[i], 852, y + 60, 1.02f, Color.WHITE);
            text("LEVEL " + game.profile.level(i), 852, y + 20, .85f, Color.LIGHT_GRAY);
            text("UPGRADE  $" + game.profile.upgradeCost(i), 1020, y + 57, .82f, Color.CYAN);
        }

        panel(830, 95, 420, 110, new Color(.055f, .085f, .13f, .98f));
        text("6-SPEED GEARBOX  /  TAP LEFT - OR RIGHT +", 840, 193, .72f, Color.YELLOW);
        for (int i = 0; i < 6; i++) {
            int x = 835 + i * 69;
            panel(x, 112, 64, 57, new Color(.11f, .15f, .20f, 1));
            text("G" + (i + 1), x + 18, 155, .78f, Color.WHITE);
            text(String.format("%.2f", game.profile.gearRatio(i)), x + 7, 128, .78f, Color.CYAN);
        }

        panel(830, 22, 420, 62, new Color(.08f, .13f, .18f, 1));
        text("FINAL DRIVE", 850, 59, .82f, Color.WHITE);
        text("-    " + String.format("%.2f", game.profile.finalDrive()) + "    +",
             1040, 59, .95f, Color.CYAN);
        text("BACK", 83, 70, 1.2f, Color.WHITE);
        if (messageTime > 0f) {
            messageTime -= delta;
            text(message, 840, 7, .72f, Color.YELLOW);
        }
        endText();
    }

    @Override public void dispose() {
        pbr.dispose();
        if (fallbackCar != null) fallbackCar.dispose();
        super.dispose();
    }
}
