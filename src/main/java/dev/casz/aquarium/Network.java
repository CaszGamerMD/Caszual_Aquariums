package dev.casz.aquarium;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.phys.AABB;

public record Network(Set<BlockPos> cells, int tanks, boolean complete) {
    static Network scan(ServerLevel level,BlockPos origin) {
        Set<BlockPos> visited=new HashSet<>(); ArrayDeque<BlockPos> queue=new ArrayDeque<>();
        queue.add(origin); int tanks=0; boolean complete=true;
        while(!queue.isEmpty()) {
            BlockPos p=queue.removeFirst();
            if(visited.contains(p))continue;
            if(!level.hasChunkAt(p)){complete=false;continue;}
            var state=level.getBlockState(p);
            if(!Enclosures.matches(level.getBlockState(origin),state))continue;
            if(visited.size()>=4096){complete=false;break;}
            visited.add(p.immutable()); if(Enclosures.isTank(state))tanks++;
            for(Direction d:Direction.values())queue.add(p.relative(d));
        }
        return new Network(visited,tanks,complete);
    }
    int capacity() { return tanks*AquariumMod.fishPerBlock; }
    List<net.minecraft.world.entity.Mob> residents(ServerLevel level) {
        if(cells.isEmpty())return List.of();
        int x0=Integer.MAX_VALUE,y0=x0,z0=x0,x1=Integer.MIN_VALUE,y1=x1,z1=x1;
        for(BlockPos p:cells){x0=Math.min(x0,p.getX());y0=Math.min(y0,p.getY());z0=Math.min(z0,p.getZ());x1=Math.max(x1,p.getX());y1=Math.max(y1,p.getY());z1=Math.max(z1,p.getZ());}
        return level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,new AABB(x0,y0,z0,x1+1,y1+1,z1+1),f->(Enclosures.isLand(level.getBlockState(cells.iterator().next()))?Terrestrial.supported(f):Inhabitants.supported(f.getType())) && cells.contains(f.blockPosition()));
    }
    List<AbstractFish> fish(ServerLevel level){return residents(level).stream().filter(AbstractFish.class::isInstance).map(AbstractFish.class::cast).toList();}
}
