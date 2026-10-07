package dev.casz.aquarium;
import java.util.*;
import com.mojang.math.Transformation;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.AABB;

public final class TankBlockEntity extends BlockEntity {
 public ItemStack decoration=ItemStack.EMPTY; // legacy migration only
 public final List<EnclosureDecoration> decorations=new ArrayList<>();

 private boolean dirty=true,migrated;private int revision;
 public TankBlockEntity(BlockPos p,BlockState s){super(AquariumMod.TANK_ENTITY,p,s);}
 public void migrateLegacy(){
  if(migrated||level==null)return;migrated=true;var state=getBlockState();int old=state.hasProperty(AquariumBlock.DECOR)?state.getValue(AquariumBlock.DECOR):0;
  if(old>0&&decoration.isEmpty())decoration=new ItemStack(Palette.item(Palette.DECORS.get(old)));
  if(!decoration.isEmpty()){decorations.add(new EnclosureDecoration(decoration,EnclosureDecoration.Anchor.FLOOR));decoration=ItemStack.EMPTY;changed();}
  if(old>0)level.setBlock(worldPosition,state.setValue(AquariumBlock.DECOR,0),3);
 }
 public void changed(){dirty=true;revision++;setChanged();if(level!=null&&!level.isClientSide()){BlockState s=getBlockState();level.sendBlockUpdated(worldPosition,s,s,Block.UPDATE_CLIENTS);}}
 public int revision(){return revision;}
 public EnclosureDecoration addDecoration(ItemStack stack,EnclosureDecoration.Anchor anchor){var d=new EnclosureDecoration(stack,anchor);decorations.add(d);changed();return d;}
 public ItemStack removeDecoration(int index){if(index<0||index>=decorations.size())return ItemStack.EMPTY;var out=decorations.remove(index).stack.copy();changed();return out;}
 public boolean collides(AABB box){return collisionPenalty(box)>1.0E-9;}
 public double collisionPenalty(AABB box){
  if(level==null)return 0;
  double total=0;
  for(var d:decorations){
   AABB obstacle=collisionBox(d);if(obstacle==null||!obstacle.intersects(box))continue;
   double x=Math.max(0,Math.min(obstacle.maxX,box.maxX)-Math.max(obstacle.minX,box.minX));
   double y=Math.max(0,Math.min(obstacle.maxY,box.maxY)-Math.max(obstacle.minY,box.minY));
   double z=Math.max(0,Math.min(obstacle.maxZ,box.maxZ)-Math.max(obstacle.minZ,box.minZ));
   total+=x*y*z;
  }
  return total;
 }
 private AABB collisionBox(EnclosureDecoration d){
  AABB local;
  if(d.stack.getItem() instanceof BlockItem bi){
   var shape=bi.getBlock().defaultBlockState().getCollisionShape(level,worldPosition);
   // Plants, flowers and other intentionally non-colliding blocks should not become invisible walls.
   if(shape.isEmpty())return null;
   local=shape.bounds();
  }else{
   // Loose item models are visual props, not near-full blocks. Keep only a small avoidance core.
   local=new AABB(.47,.47,.47,.53,.53,.53);
  }
  Matrix4f transform=new Matrix4f().translate(d.x,d.y,d.z)
   .rotateXYZ((float)Math.toRadians(d.rotX),(float)Math.toRadians(d.rotY),(float)Math.toRadians(d.rotZ))
   .scale(d.scale).translate(-.5f,-.5f,-.5f);
  double minX=Double.POSITIVE_INFINITY,minY=Double.POSITIVE_INFINITY,minZ=Double.POSITIVE_INFINITY;
  double maxX=Double.NEGATIVE_INFINITY,maxY=Double.NEGATIVE_INFINITY,maxZ=Double.NEGATIVE_INFINITY;
  for(int ix=0;ix<2;ix++)for(int iy=0;iy<2;iy++)for(int iz=0;iz<2;iz++){
   Vector3f p=transform.transformPosition((float)(ix==0?local.minX:local.maxX),(float)(iy==0?local.minY:local.maxY),(float)(iz==0?local.minZ:local.maxZ),new Vector3f());
   minX=Math.min(minX,p.x);minY=Math.min(minY,p.y);minZ=Math.min(minZ,p.z);maxX=Math.max(maxX,p.x);maxY=Math.max(maxY,p.y);maxZ=Math.max(maxZ,p.z);
  }
  return new AABB(worldPosition.getX()+minX,worldPosition.getY()+minY,worldPosition.getZ()+minZ,worldPosition.getX()+maxX,worldPosition.getY()+maxY,worldPosition.getZ()+maxZ);
 }
 public void tick(){if(!migrated)migrateLegacy();if(level instanceof ServerLevel server&&dirty)updateDisplays(server);}
 private String ownerPrefix(){return AquariumMod.ID+":decor:"+worldPosition.getX()+","+worldPosition.getY()+","+worldPosition.getZ()+":";}
 private void updateDisplays(ServerLevel server){
  String prefix=ownerPrefix();
  for(var visual:server.getEntitiesOfClass(Display.class,new AABB(worldPosition).inflate(4),d->d.entityTags().stream().anyMatch(t->t.startsWith(prefix))))visual.discard();
  dirty=false;
 }
 protected void saveAdditional(ValueOutput out){super.saveAdditional(out);if(!decoration.isEmpty())out.store("decoration",ItemStack.CODEC,decoration);out.putInt("decor_count",decorations.size());for(int i=0;i<decorations.size();i++)decorations.get(i).save(out,"decor_"+i);}
 protected void loadAdditional(ValueInput in){super.loadAdditional(in);decoration=in.read("decoration",ItemStack.CODEC).orElse(ItemStack.EMPTY);decorations.clear();int n=Math.max(0,in.getIntOr("decor_count",0));for(int i=0;i<n;i++){var d=EnclosureDecoration.load(in,"decor_"+i);if(!d.stack.isEmpty())decorations.add(d);}dirty=true;migrated=false;revision++;}
 public CompoundTag getUpdateTag(HolderLookup.Provider registries){return saveWithoutMetadata(registries);}
 public Packet<ClientGamePacketListener> getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
 public void preRemoveSideEffects(BlockPos pos,BlockState state){if(level instanceof ServerLevel server)for(var visual:server.getEntitiesOfClass(Display.class,new AABB(pos).inflate(3),d->d.entityTags().stream().anyMatch(t->t.startsWith(ownerPrefix()))))visual.discard();super.preRemoveSideEffects(pos,state);}
}
