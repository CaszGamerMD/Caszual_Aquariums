package dev.casz.aquarium;
import com.mojang.serialization.MapCodec;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
public class TankBlock extends AquariumBlock implements EntityBlock {
 public static final net.minecraft.world.level.block.state.properties.BooleanProperty[] TUBE_LINKS=java.util.Arrays.stream(new String[]{"down","up","north","south","west","east"}).map(d->net.minecraft.world.level.block.state.properties.BooleanProperty.create("tube_"+d)).toArray(net.minecraft.world.level.block.state.properties.BooleanProperty[]::new);
 public static final MapCodec<TankBlock> CODEC=simpleCodec(TankBlock::new);
 public TankBlock(Properties p){super(p);}
 protected MapCodec<? extends net.minecraft.world.level.block.Block> codec(){return CODEC;}
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new TankBlockEntity(p,s);}
 public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){
  if(level.isClientSide()||type!=AquariumMod.TANK_ENTITY)return null;
  return (l,p,s,be)->((TankBlockEntity)be).tick();
 }
 protected List<ItemStack> getDrops(BlockState state,LootParams.Builder params){
  List<ItemStack> drops=new ArrayList<>(super.getDrops(state,params));
  if(params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof TankBlockEntity be && !be.decoration.isEmpty())drops.add(be.decoration.copy());
  return drops;
 }
}
