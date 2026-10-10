package dev.casz.aquarium;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Verifies fake enclosure leaks vanish without altering live water mechanics. */
public final class ContainedWaterEffectsGameTests {
    @GameTest
    public void sealedTubeStillSupportsAquaticResidentsButNeverDrips(GameTestHelper h) {
        h.setBlock(1,1,1,AquariumMod.TUBE);
        h.setBlock(2,1,1,AquariumMod.TANK);
        var level=h.getLevel();
        var tube=h.absolutePos(new BlockPos(1,1,1));
        var water=level.getFluidState(tube);
        h.assertTrue(!water.isEmpty()&&water.getType()==AquariumMod.WATER,
            "Fish must still see the original contained water inside swim tubes");
        h.assertTrue(water.getHeight(level,tube)==1.0f,
            "Keep full-height water for fish oxygen and swimming");
        h.assertTrue(water.getDripParticle()==null,
            "Sealed tubes must not emit vanilla dripping-water particles");
        var network=Network.scan(level,tube);
        h.assertTrue(network.cells().size()==2&&network.tanks()==1,
            "Sealing the effect must not change aquarium connectivity or capacity");
        h.succeed();
    }

    @GameTest
    public void nearbyMobBrushesSealedPipeWithoutFalseSplash(GameTestHelper h) {
        h.setBlock(1,1,1,AquariumMod.TUBE);
        var p=h.absolutePos(new BlockPos(1,1,1));
        var level=h.getLevel();
        // An external mob's hitbox overlaps a sealed pipe but its body center
        // is outside the fluid block (the reported spurious-splash case).
        Vec3 outside=new Vec3(p.getX()-.13,p.getY()+.45,p.getZ()+.5);
        AABB touching=new AABB(p.getX()-.4,p.getY()+.10,p.getZ()+.20,
                              p.getX()+.2,p.getY()+.85,p.getZ()+.80);
        h.assertTrue(AquariumWaterEffects.shouldSuppressSplash(level,touching,outside),
            "An exterior collision with sealed water should not produce a splash");
        h.assertTrue(!AquariumWaterEffects.shouldSuppressSplash(level,touching,
            new Vec3(p.getX()+.15,p.getY()+.45,p.getZ()+.5)),
            "Never suppress splashes for actual inhabitants inside the tank");
        h.assertTrue(!AquariumWaterEffects.shouldSuppressSplash(level,
            new AABB(p.getX()-2,p.getY()+.1,p.getZ(),p.getX()-.5,p.getY()+.8,p.getZ()+.7),
            outside),"No fluid contact must mean no suppression");

        // Touching real vanilla water takes precedence, even while brushing
        // the edge of a tube. This ensures ocean/pool effects remain vanilla.
        h.setBlock(0,1,1,Blocks.WATER);
        h.assertTrue(!AquariumWaterEffects.shouldSuppressSplash(level,touching,outside),
            "Ordinary water must retain all normal entry splashes");
        h.succeed();
    }
}
