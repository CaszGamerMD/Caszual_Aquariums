package dev.casz.aquarium;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class QualityGameTests {
 @GameTest public void creativeBreakingMobitatKeepsResident(GameTestHelper h){
  h.setBlock(1,1,1,AquariumMod.MOBITAT);BlockPos pos=h.absolutePos(new BlockPos(1,1,1));var level=h.getLevel();var be=(MobitatBlockEntity)level.getBlockEntity(pos);
  var cow=h.spawn(EntityTypes.COW,3.5f,1.1f,1.5f);var net=Terrestrial.capture(level,cow);h.assertTrue(be.addNet(net),"Mobitat must accept captured resident");
  var player=h.makeMockPlayer(GameType.CREATIVE);player.setPos(Vec3.atCenterOf(pos));AquariumMod.MOBITAT.playerWillDestroy(level,pos,level.getBlockState(pos),player);level.removeBlock(pos,false);
  var drops=level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(2),e->e.getItem().is(AquariumMod.MOBITAT_ITEM));h.assertTrue(drops.size()==1,"Creative break must drop exactly one Mobitat");
  var restored=new MobitatBlockEntity(BlockPos.ZERO,AquariumMod.MOBITAT.defaultBlockState());restored.setLevel(level);restored.fromItem(drops.getFirst().getItem());h.assertTrue(restored.size()==1&&restored.type().equals("minecraft:cow"),"Dropped Mobitat must retain its resident");h.succeed();
 }
 @GameTest public void editorFindsExistingDecorationOwner(GameTestHelper h){
  h.setBlock(0,1,1,AquariumMod.TANK);h.setBlock(1,1,1,AquariumMod.TANK);BlockPos first=h.absolutePos(new BlockPos(0,1,1)),second=h.absolutePos(new BlockPos(1,1,1));var level=h.getLevel();
  var owner=(TankBlockEntity)level.getBlockEntity(second);owner.addDecoration(new net.minecraft.world.item.ItemStack(Items.DIAMOND_BLOCK),EnclosureDecoration.Anchor.BODY);
  var player=h.makeMockPlayer(GameType.SURVIVAL);player.setPos(Vec3.atCenterOf(first));var menu=new AquariumMenu(1,player.getInventory(),level,first);
  h.assertTrue(menu.clickMenuButton(player,22),"Editor must find decoration data stored on another connected tank");h.assertTrue(menu.transfer.getItem(1).is(Items.DIAMOND_BLOCK)&&owner.decorations.isEmpty(),"Editor must return the existing decoration without loss");h.succeed();
 }
 @GameTest public void occupiedOutputBlocksResidentExtraction(GameTestHelper h){
  h.setBlock(1,1,1,AquariumMod.TANK);BlockPos pos=h.absolutePos(new BlockPos(1,1,1));var level=h.getLevel();var fish=h.spawn(EntityTypes.COD,1.5f,1.4f,1.5f);fish.setFromBucket(true);
  var player=h.makeMockPlayer(GameType.SURVIVAL);player.setPos(Vec3.atCenterOf(pos));var menu=new AquariumMenu(2,player.getInventory(),level,pos);menu.transfer.setItem(0,new net.minecraft.world.item.ItemStack(Items.BUCKET));menu.transfer.setItem(1,new net.minecraft.world.item.ItemStack(Items.DIAMOND));
  h.assertTrue(!menu.clickMenuButton(player,20),"Resident extraction must refuse an occupied output slot");h.assertTrue(fish.isAlive()&&menu.transfer.getItem(1).is(Items.DIAMOND),"Failed extraction must preserve both resident and existing output");h.succeed();
 }
 @GameTest public void residentEditorPagesPastFiveMobs(GameTestHelper h){
  for(int x=0;x<6;x++){h.setBlock(x,1,1,AquariumMod.TANK);var fish=h.spawn(EntityTypes.COD,x+.5f,1.4f,1.5f);fish.setFromBucket(true);}
  BlockPos pos=h.absolutePos(new BlockPos(0,1,1));var player=h.makeMockPlayer(GameType.SURVIVAL);player.setPos(Vec3.atCenterOf(pos));var menu=new AquariumMenu(3,player.getInventory(),h.getLevel(),pos);
  h.assertTrue(menu.data.get(1)==6&&menu.data.get(24)==1,"Six residents must expose a second Mobs page");h.assertTrue(menu.clickMenuButton(player,24)&&menu.data.get(23)==1,"Next page must be selectable");h.assertTrue(menu.clickMenuButton(player,100)&&menu.data.get(2)==5,"First row on page two must select the sixth resident");h.succeed();
 }
 @GameTest public void decorationEditsSynchronizeWithoutDisplayEntities(GameTestHelper h){
  h.setBlock(1,1,1,AquariumMod.TANK);BlockPos pos=h.absolutePos(new BlockPos(1,1,1));var level=h.getLevel();var be=(TankBlockEntity)level.getBlockEntity(pos);
  be.addDecoration(new net.minecraft.world.item.ItemStack(Items.DIAMOND_BLOCK),EnclosureDecoration.Anchor.BODY);
  be.addDecoration(new net.minecraft.world.item.ItemStack(Items.OAK_FENCE),EnclosureDecoration.Anchor.FLOOR);
  int before=be.revision();be.tick();be.decorations.getFirst().x+=.1f;be.changed();be.tick();
  var legacy=level.getEntitiesOfClass(net.minecraft.world.entity.Display.class,new AABB(pos).inflate(3),e->true);
  h.assertTrue(be.revision()>before&&legacy.isEmpty(),"Decoration updates must sync to client without spawning unclipped display entities");
  h.succeed();
 }
 @GameTest public void squidSizesRemainAtQuarterScaleAndRestoreOnCapture(GameTestHelper h){
  h.setBlock(1,1,1,AquariumMod.TANK);
  h.setBlock(2,1,1,AquariumMod.TANK);
  var level=h.getLevel();
  var squid=h.spawn(EntityTypes.SQUID,1.5f,1.4f,1.5f);
  var glow=h.spawn(EntityTypes.GLOW_SQUID,2.5f,1.4f,1.5f);
  for(var mob:java.util.List.of(squid,glow)){
   var scale=mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.SCALE);
   h.assertTrue(scale!=null,"Squid should support the SCALE attribute");
   scale.setBaseValue(1.6);
   // An existing resident may already have the MANAGED tag from an older JAR.
   mob.addTag(AquariumMod.MANAGED);
   Inhabitants.configure(mob);Inhabitants.configure(mob);
   h.assertTrue(Math.abs(scale.getBaseValue()-.4)<.0001,"Managed squid must remain 25% of its original size, even after repeated ticks");
   var returned=Inhabitants.capture(level,mob);
   h.assertTrue(returned.is(AquariumMod.CREATURE_BUCKET),"Squid must remain retrievable");
   h.assertTrue(Math.abs(scale.getBaseValue()-1.6)<.0001,"A retrieved squid must regain its original scale");
  }
  h.succeed();
 }

}
