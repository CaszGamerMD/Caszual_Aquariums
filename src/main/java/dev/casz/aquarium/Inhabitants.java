package dev.casz.aquarium;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.fish.Pufferfish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import net.minecraft.network.chat.Component;

public final class Inhabitants {
 private static final double MOVE_FACTOR=.80,NORMAL_SWIM_SPEED=.035,NORMAL_DROWNED_SPEED=.015;
 private static final Map<Mob,BlockPos> TARGETS=new WeakHashMap<>(),PREVIOUS=new WeakHashMap<>();
 private static final Map<Mob,Vec3> LANES=new WeakHashMap<>(),DODGE_DIR=new WeakHashMap<>();
 private static final Map<Mob,Double> DODGE_DISTANCE=new WeakHashMap<>();
 private static ServerLevel activeLevel;private static final Map<BlockPos,Network> NETWORKS=new HashMap<>();
 private static Network network(ServerLevel level,BlockPos pos){if(level==activeLevel){var cached=NETWORKS.get(pos);if(cached!=null)return cached;}var net=Network.scan(level,pos);if(level==activeLevel)for(var p:net.cells())NETWORKS.put(p,net);return net;}
 public record AddResult(boolean success,ItemStack returned){}
 public static boolean supported(EntityType<?> t){return t==EntityTypes.COD||t==EntityTypes.SALMON||t==EntityTypes.TROPICAL_FISH||t==EntityTypes.PUFFERFISH||t==EntityTypes.AXOLOTL||t==EntityTypes.SQUID||t==EntityTypes.GLOW_SQUID||t==EntityTypes.TURTLE||t==EntityTypes.DROWNED;}
 private static EntityType<?> sourceType(ItemStack s){
  if(s.is(Items.COD_BUCKET))return EntityTypes.COD;if(s.is(Items.SALMON_BUCKET))return EntityTypes.SALMON;if(s.is(Items.TROPICAL_FISH_BUCKET))return EntityTypes.TROPICAL_FISH;if(s.is(Items.PUFFERFISH_BUCKET))return EntityTypes.PUFFERFISH;if(s.is(Items.AXOLOTL_BUCKET))return EntityTypes.AXOLOTL;
  if(s.is(Items.INK_SAC))return EntityTypes.SQUID;if(s.is(Items.GLOW_INK_SAC))return EntityTypes.GLOW_SQUID;if(s.is(Items.TURTLE_SCUTE))return EntityTypes.TURTLE;
  if(s.getItem() instanceof SpawnEggItem){var t=SpawnEggItem.getType(s);return supported(t)&&t!=EntityTypes.DROWNED?t:null;}
  if(s.is(AquariumMod.CREATURE_BUCKET)){var tag=s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();Identifier id=Identifier.tryParse(tag.getString("aquarium_type").orElse(""));return id==null?null:BuiltInRegistries.ENTITY_TYPE.getValue(id);}
  return null;
 }
 public static boolean accepts(ItemStack stack){var type=sourceType(stack);return type!=null&&supported(type);}
 public static AddResult add(ServerLevel level,BlockPos pos,ItemStack stack,Player player){
  var type=sourceType(stack);Network net=Network.scan(level,pos);
  if(type==null||!supported(type)||!level.getBlockState(pos).is(AquariumMod.TANK)||!net.complete()||net.residents(level).size()>=net.capacity())return new AddResult(false,ItemStack.EMPTY);
  if(!(type.create(level,EntitySpawnReason.BUCKET) instanceof Mob mob))return new AddResult(false,ItemStack.EMPTY);
  mob.applyComponentsFromItemStack(stack);
  if(stack.is(AquariumMod.CREATURE_BUCKET)){
   var data=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompound("aquarium_entity").orElse(new CompoundTag());data.remove("UUID");data.remove("uuid");mob.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),data));mob.setUUID(UUID.randomUUID());
  }else if(mob instanceof Bucketable bucketable){bucketable.loadFromBucketTag(stack.getOrDefault(DataComponents.BUCKET_ENTITY_DATA,CustomData.EMPTY).copyTag());bucketable.setFromBucket(true);}
  var name=stack.get(DataComponents.CUSTOM_NAME);if(name!=null&&!stack.is(AquariumMod.CREATURE_BUCKET))mob.setCustomName(name);
  configure(mob);mob.setPos(pos.getX()+.5,pos.getY()+height(mob),pos.getZ()+.5);
  if(!level.addFreshEntity(mob))return new AddResult(false,ItemStack.EMPTY);
  boolean bucket=stack.getItem() instanceof MobBucketItem||stack.is(AquariumMod.CREATURE_BUCKET);
  if(!player.getAbilities().instabuild){stack.shrink(1);return new AddResult(true,bucket?new ItemStack(Items.BUCKET):ItemStack.EMPTY);}
  return new AddResult(true,ItemStack.EMPTY);
 }
 public static void configure(Mob mob){
  if(!mob.entityTags().contains(AquariumMod.MANAGED)){
   if(mob.isNoAi())mob.addTag(AquariumMod.ID+":original_no_ai");if(mob.isNoGravity())mob.addTag(AquariumMod.ID+":original_no_gravity");mob.addTag(AquariumMod.MANAGED);
  }
  mob.setNoAi(true);mob.setNoGravity(true);mob.setTarget(null);mob.setPersistenceRequired();mob.setAirSupply(mob.getMaxAirSupply());
  if(mob instanceof Pufferfish puff)puff.setPuffState(0);

  if(mob.getType()==EntityTypes.TURTLE&&mob instanceof AgeableMob ageable)ageable.setAge(-24000);
 }
 private static double height(Mob mob){return mob.getType()==EntityTypes.DROWNED||mob.getType()==EntityTypes.TURTLE?.14:.4;}
 public static ItemStack capture(ServerLevel level,Mob mob){
  mob.setNoAi(mob.entityTags().contains(AquariumMod.ID+":original_no_ai"));mob.setNoGravity(mob.entityTags().contains(AquariumMod.ID+":original_no_gravity"));
  ItemStack stack;
  if(mob instanceof Bucketable bucketable){stack=bucketable.getBucketItemStack();bucketable.saveToBucketTag(stack);}
  else {stack=new ItemStack(AquariumMod.CREATURE_BUCKET);var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());mob.saveWithoutId(out);CompoundTag data=new CompoundTag();data.putString("aquarium_type",BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString());data.put("aquarium_entity",out.buildResult());stack.set(DataComponents.CUSTOM_DATA,CustomData.of(data));stack.set(DataComponents.CUSTOM_NAME,Component.literal("Aquarium "+mob.getName().getString()+" Bucket"));}
  mob.discard();TARGETS.remove(mob);PREVIOUS.remove(mob);LANES.remove(mob);DODGE_DIR.remove(mob);DODGE_DISTANCE.remove(mob);return stack;
 }
 public static boolean summonDrowned(ServerLevel level,BlockPos pos){
  var net=Network.scan(level,pos);if(!net.complete()||net.residents(level).size()>=net.capacity())return false;
  var mob=EntityTypes.DROWNED.create(level,EntitySpawnReason.TRIGGERED);if(mob==null)return false;configure(mob);mob.setPos(pos.getX()+.5,pos.getY()+.14,pos.getZ()+.5);return level.addFreshEntity(mob);
 }
 public static void tick(ServerLevel level){List<Entity> entities=new ArrayList<>();level.getAllEntities().forEach(entities::add);tick(level,entities);}
 static void tick(ServerLevel level,List<Entity> entities){activeLevel=level;NETWORKS.clear();try{
  Set<BlockPos> checked=new HashSet<>();
  for(Entity entity:entities)if(entity instanceof Mob mob&&supported(mob.getType())&&mob.isAlive()){
   BlockPos current=mob.blockPosition();if(!Enclosures.isAquatic(level.getBlockState(current))){if(mob.entityTags().contains(AquariumMod.MANAGED)){ItemStack stack=capture(level,mob);mob.spawnAtLocation(level,stack);}continue;}
   configure(mob);
   if(level.getGameTime()%20==0&&!checked.contains(current)){var net=network(level,current);checked.addAll(net.cells());if(net.complete()){var residents=net.residents(level);for(int i=net.capacity();i<residents.size();i++){var excess=residents.get(i);ItemStack stack=capture(level,excess);excess.spawnAtLocation(level,stack);}}}
   if(!mob.isAlive())continue;
   BlockPos target=TARGETS.get(mob);if(target==null||!level.hasChunkAt(target)||!Enclosures.isAquatic(level.getBlockState(target)))target=current;
   Vec3 lane=LANES.get(mob);if(lane==null){lane=laneOffset(level,target);LANES.put(mob,lane);}
   Vec3 center=center(target,mob,lane),delta=center.subtract(mob.position());
   boolean reached=delta.lengthSqr()<.004||(mob.blockPosition().equals(target)&&blocked(level,mob,center));
   if(reached){ArrayList<BlockPos> neighbors=new ArrayList<>();for(Direction d:Direction.values()){BlockPos n=target.relative(d);if(level.hasChunkAt(n)&&Enclosures.isAquatic(level.getBlockState(n)))neighbors.add(n);}if(neighbors.size()>1)neighbors.remove(PREVIOUS.get(mob));if(!neighbors.isEmpty()){PREVIOUS.put(mob,target);target=neighbors.get(level.getRandom().nextInt(neighbors.size()));LANES.put(mob,laneOffset(level,target));}lane=LANES.getOrDefault(mob,Vec3.ZERO);center=center(target,mob,lane);delta=center.subtract(mob.position());}
   TARGETS.put(mob,target);mob.setDeltaMovement(Vec3.ZERO);
   if(delta.lengthSqr()>.0001){double normal=mob.getType()==EntityTypes.DROWNED?NORMAL_DROWNED_SPEED:NORMAL_SWIM_SPEED;Vec3 desired=delta.normalize().scale(Math.min(normal*MOVE_FACTOR,delta.length()));Vec3 step=steer(level,mob,desired,true);if(step.lengthSqr()<1.0E-8){TARGETS.remove(mob);LANES.remove(mob);continue;}boolean previous=mob.noPhysics;try{mob.noPhysics=true;mob.move(MoverType.SELF,step);}finally{mob.noPhysics=previous;}float yaw=(float)Math.toDegrees(Math.atan2(-step.x,step.z));mob.setYRot(yaw);mob.setYBodyRot(yaw);mob.setYHeadRot(yaw);}
  }
  TARGETS.keySet().removeIf(Entity::isRemoved);PREVIOUS.keySet().removeIf(Entity::isRemoved);LANES.keySet().removeIf(Entity::isRemoved);DODGE_DIR.keySet().removeIf(Entity::isRemoved);DODGE_DISTANCE.keySet().removeIf(Entity::isRemoved);
  }finally{activeLevel=null;NETWORKS.clear();}
 }
 private static Vec3 laneOffset(ServerLevel level,BlockPos pos){
  boolean tube=level.getBlockState(pos).getBlock() instanceof TubeBlock;double spread=tube?.18:.32;
  return new Vec3((level.getRandom().nextDouble()*2-1)*spread,(level.getRandom().nextDouble()*2-1)*(tube?.16:.28),(level.getRandom().nextDouble()*2-1)*spread);
 }
 private static Vec3 center(BlockPos target,Mob mob,Vec3 lane){return new Vec3(target.getX()+.5+lane.x,target.getY()+height(mob)+lane.y,target.getZ()+.5+lane.z);}
 private static Vec3 steer(ServerLevel l,Mob m,Vec3 desired,boolean vertical){
  double remaining=DODGE_DISTANCE.getOrDefault(m,0.0);
  Vec3 dodge=DODGE_DIR.get(m);
  double len=Math.max(.001,desired.length());
  double overlap=collisionPenalty(l,m,m.position());
  if(overlap>1.0E-9){
   DODGE_DISTANCE.remove(m);DODGE_DIR.remove(m);
   Direction facing=horizontalFacing(desired,m);
   ArrayList<Direction> escape=perpendicularChoices(facing);
   if((m.getUUID().hashCode()&1)!=0)Collections.reverse(escape);
   escape.add(facing.getOpposite());escape.add(facing);
   Vec3 best=Vec3.ZERO;double bestPenalty=overlap;
   for(Direction d:escape){
    Vec3 candidate=new Vec3(d.getStepX()*len,0,d.getStepZ()*len);
    if(!validStep(l,m,candidate))continue;
    double after=collisionPenalty(l,m,m.position().add(candidate));
    if(after+1.0E-10<bestPenalty){bestPenalty=after;best=candidate;}
   }
   if(best.lengthSqr()>1.0E-8)return best;
  }
  if(remaining>0&&dodge!=null){
   Vec3 candidate=dodge.scale(Math.min(len,remaining));
   if(validStep(l,m,candidate)){
    remaining-=candidate.length();
    if(remaining<=1.0E-4){DODGE_DISTANCE.remove(m);DODGE_DIR.remove(m);}else DODGE_DISTANCE.put(m,remaining);
    return candidate;
   }
   DODGE_DISTANCE.remove(m);DODGE_DIR.remove(m);
  }
  if(validStep(l,m,desired))return desired;

  Direction facing=horizontalFacing(desired,m);
  ArrayList<Direction> choices=perpendicularChoices(facing);
  if(l.getRandom().nextBoolean())Collections.reverse(choices);
  Direction reverse=facing.getOpposite();
  choices.add(reverse);
  for(Direction d:choices){
   Vec3 candidate=new Vec3(d.getStepX()*len,0,d.getStepZ()*len);
   if(validStep(l,m,candidate)){
    DODGE_DIR.put(m,new Vec3(d.getStepX(),0,d.getStepZ()));
    DODGE_DISTANCE.put(m,.70-candidate.length());
    return candidate;
   }
  }
  return Vec3.ZERO;
 }
 private static ArrayList<Direction> perpendicularChoices(Direction facing){
  if(facing==Direction.EAST||facing==Direction.WEST)return new ArrayList<>(List.of(Direction.NORTH,Direction.SOUTH));
  return new ArrayList<>(List.of(Direction.EAST,Direction.WEST));
 }
 private static Direction horizontalFacing(Vec3 desired,Mob m){
  if(Math.abs(desired.x)>1.0E-6||Math.abs(desired.z)>1.0E-6){
   if(Math.abs(desired.x)>=Math.abs(desired.z))return desired.x>=0?Direction.EAST:Direction.WEST;
   return desired.z>=0?Direction.SOUTH:Direction.NORTH;
  }
  return Direction.fromYRot(m.getYRot());
 }
 private static boolean validStep(ServerLevel l,Mob m,Vec3 step){
  Vec3 next=m.position().add(step);BlockPos p=BlockPos.containing(next.x,next.y,next.z);if(!l.hasChunkAt(p)||!Enclosures.isAquatic(l.getBlockState(p)))return false;
  double current=collisionPenalty(l,m,m.position()),after=collisionPenalty(l,m,next);
  return current>1.0E-9?after<=current+1.0E-9:after<=1.0E-9;
 }
 private static boolean blocked(ServerLevel l,Mob m,Vec3 next){return collisionPenalty(l,m,next)>1.0E-9;}
 private static double collisionPenalty(ServerLevel l,Mob m,Vec3 next){
  var net=network(l,m.blockPosition());AABB moved=m.getBoundingBox().move(next.subtract(m.position()));double total=0;
  for(var p:net.cells())if(l.getBlockEntity(p) instanceof TankBlockEntity be)total+=be.collisionPenalty(moved);
  return total;
 }
}
