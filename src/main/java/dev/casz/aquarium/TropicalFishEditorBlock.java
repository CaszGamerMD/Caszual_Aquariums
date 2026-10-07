package dev.casz.aquarium;
import com.mojang.serialization.MapCodec;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public final class TropicalFishEditorBlock extends BaseEntityBlock {
 public static final MapCodec<TropicalFishEditorBlock> CODEC=simpleCodec(TropicalFishEditorBlock::new);
 public TropicalFishEditorBlock(Properties properties){super(properties);}
 protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
 public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new TropicalFishEditorBlockEntity(pos,state);}
 public BlockState playerWillDestroy(Level level,BlockPos pos,BlockState state,Player player){
  if(!level.isClientSide()&&player.getAbilities().instabuild){
   player.spawnAtLocation((ServerLevel)level,new ItemStack(AquariumMod.FISH_EDITOR));
   if(level.getBlockEntity(pos) instanceof TropicalFishEditorBlockEntity be&&be.occupied())player.spawnAtLocation((ServerLevel)level,be.fish());
  }
  return super.playerWillDestroy(level,pos,state,player);
 }
 protected List<ItemStack> getDrops(BlockState state,LootParams.Builder params){
  List<ItemStack> drops=new ArrayList<>();drops.add(new ItemStack(AquariumMod.FISH_EDITOR));
  if(params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof TropicalFishEditorBlockEntity be&&be.occupied())drops.add(be.fish());
  return drops;
 }
}
