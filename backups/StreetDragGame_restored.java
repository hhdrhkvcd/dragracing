
package com.streetdrag.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class StreetDragGame extends ApplicationAdapter {

    private static final float W = 1280;
    private static final float H = 720;

    private OrthographicCamera camera;
    private FitViewport viewport;
    private ShapeRenderer shapes;
    private SpriteBatch batch;
    private BitmapFont font;
    // Физика гонки
    private float speed = 0f;
    private float distance = 0f;
    private float rpm = 1000f;
    private float nitro = 100f;

    // Соперник
    private float rivalSpeed = 0f;
    private float rivalDistance = 0f;

    // Состояние гонки
    private float countdown = 3f;
    private boolean raceStarted = false;
    private boolean raceFinished = false;
    private boolean playerWon = false;

    // Управление
    private boolean gasPressed = false;
    private boolean brakePressed = false;
    private boolean nitroPressed = false;

    // Анимация
    private float carMovement = 0f;
    private float roadMovement = 0f;
    private int page = 0;
    private int carIndex = 0;
    private int gear = 1;

    private final int[] upgrades = {1, 1, 1, 1};

    private final String[] carNames = {
        "AUDI RS6 AVANT GT",
        "BMW M3 CS TOURING",
        "MERCEDES AMG E63 S",
        "PORSCHE PANAMERA",
        "SUBARU WRX STI",
        "MITSUBISHI EVO IX"
    };

    private final Color[] carColors = {
        new Color(0.72f, 0.76f, 0.82f, 1),
        new Color(0.15f, 0.30f, 0.68f, 1),
        new Color(0.12f, 0.13f, 0.15f, 1),
        new Color(0.65f, 0.12f, 0.12f, 1),
        new Color(0.12f, 0.36f, 0.24f, 1),
        new Color(0.80f, 0.55f, 0.12f, 1)
    };

    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(W, H, camera);

        shapes = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();

        viewport.update(
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight(),
            true
        );

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(
                int screenX,
                int screenY,
                int pointer,
                int button
            ) {
                Vector2 p = viewport.unproject(
                    new Vector2(screenX, screenY)
                );

                handleTouch(p.x, p.y);
                return true;
            }
        });
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.025f, 0.035f, 0.055f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        if (page == 0) {
            drawGarage();
        } else if (page == 1) {
            drawTuning();
        } else {
            drawRace();
        }
    }

    private void drawGarage() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        shapes.setColor(new Color(0.055f, 0.075f, 0.11f, 1));
        shapes.rect(0, 0, W, H);

        // Garage walls
        shapes.setColor(new Color(0.09f, 0.12f, 0.17f, 1));
        shapes.rect(0, 170, W, 480);

        shapes.setColor(new Color(0.13f, 0.17f, 0.23f, 1));
        for (int x = 40; x < 1280; x += 300) {
            shapes.rect(x, 175, 4, 440);
        }

        // Garage lights
        shapes.setColor(new Color(0.28f, 0.43f, 0.55f, 1));
        shapes.rect(120, 625, 210, 5);
        shapes.rect(535, 625, 210, 5);
        shapes.rect(950, 625, 210, 5);

        // Floor
        shapes.setColor(new Color(0.15f, 0.18f, 0.22f, 1));
        shapes.rect(0, 125, W, 65);

        shapes.setColor(new Color(0.24f, 0.29f, 0.34f, 1));
        shapes.rect(0, 185, W, 3);

        // Car shadow
        shapes.setColor(new Color(0.015f, 0.02f, 0.03f, 0.65f));
        shapes.ellipse(245, 190, 690, 45);

        drawCar(250, 215, 1f, carColors[carIndex]);

        // Bottom interface
        shapes.setColor(new Color(0.035f, 0.05f, 0.075f, 1));
        shapes.rect(0, 0, W, 125);

        drawButton(40, 27, 220, 72, new Color(0.13f, 0.32f, 0.55f, 1));
        drawButton(285, 27, 250, 72, new Color(0.16f, 0.20f, 0.27f, 1));
        drawButton(1000, 27, 240, 72, new Color(0.10f, 0.48f, 0.34f, 1));

        drawButton(1010, 405, 220, 70, new Color(0.14f, 0.25f, 0.39f, 1));

        shapes.end();

        batch.begin();

        text("STREET DRAG", 45, 680, 2.2f, Color.WHITE);
        text("YOUR GARAGE", 48, 590, 1.15f, Color.CYAN);

        text(carNames[carIndex], 65, 535, 1.5f, Color.WHITE);
        text("POWER: 560 HP", 65, 495, 1.1f, Color.LIGHT_GRAY);
        text("DRIVE: AWD", 65, 465, 1.1f, Color.LIGHT_GRAY);

        text("NEXT CAR", 1050, 445, 1.15f, Color.WHITE);

        text("CARS", 110, 70, 1.5f, Color.WHITE);
        text("TUNING", 345, 70, 1.5f, Color.WHITE);
        text("RACE 402M", 1045, 70, 1.5f, Color.WHITE);

        batch.end();
    }

    private void drawCar(float x, float y, float s, Color paint) {

        // Wheels
        shapes.setColor(new Color(0.025f, 0.025f, 0.03f, 1));
        shapes.circle(x + 175*s, y + 65*s, 49*s);
        shapes.circle(x + 530*s, y + 65*s, 49*s);

        // Main body
        shapes.setColor(paint);
        shapes.rect(x + 45*s, y + 85*s, 565*s, 85*s);

        shapes.triangle(
            x + 45*s, y + 85*s,
            x + 12*s, y + 125*s,
            x + 60*s, y + 170*s
        );

        shapes.triangle(
            x + 590*s, y + 85*s,
            x + 670*s, y + 110*s,
            x + 615*s, y + 170*s
        );

        // Roof
        shapes.triangle(
            x + 150*s, y + 155*s,
            x + 240*s, y + 245*s,
            x + 425*s, y + 245*s
        );

        shapes.triangle(
            x + 150*s, y + 155*s,
            x + 425*s, y + 245*s,
            x + 500*s, y + 155*s
        );

        // Windows
        shapes.setColor(new Color(0.045f, 0.075f, 0.105f, 1));

        shapes.triangle(
            x + 178*s, y + 165*s,
            x + 250*s, y + 225*s,
            x + 320*s, y + 225*s
        );

        shapes.triangle(
            x + 178*s, y + 165*s,
            x + 320*s, y + 225*s,
            x + 385*s, y + 165*s
        );

        shapes.triangle(
            x + 405*s, y + 165*s,
            x + 395*s, y + 225*s,
            x + 465*s, y + 165*s
        );

        // Door details
        shapes.setColor(new Color(0.20f, 0.23f, 0.28f, 1));
        shapes.rect(x + 365*s, y + 88*s, 3*s, 75*s);
        shapes.rect(x + 450*s, y + 135*s, 35*s, 5*s);

        // Bumpers and lights
        shapes.setColor(new Color(0.12f, 0.14f, 0.18f, 1));
        shapes.rect(x + 20*s, y + 75*s, 35*s, 22*s);
        shapes.rect(x + 625*s, y + 75*s, 35*s, 22*s);

        shapes.setColor(new Color(1f, 0.88f, 0.55f, 1));
        shapes.rect(x + 630*s, y + 135*s, 22*s, 14*s);

        shapes.setColor(new Color(0.95f, 0.12f, 0.10f, 1));
        shapes.rect(x + 28*s, y + 137*s, 25*s, 13*s);

        // Wheel rims
        shapes.setColor(new Color(0.36f, 0.40f, 0.46f, 1));
        shapes.circle(x + 175*s, y + 65*s, 29*s);
        shapes.circle(x + 530*s, y + 65*s, 29*s);

        shapes.setColor(new Color(0.09f, 0.11f, 0.14f, 1));
        shapes.circle(x + 175*s, y + 65*s, 12*s);
        shapes.circle(x + 530*s, y + 65*s, 12*s);
    }

    private void drawTuning() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        shapes.setColor(new Color(0.055f, 0.075f, 0.11f, 1));
        shapes.rect(0, 0, W, H);

        shapes.setColor(new Color(0.10f, 0.14f, 0.20f, 1));
        shapes.rect(45, 145, 1190, 505);

        int[] ys = {480, 365, 250, 135};

        for (int i = 0; i < 4; i++) {
            shapes.setColor(new Color(0.14f, 0.19f, 0.27f, 1));
            shapes.rect(75, ys[i], 1125, 85);

            drawButton(990, ys[i] + 10, 170, 62,
                new Color(0.12f, 0.38f, 0.57f, 1));

            shapes.setColor(new Color(0.12f, 0.55f, 0.39f, 1));
            shapes.rect(340, ys[i] + 28, upgrades[i] * 65, 16);
        }

        drawButton(45, 25, 205, 75,
            new Color(0.17f, 0.25f, 0.35f, 1));

        shapes.end();

        batch.begin();

        text("PERFORMANCE TUNING", 75, 685, 2f, Color.WHITE);

        String[] names = {
            "ENGINE",
            "TURBO",
            "GEARBOX",
            "TIRES"
        };

        for (int i = 0; i < 4; i++) {
            text(names[i], 110, ys[i] + 53, 1.25f, Color.WHITE);
            text("LEVEL " + upgrades[i], 700, ys[i] + 53, 1f, Color.LIGHT_GRAY);
            text("UPGRADE", 1015, ys[i] + 46, 1f, Color.WHITE);
        }

        text("BACK", 100, 70, 1.4f, Color.WHITE);

        batch.end();
    }

    private void drawRace() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        shapes.setColor(new Color(0.045f, 0.065f, 0.09f, 1));
        shapes.rect(0, 0, W, H);

        shapes.setColor(new Color(0.12f, 0.14f, 0.17f, 1));
        shapes.rect(0, 160, W, 260);

        shapes.setColor(new Color(0.85f, 0.82f, 0.60f, 1));
        for (int x = 0; x < 1280; x += 190) {
            shapes.rect(x, 285, 95, 5);
        }

        drawCar(270, 205, 0.75f, carColors[carIndex]);

        drawButton(45, 30, 230, 70,
            new Color(0.15f, 0.21f, 0.30f, 1));

        drawButton(990, 30, 245, 70,
            new Color(0.10f, 0.43f, 0.33f, 1));

        shapes.end();

        batch.begin();

        text("DRAG RACE", 55, 675, 2f, Color.WHITE);
        text("DISTANCE: 402 M", 60, 615, 1.4f, Color.CYAN);
        text("GEAR: " + gear, 60, 560, 1.4f, Color.WHITE);
        text("SPEED: 0 KM/H", 60, 520, 1.2f, Color.WHITE);

        text("BACK", 110, 72, 1.3f, Color.WHITE);
        text("SHIFT", 1070, 72, 1.4f, Color.WHITE);

        batch.end();
    }

    private void drawButton(
        float x, float y,
        float w, float h,
        Color color
    ) {
        shapes.setColor(color);
        shapes.rect(x, y, w, h);

        shapes.setColor(
            new Color(1f, 1f, 1f, 0.08f)
        );
        shapes.rect(x, y + h - 5, w, 5);
    }

    private void text(
        String value,
        float x,
        float y,
        float scale,
        Color color
    ) {
        font.getData().setScale(scale);
        font.setColor(color);
        font.draw(batch, value, x, y);
    }

    private boolean hit(
        float x, float y,
        float bx, float by,
        float bw, float bh
    ) {
        return x >= bx && x <= bx + bw
            && y >= by && y <= by + bh;
    }

    private void handleTouch(float x, float y) {

        if (page == 0) {

            if (hit(x, y, 40, 27, 220, 72)) {
                carIndex = (carIndex + 1) % carNames.length;
            }

            if (hit(x, y, 285, 27, 250, 72)) {
                page = 1;
            }

            if (hit(x, y, 1000, 27, 240, 72)) {
                page = 2;
                gear = 1;
            }

            if (hit(x, y, 1010, 405, 220, 70)) {
                carIndex = (carIndex + 1) % carNames.length;
            }

        } else if (page == 1) {

            if (hit(x, y, 45, 25, 205, 75)) {
                page = 0;
                return;
            }

            int[] ys = {480, 365, 250, 135};

            for (int i = 0; i < 4; i++) {
                if (hit(x, y, 990, ys[i] + 10, 170, 62)) {
                    upgrades[i] = Math.min(upgrades[i] + 1, 10);
                }
            }

        } else {

            if (hit(x, y, 45, 30, 230, 70)) {
                page = 0;
            }

            if (hit(x, y, 990, 30, 245, 70)) {
                gear = Math.min(gear + 1, 6);
            }
        }
    }

    @Override
    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
    }
}
