package com.streetdrag.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;
import com.streetdrag.game.rendering.PbrVehicleFleetRenderer;

/** Eight offline career events. Winning an event unlocks the next opponent. */
final class CareerScreen extends Base3DScreen {
    static final String[] EVENT_NAMES = {
        "LOCAL ROOKIE CUP",
        "NIGHT STREET BATTLE",
        "INDUSTRIAL 804",
        "CLUB CHAMPIONSHIP",
        "CITY SPRINT FINALS",
        "HIGHWAY OUTLAWS",
        "PRO STREET SERIES",
        "UNDERGROUND GRAND FINAL"
    };

    static final int[] OPPONENTS = {1, 3, 5, 7, 9, 11, 13, 14};

    private final VehicleSpec previewSpec;
    private final PbrVehicleFleetRenderer pbr;
    private final VehicleModel fallback;

    CareerScreen(StreetDragGame game) {
        super(game);
        world = new World3D(true);
        previewSpec = game.profile.car();
        pbr = new PbrVehicleFleetRenderer(camera, previewSpec);
        fallback = pbr.hasModel(previewSpec) ? null : new VehicleModel(previewSpec);
        if (fallback != null) fallback.setTransform(0f, 0f, 0f, 0f);
        pbr.setTransform(previewSpec, 0f, 0f, 0f, 0f);
        camera.fieldOfView = 49f;
        camera.position.set(8.6f, 3.2f, .5f);
        camera.lookAt(0f, .9f, 0f);
        camera.far = 110f;
        camera.update();
    }

    @Override public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override public boolean touchDown(int sx, int sy, int pointer, int button) {
                Vector3 p = new Vector3(sx, sy, 0f);
                game.hudViewport.unproject(p);
                if (p.y < 100f) {
                    game.showMenu();
                    return true;
                }
                if (p.x > 590f) {
                    for (int i = 0; i < EVENT_NAMES.length; i++) {
                        float rowY = 520f - i * 52f;
                        if (hit(p.x, p.y, 590f, rowY - 38f, 650f, 46f)) {
                            if (i <= game.profile.careerUnlocked) {
                                game.profile.startCareerEvent(i);
                                game.showRace();
                            }
                            return true;
                        }
                    }
                }
                return true;
            }

            @Override public boolean keyDown(int key) {
                if (key == Input.Keys.ESCAPE || key == Input.Keys.BACK) game.showMenu();
                return true;
            }
        });
    }

    @Override void renderModels(ModelBatch batch) {
        if (fallback != null) fallback.render(batch, world.environment);
    }

    @Override void renderPbr(float delta) { pbr.render(camera, delta); }

    @Override public void render(float delta) {
        clear(.008f, .014f, .027f);
        renderWorld(delta);
        beginHud();
        panel(0, 0, 1280, 130, new Color(.008f, .014f, .027f, .96f));
        panel(28, 420, 470, 250, new Color(.012f, .024f, .043f, .91f));
        panel(570, 105, 685, 485, new Color(.012f, .024f, .043f, .92f));
        panel(28, 22, 300, 88, new Color(.07f, .20f, .38f, 1f));

        beginText();
        text("CAREER  /  OFFLINE CHAMPIONSHIP", 50, 675, 1.75f, Color.WHITE);
        text("DRIVER", 58, 625, .92f, Color.YELLOW);
        text(game.profile.car().name, 58, 585, .90f, Color.CYAN);
        text("CAREER WINS  " + game.profile.careerWins, 58, 540, 1.05f, Color.WHITE);
        text("CASH  $ " + game.profile.money, 58, 500, 1.05f, Color.GREEN);
        text("EVENTS CLEARED  " + game.profile.careerUnlocked + " / " + (EVENT_NAMES.length - 1),
             58, 455, .88f, Color.LIGHT_GRAY);
        float pb402 = game.profile.best402;
        float pb804 = game.profile.best804;
        text("BEST 402 M  " + (pb402 > 0f ? String.format("%.2f S", pb402) : "--"),
             58, 415, .82f, Color.CYAN);
        text("BEST 804 M  " + (pb804 > 0f ? String.format("%.2f S", pb804) : "--"),
             58, 382, .82f, Color.CYAN);
        text("SELECT AN UNLOCKED EVENT", 610, 565, 1.08f, Color.CYAN);

        for (int i = 0; i < EVENT_NAMES.length; i++) {
            float y = 520f - i * 52f;
            boolean unlocked = i <= game.profile.careerUnlocked;
            panel(590, y - 38f, 650, 46f,
                unlocked ? new Color(.075f, .13f, .19f, .97f) : new Color(.045f, .055f, .07f, .97f));
            text(String.format("%02d", i + 1), 610, y - 8f, .90f, unlocked ? Color.YELLOW : Color.GRAY);
            text(EVENT_NAMES[i], 665, y - 8f, .90f, unlocked ? Color.WHITE : Color.GRAY);
            text(i % 2 == 0 ? "402 M" : "804 M", 1080, y - 8f, .88f, unlocked ? Color.CYAN : Color.GRAY);
            text(unlocked ? "START  >" : "LOCKED", 1150, y - 8f, .70f, unlocked ? Color.GREEN : Color.GRAY);
        }
        text("BACK TO MAIN MENU", 75, 77, 1.12f, Color.WHITE);
        text("WIN THE CURRENT EVENT TO UNLOCK THE NEXT RIVAL", 590, 75, .82f, Color.LIGHT_GRAY);
        endText();
    }

    @Override public void dispose() {
        pbr.dispose();
        if (fallback != null) fallback.dispose();
        super.dispose();
    }
}
