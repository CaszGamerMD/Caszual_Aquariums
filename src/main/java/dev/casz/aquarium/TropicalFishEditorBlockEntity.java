package dev.casz.aquarium;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;

public final class TropicalFishEditorBlockEntity extends BlockEntity {
 private ItemStack fish=ItemStack.EMPTY;
 private int revision;
 public TropicalFishEditorBlockEntity(BlockPos pos,BlockState state){super(AquariumMod.FISH_EDITOR_ENTITY,pos,state);}
 public boolean occupied(){return !fish.isEmpty();}
 public int revision(){return revision;}
 public ItemStack fish(){return fish.copy();}
 public TropicalFish.Pattern pattern(){return fish.getOrDefault(DataComponents.TROPICAL_FISH_PATTERN,TropicalFish.DEFAULT_VARIANT.pattern());}
 public DyeColor baseColor(){return fish.getOrDefault(DataComponents.TROPICAL_FISH_BASE_COLOR,TropicalFish.DEFAULT_VARIANT.baseColor());}
 public DyeColor patternColor(){return fish.getOrDefault(DataComponents.TROPICAL_FISH_PATTERN_COLOR,TropicalFish.DEFAULT_VARIANT.patternColor());}
 public boolean insert(ItemStack stack){if(occupied()||!stack.is(Items.TROPICAL_FISH_BUCKET))return false;fish=stack.copyWithCount(1);changed();return true;}
 public ItemStack take(){if(!occupied())return ItemStack.EMPTY;ItemStack out=fish;fish=ItemStack.EMPTY;changed();return out;}
 public boolean setPattern(int index){if(!occupied())return false;var values=TropicalFish.Pattern.values();fish.set(DataComponents.TROPICAL_FISH_PATTERN,values[Math.floorMod(index,values.length)]);changed();return true;}
 public boolean setBaseColor(int id){if(!occupied())return false;fish.set(DataComponents.TROPICAL_FISH_BASE_COLOR,DyeColor.byId(Math.floorMod(id,16)));changed();return true;}
 public boolean setPatternColor(int id){if(!occupied())return false;fish.set(DataComponents.TROPICAL_FISH_PATTERN_COLOR,DyeColor.byId(Math.floorMod(id,16)));changed();return true;}
 public void randomize(){if(!occupied()||level==null)return;setVariant(TropicalFish.Pattern.values()[level.getRandom().nextInt(TropicalFish.Pattern.values().length)],DyeColor.byId(level.getRandom().nextInt(16)),DyeColor.byId(level.getRandom().nextInt(16)));}
 public void swapColors(){if(!occupied())return;DyeColor a=baseColor(),b=patternColor();fish.set(DataComponents.TROPICAL_FISH_BASE_COLOR,b);fish.set(DataComponents.TROPICAL_FISH_PATTERN_COLOR,a);changed();}
 private void setVariant(TropicalFish.Pattern p,DyeColor base,DyeColor pattern){fish.set(DataComponents.TROPICAL_FISH_PATTERN,p);fish.set(DataComponents.TROPICAL_FISH_BASE_COLOR,base);fish.set(DataComponents.TROPICAL_FISH_PATTERN_COLOR,pattern);changed();}
 private void changed(){revision++;setChanged();if(level instanceof ServerLevel server&&server.getBlockEntity(worldPosition)==this)server.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
 protected void saveAdditional(ValueOutput out){super.saveAdditional(out);if(!fish.isEmpty())out.store("fish",ItemStack.CODEC,fish);}
 protected void loadAdditional(ValueInput in){super.loadAdditional(in);fish=in.read("fish",ItemStack.CODEC).orElse(ItemStack.EMPTY);revision++;}
 public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
 public CompoundTag getUpdateTag(HolderLookup.Provider provider){return saveCustomOnly(provider);}
}
