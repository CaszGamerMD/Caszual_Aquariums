package dev.casz.aquarium;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.*;
/** Real, water-tagged contained fluid. Rendering is handled by the aquarium mesh. */
public final class ContainedWaterFluid extends WaterFluid.Source {
 @Override public Fluid getSource(){return this;}
 @Override public Fluid getFlowing(){return this;}
 @Override public boolean isSame(Fluid fluid){return fluid==this;}
 @Override public float getHeight(FluidState state,BlockGetter level,BlockPos pos){return 1.0f;}
 @Override public float getOwnHeight(FluidState state){return 1.0f;}
 @Override public net.minecraft.world.level.block.state.BlockState createLegacyBlock(FluidState state){return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();}
 @Override public void animateTick(net.minecraft.world.level.Level level,BlockPos pos,FluidState state,net.minecraft.util.RandomSource random){}
 @Override public void tick(ServerLevel level,BlockPos pos,net.minecraft.world.level.block.state.BlockState block,FluidState state){}
}