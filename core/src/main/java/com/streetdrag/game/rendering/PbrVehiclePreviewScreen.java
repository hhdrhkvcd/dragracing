package com.streetdrag.game.rendering;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

/**
 * Stand-alone preview screen for checking a real GLB/GLTF vehicle asset.
 * Open it from a Game/Screen with:
 * game.setScreen(new PbrVehiclePreviewScreen(game, "models/vehicles/audi_rs6.glb", returnScreen));
 */
public final class PbrVehiclePreviewScreen extends ScreenAdapter {

    private final Game game;
    private final com.badlogic.gdx.Screen returnScreen;
    private final String modelPath;

    private PbrVehicleRenderer renderer;
    private SpriteBatch hudBatch;
    private BitmapFont hudFont;
    private int cameraIndex;

    private static final PbrVehicleRenderer.CameraMode[] CAMERAS = {
        PbrVehicleRenderer.CameraMode.REAR,
        PbrVehicleRenderer.CameraMode.HOOD,
        PbrVehicleRenderer.CameraMode.SIDE,
        PbrVehicleRenderer.CameraMode.ORBIT
    };

    public PbrVehiclePreviewScreen(Game game, String modelPath, com.badlogic.gdx.Screen returnScreen) {
        this.game = game;
        this.modelPath = modelPath;
        this.returnScreen = returnScreen;
    }

    @Override
    public void show() {
        renderer = new PbrVehicleRenderer(
            modelPath,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );
        hudBatch = new SpriteBatch();
        hudFont = new BitmapFont();
        hudFont.getData().setScale(1.2f);
        renderer.setCameraMode(CAMERAS[cameraIndex]);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.NUM_1) setCamera(0);
                if (keycode == Input.Keys.NUM_2) setCamera(1);
                if (keycode == Input.Keys.NUM_3) setCamera(2);
                if (keycode == Input.Keys.NUM_4) setCamera(3);
                if (keycode == Input.Keys.ESCAPE && game != null && returnScreen != null) {
                    game.setScreen(returnScreen);
                }
                return true;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                int next = (cameraIndex + 1) % CAMERAS.length;
                setCamera(next);
                return true;
            }
        });
    }

    private void setCamera(int index) {
        cameraIndex = Math.max(0, Math.min(index, CAMERAS.length - 1));
        if (renderer != null) renderer.setCameraMode(CAMERAS[cameraIndex]);
    }

    @Override
    public void resize(int width, int height) {
        if (renderer != null) renderer.resize(width, height);
    }

    @Override
    public void render(float delta) {
        if (renderer == null) return;
        renderer.render(delta);

        hudBatch.begin();
        hudFont.draw(hudBatch, "STREETDRAG PBR PREVIEW", 24, Gdx.graphics.getHeight() - 24);
        hudFont.draw(hudBatch, "1 REAR   2 HOOD   3 SIDE   4 ORBIT   |   TAP TO CHANGE", 24, 28);
        hudBatch.end();
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        if (renderer != null) renderer.dispose();
        if (hudBatch != null) hudBatch.dispose();
        if (hudFont != null) hudFont.dispose();
    }
}
