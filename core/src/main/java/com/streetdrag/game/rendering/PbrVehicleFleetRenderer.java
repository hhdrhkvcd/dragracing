package com.streetdrag.game.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import com.streetdrag.game.VehicleSpec;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;

/** PBR renderer for imported GLB cars. One active car is retained in the garage. */
public final class PbrVehicleFleetRenderer implements Disposable {

    private static final class VehicleEntry {
        final SceneAsset asset;
        final Scene scene;
        final Vector3 center;
        final float groundY;
        final float scale;
        final float axisRotation;

        VehicleEntry(SceneAsset asset, Scene scene,
                     Vector3 center, float groundY,
                     float scale, float axisRotation) {
            this.asset = asset;
            this.scene = scene;
            this.center = center;
            this.groundY = groundY;
            this.scale = scale;
            this.axisRotation = axisRotation;
        }

        void setTransform(float x, float y, float z, float yaw) {
            scene.modelInstance.transform.idt()
                .translate(x, y, z)
                .rotate(Vector3.Y, yaw)
                .rotate(Vector3.Y, axisRotation)
                .scale(scale, scale, scale)
                .translate(-center.x, -groundY, -center.z);
        }
    }

    private final SceneManager sceneManager = new SceneManager();
    private final Array<SceneAsset> assets = new Array<SceneAsset>();
    private final Map<String, VehicleEntry> vehicles =
        new HashMap<String, VehicleEntry>();

    private DirectionalShadowLight shadowLight;
    private Cubemap environmentCubemap;
    private Cubemap diffuseCubemap;
    private Cubemap specularCubemap;
    private Texture brdfLut;
    private SceneSkybox skybox;

    private int viewportWidth = -1;
    private int viewportHeight = -1;

    public PbrVehicleFleetRenderer(PerspectiveCamera camera, VehicleSpec... specs) {
        if (camera == null) {
            throw new IllegalArgumentException("Camera cannot be null");
        }

        sceneManager.setCamera(camera);
        createEnvironment();

        if (specs != null) {
            for (VehicleSpec spec : specs) {
                if (spec != null) loadVehicle(spec);
            }
        }
    }

