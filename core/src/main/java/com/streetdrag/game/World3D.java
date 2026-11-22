package com.streetdrag.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.utils.Array;

/** Detailed procedural garage and night drag strip. */
public final class World3D {
    private static final long ATTR =
        VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

    private final Array<Model> owned = new Array<Model>();
    private final Array<ModelInstance> instances =
        new Array<ModelInstance>();

    public final Environment environment = new Environment();

    public World3D(boolean garage) {
        environment.set(ColorAttribute.createAmbientLight(
            0.34f, 0.37f, 0.46f, 1f
        ));

        environment.add(new DirectionalLight().set(
            0.94f, 0.91f, 0.85f, -0.35f, -1f, -0.25f
        ));

        environment.add(new DirectionalLight().set(
            0.32f, 0.43f, 0.68f, 0.65f, -0.2f, 0.5f
        ));

        if (garage) buildGarage();
        else buildTrack();
    }

    private Material mat(Color diffuse, Color specular, float shine) {
        return new Material(
            ColorAttribute.createDiffuse(diffuse),
            ColorAttribute.createSpecular(specular),
            FloatAttribute.createShininess(shine)
        );
    }

    private Model cube(Material material) {
        ModelBuilder builder = new ModelBuilder();
        Model model = builder.createBox(
            1f, 1f, 1f, material, ATTR
        );
        owned.add(model);
        return model;
    }

    private void addBox(
        Model model,
        float x, float y, float z,
        float width, float height, float depth
    ) {
        ModelInstance instance = new ModelInstance(model);

        instance.transform
            .setToTranslation(x, y, z)
            .scale(width, height, depth);

        instances.add(instance);
    }

    private void buildGarage() {
        Model floor = cube(mat(
            new Color(.075f, .09f, .12f, 1f),
            new Color(.42f, .46f, .54f, 1f), 34f
        ));

        Model wall = cube(mat(
            new Color(.035f, .05f, .085f, 1f),
            new Color(.18f, .24f, .35f, 1f), 20f
        ));

        Model panel = cube(mat(
            new Color(.09f, .13f, .19f, 1f),
            new Color(.35f, .43f, .58f, 1f), 30f
        ));

        Model metal = cube(mat(
            new Color(.22f, .27f, .34f, 1f),
            new Color(.8f, .85f, .95f, 1f), 60f
        ));

        Model light = cube(mat(
            new Color(.78f, .84f, 1f, 1f),
            new Color(1f, 1f, 1f, 1f), 48f
        ));

        Model blue = cube(mat(
            new Color(.08f, .28f, .56f, 1f),
            new Color(.2f, .55f, 1f, 1f), 40f
        ));

        Model yellow = cube(mat(
            new Color(.85f, .53f, .12f, 1f),
            new Color(1f, .72f, .3f, 1f), 32f
        ));

        addBox(floor, 0f, -.18f, 0f, 42f, .36f, 38f);

        addBox(wall, 0f, 5.5f, -18f, 42f, 11f, .5f);
        addBox(wall, -20.5f, 5.5f, 0f, .5f, 11f, 38f);
        addBox(wall, 20.5f, 5.5f, 0f, .5f, 11f, 38f);

        // Floor detailing.
        for (int x = -18; x <= 18; x += 3) {
            addBox(metal, x, .006f, 0f, .025f, .012f, 34f);
        }

        for (int z = -15; z <= 15; z += 3) {
            addBox(metal, 0f, .007f, z, 38f, .012f, .025f);
        }

        // Display platform.
        addBox(panel, 0f, .08f, -1f, 12f, .12f, 8f);
        addBox(blue, -5.8f, .17f, -1f, .12f, .05f, 8f);
        addBox(blue, 5.8f, .17f, -1f, .12f, .05f, 8f);

        // Workshop cabinets.
        for (int x = -16; x <= 16; x += 8) {
            addBox(panel, x, 1.2f, -15.5f, 3.3f, 2.4f, 1.2f);
            addBox(metal, x, 2.45f, -15.5f, 3.35f, .12f, 1.25f);
            addBox(blue, x, 1.5f, -14.86f, 2.5f, .12f, .035f);
        }

        // Structural pillars.
        for (int x = -18; x <= 18; x += 6) {
            addBox(metal, x, 4.8f, -16.5f, .22f, 9.6f, .22f);
        }

        // Overhead lights.
        for (int x = -12; x <= 12; x += 6) {
            addBox(light, x, 9.7f, -2f, 4.5f, .10f, .55f);
            addBox(light, x, 9.7f, 7f, 4.5f, .10f, .55f);
        }

        // Blue wall panels.
        for (int x = -15; x <= 15; x += 5) {
            addBox(blue, x, 6.0f, -17.70f, 2.6f, .55f, .08f);
        }

        addBox(yellow, 0f, .08f, 8f, 14f, .08f, .18f);
    }

