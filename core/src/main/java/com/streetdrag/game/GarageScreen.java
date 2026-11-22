package com.streetdrag.game;

import com.badlogic.gdx.Gdx;
import com.streetdrag.game.rendering.PbrVehicleFleetRenderer;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;

/** 3D garage. The PBR environment is retained when the selected car changes. */
final class GarageScreen extends Base3DScreen {
    private VehicleModel fallbackCar;
    private VehicleSpec spec;
    private final PbrVehicleFleetRenderer pbr;

    GarageScreen(StreetDragGame game) {
        super(game);
        world = new World3D(true);
        spec = game.profile.car();

        pbr = new PbrVehicleFleetRenderer(camera);
        selectCurrentCar();
        camera.fieldOfView = 48f;
        camera.position.set(8.4f, 3.1f, .25f);
        camera.lookAt(0f, .9f, 0f);
        camera.far = 100f;
        camera.update();
    }

    private void selectCurrentCar() {
        spec = game.profile.car();
        pbr.setActiveVehicle(spec);
        if (fallbackCar != null) {
            fallbackCar.dispose();
            fallbackCar = null;
        }
        if (!pbr.hasModel(spec)) {
            fallbackCar = new VehicleModel(spec);
            fallbackCar.setTransform(0f, 0f, 0f, 0f);
        }
        pbr.setTransform(spec, 0f, 0f, 0f, 0f);
    }

    @Override public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override public boolean touchDown(int sx, int sy, int pointer, int button) {
                Vector3 p3 = new Vector3(sx, sy, 0);
                game.hudViewport.unproject(p3);
                float x = p3.x, y = p3.y;
                if (y < 125f) {
                    if (x < 400f) nextCar(-1);
                    else if (x < 835f) game.showTuning();
                    else game.showRace();
                    return true;
                }
                if (y > 250f && y < 510f) {
                    if (x < 170f) nextCar(-1);
                    if (x > 1090f) nextCar(1);
                }
                return true;
            }

            @Override public boolean keyDown(int key) {
                if (key == Input.Keys.LEFT) nextCar(-1);
                if (key == Input.Keys.RIGHT) nextCar(1);
                if (key == Input.Keys.T) game.showTuning();
                if (key == Input.Keys.ENTER) game.showRace();
                return true;
            }
        });
    }

    private void nextCar(int amount) {
        game.profile.select(game.profile.selected + amount);
        selectCurrentCar();
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
        panel(0, 0, 1280, 145, new Color(.012f, .022f, .04f, .94f));
        panel(24, 486, 365, 205, new Color(.012f, .022f, .04f, .88f));
        panel(20, 22, 360, 96, new Color(.07f, .20f, .38f, 1));
        panel(405, 22, 400, 96, new Color(.09f, .12f, .18f, 1));
        panel(830, 22, 430, 96, new Color(.05f, .34f, .20f, 1));
        VehicleSpec current = spec;
        beginText();
        text("STREET DRAG  /  3D GARAGE", 38, 675, 2.0f, Color.WHITE);
        text(current.name, 45, 625, 1.35f, Color.WHITE);
        text("POWER  " + (int)(current.powerHp * game.profile.engineMultiplier()) + " HP", 45, 582, 1.15f, Color.CYAN);
        text("TORQUE  " + (int)current.torqueNm + " NM", 45, 548, 1.05f, Color.LIGHT_GRAY);
        text(current.awd ? "DRIVE  AWD" : "DRIVE  RWD", 45, 516, 1.05f, Color.LIGHT_GRAY);
        text("$ " + game.profile.money, 1010, 675, 1.45f, Color.GREEN);
        text("<  CARS", 105, 78, 1.35f, Color.WHITE);
        text("TUNING", 535, 78, 1.35f, Color.WHITE);
        text("RACE 402M  >", 940, 78, 1.35f, Color.WHITE);
        text("A / D: CHANGE CAR     ENTER: RACE     T: TUNING", 405, 172, .85f, Color.LIGHT_GRAY);
        endText();
    }

    @Override public void dispose() {
        pbr.dispose();
        if (fallbackCar != null) fallbackCar.dispose();
        super.dispose();
    }
}
