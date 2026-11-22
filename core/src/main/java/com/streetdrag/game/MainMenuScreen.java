package com.streetdrag.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;
import com.streetdrag.game.rendering.PbrVehicleFleetRenderer;

/** Main menu for the offline drag-racing game. */
final class MainMenuScreen extends Base3DScreen {
    private VehicleSpec spec;
    private final PbrVehicleFleetRenderer pbr;
    private VehicleModel fallbackCar;

    MainMenuScreen(StreetDragGame game) {
        super(game);
        world = new World3D(true);
        spec = game.profile.car();
        pbr = new PbrVehicleFleetRenderer(camera);
        selectCar();
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
                    if (x < 320f) game.showRace();
                    else if (x < 640f) game.showGarage();
                    else if (x < 960f) game.showTuning();
                    else game.showCareer();
                    return true;
                }
                if (y > 420f && y < 520f && x > 500f && x < 850f) {
                    game.profile.toggleRaceDistance();
                    return true;
                }
                if (y > 270f && y < 520f) {
                    if (x < 175f) changeCar(-1);
                    else if (x > 1100f) changeCar(1);
                    return true;
                }
                return true;
            }

            @Override public boolean keyDown(int key) {
                if (key == Input.Keys.ENTER || key == Input.Keys.SPACE) game.showRace();
                if (key == Input.Keys.G) game.showGarage();
                if (key == Input.Keys.T) game.showTuning();
                if (key == Input.Keys.K) game.showCareer();
                if (key == Input.Keys.D) game.profile.toggleRaceDistance();
                if (key == Input.Keys.LEFT) changeCar(-1);
                if (key == Input.Keys.RIGHT) changeCar(1);
                return true;
            }
        });
    }

    private void changeCar(int direction) {
        game.profile.select(game.profile.selected + direction);
        selectCar();
    }

    private void selectCar() {
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

    @Override void renderModels(ModelBatch batch) {
        if (fallbackCar != null) fallbackCar.render(batch, world.environment);
    }

    @Override void renderPbr(float delta) { pbr.render(camera, delta); }

    @Override public void render(float delta) {
        clear(.008f, .014f, .027f);
        renderWorld(delta);
        beginHud();
        panel(0, 0, 1280, 190, new Color(.008f, .014f, .027f, .96f));
        panel(28, 420, 470, 250, new Color(.012f, .024f, .043f, .90f));
        panel(28, 22, 300, 135, new Color(.48f, .055f, .07f, 1f));
        panel(340, 22, 300, 135, new Color(.075f, .17f, .30f, 1f));
        panel(652, 22, 300, 135, new Color(.14f, .22f, .12f, 1f));
        panel(964, 22, 288, 135, new Color(.06f, .34f, .19f, 1f));
        panel(515, 428, 330, 75, new Color(.05f, .15f, .22f, .95f));

        beginText();
        text("STREET DRAG 3D", 55, 635, 2.25f, Color.WHITE);
        text("OFFLINE EDITION", 59, 590, 1.05f, Color.CYAN);
        text("LOCAL DRIVER PROFILE", 60, 535, .90f, Color.YELLOW);
        text("CASH   $ " + game.profile.money, 60, 485, 1.1f, Color.GREEN);
        text("CAR   " + spec.name, 60, 445, .95f, Color.WHITE);
        text("POWER   " + (int)(spec.powerHp * game.profile.engineMultiplier()) + " HP", 60, 405, .90f, Color.CYAN);
        text("CAREER WINS   " + game.profile.careerWins, 60, 365, .90f, Color.LIGHT_GRAY);
        text("DISTANCE  " + game.profile.raceDistance + " M   (TAP / D)", 550, 475, 1.0f, Color.WHITE);
        text("QUICK RACE", 95, 83, 1.18f, Color.WHITE);
        text("GARAGE", 435, 83, 1.18f, Color.WHITE);
        text("TUNING", 748, 83, 1.18f, Color.WHITE);
        text("CAREER", 1055, 83, 1.18f, Color.WHITE);
        text("LEFT / RIGHT: SELECT CAR     ENTER: RACE     D: DISTANCE     K: CAREER",
             285, 210, .78f, Color.LIGHT_GRAY);
        text("LOCAL SAVE  /  NO ACCOUNT  /  NO INTERNET", 60, 245, .78f, Color.LIGHT_GRAY);
        endText();
    }

    @Override public void dispose() {
        pbr.dispose();
        if (fallbackCar != null) fallbackCar.dispose();
        super.dispose();
    }
}
