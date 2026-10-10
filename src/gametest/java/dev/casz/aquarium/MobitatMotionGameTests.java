package dev.casz.aquarium;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * Prevents regressions to stationary, spazzing 10%-sized Mobitat previews.
 * Motion is deterministic and bounded, so tests need no live AI entities.
 */
public final class MobitatMotionGameTests {
    @GameTest
    public void miniaturesWanderSmoothlyAtScaledSpeed(GameTestHelper h){
        long seed=0x5e3a0abbbL;
        for(int slot=0;slot<5;slot++){
            boolean flying=slot%2==0;
            float previousX=Float.NaN,previousZ=Float.NaN;
            double traveled=0;
            for(int tick=0;tick<=2400;tick++){
                var p=MobitatPreviewMovement.sample(tick,slot,seed,flying,1.4f,1.9f);
                h.assertTrue(p.x()>.17f&&p.x()<.83f
                    &&p.z()>.17f&&p.z()<.83f,
                    "Miniature must stay inside the Mobitat glass footprint");
                h.assertTrue(p.y()>=.19f&&p.y()<.81f,
                    "Miniature must stay inside the Mobitat vertical bounds");
                h.assertTrue(p.horizontalSpeed()<=.009f,
                    "Miniature movement must be slowed to match its 10% size");
                if(tick>0){
                    double step=Math.hypot(p.x()-previousX,p.z()-previousZ);
                    h.assertTrue(step<=.0091,
                        "Miniature cannot teleport or jump between stationary spots");
                    traveled+=step;
                }
                previousX=p.x();previousZ=p.z();
            }
            h.assertTrue(traveled>3.5,
                "Every resident must explore the Mobitat interior instead of staying stuck");
        }
        h.succeed();
    }

    @GameTest
    public void motionStableAcrossFramesReloadsAndMobSizes(GameTestHelper h) {
        long seed=0x6a88bb44L;
        for(int slot=0;slot<5;slot++){
            var first=MobitatPreviewMovement.sample(500.25,slot,seed,false,1f,1.4f);
            var repeat=MobitatPreviewMovement.sample(500.25,slot,seed,false,1f,1.4f);
            h.assertTrue(first.equals(repeat),"Motion after reloading must be deterministic");
            var halfway=MobitatPreviewMovement.sample(500.75,slot,seed,false,1f,1.4f);
            h.assertTrue(Math.hypot(first.x()-halfway.x(),first.z()-halfway.z())<.005,
                "Partial tick interpolation must be continuous");
            var giant=MobitatPreviewMovement.sample(500.25,slot,seed,false,5f,4f);
            h.assertTrue(giant.x()>=.35f&&giant.x()<=.65f&&giant.z()>=.35f&&giant.z()<=.65f,
                "Large mob previews must roam in a smaller area to avoid clipping");
            var ground=MobitatPreviewMovement.sample(500.25,slot,seed,false,1f,1.4f);
            h.assertTrue(Math.abs(ground.y()-.20f)<.0001f,
                "Walking mobs should remain on the Mobitat floor");
        }
        h.succeed();
    }
}
