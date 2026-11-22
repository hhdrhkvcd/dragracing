package com.streetdrag.game.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;

/**
 * PBR glTF/GLB vehicle renderer for StreetDrag.
 * Expected vehicle forward direction: local -Z, up: +Y.
 * This class owns and disposes all GPU resources it creates.
 */
public final class PbrVehicleRenderer implements Disposable {

    public enum CameraMode {
        REAR,
        HOOD,
        SIDE,
        ORBIT
    }

    private final SceneAsset sceneAsset;
    private final Scene scene;
    private final SceneManager sceneManager;
    private final PerspectiveCamera camera;

    private final DirectionalShadowLight shadowLight;
    private final Cubemap environmentCubemap;
    private final Cubemap diffuseCubemap;
    private final Cubemap specularCubemap;
    private final Texture brdfLut;
    private final SceneSkybox skybox;

    private final Vector3 vehiclePosition = new Vector3();
    private final Vector3 cameraOffset = new Vector3();
    private final Vector3 targetOffset = new Vector3();
    private final Vector3 target = new Vector3();
    private final Vector3 cameraPosition = new Vector3();

    private float vehicleYawDegrees;
    private float vehicleScale = 1f;
    private float orbitAngle;
    private CameraMode cameraMode = CameraMode.REAR;

    public PbrVehicleRenderer(String internalModelPath, float viewportWidth, float viewportHeight) {
        if (internalModelPath == null || internalModelPath.trim().isEmpty()) {
            throw new GdxRuntimeException("Vehicle model path is empty.");
        }
        if (!Gdx.files.internal(internalModelPath).exists()) {
            throw new GdxRuntimeException(
                "3D vehicle model not found: " + internalModelPath
                + ". Put a GLB file in android/assets/models/vehicles/ and use its internal path."
            );
        }

        String lower = internalModelPath.toLowerCase();
        if (lower.endsWith(".glb")) {
            sceneAsset = new GLBLoader().load(Gdx.files.internal(internalModelPath));
        } else if (lower.endsWith(".gltf")) {
            sceneAsset = new GLTFLoader().load(Gdx.files.internal(internalModelPath));
        } else {
            throw new GdxRuntimeException("Expected .glb or .gltf model: " + internalModelPath);
        }

        scene = new Scene(sceneAsset.scene);
        sceneManager = new SceneManager();
        sceneManager.addScene(scene);

        camera = new PerspectiveCamera(60f, viewportWidth, viewportHeight);
        camera.near = 0.05f;
        camera.far = 250f;
        camera.position.set(0f, 2.7f, 7.5f);
        camera.lookAt(0f, 0.9f, 0f);
        camera.up.set(Vector3.Y);
        camera.update(true);
        sceneManager.setCamera(camera);

        // A compact real-time shadow map suitable as a starting point for Android.
        shadowLight = new DirectionalShadowLight(512, 512, 55f, 55f, 1f, 120f);
        shadowLight.direction.set(-0.45f, -1f, -0.35f).nor();
        shadowLight.color.set(0.95f, 0.97f, 1f, 1f);
        shadowLight.setCenter(0f, 0.5f, 0f);
        sceneManager.environment.add(shadowLight);

        // Low-resolution IBL keeps initialization and memory use reasonable on mobile.
        IBLBuilder iblBuilder = IBLBuilder.createOutdoor(shadowLight);
        environmentCubemap = iblBuilder.buildEnvMap(256);
        diffuseCubemap = iblBuilder.buildIrradianceMap(32);
        specularCubemap = iblBuilder.buildRadianceMap(8);
        iblBuilder.dispose();

        brdfLut = new Texture(
            Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png")
        );
        brdfLut.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        sceneManager.setAmbientLight(0.35f);
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

        applyVehicleTransform();
        updateCamera(0f);
    }

    /** Set world position, yaw (degrees), and uniform model scale. */
    public void setVehicleTransform(float x, float y, float z, float yawDegrees, float scale) {
        vehiclePosition.set(x, y, z);
        vehicleYawDegrees = yawDegrees;
        vehicleScale = Math.max(0.001f, scale);
        applyVehicleTransform();
    }

    private void applyVehicleTransform() {
        scene.modelInstance.transform.idt();
        scene.modelInstance.transform.translate(vehiclePosition);
        scene.modelInstance.transform.rotate(Vector3.Y, vehicleYawDegrees);
        scene.modelInstance.transform.scale(vehicleScale, vehicleScale, vehicleScale);
    }

    public void setCameraMode(CameraMode mode) {
        if (mode != null) cameraMode = mode;
        updateCamera(0f);
    }

    public CameraMode getCameraMode() {
        return cameraMode;
    }

    /** Orbit camera angle in degrees; used only in ORBIT mode. */
    public void setOrbitAngle(float degrees) {
        orbitAngle = degrees;
    }

    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update(true);
        sceneManager.updateViewport(width, height);
    }

    public void render(float delta) {
        float safeDelta = Math.max(0f, Math.min(delta, 0.05f));
        updateCamera(safeDelta);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
        Gdx.gl.glClearColor(0.012f, 0.018f, 0.03f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        sceneManager.update(safeDelta);
        sceneManager.render();
    }

    private void updateCamera(float delta) {
        if (cameraMode == CameraMode.ORBIT) {
            orbitAngle += delta * 12f;
            cameraOffset.set(8f, 3.1f, 8f).rotate(Vector3.Y, orbitAngle);
            targetOffset.set(0f, 0.8f, 0f);
        } else if (cameraMode == CameraMode.SIDE) {
            cameraOffset.set(7.5f, 2.0f, 0f);
            targetOffset.set(0f, 0.9f, 0f);
        } else if (cameraMode == CameraMode.HOOD) {
            cameraOffset.set(0f, 1.45f, -1.4f);
            targetOffset.set(0f, 1.1f, -22f);
        } else {
            cameraOffset.set(0f, 2.8f, 7.5f);
            targetOffset.set(0f, 0.85f, -1.0f);
        }

        if (cameraMode != CameraMode.ORBIT) {
            cameraOffset.rotate(Vector3.Y, vehicleYawDegrees);
        }
        targetOffset.rotate(Vector3.Y, vehicleYawDegrees);

        cameraPosition.set(vehiclePosition).add(cameraOffset);
        target.set(vehiclePosition).add(targetOffset);

        camera.position.set(cameraPosition);
        camera.direction.set(target).sub(camera.position).nor();
        camera.up.set(Vector3.Y);
        camera.update(true);
    }

    @Override
    public void dispose() {
        sceneManager.dispose();
        sceneAsset.dispose();
        skybox.dispose();
        shadowLight.dispose();
        environmentCubemap.dispose();
        diffuseCubemap.dispose();
        specularCubemap.dispose();
        brdfLut.dispose();
    }
}
