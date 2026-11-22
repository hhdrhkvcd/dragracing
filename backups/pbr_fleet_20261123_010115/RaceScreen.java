package com.streetdrag.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/** 402 m race screen: chase/hood/side/wide cameras, AI rival, touch + keyboard controls. */
final class RaceScreen extends Base3DScreen {
    private final VehicleSpec playerSpec, rivalSpec;
    private final CarPhysics player, rival;
    private final VehicleModel playerModel, rivalModel;
    private int cameraMode=0;
    private boolean gas,brake,clutch,nitro;
    private boolean started,finished,resultPaid;
    private boolean won;
    private float countdown=3f, elapsed;
    private float playerX=-1.65f, rivalX=1.65f;

    RaceScreen(StreetDragGame game) {
        super(game);
        world=new World3D(false);
        playerSpec=game.profile.car();
        rivalSpec=VehicleSpec.ALL[(game.profile.selected+1)%VehicleSpec.ALL.length];
        player=playerSpec.newPhysics();
        rival=rivalSpec.newPhysics();
        player.setEngineMultiplier(game.profile.engineMultiplier()*game.profile.turboMultiplier());
        player.setGripMultiplier(game.profile.gripMultiplier());
        player.setGearboxMultiplier(game.profile.gearboxMultiplier());
        rival.setEngineMultiplier(1.03f);
        playerModel=new VehicleModel(playerSpec);
        rivalModel=new VehicleModel(rivalSpec);
        camera.fieldOfView=64f;
        camera.far=850f;
    }

