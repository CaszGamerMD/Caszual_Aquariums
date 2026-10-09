package dev.casz.aquarium;

import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;

public final class WearableAquariumItem extends BlockItem {
 public static final int CAPACITY=4;
 public WearableAquariumItem(net.minecraft.world.level.block.Block block,Properties p){super(block,p.stacksTo(1));}
 public static boolean isFishBucket(ItemStack s){return s.is(Items.COD_BUCKET)||s.is(Items.SALMON_BUCKET)||s.is(Items.TROPICAL_FISH_BUCKET)||s.is(Items.PUFFERFISH_BUCKET);}
 public static boolean isGuardianNet(ItemStack s){
  if(!s.is(AquariumMod.MOB_NET))return false;
  return s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getString("terrarium_type").orElse("").equals("minecraft:guardian");
 }
 public static boolean valid(ItemStack s){return isFishBucket(s)||isGuardianNet(s);}
 public static int cost(ItemStack s){return isGuardianNet(s)?2:isFishBucket(s)?1:0;}
 public static int usedSlots(List<ItemStack> contents){int total=0;for(var s:contents)total+=cost(s);return total;}
 public static List<ItemStack> contents(ItemStack aquarium){
  if(!(aquarium.getItem() instanceof WearableAquariumItem))return List.of();
  List<ItemStack> out=new ArrayList<>();int used=0;
  for(var stack:aquarium.getOrDefault(DataComponents.CONTAINER,ItemContainerContents.EMPTY).nonEmptyItemCopyStream().toList()){
   int n=cost(stack);if(n==0||used+n>CAPACITY)continue;out.add(stack.copyWithCount(1));used+=n;
  }
  return out;
 }
 public static void setContents(ItemStack item,List<ItemStack> source){
  if(!(item.getItem() instanceof WearableAquariumItem))return;
  List<ItemStack> out=new ArrayList<>();int used=0;
  for(var s:source){int n=cost(s);if(n==0||used+n>CAPACITY)continue;out.add(s.copyWithCount(1));used+=n;}
  if(out.isEmpty())item.remove(DataComponents.CONTAINER);else item.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(out));
 }
 public static List<ItemStack> fish(ItemStack item){return contents(item).stream().filter(WearableAquariumItem::isFishBucket).toList();}
 public static void setFish(ItemStack item,List<ItemStack> stacks){setContents(item,stacks);}
}
