package dev.casz.aquarium;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.item.*;
import net.minecraft.world.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;
import net.minecraft.world.entity.item.ItemEntity;

public final class TropicalFishEditorGameTests {
 private InteractionResult click(GameTestHelper h,net.minecraft.world.entity.player.Player p,BlockPos relative){BlockPos pos=h.absolutePos(relative);return net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.invoker().interact(p,h.getLevel(),InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));}
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
 @GameTest public void bucketInteractionRoundTripKeepsEdits(GameTestHelper h){
  h.setBlock(1,1,1,AquariumMod.FISH_EDITOR);BlockPos pos=h.absolutePos(new BlockPos(1,1,1));var player=h.makeMockPlayer(GameType.SURVIVAL);player.setPos(Vec3.atCenterOf(pos));
  ItemStack input=new ItemStack(Items.TROPICAL_FISH_BUCKET);input.set(DataComponents.TROPICAL_FISH_PATTERN,TropicalFish.Pattern.KOB);input.set(DataComponents.TROPICAL_FISH_BASE_COLOR,DyeColor.CYAN);input.set(DataComponents.TROPICAL_FISH_PATTERN_COLOR,DyeColor.PINK);player.setItemInHand(InteractionHand.MAIN_HAND,input);
  h.assertTrue(click(h,player,new BlockPos(1,1,1)).consumesAction(),"Fish bucket insertion must be handled");h.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.BUCKET),"Inserting a fish must return the empty bucket");
  var menu=new TropicalFishEditorMenu(7,player.getInventory(),h.getLevel(),pos);h.assertTrue(menu.clickMenuButton(player,TropicalFish.Pattern.GLITTER.ordinal()),"Pattern edit must succeed");h.assertTrue(menu.clickMenuButton(player,100+DyeColor.LIME.getId()),"Body color edit must succeed");h.assertTrue(menu.clickMenuButton(player,120+DyeColor.BLUE.getId()),"Pattern color edit must succeed");
  h.assertTrue(click(h,player,new BlockPos(1,1,1)).consumesAction(),"Empty bucket retrieval must be handled");ItemStack output=player.getItemInHand(InteractionHand.MAIN_HAND);
  h.assertTrue(output.is(Items.TROPICAL_FISH_BUCKET),"Retrieval must return a tropical fish bucket");h.assertTrue(output.getOrDefault(DataComponents.TROPICAL_FISH_PATTERN,TropicalFish.DEFAULT_VARIANT.pattern())==TropicalFish.Pattern.GLITTER,"Retrieved fish must keep edited pattern");h.assertTrue(output.getOrDefault(DataComponents.TROPICAL_FISH_BASE_COLOR,TropicalFish.DEFAULT_VARIANT.baseColor())==DyeColor.LIME,"Retrieved fish must keep edited body color");h.assertTrue(output.getOrDefault(DataComponents.TROPICAL_FISH_PATTERN_COLOR,TropicalFish.DEFAULT_VARIANT.patternColor())==DyeColor.BLUE,"Retrieved fish must keep edited pattern color");h.succeed();
 }
 @GameTest public void breakingEditorReturnsContainedFish(GameTestHelper h){
  h.setBlock(1,1,1,AquariumMod.FISH_EDITOR);BlockPos pos=h.absolutePos(new BlockPos(1,1,1));var be=(TropicalFishEditorBlockEntity)h.getLevel().getBlockEntity(pos);ItemStack fish=new ItemStack(Items.TROPICAL_FISH_BUCKET);fish.set(DataComponents.TROPICAL_FISH_BASE_COLOR,DyeColor.PURPLE);be.insert(fish);
  var player=h.makeMockPlayer(GameType.CREATIVE);player.setPos(Vec3.atCenterOf(pos));AquariumMod.FISH_EDITOR.playerWillDestroy(h.getLevel(),pos,h.getLevel().getBlockState(pos),player);h.getLevel().removeBlock(pos,false);
  var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(2),e->true);h.assertTrue(drops.stream().filter(e->e.getItem().is(AquariumMod.FISH_EDITOR_ITEM)).count()==1,"Creative break must return exactly one editor");var fishDrop=drops.stream().filter(e->e.getItem().is(Items.TROPICAL_FISH_BUCKET)).findFirst().orElse(null);h.assertTrue(fishDrop!=null&&fishDrop.getItem().getOrDefault(DataComponents.TROPICAL_FISH_BASE_COLOR,TropicalFish.DEFAULT_VARIANT.baseColor())==DyeColor.PURPLE,"Creative break must return the stored fish with its data");h.succeed();
 }
}