    @Override public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override public boolean touchDown(int sx,int sy,int pointer,int button) {
                Vector2 p=touch(sx,sy);
                handleTouch(p.x,p.y);
                return true;
            }
            @Override public boolean touchUp(int sx,int sy,int pointer,int button) {
                gas=false; brake=false; clutch=false; nitro=false;
                return true;
            }
            @Override public boolean keyDown(int key) {
                if(key==Input.Keys.ESCAPE) { game.showGarage(); return true; }
                if(key==Input.Keys.C) { cameraMode=(cameraMode+1)%4; return true; }
                if(key==Input.Keys.R) { resetRace(); return true; }
                if(finished && key==Input.Keys.ENTER) { resetRace(); return true; }
                if(key==Input.Keys.SPACE) { gas=true; brake=false; }
                if(key==Input.Keys.DOWN) { brake=true; gas=false; }
                if(key==Input.Keys.SHIFT_LEFT || key==Input.Keys.SHIFT_RIGHT) clutch=true;
                if(key==Input.Keys.N) nitro=true;
                if(key==Input.Keys.UP) player.shiftUp();
                if(key==Input.Keys.Z) player.shiftDown();
                return true;
            }
            @Override public boolean keyUp(int key) {
                if(key==Input.Keys.SPACE) gas=false;
                if(key==Input.Keys.DOWN) brake=false;
                if(key==Input.Keys.SHIFT_LEFT || key==Input.Keys.SHIFT_RIGHT) clutch=false;
                if(key==Input.Keys.N) nitro=false;
                return true;
            }
        });
    }

    private void handleTouch(float x,float y) {
        if(finished) {
            if(x<230 && y<160) game.showGarage(); else resetRace();
            return;
        }
        if(x<170 && y>620) { game.showGarage(); return; }
        if(y>535 && x>820) { cameraMode=(cameraMode+1)%4; return; }
        if(y<165) {
            if(x<190) { brake=true; gas=false; }
            else if(x<390) nitro=true;
            else if(x<570) clutch=true;
            else if(x<760) player.shiftDown();
            else if(x<950) player.shiftUp();
            else { gas=true; brake=false; }
        }
    }

    private void resetRace() {
        player.reset(); rival.reset(); gas=brake=clutch=nitro=false;
        started=finished=resultPaid=won=false; countdown=3f; elapsed=0f;
    }

    private void updateRace(float delta) {
        if(finished) return;
        elapsed+=delta;
        if(!started) {
            countdown-=delta;
            if(countdown<=0f) started=true;
            return;
        }
        player.update(delta,gas?1f:0f,brake?1f:0f,clutch?1f:0f,nitro);
        if(rival.getRpm()>6550f) rival.shiftUp();
        if(rival.getRpm()<2100f && rival.getGear()>1 && rival.getSpeedKmh()<30f) rival.shiftDown();
        boolean aiNitro=rival.getDistance()>45f && rival.getDistance()<245f;
        rival.update(delta,.96f,0f,0f,aiNitro);

        if(player.isFinished() || rival.isFinished()) {
            won=player.getDistance()>=rival.getDistance();
            if(!finished) {
                finished=true;
                if(!resultPaid) { game.profile.reward(won?25000:5000); resultPaid=true; }
            }
        }
        if(elapsed>55f) {
            finished=true; won=player.getDistance()>=rival.getDistance();
            if(!resultPaid) { game.profile.reward(won?25000:5000); resultPaid=true; }
        }
    }

    private void updateCamera() {
        float pz=-player.getDistance();
        float rz=-rival.getDistance();
        Vector3 position=new Vector3();
        Vector3 target=new Vector3();
        if(cameraMode==0) { // rear chase
            position.set(playerX,3.2f,pz+8.6f);
            target.set(playerX,.85f,pz-9.0f);
        } else if(cameraMode==1) { // hood
            position.set(playerX,1.55f,pz-1.25f);
            target.set(playerX,1.35f,pz-22f);
        } else if(cameraMode==2) { // side tracking
            position.set(playerX+10f,3.2f,pz+1f);
            target.set(0f,.9f,pz-4f);
        } else { // wide overview, shows both cars
            position.set(0f,10f,pz+17f);
            target.set(0f,.5f,pz-16f);
        }
        camera.position.set(position);
        camera.lookAt(target);
        camera.up.set(Vector3.Y);
        camera.update();
        playerModel.setTransform(playerX,0f,pz,0f);
        rivalModel.setTransform(rivalX,0f,rz,0f);
    }

    @Override void renderModels(ModelBatch batch) {
        playerModel.render(batch,world.environment);
        rivalModel.render(batch,world.environment);
    }

    @Override public void render(float delta) {
        delta=Math.min(delta,.04f);
        updateRace(delta);
        updateCamera();
        clear(.008f,.014f,.025f);
        renderWorld();
        beginHud();
        panel(0,615,1280,105,new Color(.01f,.018f,.03f,.92f));
        panel(0,0,1280,165,new Color(.01f,.018f,.03f,.94f));
        panel(24,520,240,82,new Color(.025f,.04f,.065f,.91f));
        panel(1010,520,245,82,new Color(.025f,.04f,.065f,.91f));
        // progress
        panel(390,665,500,14,new Color(.05f,.07f,.10f,1));
        panel(390,665,500*player.getProgress(),14,new Color(.08f,.82f,.42f,1));
        // RPM bar
        panel(430,580,420,13,new Color(.04f,.05f,.07f,1));
        panel(430,580,420*Math.min(player.getRpm()/7200f,1f),13,
            player.getRpm()>6400f?Color.RED:Color.GREEN);
        Color gasColor=gas?new Color(.1f,.9f,.38f,1):new Color(.07f,.40f,.24f,1);
        button(1000,22,250,115,gasColor);
        button(25,22,165,115,new Color(.55f,.08f,.10f,1));
        button(210,22,170,115,new Color(.06f,.25f,.58f,1));
        button(395,22,165,115,new Color(.18f,.20f,.25f,1));
        button(585,22,170,115,new Color(.13f,.17f,.24f,1));
        button(780,22,170,115,new Color(.13f,.17f,.24f,1));

        beginText();
        text("STREET DRAG  /  402 M",34,685,1.55f,Color.WHITE);
        text("YOU: "+playerSpec.name,34,650,.95f,Color.CYAN);
        text("RIVAL: "+rivalSpec.name,970,650,.88f,Color.ORANGE);
        text("$ "+game.profile.money,1080,685,1f,Color.GREEN);
        text("SPEED  "+(int)player.getSpeedKmh()+" KM/H",38,575,1.2f,Color.WHITE);
        text("RPM  "+(int)player.getRpm(),455,550,1.05f,Color.WHITE);
        text("GEAR  "+player.getGear(),890,575,1.1f,Color.WHITE);
        text("DIST  "+(int)player.getDistance()+" / 402 M",890,545,.9f,Color.LIGHT_GRAY);
        text("CAMERA  [C]",1050,575,.92f,Color.CYAN);
        String[] cameraNames={"CHASE","HOOD","SIDE","WIDE"};
        text("VIEW: "+cameraNames[cameraMode],1050,545,.92f,Color.WHITE);
        text("BRAKE",65,80,1.0f,Color.WHITE);
        text("NITRO "+(int)player.getNitro()+"%",245,80,.95f,Color.WHITE);
        text("CLUTCH",430,80,.95f,Color.WHITE);
        text("DOWN",635,80,.95f,Color.WHITE);
        text("UP",840,80,.95f,Color.WHITE);
        text("GAS",1080,80,1.25f,Color.WHITE);
        if(!started && !finished) text(String.valueOf(Math.max(1,(int)Math.ceil(countdown))),620,410,4f,Color.YELLOW);
        if(started && !finished) text("RACE LIVE",565,470,.9f,Color.GREEN);
        if(finished) {
            text(won?"YOU WIN!":"YOU LOSE!",500,455,2.8f,won?Color.GREEN:Color.RED);
            text(won?"REWARD: $25 000":"REWARD: $5 000",505,415,1.25f,Color.WHITE);
            text("TAP TO RACE AGAIN  /  BACK TO GARAGE",395,180,1f,Color.YELLOW);
        }
        text("HOLD SPACE: GAS   UP: SHIFT   Z: DOWN   N: NITRO   C: CAMERA",320,178,.78f,Color.LIGHT_GRAY);
        endText();
    }

    @Override public void dispose() {
        playerModel.dispose(); rivalModel.dispose(); super.dispose();
    }
}