    private void buildTrack() {
        Model road = cube(mat(
            new Color(.035f, .043f, .055f, 1f),
            new Color(.25f, .28f, .34f, 1f), 24f
        ));

        Model shoulder = cube(mat(
            new Color(.12f, .14f, .17f, 1f),
            new Color(.38f, .42f, .48f, 1f), 24f
        ));

        Model white = cube(mat(
            new Color(.86f, .89f, .94f, 1f),
            new Color(.9f, .95f, 1f, 1f), 38f
        ));

        Model yellow = cube(mat(
            new Color(.95f, .59f, .09f, 1f),
            new Color(1f, .75f, .3f, 1f), 35f
        ));

        Model dark = cube(mat(
            new Color(.018f, .022f, .03f, 1f),
            new Color(.12f, .14f, .18f, 1f), 12f
        ));

        Model barrier = cube(mat(
            new Color(.19f, .23f, .29f, 1f),
            new Color(.62f, .68f, .78f, 1f), 48f
        ));

        Model buildingA = cube(mat(
            new Color(.035f, .055f, .095f, 1f),
            new Color(.25f, .32f, .45f, 1f), 20f
        ));

        Model buildingB = cube(mat(
            new Color(.07f, .085f, .13f, 1f),
            new Color(.3f, .36f, .49f, 1f), 24f
        ));

        Model windowBlue = cube(mat(
            new Color(.12f, .34f, .62f, 1f),
            new Color(.55f, .72f, 1f, 1f), 48f
        ));

        Model windowWarm = cube(mat(
            new Color(.75f, .42f, .16f, 1f),
            new Color(1f, .7f, .35f, 1f), 40f
        ));

        Model pole = cube(mat(
            new Color(.17f, .2f, .26f, 1f),
            new Color(.65f, .7f, .8f, 1f), 55f
        ));

        Model lamp = cube(mat(
            new Color(.88f, .9f, 1f, 1f),
            new Color(1f, 1f, 1f, 1f), 64f
        ));

        Model red = cube(mat(
            new Color(.85f, .055f, .045f, 1f),
            new Color(1f, .2f, .12f, 1f), 30f
        ));

        Model green = cube(mat(
            new Color(.08f, .75f, .31f, 1f),
            new Color(.3f, 1f, .55f, 1f), 30f
        ));

        // Main asphalt road.
        addBox(road, 0f, -.12f, -420f, 18f, .22f, 1000f);

        // Road shoulders.
        addBox(shoulder, -9.5f, -.13f, -420f, 1.1f, .22f, 1000f);
        addBox(shoulder, 9.5f, -.13f, -420f, 1.1f, .22f, 1000f);

        // Lane markings.
        for (int i = 0; i < 145; i++) {
            float z = 8f - i * 6.8f;
            addBox(white, 0f, .005f, z, .16f, .012f, 3.3f);
        }

        addBox(white, -7.65f, .005f, -420f, .12f, .012f, 1000f);
        addBox(white, 7.65f, .005f, -420f, .12f, .012f, 1000f);

        addBox(yellow, -8.8f, .006f, -420f, .12f, .014f, 1000f);
        addBox(yellow, 8.8f, .006f, -420f, .12f, .014f, 1000f);

        // Starting line.
        addBox(white, 0f, .012f, -18f, 16f, .025f, .55f);

        // Subtle tire marks.
        for (int i = 0; i < 14; i++) {
            float z = -42f - i * 29f;
            addBox(dark, -3.1f, .004f, z, .12f, .01f, 8f);
            addBox(dark, 3.1f, .004f, z - 3f, .12f, .01f, 8f);
        }

        // Side barriers.
        addBox(barrier, -10.1f, .42f, -420f, .42f, .84f, 1000f);
        addBox(barrier, 10.1f, .42f, -420f, .42f, .84f, 1000f);

        for (int i = 0; i < 100; i++) {
            float z = 65f - i * 10f;
            addBox(yellow, -9.86f, .48f, z, .055f, .38f, 3.4f);
            addBox(yellow, 9.86f, .48f, z, .055f, .38f, 3.4f);
        }

        // City skyline with illuminated windows.
        for (int i = 0; i < 23; i++) {
            float z = 18f - i * 42f;
            float height = 8f + (i % 5) * 2.8f;
            float width = 5f + (i % 3) * .8f;

            Model building = (i % 2 == 0) ? buildingA : buildingB;

            for (int side = -1; side <= 1; side += 2) {
                float centerX = side * 17.5f;

                addBox(
                    building,
                    centerX, height * .5f, z,
                    width, height, 8f
                );

                float faceX = centerX - side * (width * .5f + .045f);
                int floors = Math.max(2, (int)(height / 2.5f));

                for (int floor = 0; floor < floors; floor++) {
                    float wy = 1.4f + floor * 2.35f;

                    for (int w = -1; w <= 1; w++) {
                        Model window = ((i + floor + w) % 4 == 0)
                            ? windowWarm : windowBlue;

                        addBox(
                            window,
                            faceX, wy, z + w * 2.15f,
                            .09f, .72f, .92f
                        );
                    }
                }

                // Building roof trim.
                addBox(
                    yellow,
                    centerX, height + .07f, z,
                    width + .15f, .12f, .12f
                );
            }
        }

        // Street lamps.
        for (int i = 0; i < 22; i++) {
            float z = 4f - i * 45f;

            for (int side = -1; side <= 1; side += 2) {
                addBox(pole, side * 11.4f, 3.3f, z, .16f, 6.6f, .16f);

                addBox(
                    pole,
                    side * 10.8f, 6.45f, z,
                    1.2f, .14f, .16f
                );

                addBox(
                    lamp,
                    side * 10.15f, 6.36f, z,
                    .9f, .13f, .34f
                );
            }
        }

        // Starting gantry.
        addBox(pole, -6.5f, 2.6f, -18f, .24f, 5.2f, .24f);
        addBox(pole, 6.5f, 2.6f, -18f, .24f, 5.2f, .24f);
        addBox(pole, 0f, 5.2f, -18f, 13.2f, .28f, .35f);

        // Start signal.
        addBox(red, -.48f, 4.55f, -17.75f, .28f, .28f, .12f);
        addBox(yellow, 0f, 4.55f, -17.75f, .28f, .28f, .12f);
        addBox(green, .48f, 4.55f, -17.75f, .28f, .28f, .12f);
    }

    public void render(
        com.badlogic.gdx.graphics.g3d.ModelBatch batch
    ) {
        for (ModelInstance instance : instances) {
            batch.render(instance, environment);
        }
    }

    public void dispose() {
        for (Model model : owned) {
            model.dispose();
        }

        owned.clear();
        instances.clear();
    }
}