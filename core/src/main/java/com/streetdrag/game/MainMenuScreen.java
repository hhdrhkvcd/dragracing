package com.streetdrag.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;
import com.streetdrag.game.rendering.PbrVehicleFleetRenderer;

/** Offline main menu. All actions stay on-device and use the local profile. */
final class MainMenuScreen extends Base3DScreen {
    private final VehicleSpec spec;
    private final PbrVehicleFleetRenderer pbr;
    private final VehicleModel fallbackCar;

    MainMenuScreen(StreetDragGame game) {
        super(game);
        world = new World3D(true);
        spec = game.profile.car();
        pbr = new PbrVehicleFleetRenderer(camera, spec);
        fallbackCar = pbr.hasModel(spec) ? null : new VehicleModel(spec);
        if (fallbackCar != null) fallbackCar.setTransform(0f, 0f, 0f, 0f);

        camera.fieldOfView = 49f;
        camera.position.set(8.6f, 3.2f, .5f);
        camera.lookAt(0f, .9f, 0f);
        camera.far = 110f;
        camera.update();
        pbr.setTransform(spec, 0f, 0f, 0f, 0f);
    }

    @Override public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override public boolean touchDown(int sx, int sy, int pointer, int button) {
                Vector3 point = new Vector3(sx, sy, 0f);
                game.hudViewport.unproject(point);
                float x = point.x;
                float y = point.y;

                if (y < 160f) {
                    if (x < 420f) game.showRace();
                    else if (x < 850f) game.showGarage();
                    else game.showTuning();
                    return true;
                }

                if (y > 270f && y < 520f) {
                    if (x < 175f) game.profile.select(game.profile.selected - 1);
                    else if (x > 1100f) game.profile.select(game.profile.selected + 1);
                    return true;
                }
                return true;
            }

            @Override public boolean keyDown(int key) {
                if (key == Input.Keys.ENTER || key == Input.Keys.SPACE) game.showRace();
                if (key == Input.Keys.G) game.showGarage();
                if (key == Input.Keys.T) game.showTuning();
                if (key == Input.Keys.LEFT) game.profile.select(game.profile.selected - 1);
                if (key == Input.Keys.RIGHT) game.profile.select(game.profile.selected + 1);
                return true;
            }
        });
    }

    @Override void renderModels(ModelBatch batch) {
        if (fallbackCar != null) fallbackCar.render(batch, world.environment);
    }

    @Override void renderPbr(float delta) {
        pbr.render(camera, delta);
    }

    @Override public void render(float delta) {
        clear(.008f, .014f, .027f);
        renderWorld(delta);

        beginHud();
        panel(0, 0, 1280, 190, new Color(.008f, .014f, .027f, .96f));
        panel(28, 420, 470, 250, new Color(.012f, .024f, .043f, .90f));
        panel(28, 22, 390, 135, new Color(.48f, .055f, .07f, 1f));
        panel(445, 22, 390, 135, new Color(.075f, .17f, .30f, 1f));
        panel(862, 22, 390, 135, new Color(.06f, .34f, .19f, 1f));

        beginText();
        text("STREET DRAG 3D", 55, 635, 2.25f, Color.WHITE);
        text("OFFLINE EDITION", 59, 590, 1.05f, Color.CYAN);
        text("YOUR GARAGE. YOUR CARS. YOUR RACE.", 60, 535, .90f, Color.LIGHT_GRAY);
        text("DRIVER PROFILE", 60, 485, .90f, Color.YELLOW);
        text("CASH   $ " + game.profile.money, 60, 450, 1.1f, Color.GREEN);
        text("CAR   " + spec.name, 60, 402, 1.0f, Color.WHITE);
        text("POWER   " + (int)(spec.powerHp * game.profile.engineMultiplier()) + " HP", 60, 366, .95f, Color.CYAN);
        text("LOCAL SAVE  /  NO ACCOUNT  /  NO INTERNET", 60, 325, .82f, Color.LIGHT_GRAY);
        text("QUICK RACE", 105, 83, 1.35f, Color.WHITE);
        text("GARAGE", 575, 83, 1.35f, Color.WHITE);
        text("TUNING", 1000, 83, 1.35f, Color.WHITE);
        text("LEFT / RIGHT: SELECT CAR     ENTER: QUICK RACE     G: GARAGE     T: TUNING",
             300, 210, .78f, Color.LIGHT_GRAY);
        endText();
    }

    @Override public void dispose() {
        pbr.dispose();
        if (fallbackCar != null) fallbackCar.dispose();
        super.dispose();
    }
}
