package dev.casz.aquarium;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;

public final class WearableAquariumGameTests {
 private static ItemStack guardian(){
  ItemStack net=new ItemStack(AquariumMod.MOB_NET);CompoundTag tag=new CompoundTag();
  tag.putString("terrarium_type","minecraft:guardian");tag.put("terrarium_entity",new CompoundTag());
  net.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));return net;
 }
 @GameTest public void placeFillAndPickUpRetainsFishAndGuardian(GameTestHelper h){
  h.setBlock(1,1,1,AquariumMod.WEARABLE_AQUARIUM_BLOCK);
  var be=(WearableAquariumBlockEntity)h.getLevel().getBlockEntity(h.absolutePos(new BlockPos(1,1,1)));
  ItemStack tropical=new ItemStack(Items.TROPICAL_FISH_BUCKET);
  tropical.set(DataComponents.TROPICAL_FISH_BASE_COLOR,DyeColor.BLUE);
  tropical.set(DataComponents.TROPICAL_FISH_PATTERN_COLOR,DyeColor.YELLOW);
  h.assertTrue(be.insert(tropical),"Must accept a tropical fish");
  h.assertTrue(be.insert(guardian()),"Must accept guardian from Mob Net");
  h.assertTrue(be.usedSlots()==3,"A guardian must consume exactly two slots");
  h.assertTrue(be.insert(new ItemStack(Items.COD_BUCKET)),"Last free slot must accept one fish");
  h.assertTrue(!be.insert(new ItemStack(Items.SALMON_BUCKET))&&!be.insert(guardian()),"Overfilling 4 slots must fail");
  ItemStack dropped=be.asItem();
  var restored=new WearableAquariumBlockEntity(BlockPos.ZERO,AquariumMod.WEARABLE_AQUARIUM_BLOCK.defaultBlockState());
  restored.fromItem(dropped);
  h.assertTrue(restored.usedSlots()==4&&restored.contents().size()==3,"Pick-up and placement must preserve all contents");
  ItemStack removed=restored.take(true);
  h.assertTrue(WearableAquariumItem.isGuardianNet(removed)&&restored.usedSlots()==2,"Guardian retrieval must preserve Mob Net data and free two slots");
  h.assertTrue(restored.take(false).is(Items.COD_BUCKET),"An empty bucket removes the latest fish");
  var saved=restored.take(false);
  h.assertTrue(saved.is(Items.TROPICAL_FISH_BUCKET)&&saved.get(DataComponents.TROPICAL_FISH_BASE_COLOR)==DyeColor.BLUE
   &&saved.get(DataComponents.TROPICAL_FISH_PATTERN_COLOR)==DyeColor.YELLOW,"Tropical colors survive a placed tank and retrieval");
  h.succeed();
 }
 @GameTest public void invalidGuardianAndOversizedLegacyDataAreRejected(GameTestHelper h){
  ItemStack emptyNet=new ItemStack(AquariumMod.MOB_NET);
  h.assertTrue(!WearableAquariumItem.isGuardianNet(emptyNet),"Empty nets are not residents");
  var turtle=guardian();var data=turtle.get(DataComponents.CUSTOM_DATA).copyTag();data.putString("terrarium_type","minecraft:turtle");
  turtle.set(DataComponents.CUSTOM_DATA,CustomData.of(data));
  h.assertTrue(!WearableAquariumItem.valid(turtle),"Other mob nets cannot be stored");
  var item=new ItemStack(AquariumMod.WEARABLE_AQUARIUM);
  WearableAquariumItem.setContents(item,java.util.List.of(guardian(),new ItemStack(Items.COD_BUCKET),
   new ItemStack(Items.SALMON_BUCKET),new ItemStack(Items.PUFFERFISH_BUCKET),turtle));
  h.assertTrue(WearableAquariumItem.usedSlots(WearableAquariumItem.contents(item))==4
   &&WearableAquariumItem.contents(item).size()==3,"Contents must be clamped to the weighted capacity");
  h.succeed();
 }
}
