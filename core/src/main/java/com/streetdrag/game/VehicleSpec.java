package com.streetdrag.game;

import com.badlogic.gdx.graphics.Color;

/** Data-only vehicle catalogue. Values are gameplay estimates, not manufacturer claims. */
public final class VehicleSpec {
    public final String id, name;
    public final float massKg, powerHp, torqueNm, dragArea;
    public final boolean awd;
    public final Color paint;

    public VehicleSpec(String id, String name, float massKg, float powerHp, float torqueNm,
                       float dragArea, boolean awd, Color paint) {
        this.id=id; this.name=name; this.massKg=massKg; this.powerHp=powerHp;
        this.torqueNm=torqueNm; this.dragArea=dragArea; this.awd=awd; this.paint=new Color(paint);
    }

    public CarPhysics newPhysics() {
        return new CarPhysics(massKg, powerHp, torqueNm, dragArea, awd);
    }

    public static final VehicleSpec[] ALL = {
        new VehicleSpec("audi_rs6", "AUDI RS6 AVANT GT", 2100, 630, 850, .85f, true, new Color(.24f,.27f,.31f,1)),
        new VehicleSpec("bmw_m3", "BMW M3 CS TOURING", 1780, 550, 650, .82f, true, new Color(.035f,.16f,.78f,1)),
        new VehicleSpec("amg_e63", "MERCEDES-AMG E63 S", 1950, 612, 850, .90f, true, new Color(.08f,.09f,.11f,1)),
        new VehicleSpec("panamera", "PORSCHE PANAMERA TURBO", 2000, 630, 820, .78f, true, new Color(.65f,.08f,.08f,1)),
        new VehicleSpec("wrx_sti", "SUBARU WRX STI", 1500, 310, 400, .88f, true, new Color(.04f,.20f,.73f,1)),
        new VehicleSpec("evo_ix", "MITSUBISHI EVO IX", 1450, 350, 430, .86f, true, new Color(.78f,.50f,.07f,1)),
        new VehicleSpec("gtr_r35", "NISSAN GT-R R35", 1740, 600, 650, .75f, true, new Color(.045f,.05f,.06f,1)),
        new VehicleSpec("supra_mk5", "TOYOTA SUPRA MK5", 1520, 500, 650, .72f, false, new Color(.80f,.81f,.83f,1)),
        new VehicleSpec("911_turbo", "PORSCHE 911 TURBO S", 1640, 650, 800, .70f, true, new Color(.78f,.78f,.72f,1)),
        new VehicleSpec("silvia_s15", "NISSAN SILVIA S15", 1250, 350, 420, .80f, false, new Color(.84f,.84f,.88f,1)),
        new VehicleSpec("bmw_m5", "BMW M5 F90", 1930, 625, 750, .88f, true, new Color(.08f,.12f,.18f,1)),
        new VehicleSpec("stagea", "NISSAN STAGEA 260RS", 1720, 280, 370, .92f, true, new Color(.15f,.18f,.22f,1)),
        new VehicleSpec("audi_rs7", "AUDI RS7", 2050, 630, 850, .83f, true, new Color(.12f,.13f,.15f,1)),
        new VehicleSpec("amg_gtr", "MERCEDES-AMG GT R", 1630, 585, 700, .74f, false, new Color(.06f,.38f,.20f,1)),
        // Additional generic catalogue entries expose the remaining two GLB assets.
        new VehicleSpec("luxury_suv", "LUXURY SUV", 2250, 520, 750, .94f, true, new Color(.12f,.15f,.19f,1)),
        new VehicleSpec("retro_city", "RETRO CITY TURBO", 1100, 180, 250, .78f, false, new Color(.63f,.14f,.10f,1))
    };
}
