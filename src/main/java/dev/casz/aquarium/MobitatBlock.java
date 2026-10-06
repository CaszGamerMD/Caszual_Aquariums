package dev.casz.aquarium;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
public final class MobitatBlock extends BaseEntityBlock {
 public static final MapCodec<MobitatBlock> CODEC=simpleCodec(MobitatBlock::new);
 public MobitatBlock(Properties p){super(p);} protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new MobitatBlockEntity(p,s);}
 public void setPlacedBy(Level l,BlockPos p,BlockState s,LivingEntity placer,ItemStack stack){super.setPlacedBy(l,p,s,placer,stack);if(l.getBlockEntity(p) instanceof MobitatBlockEntity be)be.fromItem(stack);}
 public BlockState playerWillDestroy(Level l,BlockPos p,BlockState s,Player player){if(!l.isClientSide()&&player.getAbilities().instabuild&&l.getBlockEntity(p) instanceof MobitatBlockEntity be)player.spawnAtLocation((ServerLevel)l,be.asItem());return super.playerWillDestroy(l,p,s,player);}
 public java.util.List<ItemStack> getDrops(BlockState s,net.minecraft.world.level.storage.loot.LootParams.Builder b){if(b.getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY) instanceof MobitatBlockEntity be)return java.util.List.of(be.asItem());return java.util.List.of(new ItemStack(AquariumMod.MOBITAT_ITEM));}
}