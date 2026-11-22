package com.streetdrag.game;

import org.junit.Test;
import static org.junit.Assert.*;

public class CarPhysicsTest {
    @Test
    public void configuredGearRatiosAreApplied() {
        CarPhysics car = new CarPhysics(1500f, 500f, 650f, .78f, true);
        float[] ratios = {3.45f, 2.40f, 1.78f, 1.35f, 1.08f, .88f};
        car.setGearRatios(ratios);

        for (int i = 0; i < ratios.length; i++) {
            assertEquals(ratios[i], car.getGearRatio(i), .0001f);
        }
        assertTrue(car.shiftUp());
        assertEquals(2, car.getGear());
    }

    @Test
    public void carCanCompleteAnEightHundredAndFourMetreRace() {
        CarPhysics car = new CarPhysics(1450f, 620f, 760f, .74f, true);
        car.setFinishDistance(804f);
        car.setEngineMultiplier(1.8f);
        car.setGearboxMultiplier(1.6f);

        for (int i = 0; i < 30000 && !car.isFinished(); i++) {
            if (car.getRpm() >= 6200f) car.shiftUp();
            car.update(.04f, 1f, 0f, 0f, false);
        }

        assertTrue("Car should cross the 804 m finish line", car.isFinished());
        assertEquals(804f, car.getDistance(), .001f);
        assertEquals(1f, car.getProgress(), .001f);
    }

    @Test
    public void resetRestoresStartLineForTheSelectedDistance() {
        CarPhysics car = new CarPhysics(1500f, 500f, 650f, .78f, true);
        car.setFinishDistance(804f);
        for (int i = 0; i < 100; i++) car.update(.04f, 1f, 0f, 0f, false);

        car.reset();

        assertEquals(0f, car.getDistance(), .0001f);
        assertEquals(0f, car.getSpeedMps(), .0001f);
        assertEquals(0f, car.getProgress(), .0001f);
        assertEquals(1, car.getGear());
        assertEquals(100f, car.getNitro(), .0001f);
    }
}
