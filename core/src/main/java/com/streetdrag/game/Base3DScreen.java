package com.streetdrag.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

abstract class Base3DScreen extends ScreenAdapter {
    final StreetDragGame game;
    final ModelBatch modelBatch = new ModelBatch();
    final PerspectiveCamera camera = new PerspectiveCamera(62f, StreetDragGame.HUD_W, StreetDragGame.HUD_H);
    World3D world;

    Base3DScreen(StreetDragGame game) {
        this.game=game;
        camera.near=.1f;
        camera.far=700f;
        camera.up.set(0,1,0);
    }

    void clear(float r,float g,float b) {
        Gdx.gl.glViewport(0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(r,g,b,1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
    }

    void renderWorld() {
        renderWorld(0f);
    }

    void renderWorld(float delta) {
        camera.update();
        modelBatch.begin(camera);
        if(world!=null) world.render(modelBatch);
        renderModels(modelBatch);
        modelBatch.end();

        renderPbr(delta);

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
    }

    void renderPbr(float delta) {
        // PBR rendering hook for vehicle models.
    }

    abstract void renderModels(ModelBatch batch);

    void beginHud() {
        game.hudViewport.apply();
        game.hudCamera.update();
        game.uiBatch.setProjectionMatrix(game.hudCamera.combined);
        game.uiShapes.setProjectionMatrix(game.hudCamera.combined);
    }

    void panel(float x,float y,float w,float h,Color color) {
        game.uiShapes.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        game.uiShapes.setColor(color);
        game.uiShapes.rect(x,y,w,h);
        game.uiShapes.end();
    }

    void button(float x,float y,float w,float h,Color color) {
        game.uiShapes.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        game.uiShapes.setColor(color); game.uiShapes.rect(x,y,w,h);
        game.uiShapes.setColor(1f,1f,1f,.18f); game.uiShapes.rect(x,y+h-3,w,3);
        game.uiShapes.end();
    }

    void beginText() { game.uiBatch.begin(); }
    void text(String value,float x,float y,float scale,Color color) {
        game.font.getData().setScale(scale); game.font.setColor(color);
        game.font.draw(game.uiBatch,value,x,y);
    }
    void endText() { game.uiBatch.end(); }

    Vector2 touch(int sx,int sy) {
        return game.hudViewport.unproject(new Vector2(sx,sy));
    }

    boolean hit(float x,float y,float bx,float by,float bw,float bh) {
        return x>=bx && x<=bx+bw && y>=by && y<=by+bh;
    }

    @Override public void dispose() {
        if(world!=null) world.dispose();
        modelBatch.dispose();
    }
}
