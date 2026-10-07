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
 @GameTest public void decorationsStayInBlockEntityWithoutDisplayEntities(GameTestHelper h){
  h.setBlock(1,1,1,AquariumMod.TANK);BlockPos pos=h.absolutePos(new BlockPos(1,1,1));var level=h.getLevel();var be=(TankBlockEntity)level.getBlockEntity(pos);
  be.addDecoration(new net.minecraft.world.item.ItemStack(Items.DIAMOND_BLOCK),EnclosureDecoration.Anchor.BODY);be.addDecoration(new net.minecraft.world.item.ItemStack(Items.OAK_FENCE),EnclosureDecoration.Anchor.FLOOR);be.tick();
  var displays=level.getEntitiesOfClass(net.minecraft.world.entity.Display.class,new AABB(pos).inflate(3),e->true);
  h.assertTrue(displays.isEmpty(),"Tank decorations must no longer create server-side Display entities");
  h.assertTrue(be.decorations.size()==2&&be.decorations.getFirst().stack.is(Items.DIAMOND_BLOCK),"Decoration data must remain owned by the tank block entity");h.succeed();
 }
 @GameTest public void decorationAvoidanceKeepsResidentsMoving(GameTestHelper h){
  h.assertTrue(java.lang.Math.abs(Inhabitants.MOVE_SPEED_MULTIPLIER-.80)<1.0E-9&&java.lang.Math.abs(Terrestrial.MOVE_SPEED_MULTIPLIER-.80)<1.0E-9,"Aquarium and terrarium residents must remain at 80% enclosure movement speed");

  h.setBlock(1,1,1,AquariumMod.TANK);BlockPos waterPos=h.absolutePos(new BlockPos(1,1,1));var waterBe=(TankBlockEntity)h.getLevel().getBlockEntity(waterPos);
  var waterDecor=waterBe.addDecoration(new net.minecraft.world.item.ItemStack(Items.DIAMOND_BLOCK),EnclosureDecoration.Anchor.BODY);waterDecor.x=.5f;waterDecor.y=.5f;waterDecor.z=.5f;waterDecor.scale=.2f;waterBe.changed();
  var fish=h.spawn(EntityTypes.TROPICAL_FISH,1.1f,1.5f,1.5f);fish.setPos(waterPos.getX()+.1,waterPos.getY()+.5,waterPos.getZ()+.5);
  Vec3 waterDesired=new Vec3(.30,0,0),waterStep=Inhabitants.steer(h.getLevel(),fish,waterDesired,true);
  h.assertTrue(Inhabitants.penalty(h.getLevel(),fish,fish.position().add(waterDesired))>1.0E-8,"Test setup must put the direct aquarium route through decor");
  h.assertTrue(waterStep!=null&&Inhabitants.penalty(h.getLevel(),fish,fish.position().add(waterStep))<Inhabitants.penalty(h.getLevel(),fish,fish.position().add(waterDesired)),"Aquarium residents must steer around decor instead of stopping");

  h.setBlock(4,1,1,AquariumMod.PASSIVE_TERRARIUM);BlockPos landPos=h.absolutePos(new BlockPos(4,1,1));var landBe=(TankBlockEntity)h.getLevel().getBlockEntity(landPos);
  var landDecor=landBe.addDecoration(new net.minecraft.world.item.ItemStack(Items.DIAMOND_BLOCK),EnclosureDecoration.Anchor.BODY);landDecor.x=.5f;landDecor.y=.5f;landDecor.z=.5f;landDecor.scale=.2f;landBe.changed();
  var rabbit=h.spawn(EntityTypes.RABBIT,4.1f,1.18f,1.5f);rabbit.setPos(landPos.getX()+.1,landPos.getY()+.18,landPos.getZ()+.5);
  Vec3 landDesired=new Vec3(.30,0,0),landStep=Terrestrial.steer(h.getLevel(),rabbit,landDesired,false,h.getLevel().getBlockState(landPos));
  h.assertTrue(Terrestrial.penalty(h.getLevel(),rabbit,rabbit.position().add(landDesired))>1.0E-8,"Test setup must put the direct terrarium route through decor");
  h.assertTrue(landStep!=null&&Terrestrial.penalty(h.getLevel(),rabbit,rabbit.position().add(landStep))<Terrestrial.penalty(h.getLevel(),rabbit,rabbit.position().add(landDesired)),"Terrarium residents must steer around decor instead of stopping");
  h.succeed();
 }

}
