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
    public void showCareer() { replaceScreen(new CareerScreen(this)); }
    public void showGarage() { replaceScreen(new GarageScreen(this)); }
    public void showTuning() { replaceScreen(new TuningScreen(this)); }
    public void showDyno() { replaceScreen(new DynoScreen(this)); }
    public void showRace() { replaceScreen(new RaceScreen(this)); }

    @Override public void dispose() {
        if (getScreen() != null) getScreen().dispose();
        uiBatch.dispose();
        uiShapes.dispose();
        font.dispose();
    }

    /** Device-local save. No account or network service is used. */
    static final class Profile {
        private static final float[] DEFAULT_RATIOS = {3.20f, 2.15f, 1.58f, 1.22f, .98f, .80f};
        private static final int[] UPGRADE_BASE_COSTS = {12000, 15000, 8000, 9000, 7000, 8500, 10000, 18000};
        private final Preferences prefs = Gdx.app.getPreferences("StreetDrag3D");
        int money = prefs.getInteger("money", 125000);
        int selected = prefs.getInteger("selected", 0);
        int careerUnlocked = prefs.getInteger("career_unlocked", 0);
        int careerWins = prefs.getInteger("career_wins", 0);
        int raceDistance = prefs.getInteger("race_distance", 402);
        float best402 = prefs.getFloat("best_402", 0f);
        float best804 = prefs.getFloat("best_804", 0f);
        int careerEvent;
        int careerOpponentIndex;
        boolean careerMode;
        final int[][] levels = new int[VehicleSpec.ALL.length][8];
        final float[] gearRatios = new float[6];
        float finalDrive;

        Profile() {
            selected = Math.max(0, Math.min(selected, VehicleSpec.ALL.length - 1));
            careerUnlocked = Math.max(0, Math.min(careerUnlocked, CareerScreen.EVENT_NAMES.length - 1));
            careerWins = Math.max(0, careerWins);
            raceDistance = raceDistance == 804 ? 804 : 402;
            for (int c = 0; c < levels.length; c++) {
                for (int u = 0; u < 4; u++) {
                    levels[c][u] = Math.max(1, Math.min(10, prefs.getInteger("u_" + c + "_" + u, 1)));
                }
            }
            for (int i = 0; i < gearRatios.length; i++) {
                gearRatios[i] = clamp(prefs.getFloat("ratio_" + selected + "_" + i, DEFAULT_RATIOS[i]), .5f, 4.5f);
            }
            finalDrive = clamp(prefs.getFloat("final_drive_" + selected, 3.55f), 2f, 5.5f);
        }

        VehicleSpec car() { return VehicleSpec.ALL[selected]; }
        int level(int category) { return levels[selected][category]; }
        int upgradeCost(int category) {
            if (category < 0 || category >= UPGRADE_BASE_COSTS.length) return 0;
            return UPGRADE_BASE_COSTS[category] * levels[selected][category];
        }

        boolean upgrade(int category) {
            if (category < 0 || category >= 8) return false;
            int cost = upgradeCost(category);
            if (levels[selected][category] >= 10 || money < cost) return false;
            money -= cost;
            levels[selected][category]++;
            save();
            return true;
        }

        void select(int index) {
            selected = (index + VehicleSpec.ALL.length) % VehicleSpec.ALL.length;
            loadGearbox();
            save();
        }

        private void loadGearbox() {
            for (int i = 0; i < gearRatios.length; i++) {
                gearRatios[i] = clamp(prefs.getFloat("ratio_" + selected + "_" + i, DEFAULT_RATIOS[i]), .5f, 4.5f);
            }
            finalDrive = clamp(prefs.getFloat("final_drive_" + selected, 3.55f), 2f, 5.5f);
        }

        float gearRatio(int gear) { return gearRatios[Math.max(0, Math.min(gear, 5))]; }
        void setGearRatio(int gear, float value) {
            if (gear < 0 || gear >= gearRatios.length) return;
            gearRatios[gear] = clamp(value, .5f, 4.5f);
            save();
        }
        float finalDrive() { return finalDrive; }
        void setFinalDrive(float value) {
            finalDrive = clamp(value, 2f, 5.5f);
            save();
        }

        void toggleRaceDistance() {
            raceDistance = raceDistance == 402 ? 804 : 402;
            save();
        }

        void startCareerEvent(int event) {
            if (event < 0 || event >= CareerScreen.EVENT_NAMES.length || event > careerUnlocked) return;
            careerMode = true;
            careerEvent = event;
            careerOpponentIndex = CareerScreen.OPPONENTS[event];
            raceDistance = event % 2 == 0 ? 402 : 804;
            save();
        }

        float bestTime() { return raceDistance == 804 ? best804 : best402; }

        void recordRaceResult(boolean won, float raceTime) {
            if (won && raceTime > 0f) {
                if (raceDistance == 804) {
                    if (best804 <= 0f || raceTime < best804) best804 = raceTime;
                } else {
                    if (best402 <= 0f || raceTime < best402) best402 = raceTime;
                }
            }
            if (careerMode) {
                if (won) {
                    money += 35000 + careerEvent * 5000;
                    careerWins++;
                    if (careerEvent == careerUnlocked && careerUnlocked < CareerScreen.EVENT_NAMES.length - 1) {
                        careerUnlocked++;
                    }
                } else {
                    money += 6000;
                }
                careerMode = false;
            } else {
                money += won ? 25000 : 5000;
            }
            save();
        }

        float engineMultiplier() {
            return 1f + (levels[selected][0] - 1) * .055f
                + (levels[selected][4] - 1) * .02f
                + (levels[selected][5] - 1) * .025f;
        }
        float turboMultiplier() { return 1f + (levels[selected][1] - 1) * .075f; }
        float gearboxMultiplier() {
            return 1f + (levels[selected][2] - 1) * .07f
                + (levels[selected][6] - 1) * .045f;
        }
        float gripMultiplier() { return 1f + (levels[selected][3] - 1) * .045f; }
        float massMultiplier() { return Math.max(.78f, 1f - (levels[selected][7] - 1) * .025f); }

        void reward(int amount) {
            money += Math.max(0, amount);
            save();
        }

        void save() {
            prefs.putInteger("money", money);
            prefs.putInteger("selected", selected);
            prefs.putInteger("career_unlocked", careerUnlocked);
            prefs.putInteger("career_wins", careerWins);
            prefs.putInteger("race_distance", raceDistance);
            prefs.putFloat("best_402", best402);
            prefs.putFloat("best_804", best804);
            for (int c = 0; c < levels.length; c++) {
                for (int u = 0; u < 4; u++) prefs.putInteger("u_" + c + "_" + u, levels[c][u]);
            }
            for (int i = 0; i < gearRatios.length; i++) prefs.putFloat("ratio_" + selected + "_" + i, gearRatios[i]);
            prefs.putFloat("final_drive_" + selected, finalDrive);
            prefs.flush();
        }

        private static float clamp(float value, float min, float max) {
            return Math.max(min, Math.min(max, value));
        }
    }
}
