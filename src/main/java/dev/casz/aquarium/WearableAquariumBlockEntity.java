package dev.casz.aquarium;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;

public final class WearableAquariumBlockEntity extends BlockEntity {
 private final List<ItemStack> saved=new ArrayList<>();
 private int revision;
 public WearableAquariumBlockEntity(BlockPos p,BlockState s){super(AquariumMod.WEARABLE_AQUARIUM_ENTITY,p,s);}
 public int revision(){return revision;}
 public List<ItemStack> contents(){return saved.stream().map(ItemStack::copy).toList();}
 public int usedSlots(){return WearableAquariumItem.usedSlots(saved);}
 public boolean insert(ItemStack source){
  int cost=WearableAquariumItem.cost(source);
  if(cost==0||usedSlots()+cost>WearableAquariumItem.CAPACITY)return false;
  saved.add(source.copyWithCount(1));changed();return true;
 }
 public ItemStack take(boolean guardian){
  for(int i=saved.size()-1;i>=0;i--)if(guardian==WearableAquariumItem.isGuardianNet(saved.get(i))){
   var result=saved.remove(i);changed();return result;
  }
  return ItemStack.EMPTY;
 }
 public ItemStack asItem(){
  var item=new ItemStack(AquariumMod.WEARABLE_AQUARIUM);
  WearableAquariumItem.setContents(item,saved);return item;
 }
 public void fromItem(ItemStack item){saved.clear();saved.addAll(WearableAquariumItem.contents(item));changed();}
 private void changed(){
  revision++;setChanged();
  if(level instanceof ServerLevel l&&l.getBlockEntity(worldPosition)==this)l.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
 }
 protected void saveAdditional(ValueOutput out){super.saveAdditional(out);out.store("wearable_item",ItemStack.CODEC,asItem());}
 protected void loadAdditional(ValueInput in){
  super.loadAdditional(in);saved.clear();
  in.read("wearable_item",ItemStack.CODEC).ifPresent(item->saved.addAll(WearableAquariumItem.contents(item)));
  revision++;
 }
 public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
 public CompoundTag getUpdateTag(HolderLookup.Provider provider){return saveCustomOnly(provider);}
}
