package dev.casz.aquarium;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Pure, bounded spatial check for water-splash visuals at enclosed tanks/pipes.
 * The contained fluid remains real to resident fish, which still breathe,
 * swim, navigate pipes and are captured with buckets as before.
 */
public final class AquariumWaterEffects {
    private AquariumWaterEffects() {}

    /**
     * Suppress an entry splash only for an entity standing OUTSIDE a sealed
     * aquarium/tube while the edge of its bounding box brushes the contained
     * fluid. Do not suppress splashes from ordinary world water, mixed
     * ordinary/contained water, or entities actually inside an enclosure.
     */
    public static boolean shouldSuppressSplash(BlockGetter level,AABB bounds,Vec3 entityPosition) {
        if (Enclosures.isAquatic(level.getBlockState(BlockPos.containing(entityPosition))))
            return false;

        // The check only runs when vanilla attempts a water-entry splash;
        // remain inexpensive even with unusually oversized modded entities.
        if(bounds.getXsize()>8||bounds.getYsize()>8||bounds.getZsize()>8)
            return false;

        boolean touchesContained=false;
        int minX=(int)Math.floor(bounds.minX+1.0E-7);
        int minY=(int)Math.floor(bounds.minY+1.0E-7);
        int minZ=(int)Math.floor(bounds.minZ+1.0E-7);
        int maxX=(int)Math.floor(bounds.maxX-1.0E-7);
        int maxY=(int)Math.floor(bounds.maxY-1.0E-7);
        int maxZ=(int)Math.floor(bounds.maxZ-1.0E-7);
        for(BlockPos position:BlockPos.betweenClosed(minX,minY,minZ,maxX,maxY,maxZ)) {
            var fluid=level.getFluidState(position);
            if(fluid.isEmpty())continue;
            // An ordinary liquid is enough to keep vanilla entry effects;
            // never globally disable splashes from oceans, rivers or rain.
            if(fluid.getType()!=AquariumMod.WATER)return false;
            touchesContained=true;
        }
        return touchesContained;
    }
}
