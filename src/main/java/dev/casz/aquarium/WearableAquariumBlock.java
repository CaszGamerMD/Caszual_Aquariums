package dev.casz.aquarium;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public final class WearableAquariumBlock extends BaseEntityBlock {
 public static final MapCodec<WearableAquariumBlock> CODEC=simpleCodec(WearableAquariumBlock::new);
 public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
 public WearableAquariumBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
 protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
 protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
 public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
 public RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new WearableAquariumBlockEntity(p,s);}
 public void setPlacedBy(Level l,BlockPos p,BlockState s,LivingEntity entity,ItemStack stack){
  super.setPlacedBy(l,p,s,entity,stack);if(l.getBlockEntity(p) instanceof WearableAquariumBlockEntity be)be.fromItem(stack);
 }
 public BlockState playerWillDestroy(Level l,BlockPos p,BlockState s,Player player){
  if(!l.isClientSide()&&player.getAbilities().instabuild&&l.getBlockEntity(p) instanceof WearableAquariumBlockEntity be)player.spawnAtLocation((ServerLevel)l,be.asItem());
  return super.playerWillDestroy(l,p,s,player);
 }
 public java.util.List<ItemStack> getDrops(BlockState s,net.minecraft.world.level.storage.loot.LootParams.Builder params){
  if(params.getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY) instanceof WearableAquariumBlockEntity be)return java.util.List.of(be.asItem());
  return java.util.List.of(new ItemStack(AquariumMod.WEARABLE_AQUARIUM));
 }
}
