package com.streetdrag.game;

import com.badlogic.gdx.Gdx;
import com.streetdrag.game.rendering.PbrVehicleFleetRenderer;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector2;

/** 3D GLB vehicle in the workshop plus four persistent offline upgrade categories. */
final class TuningScreen extends Base3DScreen {
    private final VehicleModel fallbackCar;
    private final VehicleSpec spec;
    private final PbrVehicleFleetRenderer pbr;
    private final String[] names={"ENGINE","TURBO","GEARBOX","TIRES"};
    private String message="";
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
                if (p.x < 250 && p.y < 115) { game.showGarage(); return true; }
                for (int i = 0; i < 4; i++) {
                    if (hit(p.x, p.y, 900, 430 - i * 105, 350, 82)) upgrade(i);
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

    @Override void renderPbr(float delta) {
        pbr.render(camera, delta);
    }

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
            int y = 470 - i * 112;
            panel(830, y, 420, 88, new Color(.08f, .11f, .16f, .96f));
            panel(1010, y + 27, 170, 13, new Color(.02f, .03f, .05f, 1));
            panel(1010, y + 27, 17 * game.profile.level(i), 13, new Color(.12f, .78f, .38f, 1));
            text(names[i], 852, y + 65, 1.08f, Color.WHITE);
            text("LEVEL " + game.profile.level(i), 852, y + 28, .90f, Color.LIGHT_GRAY);
            text("UPGRADE  $" + game.profile.upgradeCost(i), 1020, y + 63, .88f, Color.CYAN);
        }
        text("BACK", 83, 70, 1.2f, Color.WHITE);
        if (messageTime > 0f) { messageTime -= delta; text(message, 840, 48, .85f, Color.YELLOW); }
        endText();
    }

    @Override public void dispose() {
        pbr.dispose();
        if (fallbackCar != null) fallbackCar.dispose();
        super.dispose();
    }
}
