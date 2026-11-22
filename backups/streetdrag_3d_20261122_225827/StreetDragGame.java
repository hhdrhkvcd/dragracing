package com.streetdrag.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class StreetDragGame extends Game {

    public static final float W = 1280;
    public static final float H = 720;

    SpriteBatch batch;
    ShapeRenderer shapes;
    BitmapFont font;

    OrthographicCamera camera;
    FitViewport viewport;

    Profile profile;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        font = new BitmapFont();

        camera = new OrthographicCamera();
        viewport = new FitViewport(W, H, camera);
        viewport.update(
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight(),
            true
        );

        profile = new Profile();

        showGarage();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        super.resize(width, height);
    }

    public void showGarage() {
        changeScreen(new GarageScreen(this));
    }

    public void showTuning() {
        changeScreen(new TuningScreen(this));
    }

    public void showRace() {
        changeScreen(new RaceScreen(this));
    }

    private void changeScreen(Screen next) {
        Screen old = getScreen();
        setScreen(next);
        if (old != null) old.dispose();
    }

    @Override
    public void dispose() {
        Screen current = getScreen();
        if (current != null) current.dispose();

        batch.dispose();
        shapes.dispose();
        font.dispose();
    }

    // ----------------------------------------------------
    // CAR CATALOG
    // ----------------------------------------------------

    static final class CarData {

        final String name;
        final float mass;
        final float hp;
        final float torque;
        final float drag;
        final boolean awd;
        final Color color;

        final String sideImage;
        final String rearImage;

        CarData(
            String name,
            float mass,
            float hp,
            float torque,
            float drag,
            boolean awd,
            Color color,
            String sideImage,
            String rearImage
        ) {
            this.name = name;
            this.mass = mass;
            this.hp = hp;
            this.torque = torque;
            this.drag = drag;
            this.awd = awd;
            this.color = color;
            this.sideImage = sideImage;
            this.rearImage = rearImage;
        }
    }

    static final CarData[] CARS = {

        new CarData(
            "AUDI RS6 AVANT GT",
            2100, 630, 850, 0.85f, true,
            new Color(0.72f,0.76f,0.82f,1),
            "images/cars/audi_rs6_side.png",
            "images/cars/audi_rs6_rear.png"
        ),

        new CarData(
            "BMW M3 CS TOURING",
            1780, 550, 650, 0.82f, true,
            new Color(0.10f,0.25f,0.75f,1),
            "images/cars/bmw_m3_side.png",
            "images/cars/bmw_m3_rear.png"
        ),

        new CarData(
            "MERCEDES AMG E63 S",
            1950, 612, 850, 0.90f, true,
            new Color(0.10f,0.12f,0.15f,1),
            "images/cars/amg_e63_side.png",
            "images/cars/amg_e63_rear.png"
        ),

        new CarData(
            "PORSCHE PANAMERA TURBO",
            2000, 630, 820, 0.78f, true,
            new Color(0.65f,0.12f,0.12f,1),
            "images/cars/panamera_side.png",
            "images/cars/panamera_rear.png"
        ),

        new CarData(
            "SUBARU WRX STI",
            1500, 310, 400, 0.88f, true,
            new Color(0.08f,0.25f,0.75f,1),
            "images/cars/wrx_side.png",
            "images/cars/wrx_rear.png"
        ),

        new CarData(
            "MITSUBISHI EVO IX",
            1450, 350, 430, 0.86f, true,
            new Color(0.80f,0.55f,0.12f,1),
            "images/cars/evo_side.png",
            "images/cars/evo_rear.png"
        ),

        new CarData(
            "NISSAN GT-R R35",
            1740, 600, 650, 0.75f, true,
            new Color(0.06f,0.07f,0.09f,1),
            "images/cars/gtr_side.png",
            "images/cars/gtr_rear.png"
        ),

        new CarData(
            "TOYOTA SUPRA MK5",
            1520, 500, 650, 0.72f, false,
            new Color(0.85f,0.85f,0.85f,1),
            "images/cars/supra_side.png",
            "images/cars/supra_rear.png"
        ),

        new CarData(
            "PORSCHE 911 TURBO S",
            1640, 650, 800, 0.70f, true,
            new Color(0.85f,0.85f,0.80f,1),
            "images/cars/911_side.png",
            "images/cars/911_rear.png"
        ),

        new CarData(
            "NISSAN SILVIA S15",
            1250, 350, 420, 0.80f, false,
            new Color(0.85f,0.85f,0.88f,1),
            "images/cars/silvia_side.png",
            "images/cars/silvia_rear.png"
        ),

        new CarData(
            "BMW M5 F90",
            1930, 625, 750, 0.88f, true,
            new Color(0.10f,0.13f,0.18f,1),
            "images/cars/m5_side.png",
            "images/cars/m5_rear.png"
        ),

        new CarData(
            "NISSAN STAGEA 260RS",
            1720, 280, 370, 0.92f, true,
            new Color(0.15f,0.20f,0.25f,1),
            "images/cars/stagea_side.png",
            "images/cars/stagea_rear.png"
        ),

        new CarData(
            "AUDI RS7",
            2050, 630, 850, 0.83f, true,
            new Color(0.15f,0.15f,0.17f,1),
            "images/cars/rs7_side.png",
            "images/cars/rs7_rear.png"
        ),

        new CarData(
            "MERCEDES AMG GT R",
            1630, 585, 700, 0.74f, false,
            new Color(0.10f,0.42f,0.23f,1),
            "images/cars/amg_gtr_side.png",
            "images/cars/amg_gtr_rear.png"
        )
    };

    // ----------------------------------------------------
    // PLAYER PROFILE AND SAVE DATA
    // ----------------------------------------------------

    static final class Profile {

        private final Preferences prefs =
            Gdx.app.getPreferences("StreetDragProfile");

        private int money;
        private int selectedCar;

        private final int[][] levels =
            new int[CARS.length][4];

        Profile() {
            money = prefs.getInteger("money", 125000);
            selectedCar = prefs.getInteger("selectedCar", 0);

            selectedCar = Math.max(
                0,
                Math.min(selectedCar, CARS.length - 1)
            );

            for (int c = 0; c < CARS.length; c++) {
                for (int u = 0; u < 4; u++) {
                    levels[c][u] = prefs.getInteger(
                        "upgrade_" + c + "_" + u,
                        1
                    );
                }
            }
        }

        int getMoney() {
            return money;
        }

        int getSelectedCar() {
            return selectedCar;
        }

        void selectCar(int index) {
            selectedCar = Math.max(
                0,
                Math.min(index, CARS.length - 1)
            );
            save();
        }

        int getLevel(int category) {
            return levels[selectedCar][category];
        }

        int getUpgradeCost(int category) {
            return 15000 * levels[selectedCar][category];
        }

        boolean upgrade(int category) {
            int level = levels[selectedCar][category];

            if (level >= 10) return false;

            int cost = getUpgradeCost(category);

            if (money < cost) return false;

            money -= cost;
            levels[selectedCar][category]++;

            save();
            return true;
        }

        float getEngineMultiplier() {
            return 1f + (levels[selectedCar][0] - 1) * 0.10f;
        }

        float getTurboMultiplier() {
            return 1f + (levels[selectedCar][1] - 1) * 0.07f;
        }

        float getGearboxMultiplier() {
            return 1f + (levels[selectedCar][2] - 1) * 0.08f;
        }

        float getGripMultiplier() {
            return 1f + (levels[selectedCar][3] - 1) * 0.045f;
        }

        void addMoney(int amount) {
            money += Math.max(0, amount);
            save();
        }

        void save() {
            prefs.putInteger("money", money);
            prefs.putInteger("selectedCar", selectedCar);

            for (int c = 0; c < CARS.length; c++) {
                for (int u = 0; u < 4; u++) {
                    prefs.putInteger(
                        "upgrade_" + c + "_" + u,
                        levels[c][u]
                    );
                }
            }

            prefs.flush();
        }
    }

    // ----------------------------------------------------
    // COMMON SCREEN
    // ----------------------------------------------------

    abstract static class BaseScreen extends ScreenAdapter {

        final StreetDragGame game;

        Texture background;

        BaseScreen(StreetDragGame game) {
            this.game = game;
        }

        void prepare(Color color) {
            Gdx.gl.glClearColor(
                color.r, color.g, color.b, color.a
            );

            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            game.viewport.apply();
            game.camera.update();

            game.batch.setProjectionMatrix(game.camera.combined);
            game.shapes.setProjectionMatrix(game.camera.combined);
        }

        Texture optionalTexture(String path) {
            if (!Gdx.files.internal(path).exists()) return null;

            Texture texture = new Texture(Gdx.files.internal(path));

            texture.setFilter(
                Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear
            );

            return texture;
        }

        void drawTexture(Texture texture, float x, float y, float w, float h) {
            if (texture == null) return;

            game.batch.begin();
            game.batch.draw(texture, x, y, w, h);
            game.batch.end();
        }

        void beginText() {
            game.batch.begin();
        }

        void text(
            String value,
            float x, float y,
            float scale,
            Color color
        ) {
            game.font.getData().setScale(scale);
            game.font.setColor(color);
            game.font.draw(game.batch, value, x, y);
        }

        void endText() {
            game.batch.end();
        }

        void drawButton(
            float x, float y,
            float w, float h,
            Color color
        ) {
            game.shapes.begin(ShapeRenderer.ShapeType.Filled);

            game.shapes.setColor(color);
            game.shapes.rect(x, y, w, h);

            game.shapes.setColor(
                new Color(1f, 1f, 1f, 0.18f)
            );
            game.shapes.rect(x, y + h - 4, w, 4);

            game.shapes.end();
        }

        boolean hit(
            float x, float y,
            float bx, float by,
            float bw, float bh
        ) {
            return x >= bx && x <= bx + bw
                && y >= by && y <= by + bh;
        }

        Vector2 position(int screenX, int screenY) {
            return game.viewport.unproject(
                new Vector2(screenX, screenY)
            );
        }

        void drawGarageRoom() {
            game.shapes.begin(ShapeRenderer.ShapeType.Filled);

            game.shapes.setColor(0.025f, 0.035f, 0.055f, 1);
            game.shapes.rect(0, 0, W, H);

            game.shapes.setColor(0.07f, 0.095f, 0.13f, 1);
            game.shapes.rect(0, 170, W, 460);

            game.shapes.setColor(0.12f, 0.16f, 0.22f, 1);
            for (int x = 80; x < 1280; x += 300) {
                game.shapes.rect(x, 170, 5, 450);
            }

            game.shapes.setColor(0.30f, 0.55f, 0.8f, 0.85f);
            game.shapes.rect(130, 615, 220, 5);
            game.shapes.rect(530, 615, 220, 5);
            game.shapes.rect(930, 615, 220, 5);

            game.shapes.setColor(0.16f, 0.18f, 0.21f, 1);
            game.shapes.rect(0, 125, W, 60);

            game.shapes.setColor(0.04f, 0.05f, 0.07f, 0.8f);
            game.shapes.ellipse(260, 165, 700, 48);

            game.shapes.end();
        }

        void drawSideFallback(CarData car, float x, float y, float s) {
            game.shapes.begin(ShapeRenderer.ShapeType.Filled);

            Color dark = new Color(0.025f, 0.025f, 0.032f, 1);
            game.shapes.setColor(dark);
            game.shapes.circle(x + 145*s, y + 48*s, 48*s);
            game.shapes.circle(x + 540*s, y + 48*s, 48*s);

            game.shapes.setColor(car.color);
            game.shapes.rect(x + 35*s, y + 75*s, 610*s, 95*s);

            game.shapes.triangle(
                x + 35*s, y + 75*s,
                x + 4*s, y + 112*s,
                x + 60*s, y + 170*s
            );

            game.shapes.triangle(
                x + 640*s, y + 75*s,
                x + 690*s, y + 105*s,
                x + 620*s, y + 170*s
            );

            game.shapes.triangle(
                x + 145*s, y + 155*s,
                x + 245*s, y + 250*s,
                x + 445*s, y + 250*s
            );

            game.shapes.triangle(
                x + 145*s, y + 155*s,
                x + 445*s, y + 250*s,
                x + 525*s, y + 155*s
            );

            game.shapes.setColor(0.025f, 0.055f, 0.08f, 1);

            game.shapes.triangle(
                x + 180*s, y + 165*s,
                x + 260*s, y + 225*s,
                x + 340*s, y + 225*s
            );

            game.shapes.triangle(
                x + 180*s, y + 165*s,
                x + 340*s, y + 225*s,
                x + 385*s, y + 165*s
            );

            game.shapes.triangle(
                x + 405*s, y + 165*s,
                x + 425*s, y + 225*s,
                x + 485*s, y + 165*s
            );

            game.shapes.setColor(0.45f, 0.48f, 0.53f, 1);
            game.shapes.circle(x + 145*s, y + 48*s, 27*s);
            game.shapes.circle(x + 540*s, y + 48*s, 27*s);

            game.shapes.setColor(0.06f, 0.07f, 0.09f, 1);
            game.shapes.circle(x + 145*s, y + 48*s, 12*s);
            game.shapes.circle(x + 540*s, y + 48*s, 12*s);

            game.shapes.setColor(1f, 0.82f, 0.45f, 1);
            game.shapes.rect(x + 650*s, y + 125*s, 25*s, 12*s);

            game.shapes.setColor(0.95f, 0.05f, 0.06f, 1);
            game.shapes.rect(x + 8*s, y + 125*s, 24*s, 12*s);

            game.shapes.end();
        }

        void drawRearFallback(CarData car, float x, float y, float s) {
            game.shapes.begin(ShapeRenderer.ShapeType.Filled);

            game.shapes.setColor(0.015f, 0.015f, 0.02f, 0.9f);
            game.shapes.ellipse(x + 12*s, y, 250*s, 28*s);

            game.shapes.setColor(0.015f, 0.015f, 0.018f, 1);
            game.shapes.rect(x + 12*s, y + 12*s, 34*s, 98*s);
            game.shapes.rect(x + 228*s, y + 12*s, 34*s, 98*s);

            game.shapes.setColor(car.color);
            game.shapes.rect(x + 35*s, y + 30*s, 215*s, 125*s);

            game.shapes.triangle(
                x + 35*s, y + 125*s,
                x + 68*s, y + 195*s,
                x + 220*s, y + 195*s
            );

            game.shapes.triangle(
                x + 220*s, y + 195*s,
                x + 250*s, y + 125*s,
                x + 35*s, y + 125*s
            );

            game.shapes.setColor(0.025f, 0.045f, 0.065f, 1);
            game.shapes.rect(x + 77*s, y + 125*s, 130*s, 55*s);

            game.shapes.setColor(0.95f, 0.025f, 0.045f, 1);
            game.shapes.rect(x + 36*s, y + 130*s, 46*s, 13*s);
            game.shapes.rect(x + 203*s, y + 130*s, 46*s, 13*s);

            game.shapes.setColor(0.07f, 0.075f, 0.085f, 1);
            game.shapes.rect(x + 30*s, y + 25*s, 220*s, 13*s);

            game.shapes.setColor(0.45f, 0.48f, 0.53f, 1);
            game.shapes.rect(x + 75*s, y + 50*s, 130*s, 22*s);

            game.shapes.end();
        }

        @Override
        public void dispose() {
            if (background != null) background.dispose();
        }
    }

    // ----------------------------------------------------
    // GARAGE
    // ----------------------------------------------------

    static final class GarageScreen extends BaseScreen {

        Texture garageBackground;
        Texture carTexture;

        GarageScreen(StreetDragGame game) {
            super(game);
            garageBackground = optionalTexture(
                "images/garage/garage_bg.png"
            );
            loadCar();
        }

        private void loadCar() {
            if (carTexture != null) carTexture.dispose();

            CarData car = CARS[game.profile.getSelectedCar()];
            carTexture = optionalTexture(car.sideImage);
        }

        private void changeCar(int amount) {
            int index = game.profile.getSelectedCar();
            index = (index + amount + CARS.length) % CARS.length;

            game.profile.selectCar(index);
            loadCar();
        }

        @Override
        public void show() {

            Gdx.input.setInputProcessor(new InputAdapter() {

                @Override
                public boolean touchDown(
                    int sx, int sy, int pointer, int button
                ) {
                    Vector2 p = position(sx, sy);
                    float x = p.x;
                    float y = p.y;

                    if (y < 145) {
                        if (x > 400 && x < 840) {
                            game.showTuning();
                        } else if (x > 850) {
                            game.showRace();
                        }
                    }

                    if (y > 280 && y < 480) {
                        if (x < 150) changeCar(-1);
                        if (x > 1120) changeCar(1);
                    }

                    return true;
                }

                @Override
                public boolean keyDown(int key) {
                    if (key == Input.Keys.LEFT) changeCar(-1);
                    if (key == Input.Keys.RIGHT) changeCar(1);
                    if (key == Input.Keys.T) game.showTuning();
                    if (key == Input.Keys.ENTER) game.showRace();
                    return true;
                }
            });
        }

        @Override
        public void render(float delta) {

            prepare(new Color(0.02f,0.025f,0.04f,1));

            if (garageBackground != null) {
                drawTexture(garageBackground,0,0,W,H);
            } else {
                drawGarageRoom();
            }

            CarData car = CARS[game.profile.getSelectedCar()];

            if (carTexture != null) {
                drawTexture(carTexture,300,190,690,345);
            } else {
                drawSideFallback(car,285,195,1f);
            }

            game.shapes.begin(ShapeRenderer.ShapeType.Filled);

            game.shapes.setColor(0.02f,0.025f,0.04f,0.93f);
            game.shapes.rect(0,0,W,140);
            game.shapes.rect(20,510,360,185);

            game.shapes.setColor(0.08f,0.16f,0.27f,0.97f);
            game.shapes.rect(15,20,365,95);

            game.shapes.setColor(0.08f,0.12f,0.19f,0.97f);
            game.shapes.rect(410,20,400,95);

            game.shapes.setColor(0.06f,0.38f,0.23f,0.97f);
            game.shapes.rect(850,20,415,95);

            game.shapes.end();

            beginText();

            text("STREET DRAG",35,675,2.5f,Color.WHITE);
            text("YOUR GARAGE",40,625,1.5f,Color.CYAN);

            text(car.name,45,570,1.4f,Color.WHITE);
            text("POWER: "+(int)car.hp+" HP",45,535,1.2f,Color.LIGHT_GRAY);
            text("TORQUE: "+(int)car.torque+" NM",45,505,1.1f,Color.LIGHT_GRAY);
            text(car.awd ? "DRIVE: AWD" : "DRIVE: RWD",45,475,1.1f,Color.LIGHT_GRAY);

            text("$ "+game.profile.getMoney(),1000,675,1.6f,Color.GREEN);

            text("CARS",145,75,1.5f,Color.WHITE);
            text("TUNING",535,75,1.5f,Color.WHITE);
            text("RACE 402M",975,75,1.5f,Color.WHITE);

            text("<",65,390,3f,Color.WHITE);
            text(">",1170,390,3f,Color.WHITE);

            endText();
        }

        @Override
        public void dispose() {
            if (garageBackground != null) garageBackground.dispose();
            if (carTexture != null) carTexture.dispose();
        }
    }

    // ----------------------------------------------------
    // TUNING
    // ----------------------------------------------------

    static final class TuningScreen extends BaseScreen {

        private static final String[] NAMES = {
            "ENGINE",
            "TURBO",
            "GEARBOX",
            "TIRES"
        };

        private static final String[] DESCRIPTIONS = {
            "More engine power",
            "Higher turbo pressure",
            "Faster gear changes",
            "Better road grip"
        };

        private final int[] ys = {470,355,240,125};

        private String message = "";
        private float messageTimer;

        TuningScreen(StreetDragGame game) {
            super(game);
        }

        @Override
        public void show() {

            Gdx.input.setInputProcessor(new InputAdapter() {

                @Override
                public boolean touchDown(
                    int sx, int sy, int pointer, int button
                ) {
                    Vector2 p = position(sx,sy);

                    if (p.x < 250 && p.y < 105) {
                        game.showGarage();
                        return true;
                    }

                    for (int i=0;i<4;i++) {
                        if (hit(p.x,p.y,930,ys[i],300,85)) {
                            boolean success = game.profile.upgrade(i);

                            message = success
                                ? NAMES[i]+" UPGRADED!"
                                : "NOT ENOUGH MONEY OR MAX LEVEL";

                            messageTimer = 2.5f;
                        }
                    }

                    return true;
                }
            });
        }

        @Override
        public void render(float delta) {

            prepare(new Color(0.015f,0.025f,0.045f,1));

            if (messageTimer > 0) messageTimer -= delta;

            game.shapes.begin(ShapeRenderer.ShapeType.Filled);

            game.shapes.setColor(0.035f,0.05f,0.08f,1);
            game.shapes.rect(0,0,W,H);

            game.shapes.setColor(0.07f,0.11f,0.17f,1);
            game.shapes.rect(35,100,1210,550);

            for (int i=0;i<4;i++) {
                game.shapes.setColor(0.12f,0.16f,0.23f,1);
                game.shapes.rect(65,ys[i],1140,85);

                game.shapes.setColor(0.025f,0.04f,0.06f,1);
                game.shapes.rect(365,ys[i]+32,420,18);

                game.shapes.setColor(0.10f,0.75f,0.37f,1);
                game.shapes.rect(
                    365,ys[i]+32,
                    42*game.profile.getLevel(i),18
                );

                game.shapes.setColor(0.07f,0.32f,0.55f,1);
                game.shapes.rect(930,ys[i]+10,250,62);
            }

            game.shapes.setColor(0.11f,0.17f,0.26f,1);
            game.shapes.rect(25,20,205,70);

            game.shapes.end();

            beginText();

            text("PERFORMANCE TUNING",65,690,2.1f,Color.WHITE);
            text("$ "+game.profile.getMoney(),1000,690,1.5f,Color.GREEN);

            for (int i=0;i<4;i++) {
                text(NAMES[i],100,ys[i]+56,1.4f,Color.WHITE);
                text(DESCRIPTIONS[i],100,ys[i]+25,0.9f,Color.LIGHT_GRAY);
                text("LEVEL "+game.profile.getLevel(i),665,ys[i]+52,1.1f,Color.WHITE);
                text("UPGRADE $"+game.profile.getUpgradeCost(i),965,ys[i]+45,1.1f,Color.WHITE);
            }

            text("BACK",85,62,1.4f,Color.WHITE);

            if (messageTimer > 0) {
                text(message,420,75,1.2f,Color.YELLOW);
            }

            endText();
        }
    }

    // ----------------------------------------------------
    // RACE
    // ----------------------------------------------------

    static final class RaceScreen extends BaseScreen {

        private CarPhysics player;
        private CarPhysics rival;

        private Texture playerTexture;
        private Texture rivalTexture;
        private Texture raceBackground;

        private CarData playerData;
        private CarData rivalData;

        private boolean gas;
        private boolean brake;
        private boolean clutch;
        private boolean nitro;

        private boolean started;
        private boolean finished;
        private boolean resultSaved;
        private boolean playerWon;

        private float countdown = 3f;
        private float finishTimer;
        private float raceTimer;
        private float roadOffset;

        RaceScreen(StreetDragGame game) {
            super(game);

            playerData = CARS[game.profile.getSelectedCar()];

            int rivalIndex =
                (game.profile.getSelectedCar()+1)%CARS.length;

            rivalData = CARS[rivalIndex];

            player = new CarPhysics(
                playerData.mass,
                playerData.hp,
                playerData.torque,
                playerData.drag,
                playerData.awd
            );

            player.setEngineUpgrade(
                game.profile.getEngineMultiplier()
                * game.profile.getTurboMultiplier()
            );

            player.setGripMultiplier(
                game.profile.getGripMultiplier()
            );

            player.setGearboxUpgrade(
                game.profile.getGearboxMultiplier()
            );

            rival = new CarPhysics(
                rivalData.mass,
                rivalData.hp,
                rivalData.torque,
                rivalData.drag,
                rivalData.awd
            );

            raceBackground = optionalTexture(
                "images/tracks/street_night.png"
            );

            playerTexture = optionalTexture(playerData.rearImage);
            rivalTexture = optionalTexture(rivalData.rearImage);
        }

        @Override
        public void show() {

            Gdx.input.setInputProcessor(new InputAdapter() {

                @Override
                public boolean touchDown(
                    int sx,int sy,int pointer,int button
                ) {
                    Vector2 p = position(sx,sy);
                    touch(p.x,p.y);
                    return true;
                }

                @Override
                public boolean touchUp(
                    int sx,int sy,int pointer,int button
                ) {
                    gas=false;
                    brake=false;
                    clutch=false;
                    nitro=false;
                    return true;
                }

                @Override
                public boolean keyDown(int key) {

                    if (key==Input.Keys.ESCAPE) {
                        game.showGarage();
                        return true;
                    }

                    if (key==Input.Keys.R) {
                        startRace();
                    }

                    if (finished) {
                        if (key==Input.Keys.ENTER) startRace();
                        return true;
                    }

                    if (key==Input.Keys.SPACE) {
                        gas=true;
                        brake=false;
                    }

                    if (key==Input.Keys.DOWN) {
                        brake=true;
                        gas=false;
                    }

                    if (key==Input.Keys.SHIFT_LEFT) clutch=true;
                    if (key==Input.Keys.N) nitro=true;

                    if (key==Input.Keys.UP) player.shiftUp();
                    if (key==Input.Keys.Z) player.shiftDown();

                    return true;
                }

                @Override
                public boolean keyUp(int key) {

                    if (key==Input.Keys.SPACE) gas=false;
                    if (key==Input.Keys.DOWN) brake=false;
                    if (key==Input.Keys.SHIFT_LEFT) clutch=false;
                    if (key==Input.Keys.N) nitro=false;

                    return true;
                }
            });
        }

        private void touch(float x,float y) {

            if (finished) {
                if (x<230 && y<170) {
                    game.showGarage();
                } else {
                    startRace();
                }
                return;
            }

            if (x<150 && y>610) {
                game.showGarage();
                return;
            }

            if (y>35 && y<180) {

                if (x<195) {
                    brake=true;
                    gas=false;
                } else if (x<390) {
                    nitro=true;
                } else if (x<555) {
                    clutch=true;
                } else if (x<760) {
                    player.shiftDown();
                } else if (x<960) {
                    player.shiftUp();
                } else {
                    gas=true;
                    brake=false;
                }
            }
        }

        private void startRace() {
            player.reset();
            rival.reset();

            gas=false;
            brake=false;
            clutch=false;
            nitro=false;

            countdown=3f;
            finishTimer=0f;
            raceTimer=0f;
            roadOffset=0f;

            started=false;
            finished=false;
            resultSaved=false;
            playerWon=false;
        }

        private void updateRace(float delta) {

            if (finished) return;

            raceTimer += delta;

            if (!started) {
                countdown -= delta;

                if (countdown<=0) {
                    started=true;
                }

                return;
            }

            player.update(
                delta,
                gas?1f:0f,
                brake?1f:0f,
                clutch?1f:0f,
                nitro
            );

            // Rival AI with automatic gear changes.
            if (rival.getRpm()>6500f) {
                rival.shiftUp();
            }

            boolean rivalNitro =
                rival.getDistance()>70f &&
                rival.getDistance()<220f;

            rival.update(
                delta,
                0.98f,
                0f,
                0f,
                rivalNitro
            );

            roadOffset =
                (roadOffset+player.getSpeedKmh()*delta*0.12f)%100f;

            if (player.isFinished() || rival.isFinished()) {
                finishTimer += delta;

                if (finishTimer>1.5f ||
                    (player.isFinished() && rival.isFinished())) {
                    finishRace();
                }
            }

            if (raceTimer>45f) finishRace();
        }

        private void finishRace() {

            if (finished) return;

            finished=true;

            playerWon =
                player.getDistance()>=rival.getDistance();

            if (!resultSaved) {
                game.profile.addMoney(playerWon?25000:5000);
                resultSaved=true;
            }

            gas=false;
            nitro=false;
            brake=false;
            clutch=false;
        }

        private void drawRoad() {

            game.shapes.begin(ShapeRenderer.ShapeType.Filled);

            game.shapes.setColor(0.045f,0.055f,0.07f,1);
            game.shapes.rect(0,0,W,H);

            game.shapes.setColor(0.12f,0.13f,0.15f,1);
            game.shapes.rect(0,120,W,390);

            game.shapes.setColor(0.92f,0.76f,0.30f,0.9f);

            game.shapes.rect(20,220,W-40,4);
            game.shapes.rect(20,495,W-40,4);

            for (int i=0;i<9;i++) {
                float x=i*190-roadOffset;
                if (x<0) x+=W+190;

                game.shapes.setColor(0.9f,0.9f,0.85f,0.65f);
                game.shapes.rect(x,355,85,5);
            }

            game.shapes.end();
        }

        private void drawRaceCar(
            CarData data,
            Texture texture,
            float x,float y,float scale
        ) {
            if (texture!=null) {
                drawTexture(
                    texture,x,y,
                    280*scale,200*scale
                );
            } else {
                drawRearFallback(data,x,y,scale);
            }
        }

        @Override
        public void render(float delta) {

            delta=Math.min(delta,0.04f);

            updateRace(delta);

            prepare(new Color(0.01f,0.015f,0.025f,1));

            if (raceBackground!=null) {
                drawTexture(raceBackground,0,0,W,H);
            } else {
                drawRoad();
            }

            float lead =
                player.getDistance()-rival.getDistance();

            drawRaceCar(
                rivalData,
                rivalTexture,
                800,
                205+Math.max(-40,Math.min(40,lead*0.3f)),
                0.82f
            );

            drawRaceCar(
                playerData,
                playerTexture,
                335,
                170,
                1f
            );

            game.shapes.begin(ShapeRenderer.ShapeType.Filled);

            // Top dashboard.
            game.shapes.setColor(0.015f,0.025f,0.045f,0.92f);
            game.shapes.rect(0,620,W,100);

            // Bottom controls.
            game.shapes.rect(0,0,W,175);

            // Progress bar.
            game.shapes.setColor(0.10f,0.12f,0.16f,1);
            game.shapes.rect(350,665,580,15);

            game.shapes.setColor(0.10f,0.8f,0.42f,1);
            game.shapes.rect(
                350,665,
                580*player.getRaceProgress(),15
            );

            // RPM bar.
            game.shapes.setColor(0.025f,0.035f,0.05f,1);
            game.shapes.rect(400,565,480,16);

            game.shapes.setColor(
                player.getRpm()>6500?Color.RED:Color.GREEN
            );

            game.shapes.rect(
                400,565,
                480*Math.min(player.getRpm()/7200f,1f),
                16
            );

            // Control buttons.
            game.shapes.setColor(0.65f,0.12f,0.12f,1);
            game.shapes.rect(30,35,165,115);

            game.shapes.setColor(0.07f,0.27f,0.62f,1);
            game.shapes.rect(215,35,170,115);

            game.shapes.setColor(0.20f,0.23f,0.29f,1);
            game.shapes.rect(405,35,165,115);

            game.shapes.setColor(0.17f,0.19f,0.25f,1);
            game.shapes.rect(590,35,170,115);
            game.shapes.rect(780,35,170,115);

            game.shapes.setColor(
                gas?Color.GREEN:new Color(0.07f,0.43f,0.25f,1)
            );
            game.shapes.rect(1010,35,240,115);

            game.shapes.end();

            beginText();

            text("STREET DRAG",35,690,1.8f,Color.WHITE);
            text("402 M",570,690,1.5f,Color.CYAN);

            text("YOU: "+playerData.name,35,650,0.9f,Color.WHITE);
            text("RIVAL: "+rivalData.name,800,650,0.9f,Color.WHITE);

            text("SPEED "+(int)player.getSpeedKmh()+" KM/H",
                40,590,1.3f,Color.WHITE);

            text("DISTANCE "+(int)player.getDistance()+" / 402 M",
                850,590,1f,Color.WHITE);

            text("RPM "+(int)player.getRpm(),
                420,540,1.2f,Color.WHITE);

            text("GEAR "+player.getGear(),
                950,690,1.3f,Color.WHITE);

            text("BRAKE",75,85,1.2f,Color.WHITE);
            text("NITRO "+(int)player.getNitro()+"%",245,85,1.1f,Color.WHITE);
            text("CLUTCH",440,85,1.1f,Color.WHITE);
            text("DOWN",635,85,1.1f,Color.WHITE);
            text("SHIFT UP",810,85,1.1f,Color.WHITE);
            text("GAS",1090,85,1.5f,Color.WHITE);

            if (!started && !finished) {
                text(
                    String.valueOf(Math.max(1,(int)Math.ceil(countdown))),
                    615,445,4f,Color.YELLOW
                );
            }

            if (finished) {
                text(
                    playerWon?"YOU WIN!":"YOU LOSE!",
                    480,450,3f,
                    playerWon?Color.GREEN:Color.RED
                );

                text("TAP TO RACE AGAIN",480,405,1.2f,Color.WHITE);
                text("BACK TO GARAGE",35,85,1f,Color.WHITE);
            }

            text("ESC - GARAGE | SPACE - GAS | UP - SHIFT",
                35,620,0.8f,Color.LIGHT_GRAY);

            endText();
        }

        @Override
        public void dispose() {
            if (raceBackground!=null) raceBackground.dispose();
            if (playerTexture!=null) playerTexture.dispose();
            if (rivalTexture!=null) rivalTexture.dispose();
        }
    }
}