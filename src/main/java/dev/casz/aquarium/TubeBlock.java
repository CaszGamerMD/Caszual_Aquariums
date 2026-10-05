package dev.casz.aquarium;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;
public class TubeBlock extends AquariumBlock {
 public static final net.minecraft.world.level.block.state.properties.BooleanProperty[] TANK_LINKS=java.util.Arrays.stream(new String[]{"down","up","north","south","west","east"}).map(d->net.minecraft.world.level.block.state.properties.BooleanProperty.create("tank_"+d)).toArray(net.minecraft.world.level.block.state.properties.BooleanProperty[]::new);
 public static final MapCodec<TubeBlock> CODEC=simpleCodec(TubeBlock::new);
 private static final VoxelShape[] SHAPES=new VoxelShape[64];
 public TubeBlock(Properties p){super(p);}
 protected MapCodec<? extends Block> codec(){return CODEC;}
 protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
  int mask=0;for(Direction d:Direction.values())if(s.getValue(LINKS[d.ordinal()]))mask|=1<<d.ordinal();
  if(SHAPES[mask]!=null)return SHAPES[mask];
  VoxelShape inside=box(2,2,2,14,14,14);
  for(Direction d:Direction.values())if(s.getValue(LINKS[d.ordinal()]))inside=Shapes.or(inside,switch(d){
   case DOWN->box(2,0,2,14,14,14);case UP->box(2,2,2,14,16,14);
   case NORTH->box(2,2,0,14,14,14);case SOUTH->box(2,2,2,14,14,16);
   case WEST->box(0,2,2,14,14,14);case EAST->box(2,2,2,16,14,14);
  });
  return SHAPES[mask]=Shapes.join(Shapes.block(),inside,BooleanOp.ONLY_FIRST);
 }
}