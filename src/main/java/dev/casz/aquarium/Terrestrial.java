package dev.casz.aquarium;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;

public final class Terrestrial {
 private static final double MOVE_FACTOR=.80,NORMAL_GROUND_SPEED=.075,NORMAL_FLY_SPEED=.055;
 public static final String MANAGED=AquariumMod.ID+":terrarium_managed",ORIGINAL_SCALE=AquariumMod.ID+":original_scale=";
 public static final TagKey<EntityType<?>> FLYING=TagKey.create(Registries.ENTITY_TYPE,AquariumMod.id("flying"));
 private static final Map<Mob,BlockPos> TARGETS=new WeakHashMap<>(),PREVIOUS=new WeakHashMap<>();
 private static final Map<Mob,Vec3> DODGE_DIR=new WeakHashMap<>();
 private static final Map<Mob,Double> DODGE_DISTANCE=new WeakHashMap<>();
 private static ServerLevel activeLevel;
 private static final Map<BlockPos,Chamber> CHAMBERS=new HashMap<>();private static final Map<BlockPos,Network> NETWORKS=new HashMap<>();
 private static Network network(ServerLevel l,BlockPos pos){if(l==activeLevel){var cached=NETWORKS.get(pos);if(cached!=null)return cached;}var net=Network.scan(l,pos);if(l==activeLevel)for(var p:net.cells())NETWORKS.put(p,net);return net;}
 public record Chamber(Set<BlockPos> cells,int bottom,int top){public int height(){return top-bottom+1;}}
 public static boolean supported(Mob m){String id=BuiltInRegistries.ENTITY_TYPE.getKey(m.getType()).getPath();return !m.getType().getCategory().name().contains("WATER")&&!Set.of("ender_dragon","wither","cod","salmon","pufferfish","tropical_fish","squid","glow_squid","axolotl","dolphin","guardian","elder_guardian").contains(id)&&!m.entityTags().contains(AquariumMod.MANAGED);}
 public static boolean hostile(Mob m){return m.getType().getCategory()==MobCategory.MONSTER;}
 public static boolean flying(Mob m){return m.getMoveControl() instanceof net.minecraft.world.entity.ai.control.FlyingMoveControl||m.getNavigation() instanceof net.minecraft.world.entity.ai.navigation.FlyingPathNavigation||BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(m.getType()).is(FLYING);}
 public static Chamber chamber(ServerLevel l,BlockPos origin){
  if(l==activeLevel&&CHAMBERS.containsKey(origin))return CHAMBERS.get(origin);
  Set<BlockPos> cells=new HashSet<>();ArrayDeque<BlockPos> q=new ArrayDeque<>();q.add(origin);int low=origin.getY(),high=low;var start=l.getBlockState(origin);
  while(!q.isEmpty()&&cells.size()<4096){var p=q.removeFirst();if(cells.contains(p)||!l.hasChunkAt(p))continue;var s=l.getBlockState(p);if(!Enclosures.isTank(s)||!Enclosures.matches(start,s))continue;cells.add(p);low=Math.min(low,p.getY());high=Math.max(high,p.getY());for(Direction d:Direction.values())q.add(p.relative(d));}
  var result=new Chamber(cells,low,high);if(l==activeLevel)for(var p:cells)CHAMBERS.put(p,result);return result;
 }
 public static boolean groundEntry(ServerLevel l,BlockPos tank){return tank.getY()==chamber(l,tank).bottom();}
 public static boolean flyingEntry(ServerLevel l,BlockPos tank){var c=chamber(l,tank);return c.height()>4&&tank.getY()-c.bottom()>=3;}
 public static boolean canStep(ServerLevel l,BlockPos from,BlockPos to,boolean flying){
  var a=l.getBlockState(from);var b=l.getBlockState(to);if(!Enclosures.isLand(a)||!Enclosures.matches(a,b))return false;
  boolean at=Enclosures.isTank(a),bt=Enclosures.isTank(b);
  if(flying){if(at&&chamber(l,from).height()<=4||bt&&chamber(l,to).height()<=4)return false;if(at&&!bt&&!flyingEntry(l,from)||!at&&bt&&!flyingEntry(l,to))return false;return true;}
  if(from.getY()!=to.getY())return false;
  return (!at||groundEntry(l,from))&&(!bt||groundEntry(l,to));
 }
 public static boolean filled(ItemStack net){return net.is(AquariumMod.MOB_NET)&&net.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().contains("terrarium_type");}
 public static void restore(Mob m){
  if(!m.entityTags().contains(MANAGED))return;
  m.setNoAi(m.entityTags().contains(MANAGED+":no_ai"));m.setNoGravity(m.entityTags().contains(MANAGED+":no_gravity"));
  for(String tag:new HashSet<>(m.entityTags())){if(tag.startsWith(ORIGINAL_SCALE)){try{var attr=m.getAttribute(Attributes.SCALE);if(attr!=null)attr.setBaseValue(Double.parseDouble(tag.substring(ORIGINAL_SCALE.length())));}catch(NumberFormatException ignored){}m.removeTag(tag);}if(tag.equals(MANAGED)||tag.startsWith(MANAGED+":"))m.removeTag(tag);}
 }
 public static ItemStack capture(ServerLevel l,Mob m){
  restore(m);var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,l.registryAccess());m.saveWithoutId(out);
  CompoundTag tag=new CompoundTag();tag.putString("terrarium_type",BuiltInRegistries.ENTITY_TYPE.getKey(m.getType()).toString());tag.put("terrarium_entity",out.buildResult());
  var stack=new ItemStack(AquariumMod.MOB_NET);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));stack.set(DataComponents.CUSTOM_NAME,Component.literal(m.getName().getString()+" in Mob Net"));m.discard();TARGETS.remove(m);PREVIOUS.remove(m);DODGE_DIR.remove(m);DODGE_DISTANCE.remove(m);return stack;
 }
 public static Mob load(ServerLevel l,ItemStack net){
  var tag=net.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();var id=Identifier.tryParse(tag.getString("terrarium_type").orElse(""));var type=id==null?null:BuiltInRegistries.ENTITY_TYPE.getValue(id);
  if(type==null||!(type.create(l,EntitySpawnReason.TRIGGERED) instanceof Mob m))return null;
  var data=tag.getCompound("terrarium_entity").orElse(new CompoundTag());data.remove("UUID");data.remove("uuid");m.load(TagValueInput.create(ProblemReporter.DISCARDING,l.registryAccess(),data));m.setUUID(UUID.randomUUID());if(net.has(DataComponents.CUSTOM_NAME)){String n=net.getHoverName().getString().replace(" in Mob Net","");m.setCustomName(Component.literal(n));}return m;
 }
 public static String add(ServerLevel l,BlockPos tank,ItemStack net){
  if(!filled(net))return "Catch a mob with an empty net first.";var state=l.getBlockState(tank);if(!Enclosures.isLand(state)||!Enclosures.isTank(state))return "Use the net on a terrarium tank.";
  var m=load(l,net);if(m==null||!supported(m))return "This mob cannot live in a terrarium.";
  if(hostile(m)!=(Enclosures.kind(state)==2))return "This mob needs a matching passive or hostile terrarium.";
  var network=Network.scan(l,tank);if(!network.complete()||network.residents(l).size()>=network.capacity())return "No room, or terrarium network not fully loaded.";
  var c=chamber(l,tank);if(flying(m)&&c.height()<=4)return "Flying mobs need a cage at least five blocks tall.";
  BlockPos spawn=c.cells().stream().filter(p->p.getY()==c.bottom()).min(Comparator.comparingDouble(p->p.distSqr(tank))).orElse(tank);
  configure(m);m.setPos(spawn.getX()+.5,spawn.getY()+(flying(m)?.5:.18),spawn.getZ()+.5);
  if(!l.addFreshEntity(m))return "Could not add the mob.";
  net.set(DataComponents.CUSTOM_DATA,CustomData.EMPTY);net.remove(DataComponents.CUSTOM_NAME);return null;
 }
 public static String release(ServerLevel l,BlockPos pos,ItemStack net){
  var m=load(l,net);if(m==null)return "The net has no supported mob.";
  m.setPos(pos.getX()+.5,pos.getY()+.05,pos.getZ()+.5);if(!l.noCollision(m))return "There is not enough room to release the mob.";
  if(!l.addFreshEntity(m))return "Could not release the mob.";net.set(DataComponents.CUSTOM_DATA,CustomData.EMPTY);net.remove(DataComponents.CUSTOM_NAME);return null;
 }
 public static void configure(Mob m){
  if(!m.entityTags().contains(MANAGED)){if(m.isNoAi())m.addTag(MANAGED+":no_ai");if(m.isNoGravity())m.addTag(MANAGED+":no_gravity");var attr=m.getAttribute(Attributes.SCALE);if(attr!=null)m.addTag(ORIGINAL_SCALE+attr.getBaseValue());m.addTag(MANAGED);}
  m.setNoAi(true);m.setNoGravity(true);m.setTarget(null);m.setPersistenceRequired();m.setAirSupply(m.getMaxAirSupply());m.setRemainingFireTicks(0);var attr=m.getAttribute(Attributes.SCALE);if(attr!=null){double original=1;for(String tag:m.entityTags())if(tag.startsWith(ORIGINAL_SCALE))try{original=Double.parseDouble(tag.substring(ORIGINAL_SCALE.length()));}catch(NumberFormatException ignored){}attr.setBaseValue(original*.75);}
 }
 public static void tick(ServerLevel l){List<Entity> all=new ArrayList<>();l.getAllEntities().forEach(all::add);tick(l,all);}
 static void tick(ServerLevel l,List<Entity> all){
  activeLevel=l;CHAMBERS.clear();NETWORKS.clear();try{
  Set<BlockPos> checked=new HashSet<>();
  for(Entity e:all)if(e instanceof Mob m&&m.isAlive()&&m.entityTags().contains(MANAGED)){
   var current=m.blockPosition();var state=l.getBlockState(current);
   if(!Enclosures.isLand(state)||hostile(m)!=(Enclosures.kind(state)==2)){var stack=capture(l,m);m.spawnAtLocation(l,stack);continue;}
   configure(m);
   if(l.getGameTime()%20==0&&!checked.contains(current)){var net=network(l,current);checked.addAll(net.cells());if(net.complete()){var residents=net.residents(l);for(int i=net.capacity();i<residents.size();i++){var extra=residents.get(i);var stack=capture(l,extra);extra.spawnAtLocation(l,stack);}}}
   if(m.isRemoved())continue;boolean air=flying(m);
   if(Enclosures.isTank(state)&&air&&chamber(l,current).height()<=4&&network(l,current).complete()){var stack=capture(l,m);m.spawnAtLocation(l,stack);continue;}
   BlockPos target=TARGETS.get(m);if(target==null||!Enclosures.matches(state,l.getBlockState(target))||!target.equals(current)&&!canStep(l,current,target,air))target=current;
   Vec3 center=Vec3.atLowerCornerOf(target).add(.5,air?.5:.18,.5),delta=center.subtract(m.position());
   boolean reached=delta.lengthSqr()<.003||(m.blockPosition().equals(target)&&blocked(l,m,center));
   if(reached){var neighbors=new ArrayList<BlockPos>();for(Direction d:Direction.values()){var p=target.relative(d);if(l.hasChunkAt(p)&&canStep(l,target,p,air))neighbors.add(p);}if(neighbors.size()>1)neighbors.remove(PREVIOUS.get(m));if(!neighbors.isEmpty()){PREVIOUS.put(m,target);target=neighbors.get(l.getRandom().nextInt(neighbors.size()));}center=Vec3.atLowerCornerOf(target).add(.5,air?.5:.18,.5);delta=center.subtract(m.position());}
   TARGETS.put(m,target);m.setDeltaMovement(Vec3.ZERO);if(delta.lengthSqr()>.0001){double normal=air?NORMAL_FLY_SPEED:NORMAL_GROUND_SPEED;Vec3 desired=delta.normalize().scale(Math.min(normal*MOVE_FACTOR,delta.length()));Vec3 step=steer(l,m,desired,air);if(step.lengthSqr()<1.0E-8){TARGETS.remove(m);continue;}boolean old=m.noPhysics;try{m.noPhysics=true;m.move(MoverType.SELF,step);}finally{m.noPhysics=old;}float yaw=(float)Math.toDegrees(Math.atan2(-step.x,step.z));m.setYRot(yaw);m.setYBodyRot(yaw);m.setYHeadRot(yaw);m.walkAnimation.update((float)(step.horizontalDistance()*12.0),1.0f,1.0f);}
  }
  TARGETS.keySet().removeIf(Entity::isRemoved);PREVIOUS.keySet().removeIf(Entity::isRemoved);DODGE_DIR.keySet().removeIf(Entity::isRemoved);DODGE_DISTANCE.keySet().removeIf(Entity::isRemoved);
  }finally{activeLevel=null;CHAMBERS.clear();NETWORKS.clear();}
 }
 private static Vec3 steer(ServerLevel l,Mob m,Vec3 desired,boolean air){
  double remaining=DODGE_DISTANCE.getOrDefault(m,0.0);
  Vec3 dodge=DODGE_DIR.get(m);
  double len=Math.max(.001,desired.length());
  double overlap=collisionPenalty(l,m,m.position());

  // If decor was moved onto a resident, lock one escape direction until overlap is zero.
  if(overlap>1.0E-9){
   if(remaining<0&&dodge!=null){
    Vec3 candidate=dodge.scale(len);
    if(validStep(l,m,candidate,air)){
     double after=collisionPenalty(l,m,m.position().add(candidate));
     if(after+1.0E-10<overlap)return candidate;
    }
    DODGE_DISTANCE.remove(m);DODGE_DIR.remove(m);dodge=null;remaining=0;
   }
   Direction facing=horizontalFacing(desired,m);
   ArrayList<Direction> escape=perpendicularChoices(facing);
   if((m.getUUID().hashCode()&1)!=0)Collections.reverse(escape);
   escape.add(facing.getOpposite());escape.add(facing);
   Vec3 best=Vec3.ZERO;double bestPenalty=overlap;
   for(Direction d:escape){
    Vec3 candidate=new Vec3(d.getStepX()*len,0,d.getStepZ()*len);
    if(!validStep(l,m,candidate,air))continue;
    double after=collisionPenalty(l,m,m.position().add(candidate));
    if(after+1.0E-10<bestPenalty){bestPenalty=after;best=candidate;}
   }
   if(best.lengthSqr()>1.0E-8){
    DODGE_DIR.put(m,best.normalize());DODGE_DISTANCE.put(m,-1.0);
    return best;
   }
   return Vec3.ZERO;
  }else if(remaining<0){
   DODGE_DISTANCE.remove(m);DODGE_DIR.remove(m);dodge=null;remaining=0;
  }

  if(remaining>0&&dodge!=null){
   Vec3 candidate=dodge.scale(Math.min(len,remaining));
   if(validStep(l,m,candidate,air)){
    remaining-=candidate.length();
    if(remaining<=1.0E-4){DODGE_DISTANCE.remove(m);DODGE_DIR.remove(m);}else DODGE_DISTANCE.put(m,remaining);
    return candidate;
   }
   DODGE_DISTANCE.remove(m);DODGE_DIR.remove(m);
  }
  if(validStep(l,m,desired,air))return desired;

  Direction facing=horizontalFacing(desired,m);
  ArrayList<Direction> choices=perpendicularChoices(facing);
  if(l.getRandom().nextBoolean())Collections.reverse(choices);
  choices.add(facing.getOpposite());
  for(Direction d:choices){
   Vec3 candidate=new Vec3(d.getStepX()*len,0,d.getStepZ()*len);
   if(validStep(l,m,candidate,air)){
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
 private static boolean validStep(ServerLevel l,Mob m,Vec3 step,boolean air){
  Vec3 next=m.position().add(step);BlockPos from=m.blockPosition(),to=BlockPos.containing(next.x,next.y,next.z);if(!l.hasChunkAt(to)||!Enclosures.isLand(l.getBlockState(to)))return false;if(!to.equals(from)&&!canStep(l,from,to,air))return false;
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
