package dev.casz.aquarium;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.minecraft.world.*;
import net.minecraft.network.chat.Component;
public class TerrariumGameTests {
 private BlockPos abs(GameTestHelper h,int x,int y,int z){return h.absolutePos(new BlockPos(x,y,z));}
 private void column(GameTestHelper h,int x,int height,boolean hostile){for(int y=1;y<=height;y++)h.setBlock(x,y,1,hostile?AquariumMod.HOSTILE_TERRARIUM:AquariumMod.PASSIVE_TERRARIUM);}
 private void click(GameTestHelper h,net.minecraft.world.entity.player.Player p,BlockPos pos){net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.invoker().interact(p,h.getLevel(),InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));}
 @GameTest public void enclosureKindsNeverJoin(GameTestHelper h){
  h.setBlock(0,1,1,AquariumMod.PASSIVE_TERRARIUM);h.setBlock(1,1,1,AquariumMod.PASSIVE_PIPE);h.setBlock(2,1,1,AquariumMod.PASSIVE_TERRARIUM);h.setBlock(3,1,1,AquariumMod.HOSTILE_PIPE);h.setBlock(4,1,1,AquariumMod.HOSTILE_TERRARIUM);h.setBlock(0,1,2,AquariumMod.TANK);
  var net=Network.scan(h.getLevel(),abs(h,0,1,1));h.assertTrue(net.tanks()==2&&net.cells().size()==3&&net.capacity()==2*AquariumMod.fishPerBlock,"Passive network must exclude hostile modules and aquariums; pipes add zero capacity");
  h.assertTrue(!h.getLevel().getBlockState(abs(h,2,1,1)).getValue(AquariumBlock.LINKS[Direction.EAST.ordinal()]),"Mismatched pipe must leave wall closed");h.assertTrue(h.getLevel().getFluidState(abs(h,1,1,1)).isEmpty(),"Terrarium pipe must be dry");h.succeed();
 }
 @GameTest public void matchingAndBirdHeightRefusalsKeepTheNet(GameTestHelper h){
  column(h,1,4,false);var pos=abs(h,1,1,1);var zombie=h.spawn(EntityTypes.ZOMBIE,3.5f,1.1f,1.5f);var net=Terrestrial.capture(h.getLevel(),zombie);
  h.assertTrue(Terrestrial.add(h.getLevel(),pos,net)!=null&&Terrestrial.filled(net),"Hostile mob cannot enter passive cage or be lost on refusal");
  var parrot=h.spawn(EntityTypes.PARROT,3.5f,1.1f,1.5f);var birdNet=Terrestrial.capture(h.getLevel(),parrot);h.assertTrue(Terrestrial.add(h.getLevel(),pos,birdNet)!=null&&Terrestrial.filled(birdNet),"Four-block cage must refuse bird without losing it");
  h.setBlock(1,5,1,AquariumMod.PASSIVE_TERRARIUM);h.assertTrue(Terrestrial.add(h.getLevel(),pos,birdNet)==null&&!Terrestrial.filled(birdNet),"Five-block cage must accept bird and return reusable empty net");h.succeed();
 }
 @GameTest public void pipeEntrancesObeyHeight(GameTestHelper h){
  column(h,1,5,false);h.setBlock(2,1,1,AquariumMod.PASSIVE_PIPE);h.setBlock(2,3,1,AquariumMod.PASSIVE_PIPE);h.setBlock(2,4,1,AquariumMod.PASSIVE_PIPE);
  var l=h.getLevel();h.assertTrue(Terrestrial.canStep(l,abs(h,1,1,1),abs(h,2,1,1),false),"Ground mobs must use floor-level pipe");h.assertTrue(!Terrestrial.canStep(l,abs(h,1,3,1),abs(h,2,3,1),false),"Ground mobs must not enter raised pipe");
  h.assertTrue(!Terrestrial.canStep(l,abs(h,1,3,1),abs(h,2,3,1),true),"Birds must not use third-level pipe");h.assertTrue(Terrestrial.canStep(l,abs(h,1,4,1),abs(h,2,4,1),true)&&Terrestrial.canStep(l,abs(h,2,4,1),abs(h,1,4,1),true),"Birds must use fourth-level pipe in either direction");h.succeed();
 }
 @GameTest(maxTicks=120) public void groundMobsWalkBetweenTerrariums(GameTestHelper h){
  h.setBlock(0,1,1,AquariumMod.PASSIVE_TERRARIUM);h.setBlock(1,1,1,AquariumMod.PASSIVE_PIPE);h.setBlock(2,1,1,AquariumMod.PASSIVE_TERRARIUM);var cow=h.spawn(EntityTypes.COW,3.5f,1.1f,1.5f);var net=Terrestrial.capture(h.getLevel(),cow);h.assertTrue(Terrestrial.add(h.getLevel(),abs(h,0,1,1),net)==null,"Cow must enter cage");var mob=Network.scan(h.getLevel(),abs(h,0,1,1)).residents(h.getLevel()).getFirst();
  h.assertTrue(Math.abs(mob.getAttribute(Attributes.SCALE).getBaseValue()-.75)<.0001,"Resident must be 75% normal size");
  h.runAfterDelay(90,()->{h.assertTrue(mob.isAlive()&&Network.scan(h.getLevel(),abs(h,0,1,1)).residents(h.getLevel()).contains(mob),"Cow must remain a valid moving terrarium resident");h.assertTrue(!mob.noPhysics,"Collision bypass must not remain enabled");h.succeed();});
 }
 @GameTest public void netPreservesNamesAndRestoresNormalScale(GameTestHelper h){
  column(h,1,1,false);var pig=h.spawn(EntityTypes.PIG,3.5f,1.1f,1.5f);pig.setCustomName(Component.literal("Tiny Friend"));pig.getAttribute(Attributes.SCALE).setBaseValue(1.2);var net=Terrestrial.capture(h.getLevel(),pig);Terrestrial.add(h.getLevel(),abs(h,1,1,1),net);var mob=Network.scan(h.getLevel(),abs(h,1,1,1)).residents(h.getLevel()).getFirst();
  h.assertTrue(mob.getName().getString().equals("Tiny Friend")&&!mob.getUUID().equals(pig.getUUID()),"Net must preserve name and assign fresh UUID");var full=Terrestrial.capture(h.getLevel(),mob);h.assertTrue(Terrestrial.release(h.getLevel(),abs(h,3,1,3),full)==null,"Net must release mob into world");var released=h.getLevel().getEntitiesOfClass(Mob.class,new AABB(abs(h,3,1,3)).inflate(1),m->m.getName().getString().equals("Tiny Friend")).getFirst();h.assertTrue(!released.isNoAi()&&!released.isNoGravity()&&Math.abs(released.getAttribute(Attributes.SCALE).getBaseValue()-1.2)<.0001,"World release must restore original AI, gravity and scale");h.succeed();
 }
 @GameTest public void waterPatchesAndSharedDecorMenu(GameTestHelper h){
  column(h,1,2,false);var pos=abs(h,1,1,1);var p=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);p.setPos(Vec3.atCenterOf(pos));p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.WATER_BUCKET));click(h,p,pos);
  h.assertTrue(h.getLevel().getBlockState(pos).getValue(TerrariumBlock.GROUND_WATER)&&p.getItemInHand(InteractionHand.MAIN_HAND).is(Items.BUCKET),"Water bucket must add ground water and return empty bucket");
  h.assertTrue(h.getLevel().getFluidState(pos).isEmpty(),"Ground patch must not flood the enclosure");var menu=new AquariumMenu(1,p.getInventory(),h.getLevel(),pos);menu.transfer.setItem(0,new ItemStack(Items.OAK_FENCE));h.assertTrue(menu.clickMenuButton(p,21),"Terrarium must share decor insertion UI");menu.clickMenuButton(p,41);h.assertTrue(!((TankBlockEntity)h.getLevel().getBlockEntity(pos)).decorations.isEmpty(),"Terrarium decor must be editable");
  p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.WATER_BUCKET));click(h,p,pos.above());h.assertTrue(!h.getLevel().getBlockState(pos.above()).getValue(TerrariumBlock.GROUND_WATER),"Water cannot be placed above ground level");
  p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.BUCKET));click(h,p,pos);h.assertTrue(!h.getLevel().getBlockState(pos).getValue(TerrariumBlock.GROUND_WATER)&&p.getItemInHand(InteractionHand.MAIN_HAND).is(Items.WATER_BUCKET),"Empty bucket must retrieve patch");h.getLevel().removeBlock(pos,false);h.assertTrue(h.getLevel().getFluidState(pos).isEmpty(),"Breaking terrarium must not spill water");h.succeed();
 }
 @GameTest(maxTicks=240) public void hostileResidentsAreProtectedAndPeaceful(GameTestHelper h){
  column(h,1,1,true);var zombie=h.spawn(EntityTypes.ZOMBIE,3.5f,1.1f,1.5f);var net=Terrestrial.capture(h.getLevel(),zombie);h.assertTrue(Terrestrial.add(h.getLevel(),abs(h,1,1,1),net)==null,"Zombie must enter hostile cage");var mob=Network.scan(h.getLevel(),abs(h,1,1,1)).residents(h.getLevel()).getFirst();float health=mob.getHealth();h.getLevel().clockManager().setTotalTicks(h.getLevel().registryAccess().lookupOrThrow(Registries.WORLD_CLOCK).getOrThrow(WorldClocks.OVERWORLD),6000);
  h.runAfterDelay(200,()->{h.assertTrue(mob.isAlive()&&mob.getHealth()==health&&!mob.isOnFire()&&mob.isNoAi(),"Tinted hostile enclosure must protect zombie from burning and disable attacks");h.succeed();});
 }
 @GameTest(maxTicks=80) public void removalRescuesResidentsIntoNets(GameTestHelper h){
  column(h,1,1,false);var pig=h.spawn(EntityTypes.PIG,3.5f,1.1f,1.5f);var net=Terrestrial.capture(h.getLevel(),pig);Terrestrial.add(h.getLevel(),abs(h,1,1,1),net);var mob=Network.scan(h.getLevel(),abs(h,1,1,1)).residents(h.getLevel()).getFirst();h.getLevel().removeBlock(abs(h,1,1,1),false);
  h.runAfterDelay(3,()->{h.assertTrue(mob.isRemoved(),"Stranded mob must become a net");var items=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(abs(h,1,1,1)).inflate(2),e->Terrestrial.filled(e.getItem()));h.assertTrue(items.size()==1,"Rescue must drop exactly one filled net");h.succeed();});
 }
 @GameTest(maxTicks=100) public void ignitedCreepersCannotExplodeInsideTerrariums(GameTestHelper h){
  column(h,1,1,true);var creeper=h.spawn(EntityTypes.CREEPER,3.5f,1.1f,1.5f);creeper.ignite();var net=Terrestrial.capture(h.getLevel(),creeper);h.assertTrue(Terrestrial.add(h.getLevel(),abs(h,1,1,1),net)==null,"Ignited creeper must enter matching cage");var mob=Network.scan(h.getLevel(),abs(h,1,1,1)).residents(h.getLevel()).getFirst();
  h.runAfterDelay(60,()->{h.assertTrue(mob.isAlive()&&h.getLevel().getBlockState(abs(h,1,1,1)).is(AquariumMod.HOSTILE_TERRARIUM),"Captured ignited creeper must not explode or damage cage");h.succeed();});
 }

 @GameTest(maxTicks=80) public void shortenedBirdCageReturnsBirdToNet(GameTestHelper h){
  column(h,1,5,false);var parrot=h.spawn(EntityTypes.PARROT,3.5f,1.1f,1.5f);var full=Terrestrial.capture(h.getLevel(),parrot);Terrestrial.add(h.getLevel(),abs(h,1,1,1),full);var bird=Network.scan(h.getLevel(),abs(h,1,1,1)).residents(h.getLevel()).getFirst();h.getLevel().removeBlock(abs(h,1,5,1),false);
  h.runAfterDelay(3,()->{h.assertTrue(bird.isRemoved(),"Bird must be rescued after cage is shortened below five blocks");h.assertTrue(h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(abs(h,1,1,1)).inflate(2),e->Terrestrial.filled(e.getItem())).size()==1,"Shortened bird cage must return exactly one filled net");h.succeed();});
 }
 @GameTest public void fullTerrariumKeepsCapturedMobInNet(GameTestHelper h){
  column(h,1,1,false);var pos=abs(h,1,1,1);
  for(int n=0;n<AquariumMod.fishPerBlock;n++){var cow=h.spawn(EntityTypes.COW,3.5f,1.1f,1.5f);h.assertTrue(Terrestrial.add(h.getLevel(),pos,Terrestrial.capture(h.getLevel(),cow))==null,"Capacity slot must accept a resident");}
  var pig=h.spawn(EntityTypes.PIG,3.5f,1.1f,1.5f);var full=Terrestrial.capture(h.getLevel(),pig);h.assertTrue(Terrestrial.add(h.getLevel(),pos,full)!=null&&Terrestrial.filled(full),"Full terrarium must leave captured mob in its net");h.assertTrue(Network.scan(h.getLevel(),pos).residents(h.getLevel()).size()==AquariumMod.fishPerBlock,"Full terrarium must never spawn excess residents");h.succeed();
 }

 @GameTest(maxTicks=280) public void groundMobSteersAroundBodyDecoration(GameTestHelper h){
  for(int x=0;x<3;x++)h.setBlock(x,1,1,AquariumMod.PASSIVE_TERRARIUM);
  BlockPos middle=abs(h,1,1,1),far=abs(h,2,1,1);
  var be=(TankBlockEntity)h.getLevel().getBlockEntity(middle);var decor=be.addDecoration(new ItemStack(Items.SEA_PICKLE),EnclosureDecoration.Anchor.BODY);decor.scale=.8f;be.changed();
  var chicken=h.spawn(EntityTypes.CHICKEN,.5f,1.18f,1.5f);Terrestrial.configure(chicken);
  h.onEachTick(()->{if(chicken.isAlive()&&chicken.blockPosition().equals(far))h.succeed();});
 }
 @GameTest(maxTicks=300) public void groundMobEscapesDecorationOverlap(GameTestHelper h){
  for(int x=0;x<3;x++)h.setBlock(x,1,1,AquariumMod.PASSIVE_TERRARIUM);BlockPos start=abs(h,0,1,1),far=abs(h,2,1,1);
  var be=(TankBlockEntity)h.getLevel().getBlockEntity(start);var decor=be.addDecoration(new ItemStack(Items.DIAMOND_BLOCK),EnclosureDecoration.Anchor.BODY);
  decor.x=.5f;decor.y=.18f;decor.z=.5f;decor.scale=.45f;be.changed();
  var chicken=h.spawn(EntityTypes.CHICKEN,.5f,1.18f,1.5f);Terrestrial.configure(chicken);
  h.assertTrue(be.collides(chicken.getBoundingBox()),"Regression setup must start the mob intersecting decor");
  h.onEachTick(()->{
   if(h.getLevel().getGameTime()%20==0)System.out.println("[terrarium-overlap] pos="+chicken.position()+" block="+chicken.blockPosition()+" penalty="+be.collisionPenalty(chicken.getBoundingBox())+" far="+chicken.blockPosition().equals(far));
   if(chicken.isAlive()&&!be.collides(chicken.getBoundingBox())&&chicken.blockPosition().equals(far))h.succeed();
  });
 }

}
