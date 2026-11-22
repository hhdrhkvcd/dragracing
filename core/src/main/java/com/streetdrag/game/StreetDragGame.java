package com.streetdrag.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** Android-first, fully offline StreetDrag 3D game entry point. */
public final class StreetDragGame extends Game {
    public static final float HUD_W = 1280f;
    public static final float HUD_H = 720f;

    SpriteBatch uiBatch;
    ShapeRenderer uiShapes;
    BitmapFont font;
    OrthographicCamera hudCamera;
    FitViewport hudViewport;
    Profile profile;

    @Override public void create() {
        uiBatch = new SpriteBatch();
        uiShapes = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(1f);
        hudCamera = new OrthographicCamera();
        hudViewport = new FitViewport(HUD_W, HUD_H, hudCamera);
        hudViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        profile = new Profile();
        showMenu();
    }

    @Override public void resize(int width, int height) {
        hudViewport.update(width, height, true);
        super.resize(width, height);
    }

    private void replaceScreen(com.badlogic.gdx.Screen screen) {
        com.badlogic.gdx.Screen old = getScreen();
        setScreen(screen);
        if (old != null) old.dispose();
    }

    public void showMenu() { replaceScreen(new MainMenuScreen(this)); }
    public void showGarage() { replaceScreen(new GarageScreen(this)); }
    public void showTuning() { replaceScreen(new TuningScreen(this)); }
    public void showRace() { replaceScreen(new RaceScreen(this)); }

    @Override public void dispose() {
        if (getScreen() != null) getScreen().dispose();
        uiBatch.dispose();
        uiShapes.dispose();
        font.dispose();
    }

    /** Device-local save. No account or network service is used. */
    static final class Profile {
        private final Preferences prefs = Gdx.app.getPreferences("StreetDrag3D");
        int money = prefs.getInteger("money", 125000);
        int selected = prefs.getInteger("selected", 0);
        final int[][] levels = new int[VehicleSpec.ALL.length][4];

        Profile() {
            selected = Math.max(0, Math.min(selected, VehicleSpec.ALL.length - 1));
            for (int c = 0; c < levels.length; c++) {
                for (int u = 0; u < 4; u++) {
                    levels[c][u] = Math.max(1, Math.min(10, prefs.getInteger("u_" + c + "_" + u, 1)));
                }
            }
        }

        VehicleSpec car() { return VehicleSpec.ALL[selected]; }
        int level(int category) { return levels[selected][category]; }
        int upgradeCost(int category) { return 12000 * levels[selected][category]; }

        boolean upgrade(int category) {
            int cost = upgradeCost(category);
            if (levels[selected][category] >= 10 || money < cost) return false;
            money -= cost;
            levels[selected][category]++;
            save();
            return true;
        }

        void select(int index) {
            selected = (index + VehicleSpec.ALL.length) % VehicleSpec.ALL.length;
            save();
        }

        float engineMultiplier() { return 1f + (levels[selected][0] - 1) * .075f; }
        float turboMultiplier() { return 1f + (levels[selected][1] - 1) * .065f; }
        float gearboxMultiplier() { return 1f + (levels[selected][2] - 1) * .07f; }
        float gripMultiplier() { return 1f + (levels[selected][3] - 1) * .045f; }

        void reward(int amount) {
            money += Math.max(0, amount);
            save();
        }

        void save() {
            prefs.putInteger("money", money);
            prefs.putInteger("selected", selected);
            for (int c = 0; c < levels.length; c++) {
                for (int u = 0; u < 4; u++) prefs.putInteger("u_" + c + "_" + u, levels[c][u]);
            }
            prefs.flush();
        }
    }
}
