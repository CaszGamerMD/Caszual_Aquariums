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
 private static final Map<Mob,BlockPos> TARGETS=new WeakHashMap<>(),PREVIOUS=new WeakHashMap<>();
 private static final Map<Mob,Vec3> LANES=new WeakHashMap<>();
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
  mob.discard();TARGETS.remove(mob);PREVIOUS.remove(mob);LANES.remove(mob);return stack;
 }
 public static boolean summonDrowned(ServerLevel level,BlockPos pos){
  var net=Network.scan(level,pos);if(!net.complete()||net.residents(level).size()>=net.capacity())return false;
  var mob=EntityTypes.DROWNED.create(level,EntitySpawnReason.TRIGGERED);if(mob==null)return false;configure(mob);mob.setPos(pos.getX()+.5,pos.getY()+.14,pos.getZ()+.5);return level.addFreshEntity(mob);
 }
 public static void tick(ServerLevel level){
  Set<BlockPos> checked=new HashSet<>();List<Entity> entities=new ArrayList<>();level.getAllEntities().forEach(entities::add);
  for(Entity entity:entities)if(entity instanceof Mob mob&&supported(mob.getType())&&mob.isAlive()){
   BlockPos current=mob.blockPosition();if(!Enclosures.isAquatic(level.getBlockState(current))){if(mob.entityTags().contains(AquariumMod.MANAGED)){ItemStack stack=capture(level,mob);mob.spawnAtLocation(level,stack);}continue;}
   configure(mob);
   if(level.getGameTime()%20==0&&!checked.contains(current)){var net=Network.scan(level,current);checked.addAll(net.cells());if(net.complete()){var residents=net.residents(level);for(int i=net.capacity();i<residents.size();i++){var excess=residents.get(i);ItemStack stack=capture(level,excess);excess.spawnAtLocation(level,stack);}}}
   if(!mob.isAlive())continue;
   BlockPos target=TARGETS.get(mob);if(target==null||!level.hasChunkAt(target)||!Enclosures.isAquatic(level.getBlockState(target)))target=current;
   Vec3 lane=LANES.get(mob);if(lane==null){lane=laneOffset(level,target);LANES.put(mob,lane);}
   Vec3 center=center(target,mob,lane),delta=center.subtract(mob.position());
   if(delta.lengthSqr()<.004){ArrayList<BlockPos> neighbors=new ArrayList<>();for(Direction d:Direction.values()){BlockPos n=target.relative(d);if(level.hasChunkAt(n)&&Enclosures.isAquatic(level.getBlockState(n)))neighbors.add(n);}if(neighbors.size()>1)neighbors.remove(PREVIOUS.get(mob));if(!neighbors.isEmpty()){PREVIOUS.put(mob,target);target=neighbors.get(level.getRandom().nextInt(neighbors.size()));LANES.put(mob,laneOffset(level,target));}lane=LANES.getOrDefault(mob,Vec3.ZERO);center=center(target,mob,lane);delta=center.subtract(mob.position());}
   TARGETS.put(mob,target);mob.setDeltaMovement(Vec3.ZERO);
   if(delta.lengthSqr()>.0001){Vec3 step=delta.normalize().scale(Math.min(mob.getType()==EntityTypes.DROWNED?.015:.035,delta.length()));if(blocked(level,mob,mob.position().add(step))){TARGETS.remove(mob);continue;}boolean previous=mob.noPhysics;try{mob.noPhysics=true;mob.move(MoverType.SELF,step);}finally{mob.noPhysics=previous;}float yaw=(float)Math.toDegrees(Math.atan2(-step.x,step.z));mob.setYRot(yaw);mob.setYBodyRot(yaw);mob.setYHeadRot(yaw);}
  }
  TARGETS.keySet().removeIf(Entity::isRemoved);PREVIOUS.keySet().removeIf(Entity::isRemoved);
 }
 private static Vec3 laneOffset(ServerLevel level,BlockPos pos){
  boolean tube=level.getBlockState(pos).getBlock() instanceof TubeBlock;double spread=tube?.18:.32;
  return new Vec3((level.getRandom().nextDouble()*2-1)*spread,(level.getRandom().nextDouble()*2-1)*(tube?.16:.28),(level.getRandom().nextDouble()*2-1)*spread);
 }
 private static Vec3 center(BlockPos target,Mob mob,Vec3 lane){return new Vec3(target.getX()+.5+lane.x,target.getY()+height(mob)+lane.y,target.getZ()+.5+lane.z);}
 private static boolean blocked(ServerLevel l,Mob m,Vec3 next){var net=Network.scan(l,m.blockPosition());AABB moved=m.getBoundingBox().move(next.subtract(m.position()));for(var p:net.cells())if(l.getBlockEntity(p) instanceof TankBlockEntity be)for(var box:be.collisionBoxes())if(box.intersects(moved))return true;return false;}
}
