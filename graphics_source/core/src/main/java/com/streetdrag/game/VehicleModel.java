package com.streetdrag.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.UBJsonReader;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;

/** 3D vehicle renderer. Loads optional .g3db art assets; otherwise builds a detailed low-poly fallback. */
public final class VehicleModel {
    private static final long ATTR = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
    private final Array<Model> owned = new Array<Model>();
    private final Array<ModelInstance> parts = new Array<ModelInstance>();
    private Model externalModel;
    private ModelInstance externalInstance;
    private final VehicleSpec spec;

    public VehicleModel(VehicleSpec spec) {
        this.spec = spec;
        String modelPath = "models/cars/" + spec.id + ".g3db";
        FileHandle file = Gdx.files.internal(modelPath);
        if (file.exists()) {
            try {
                externalModel = new G3dModelLoader(new UBJsonReader()).loadModel(file);
                externalInstance = new ModelInstance(externalModel);
                return;
            } catch (Exception ex) {
                Gdx.app.error("VehicleModel", "Could not load " + modelPath + "; using fallback", ex);
            }
        }
        buildFallback();
    }

    private Material material(Color color, float shininess) {
        return new Material(
            ColorAttribute.createDiffuse(color),
            ColorAttribute.createSpecular(.22f,.22f,.24f,1f),
            FloatAttribute.createShininess(shininess)
        );
    }

    private void addBox(ModelBuilder b, float w,float h,float d, Material m,
                        float x,float y,float z) {
        Model model=b.createBox(w,h,d,m,ATTR);
        owned.add(model);
        ModelInstance instance=new ModelInstance(model);
        instance.transform.setToTranslation(x,y,z);
        parts.add(instance);
    }

    private void buildFallback() {
        ModelBuilder b=new ModelBuilder();
        Material paint=material(spec.paint,48f);
        Material glass=material(new Color(.018f,.045f,.065f,1),72f);
        Material rubber=material(new Color(.012f,.014f,.018f,1),8f);
        Material rim=material(new Color(.42f,.46f,.50f,1),64f);
        Material trim=material(new Color(.045f,.05f,.06f,1),24f);
        Material red=material(new Color(.95f,.018f,.025f,1),35f);
        Material white=material(new Color(.78f,.85f,.92f,1),35f);
        Material shadow=material(new Color(.018f,.022f,.03f,1),1f);
        Model shadowModel=b.createCylinder(3.2f,.018f,5.0f,32,shadow,ATTR);
        owned.add(shadowModel);
        ModelInstance shadowInstance=new ModelInstance(shadowModel);
        shadowInstance.transform.setToTranslation(0f,.005f,0f);
        parts.add(shadowInstance);

        // Body shell, wagon roof, hood and tailgate.
        addBox(b,1.92f,.62f,4.35f,paint,0,.78f,0);
        addBox(b,1.70f,.56f,2.25f,paint,0,1.30f,-.20f);
        addBox(b,1.72f,.16f,1.08f,paint,0,1.43f,-1.58f);
        addBox(b,1.72f,.15f,.78f,paint,0,.96f,1.91f);
        addBox(b,1.98f,.20f,.28f,trim,0,.53f,-2.12f);
        addBox(b,1.98f,.20f,.28f,trim,0,.52f,2.12f);
        // Glass surfaces and pillars.
        addBox(b,1.57f,.36f,1.96f,glass,0,1.34f,-.22f);
        addBox(b,1.58f,.29f,.36f,glass,0,1.31f,1.05f);
        addBox(b,1.58f,.28f,.32f,glass,0,1.30f,-1.48f);
        addBox(b,.055f,.43f,2.15f,trim,-.78f,1.34f,-.22f);
        addBox(b,.055f,.43f,2.15f,trim,.78f,1.34f,-.22f);
        // Mirrors, side skirts, grille and lamps.
        addBox(b,.18f,.12f,.28f,trim,-1.00f,1.20f,-1.05f);
        addBox(b,.18f,.12f,.28f,trim,1.00f,1.20f,-1.05f);
        addBox(b,.10f,.12f,3.55f,trim,-.97f,.43f,0);
        addBox(b,.10f,.12f,3.55f,trim,.97f,.43f,0);
        addBox(b,.88f,.20f,.055f,trim,0,.72f,-2.22f);
        addBox(b,.46f,.11f,.035f,white,-.64f,.94f,-2.20f);
        addBox(b,.46f,.11f,.035f,white,.64f,.94f,-2.20f);
        addBox(b,.50f,.12f,.035f,red,-.65f,1.05f,2.20f);
        addBox(b,.50f,.12f,.035f,red,.65f,1.05f,2.20f);
        addBox(b,1.85f,.08f,.34f,trim,0,1.58f,1.80f);

        // Four detailed wheel assemblies. Cylinder axis is rotated to the axle direction.
        Model tire=b.createCylinder(.76f,.25f,.76f,24,rubber,ATTR); owned.add(tire);
        Model wheelRim=b.createCylinder(.51f,.27f,.51f,24,rim,ATTR); owned.add(wheelRim);
        float[] xs={-.99f,.99f}; float[] zs={-1.42f,1.42f};
        for(float x:xs) for(float z:zs) {
            ModelInstance t=new ModelInstance(tire);
            t.transform.setToTranslation(x,.40f,z).rotate(Vector3.Z,90f);
            parts.add(t);
            ModelInstance r=new ModelInstance(wheelRim);
            r.transform.setToTranslation(x,.40f,z).rotate(Vector3.Z,90f);
            parts.add(r);
        }
    }

    public void setTransform(float x,float y,float z,float yawDegrees) {
        if (externalInstance!=null) {
            externalInstance.transform.setToTranslation(x,y,z).rotate(Vector3.Y,yawDegrees);
        } else {
            for(ModelInstance p:parts) {
                // Keep each generated part's local offset and apply world translation/yaw.
                Vector3 local=p.userData instanceof Vector3 ? (Vector3)p.userData : null;
                if(local==null) {
                    local=p.transform.getTranslation(new Vector3());
                    p.userData=new Vector3(local);
                }
                p.transform.setToTranslation(local.x+x,local.y+y,local.z+z)
                    .rotate(Vector3.Y,yawDegrees);
            }
        }
    }

    public void render(com.badlogic.gdx.graphics.g3d.ModelBatch batch,
                       com.badlogic.gdx.graphics.g3d.Environment environment) {
        if(externalInstance!=null) batch.render(externalInstance,environment);
        else for(ModelInstance p:parts) batch.render(p,environment);
    }

    public void dispose() {
        if(externalModel!=null) externalModel.dispose();
        for(Model m:owned) m.dispose();
        owned.clear(); parts.clear();
    }
}
