package com.streetdrag.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/** Reusable procedural street/garage environment with mobile-friendly geometry. */
public final class World3D {
    private static final long ATTR = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
    private final Array<Model> owned=new Array<Model>();
    private final Array<ModelInstance> instances=new Array<ModelInstance>();
    public final Environment environment=new Environment();

    public World3D(boolean garage) {
        environment.set(ColorAttribute.createAmbientLight(.48f,.50f,.56f,1f));
        environment.add(new DirectionalLight().set(.90f,.88f,.82f,-.35f,-1f,-.25f));
        environment.add(new DirectionalLight().set(.24f,.34f,.55f,.65f,-.2f,.5f));
        if(garage) buildGarage(); else buildTrack();
    }

    private Material mat(Color c) { return new Material(ColorAttribute.createDiffuse(c)); }

    private Model box(ModelBuilder b,float w,float h,float d,Material m) {
        Model model=b.createBox(w,h,d,m,ATTR); owned.add(model); return model;
    }

    private void add(Model model,float x,float y,float z) {
        ModelInstance i=new ModelInstance(model);
        i.transform.setToTranslation(x,y,z);
        instances.add(i);
    }

    private void buildGarage() {
        ModelBuilder b=new ModelBuilder();
        Model floor=box(b,34f,.18f,32f,mat(new Color(.12f,.14f,.17f,1)));
        add(floor,0,-.12f,0);
        Model wall=box(b,34f,9f,.35f,mat(new Color(.045f,.06f,.085f,1)));
        add(wall,0,4f,-9f);
        Model cabinet=box(b,2.2f,2.2f,.7f,mat(new Color(.12f,.15f,.20f,1)));
        for(int x=-12;x<=12;x+=6) add(cabinet,x,1.0f,-7.8f);
        Model post=box(b,.18f,7f,.18f,mat(new Color(.15f,.19f,.25f,1)));
        for(int x=-15;x<=15;x+=5) add(post,x,3.4f,-8.5f);
        Model light=box(b,4f,.08f,.45f,mat(new Color(.72f,.82f,1f,1)));
        for(int x=-10;x<=10;x+=10) add(light,x,7.8f,-1f);
    }

    private void buildTrack() {
        ModelBuilder b=new ModelBuilder();
        Material asphalt=mat(new Color(.055f,.065f,.078f,1));
        Material line=mat(new Color(.78f,.80f,.80f,1));
        Material yellow=mat(new Color(.95f,.63f,.10f,1));
        Material barrier=mat(new Color(.28f,.31f,.35f,1));
        Material buildingA=mat(new Color(.045f,.065f,.11f,1));
        Material buildingB=mat(new Color(.08f,.095f,.14f,1));
        Model road=box(b,15f,.14f,900f,asphalt); add(road,0,-.10f,-420f);
        Model lineModel=box(b,.10f,.025f,6f,line);
        for(int i=0;i<145;i++) add(lineModel,0,.0f,8f-i*6.2f);
        Model edge=box(b,.18f,.025f,900f,yellow);
        add(edge,-7.25f,.0f,-420f); add(edge,7.25f,.0f,-420f);
        Model wall=box(b,.55f,1.15f,900f,barrier);
        add(wall,-8f,.48f,-420f); add(wall,8f,.48f,-420f);
        Model blockA=box(b,1f,1f,1f,buildingA);
        Model blockB=box(b,1f,1f,1f,buildingB);
        for(int i=0;i<38;i++) {
            float z=12f-i*23f;
            float h=5f+(i%5)*2.2f;
            Model m=(i%2==0)?blockA:blockB;
            ModelInstance left=new ModelInstance(m);
            left.transform.setToTranslation(-12f-h*.18f,h*.5f,z).scale(2.8f,h,5.5f);
            instances.add(left);
            ModelInstance right=new ModelInstance(m);
            right.transform.setToTranslation(12f+h*.18f,h*.5f,z-8f).scale(2.8f,h+2f,5.5f);
            instances.add(right);
        }
        Model lampPole=box(b,.12f,6f,.12f,mat(new Color(.20f,.22f,.26f,1)));
        Model lampHead=box(b,1.4f,.18f,.42f,mat(new Color(.75f,.82f,.95f,1)));
        for(int i=0;i<18;i++) {
            float z=4f-i*45f;
            add(lampPole,-9.5f,2.9f,z); add(lampHead,-9.0f,5.9f,z);
            add(lampPole,9.5f,2.9f,z); add(lampHead,9.0f,5.9f,z);
        }
        // Start-line gantry.
        Model gantry=box(b,.22f,5.2f,.22f,mat(new Color(.25f,.28f,.34f,1)));
        add(gantry,-6.3f,2.6f,-18f); add(gantry,6.3f,2.6f,-18f);
        Model beam=box(b,12.8f,.25f,.35f,mat(new Color(.25f,.28f,.34f,1)));
        add(beam,0,5.1f,-18f);
    }

    public void render(com.badlogic.gdx.graphics.g3d.ModelBatch batch) {
        for(ModelInstance i:instances) batch.render(i,environment);
    }

    public void dispose() {
        for(Model m:owned) m.dispose();
        owned.clear(); instances.clear();
    }
}
