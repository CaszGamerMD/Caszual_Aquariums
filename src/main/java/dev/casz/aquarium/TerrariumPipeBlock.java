package dev.casz.aquarium;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.*;
import net.minecraft.world.level.pathfinder.PathComputationType;
public final class TerrariumPipeBlock extends TubeBlock {
 public final boolean hostile;
 public TerrariumPipeBlock(Properties p,boolean hostile){super(p);this.hostile=hostile;}
 protected FluidState getFluidState(BlockState s){return Fluids.EMPTY.defaultFluidState();}
 protected boolean isPathfindable(BlockState s,PathComputationType type){return type==PathComputationType.LAND;}
}