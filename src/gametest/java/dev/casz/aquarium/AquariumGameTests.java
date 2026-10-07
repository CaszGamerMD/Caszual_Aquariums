package dev.casz.aquarium;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.CollisionContext;

public class AquariumGameTests {
 private net.minecraft.world.InteractionResult click(GameTestHelper h,net.minecraft.world.entity.player.Player p,BlockPos relative){
  BlockPos pos=h.absolutePos(relative);return net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.invoker().interact(p,h.getLevel(),net.minecraft.world.InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));
 }
 @GameTest public void bucketsEnforceCapacityAndRetrieveFish(GameTestHelper h){
  h.setBlock(1,1,1,AquariumMod.TANK);var player=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);var hand=net.minecraft.world.InteractionHand.MAIN_HAND;
  for(int i=0;i<AquariumMod.fishPerBlock;i++){player.setItemInHand(hand,new net.minecraft.world.item.ItemStack(Items.COD_BUCKET));click(h,player,new BlockPos(1,1,1));h.assertTrue(player.getItemInHand(hand).is(Items.BUCKET),"Adding fish must return an empty bucket");}
  player.setItemInHand(hand,new net.minecraft.world.item.ItemStack(Items.COD_BUCKET));click(h,player,new BlockPos(1,1,1));
  h.assertTrue(player.getItemInHand(hand).is(Items.COD_BUCKET),"Full tank must not consume a fish bucket");
  var net=Network.scan(h.getLevel(),h.absolutePos(new BlockPos(1,1,1)));h.assertTrue(net.fish(h.getLevel()).size()==net.capacity(),"Full tank must not spawn an extra fish");
  player.setItemInHand(hand,new net.minecraft.world.item.ItemStack(Items.BUCKET));click(h,player,new BlockPos(1,1,1));
  h.assertTrue(player.getItemInHand(hand).is(Items.COD_BUCKET),"Retrieval must return a fish bucket");h.assertTrue(net.fish(h.getLevel()).size()==net.capacity()-1,"Retrieval must remove exactly one fish");h.succeed();
 }
 @GameTest public void floorsAndDecorPersistAsBlockStates(GameTestHelper h){
  h.setBlock(1,1,1,AquariumMod.TANK);var player=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);var hand=net.minecraft.world.InteractionHand.MAIN_HAND;
  player.setItemInHand(hand,new net.minecraft.world.item.ItemStack(Palette.item("red_concrete_powder")));click(h,player,new BlockPos(1,1,1));
  player.setItemInHand(hand,new net.minecraft.world.item.ItemStack(Items.STONE_BUTTON));click(h,player,new BlockPos(1,1,1));
  var state=h.getLevel().getBlockState(h.absolutePos(new BlockPos(1,1,1)));
  h.assertTrue(Palette.SOILS.get(state.getValue(AquariumBlock.SOIL)).equals("red_concrete_powder"),"Powder must be stored without hardening");
  h.assertTrue(((TankBlockEntity)h.getLevel().getBlockEntity(h.absolutePos(new BlockPos(1,1,1)))).decorations.stream().anyMatch(d->d.stack.is(Items.STONE_BUTTON)),"Button must be stored as decoration");
  h.assertTrue(player.getItemInHand(hand).isEmpty(),"Decoration must consume exactly one item");
  var be=(TankBlockEntity)h.getLevel().getBlockEntity(h.absolutePos(new BlockPos(1,1,1)));be.removeDecoration(0);state=h.getLevel().getBlockState(h.absolutePos(new BlockPos(1,1,1)));h.assertTrue(state.getValue(AquariumBlock.SOIL)>0&&be.decorations.isEmpty(),"Removing decor must keep the floor");h.succeed();
 }

 @GameTest(maxTicks=140) public void fishTraverseTubeToOtherTank(GameTestHelper h){
  line(h);var fish=h.spawn(EntityTypes.COD,.5f,1.4f,1.5f);fish.setFromBucket(true);BlockPos far=h.absolutePos(new BlockPos(2,1,1));
  h.onEachTick(()->{if(fish.isAlive()&&fish.isInWater()&&fish.blockPosition().equals(far))h.succeed();});
 }
 @GameTest(maxTicks=100) public void breakingModulesLeavesAirNotWater(GameTestHelper h){
  h.setBlock(0,1,1,AquariumMod.TANK);h.setBlock(1,1,1,AquariumMod.TUBE);
  var level=h.getLevel();var tank=h.absolutePos(new BlockPos(0,1,1));var tube=h.absolutePos(new BlockPos(1,1,1));
  h.assertTrue(level.getFluidState(tube).getHeight(level,tube)==1.0f,"Tube must contain full-height water");
  level.removeBlock(tank,false);level.removeBlock(tube,false);
  h.runAfterDelay(20,()->{h.assertTrue(level.getBlockState(tank).isAir()&&level.getFluidState(tank).isEmpty(),"Tank removal must leave air");h.assertTrue(level.getBlockState(tube).isAir()&&level.getFluidState(tube).isEmpty(),"Tube removal must leave air");h.succeed();});
 }
 @GameTest(maxTicks=120) public void largeSalmonEnterTubesWithoutPushing(GameTestHelper h){
  line(h);var player=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);var stack=new net.minecraft.world.item.ItemStack(Items.SALMON_BUCKET);
  stack.set(net.minecraft.core.component.DataComponents.SALMON_SIZE,net.minecraft.world.entity.animal.fish.Salmon.Variant.LARGE);
  player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,stack);click(h,player,new BlockPos(0,1,1));
  var fish=(net.minecraft.world.entity.animal.fish.Salmon)Network.scan(h.getLevel(),h.absolutePos(new BlockPos(0,1,1))).fish(h.getLevel()).getFirst();
  h.assertTrue(fish.getVariant()==net.minecraft.world.entity.animal.fish.Salmon.Variant.LARGE,"Bucket must preserve salmon size");
  h.runAfterDelay(35,()->{h.assertTrue(h.getLevel().getBlockState(fish.blockPosition()).is(AquariumMod.TUBE),"Large salmon must enter tube without pushing");h.assertTrue(!fish.noPhysics,"Collision bypass must be confined to managed swim steps");h.succeed();});
 }
 private void line(GameTestHelper h){h.setBlock(0,1,1,AquariumMod.TANK);h.setBlock(1,1,1,AquariumMod.TUBE);h.setBlock(2,1,1,AquariumMod.TANK);}
 @GameTest public void tubesConnectWithoutCapacity(GameTestHelper h){
  line(h);var net=Network.scan(h.getLevel(),h.absolutePos(new BlockPos(0,1,1)));
  h.assertTrue(net.cells().size()==3,"All three modules must link");h.assertTrue(net.capacity()==2*AquariumMod.fishPerBlock,"Tubes must add zero capacity");
  h.setBlock(1,1,1,Blocks.AIR);net=Network.scan(h.getLevel(),h.absolutePos(new BlockPos(0,1,1)));
  h.assertTrue(net.tanks()==1&&net.cells().size()==1,"Removing a tube must split tanks");h.succeed();
 }
 @GameTest public void facesOpenAndClose(GameTestHelper h){
  line(h);BlockPos pos=h.absolutePos(new BlockPos(0,1,1));var state=h.getLevel().getBlockState(pos);
  h.assertTrue(state.getValue(AquariumBlock.LINKS[Direction.EAST.ordinal()]),"Tank must open east face toward tube");
  var tube=h.getLevel().getBlockState(pos.east());h.assertTrue(tube.getValue(TubeBlock.TANK_LINKS[Direction.WEST.ordinal()]),"Tube must seal tank with a glass ring");
  h.setBlock(1,1,1,Blocks.AIR);state=h.getLevel().getBlockState(pos);
  h.assertTrue(!state.getValue(AquariumBlock.LINKS[Direction.EAST.ordinal()]),"Tank wall must reseal on removal");h.succeed();
 }
 @GameTest(maxTicks=100) public void fishStayAliveAndAreRescued(GameTestHelper h){
  h.setBlock(1,1,1,AquariumMod.TANK);AbstractFish fish=h.spawn(EntityTypes.TROPICAL_FISH,1.5f,1.4f,1.5f);fish.setFromBucket(true);
  h.runAfterDelay(25,()->{
   h.assertTrue(fish.isAlive()&&fish.entityTags().contains(AquariumMod.MANAGED),"Real fish must remain alive inside the aquarium");
   h.setBlock(1,1,1,Blocks.AIR);
   h.runAfterDelay(3,()->{
    h.assertTrue(fish.isRemoved(),"Stranded fish must be rescued");
    var items=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(h.absolutePos(new BlockPos(0,0,0))).inflate(4),e->e.getItem().is(Items.TROPICAL_FISH_BUCKET));
    h.assertTrue(items.size()==1,"Rescue must drop exactly one fish bucket");h.succeed();
   });
  });
 }
 @GameTest(maxTicks=100) public void realFishSwimIntoTube(GameTestHelper h){
  line(h);AbstractFish fish=h.spawn(EntityTypes.SALMON,.5f,1.4f,1.5f);fish.setFromBucket(true);
  h.runAfterDelay(35,()->{h.assertTrue(fish.isAlive(),"Salmon must stay alive");h.assertTrue(h.getLevel().getBlockState(fish.blockPosition()).is(AquariumMod.TUBE),"Real fish must swim into the tube");h.succeed();});
 }
 @GameTest(maxTicks=100) public void excessFishBecomeBuckets(GameTestHelper h){
  h.setBlock(1,1,1,AquariumMod.TANK);for(int i=0;i<AquariumMod.fishPerBlock+1;i++)h.spawn(EntityTypes.COD,1.5f,1.4f,1.5f);
  h.runAfterDelay(25,()->{
   var net=Network.scan(h.getLevel(),h.absolutePos(new BlockPos(1,1,1)));
   h.assertTrue(net.fish(h.getLevel()).size()==AquariumMod.fishPerBlock,"Overcrowding must be prevented");
   var items=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(h.absolutePos(new BlockPos(0,0,0))).inflate(4),e->e.getItem().is(Items.COD_BUCKET));
   h.assertTrue(items.size()==1,"Extra fish must be returned without loss or duplication");h.succeed();
  });
 }

 @GameTest public void layoutMenuPreservesItemsAndControls(GameTestHelper h){
  h.setBlock(1,1,1,AquariumMod.TANK);var p=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);var pos=h.absolutePos(new BlockPos(1,1,1));p.setPos(Vec3.atCenterOf(pos));
  var menu=new AquariumMenu(1,p.getInventory(),h.getLevel(),pos);var be=(TankBlockEntity)h.getLevel().getBlockEntity(pos);
  menu.transfer.setItem(0,new net.minecraft.world.item.ItemStack(Items.OAK_FENCE,2));h.assertTrue(menu.clickMenuButton(p,21),"UI must accept items");
  menu.clickMenuButton(p,41);menu.clickMenuButton(p,31);h.assertTrue(!be.decorations.isEmpty(),"UI must rotate, change shape and move decor");
  h.assertTrue(menu.transfer.getItem(0).getCount()==1,"UI must consume one decor");h.assertTrue(menu.clickMenuButton(p,22),"UI must remove decor");
  h.assertTrue(menu.transfer.getItem(1).is(Items.OAK_FENCE)&&be.decorations.isEmpty(),"Removal must return exactly the original item");
  h.assertTrue(menu.transfer.getItem(0).getCount()==1,"Input must not be lost while output is occupied");h.succeed();
 }
 @GameTest public void spawnEggsAndMiniCreatureBucketsRespectCapacity(GameTestHelper h){
  h.setBlock(1,1,1,AquariumMod.TANK);var pos=h.absolutePos(new BlockPos(1,1,1));var p=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
  var ink=new net.minecraft.world.item.ItemStack(Items.GLOW_INK_SAC);h.assertTrue(Inhabitants.add(h.getLevel(),pos,ink,p).success(),"Glow ink must add a mini squid");
  var mob=Network.scan(h.getLevel(),pos).residents(h.getLevel()).getFirst();h.assertTrue(mob.getType()==EntityTypes.GLOW_SQUID&&mob.isNoAi(),"Mini squid must be peaceful");
  h.assertTrue(mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.SCALE).getBaseValue()==1.0,"Aquarium mobs must remain full scale");
  var bucket=Inhabitants.capture(h.getLevel(),mob);h.assertTrue(bucket.is(AquariumMod.CREATURE_BUCKET),"Squid must have a reusable creature bucket");
  h.assertTrue(Inhabitants.add(h.getLevel(),pos,bucket,p).success(),"Creature bucket must restore squid");
  var restored=Network.scan(h.getLevel(),pos).residents(h.getLevel()).getFirst();h.assertTrue(!restored.getUUID().equals(mob.getUUID()),"Restored squid must have a fresh UUID");Inhabitants.capture(h.getLevel(),restored);
  h.assertTrue(Inhabitants.add(h.getLevel(),pos,new net.minecraft.world.item.ItemStack(Items.COD_SPAWN_EGG),p).success(),"Fish egg must spawn inside tank");
  h.assertTrue(!Inhabitants.accepts(new net.minecraft.world.item.ItemStack(Items.ZOMBIE_SPAWN_EGG)),"Non-aquatic eggs must be rejected");h.succeed();
 }
 @GameTest(maxTicks=100) public void turtlesStayBabiesAndAxolotlsLeaveFishAlive(GameTestHelper h){
  line(h);h.setBlock(2,1,2,AquariumMod.TANK);var pos=h.absolutePos(new BlockPos(0,1,1));var p=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
  Inhabitants.add(h.getLevel(),pos,new net.minecraft.world.item.ItemStack(Items.TURTLE_SCUTE),p);Inhabitants.add(h.getLevel(),pos,new net.minecraft.world.item.ItemStack(Items.AXOLOTL_BUCKET),p);Inhabitants.add(h.getLevel(),pos,new net.minecraft.world.item.ItemStack(Items.COD_BUCKET),p);
  var residents=Network.scan(h.getLevel(),pos).residents(h.getLevel());h.assertTrue(residents.size()==3,"Each creature must use one capacity slot");
  var turtle=(AgeableMob)residents.stream().filter(m->m.getType()==EntityTypes.TURTLE).findFirst().orElseThrow();turtle.setAge(0);
  h.runAfterDelay(60,()->{h.assertTrue(turtle.isBaby(),"Turtle must remain a baby");h.assertTrue(Network.scan(h.getLevel(),pos).residents(h.getLevel()).stream().allMatch(Mob::isAlive),"Aquarium residents must remain alive");h.assertTrue(Network.scan(h.getLevel(),pos).residents(h.getLevel()).stream().allMatch(Mob::isNoAi),"Residents must have no predation AI");h.succeed();});
 }
 @GameTest public void genericDecorAndLegacyMigration(GameTestHelper h){
  h.setBlock(1,1,1,AquariumMod.TANK.defaultBlockState().setValue(AquariumBlock.DECOR,19));var pos=h.absolutePos(new BlockPos(1,1,1));var be=(TankBlockEntity)h.getLevel().getBlockEntity(pos);be.migrateLegacy();
  h.assertTrue(be.decorations.stream().anyMatch(d->d.stack.is(Items.STONE_BUTTON))&&be.getBlockState().getValue(AquariumBlock.DECOR)==0,"Old rock decoration must migrate once");
  for(var item:new net.minecraft.world.item.Item[]{Items.CACTUS,Items.WITHER_ROSE,Items.OAK_SAPLING,Items.DIAMOND_BLOCK})h.assertTrue(AquariumMod.isDecoration(new net.minecraft.world.item.ItemStack(item)),"All placeable blocks must be harmless decor");
  be.addDecoration(new net.minecraft.world.item.ItemStack(Items.DIAMOND_BLOCK),EnclosureDecoration.Anchor.FLOOR);be.tick();h.assertTrue(be.decorations.stream().anyMatch(d->d.stack.is(Items.DIAMOND_BLOCK)),"Generic blocks must use their vanilla placed model");
  h.assertTrue(h.getLevel().getEntitiesOfClass(Display.BlockDisplay.class,new AABB(pos).inflate(2),d->d.getBlockState().is(Blocks.DIAMOND_BLOCK)).size()==1,"Generic decor must create exactly one display");h.succeed();
 }
 @GameTest public void chestDecorPersistsWithoutServerDisplay(GameTestHelper h){
  h.setBlock(1,1,1,AquariumMod.TANK);var pos=h.absolutePos(new BlockPos(1,1,1));var be=(TankBlockEntity)h.getLevel().getBlockEntity(pos);be.addDecoration(new net.minecraft.world.item.ItemStack(Items.CHEST),EnclosureDecoration.Anchor.FLOOR);be.decorations.getFirst().rotY=45;be.decorations.getFirst().x=.7f;be.changed();be.tick();
  h.assertTrue(h.getLevel().getEntitiesOfClass(Display.class,new AABB(pos).inflate(2),d->true).isEmpty(),"Client-rendered decor must not create server-side Display entities");
  var tag=be.saveWithoutMetadata(h.getLevel().registryAccess());var loaded=new TankBlockEntity(pos,be.getBlockState());loaded.loadAdditional(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,h.getLevel().registryAccess(),tag));h.assertTrue(loaded.decorations.size()==1&&loaded.decorations.getFirst().stack.is(Items.CHEST)&&java.lang.Math.abs(loaded.decorations.getFirst().rotY-45)<.01&&java.lang.Math.abs(loaded.decorations.getFirst().x-.7f)<.01,"Decor item and layout must persist through saving");h.succeed();
 }
  @GameTest public void miniDrownedUsesCapacityAndIsPeaceful(GameTestHelper h){
  h.setBlock(1,1,1,AquariumMod.TANK);var pos=h.absolutePos(new BlockPos(1,1,1));h.assertTrue(Inhabitants.summonDrowned(h.getLevel(),pos),"Trident helper must spawn mini drowned with room");var mob=Network.scan(h.getLevel(),pos).residents(h.getLevel()).getFirst();h.assertTrue(mob.getType()==EntityTypes.DROWNED&&mob.isNoAi()&&mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.SCALE).getBaseValue()==1.0,"Aquarium drowned must be full scale and peaceful");
  for(int n=1;n<AquariumMod.fishPerBlock;n++)Inhabitants.summonDrowned(h.getLevel(),pos);h.assertTrue(!Inhabitants.summonDrowned(h.getLevel(),pos),"Mini drowned must never exceed capacity");h.succeed();
 }
}
