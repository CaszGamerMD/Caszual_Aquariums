package dev.casz.aquarium;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.item.*;

public final class TropicalFishEditorGameTests {
 @GameTest public void editorStoresAndChangesVanillaFishVariant(GameTestHelper h){
  h.setBlock(1,1,1,AquariumMod.FISH_EDITOR);BlockPos pos=h.absolutePos(new BlockPos(1,1,1));var be=(TropicalFishEditorBlockEntity)h.getLevel().getBlockEntity(pos);
  ItemStack fish=new ItemStack(Items.TROPICAL_FISH_BUCKET);fish.set(DataComponents.TROPICAL_FISH_PATTERN,TropicalFish.Pattern.SPOTTY);fish.set(DataComponents.TROPICAL_FISH_BASE_COLOR,DyeColor.BLUE);fish.set(DataComponents.TROPICAL_FISH_PATTERN_COLOR,DyeColor.YELLOW);
  h.assertTrue(be.insert(fish),"Editor must accept a tropical fish bucket");h.assertTrue(be.pattern()==TropicalFish.Pattern.SPOTTY&&be.baseColor()==DyeColor.BLUE&&be.patternColor()==DyeColor.YELLOW,"Editor must preserve the inserted fish variant");
  be.setPattern(TropicalFish.Pattern.BLOCKFISH.ordinal());be.setBaseColor(DyeColor.RED.getId());be.setPatternColor(DyeColor.WHITE.getId());ItemStack edited=be.take();
  h.assertTrue(edited.getOrDefault(DataComponents.TROPICAL_FISH_PATTERN,TropicalFish.DEFAULT_VARIANT.pattern())==TropicalFish.Pattern.BLOCKFISH,"Edited bucket must keep the selected pattern");
  h.assertTrue(edited.getOrDefault(DataComponents.TROPICAL_FISH_BASE_COLOR,TropicalFish.DEFAULT_VARIANT.baseColor())==DyeColor.RED,"Edited bucket must keep the selected body color");
  h.assertTrue(edited.getOrDefault(DataComponents.TROPICAL_FISH_PATTERN_COLOR,TropicalFish.DEFAULT_VARIANT.patternColor())==DyeColor.WHITE,"Edited bucket must keep the selected pattern color");h.succeed();
 }
 @GameTest public void editorRejectsSecondFishAndNonTropicalBuckets(GameTestHelper h){
  h.setBlock(1,1,1,AquariumMod.FISH_EDITOR);var be=(TropicalFishEditorBlockEntity)h.getLevel().getBlockEntity(h.absolutePos(new BlockPos(1,1,1)));
  h.assertTrue(!be.insert(new ItemStack(Items.COD_BUCKET)),"Editor must reject non-tropical fish buckets");h.assertTrue(be.insert(new ItemStack(Items.TROPICAL_FISH_BUCKET)),"Editor must accept its first tropical fish");h.assertTrue(!be.insert(new ItemStack(Items.TROPICAL_FISH_BUCKET)),"Editor must only hold one fish");h.succeed();
 }
}
