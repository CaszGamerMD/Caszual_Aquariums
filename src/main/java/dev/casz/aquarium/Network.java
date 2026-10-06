package dev.casz.aquarium;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public record Network(Set<BlockPos> cells, int tanks, boolean complete) {
 static Network scan(ServerLevel level,BlockPos origin) {
  Set<BlockPos> cells=new HashSet<>(),seen=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();
  BlockState originState=level.getBlockState(origin);queue.add(origin);int tanks=0;boolean complete=true;
  while(!queue.isEmpty()){
   BlockPos p=queue.removeFirst();if(!seen.add(p))continue;
   if(!level.hasChunkAt(p)){complete=false;continue;}
   BlockState state=level.getBlockState(p);if(!Enclosures.matches(originState,state))continue;
   if(cells.size()>=4096){complete=false;break;}
   cells.add(p.immutable());if(Enclosures.isTank(state))tanks++;
   for(Direction d:Direction.values()){BlockPos next=p.relative(d);if(!seen.contains(next))queue.addLast(next);}
  }
  return new Network(Collections.unmodifiableSet(cells),tanks,complete);
 }
 int capacity(){return tanks*AquariumMod.fishPerBlock;}
 List<Mob> residents(ServerLevel level){
  if(cells.isEmpty())return List.of();
  int x0=Integer.MAX_VALUE,y0=x0,z0=x0,x1=Integer.MIN_VALUE,y1=x1,z1=x1;
  for(BlockPos p:cells){x0=Math.min(x0,p.getX());y0=Math.min(y0,p.getY());z0=Math.min(z0,p.getZ());x1=Math.max(x1,p.getX());y1=Math.max(y1,p.getY());z1=Math.max(z1,p.getZ());}
  boolean land=Enclosures.isLand(level.getBlockState(cells.iterator().next()));
  AABB bounds=new AABB(x0,y0,z0,x1+1,y1+1,z1+1);
  return level.getEntitiesOfClass(Mob.class,bounds,m->(land?Terrestrial.supported(m):Inhabitants.supported(m.getType()))&&cells.contains(m.blockPosition()));
 }
 List<AbstractFish> fish(ServerLevel level){return residents(level).stream().filter(AbstractFish.class::isInstance).map(AbstractFish.class::cast).toList();}
}