    private void createEnvironment() {
        shadowLight = new DirectionalShadowLight(
            1024, 1024, 55f, 55f, 1f, 150f
        );

        shadowLight.direction.set(-0.45f, -1f, -0.35f).nor();
        shadowLight.color.set(0.95f, 0.97f, 1f, 1f);
        shadowLight.setCenter(0f, 0.5f, 0f);
        sceneManager.environment.add(shadowLight);

        IBLBuilder builder = IBLBuilder.createOutdoor(shadowLight);
        try {
            environmentCubemap = builder.buildEnvMap(256);
            diffuseCubemap = builder.buildIrradianceMap(32);
            specularCubemap = builder.buildRadianceMap(8);
        } finally {
            builder.dispose();
        }

        brdfLut = new Texture(
            Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png")
        );
        brdfLut.setFilter(
            Texture.TextureFilter.Linear,
            Texture.TextureFilter.Linear
        );

        sceneManager.setAmbientLight(0.29f);
        sceneManager.environment.set(
            new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLut)
        );
        sceneManager.environment.set(
            PBRCubemapAttribute.createSpecularEnv(specularCubemap)
        );
        sceneManager.environment.set(
            PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap)
        );

        skybox = new SceneSkybox(environmentCubemap);
        sceneManager.setSkyBox(skybox);
    }

    private String resolveModelFile(String id) {
        switch (id) {
            case "audi_rs6": return "sport-sedan.glb";
            case "bmw_m3": return "executive-sedan.glb";
            case "amg_e63": return "luxury-limousine.glb";
            case "panamera": return "grand-tourer.glb";
            case "wrx_sti": return "hot-hatchback.glb";
            case "evo_ix": return "sport-sedan.glb";
            case "gtr_r35": return "rear-engine-sports-coupe.glb";
            case "supra_mk5": return "grand-tourer.glb";
            case "911_turbo": return "rear-engine-sports-coupe.glb";
            case "silvia_s15": return "muscle-coupe.glb";
            case "bmw_m5": return "executive-sedan.glb";
            case "stagea": return "boxy-off-roader.glb";
            case "audi_rs7": return "electric-fastback.glb";
            case "amg_gtr": return "mid-engine-supercar.glb";
            case "luxury_suv": return "luxury-suv.glb";
            case "retro_city": return "retro-city-car.glb";
            default: return "sport-sedan.glb";
        }
    }

    /** Loads a car if needed. Existing loaded cars are not reloaded. */
    public void prepareVehicle(VehicleSpec spec) {
        if (spec != null && !vehicles.containsKey(spec.id)) {
            loadVehicle(spec);
        }
    }

    /**
     * Keeps only the requested model loaded. Reusing the SceneManager means the
     * expensive IBL cubemaps and shadow framebuffer are not rebuilt per car.
     */
    public void setActiveVehicle(VehicleSpec spec) {
        if (spec == null) return;

        Iterator<Map.Entry<String, VehicleEntry>> iterator =
            vehicles.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, VehicleEntry> entry = iterator.next();
            if (!entry.getKey().equals(spec.id)) {
                VehicleEntry old = entry.getValue();
                sceneManager.removeScene(old.scene);
                old.asset.dispose();
                assets.removeValue(old.asset, true);
                iterator.remove();
            }
        }

        prepareVehicle(spec);
    }

    private void loadVehicle(VehicleSpec spec) {
        String filename = resolveModelFile(spec.id);
        String path = "models/cars/" + filename;

        if (!Gdx.files.internal(path).exists()) {
            Gdx.app.error("PBR", "Model not found: " + path);
            return;
        }

        SceneAsset asset = null;
        try {
            asset = new GLBLoader().load(Gdx.files.internal(path));
            Scene scene = new Scene(asset.scene);

            BoundingBox bounds = new BoundingBox();
            scene.modelInstance.calculateBoundingBox(bounds);
            Vector3 dimensions = bounds.getDimensions(new Vector3());
            Vector3 center = bounds.getCenter(new Vector3());

            float length = Math.max(dimensions.x, dimensions.z);
            if (length < 0.001f) {
                throw new RuntimeException("Invalid model dimensions: " + path);
            }

            float scale = 4.35f / length;

            // Axis correction keeps the long side on Z and flips the model
            // to face the race direction used by RaceScreen (-Z).
            float axisRotation = (dimensions.x > dimensions.z ? 90f : 0f) + 180f;

            VehicleEntry entry = new VehicleEntry(
                asset, scene, center, bounds.min.y, scale, axisRotation
            );

            vehicles.put(spec.id, entry);
            assets.add(asset);
            sceneManager.addScene(scene);
            Gdx.app.log("PBR", "Loaded " + spec.id + " -> " + filename);
        } catch (Exception ex) {
            if (asset != null) asset.dispose();
            Gdx.app.error("PBR", "Cannot load " + path + "; using old vehicle model", ex);
        }
    }

    public boolean hasModel(VehicleSpec spec) {
        return spec != null && vehicles.containsKey(spec.id);
    }

    public void setTransform(
        VehicleSpec spec, float x, float y, float z, float yawDegrees
    ) {
        if (spec == null) return;
        VehicleEntry entry = vehicles.get(spec.id);
        if (entry != null) entry.setTransform(x, y, z, yawDegrees);
    }

    public void render(PerspectiveCamera camera, float delta) {
        if (vehicles.isEmpty() || camera == null) return;

        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        if (width != viewportWidth || height != viewportHeight) {
            viewportWidth = width;
            viewportHeight = height;
            sceneManager.updateViewport(width, height);
        }

        sceneManager.setCamera(camera);
        if (shadowLight != null) {
            shadowLight.setCenter(camera.position.x, 0.5f, camera.position.z);
        }

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
        float safeDelta = Math.max(0f, Math.min(delta, 0.05f));
        sceneManager.update(safeDelta);
        sceneManager.render();
    }

    @Override public void dispose() {
        sceneManager.dispose();
        for (SceneAsset asset : assets) asset.dispose();
        assets.clear();
        vehicles.clear();
        if (skybox != null) skybox.dispose();
        if (shadowLight != null) shadowLight.dispose();
        if (environmentCubemap != null) environmentCubemap.dispose();
        if (diffuseCubemap != null) diffuseCubemap.dispose();
        if (specularCubemap != null) specularCubemap.dispose();
        if (brdfLut != null) brdfLut.dispose();
    }
}
