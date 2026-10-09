package dev.casz.aquarium;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.AABB;

public final class TankBlockEntity extends BlockEntity {
 public ItemStack decoration=ItemStack.EMPTY; // legacy migration only
 public final List<EnclosureDecoration> decorations=new ArrayList<>();

 private boolean migrated,legacyVisualsCleaned;
 private int revision;

 public TankBlockEntity(BlockPos p,BlockState s){super(AquariumMod.TANK_ENTITY,p,s);}
 public int revision(){return revision;}

 public void migrateLegacy(){
  if(migrated||level==null)return;migrated=true;var state=getBlockState();int old=state.hasProperty(AquariumBlock.DECOR)?state.getValue(AquariumBlock.DECOR):0;
  if(old>0&&decoration.isEmpty())decoration=new ItemStack(Palette.item(Palette.DECORS.get(old)));
  if(!decoration.isEmpty()){decorations.add(new EnclosureDecoration(decoration,EnclosureDecoration.Anchor.FLOOR));decoration=ItemStack.EMPTY;changed();}
  if(old>0)level.setBlock(worldPosition,state.setValue(AquariumBlock.DECOR,0),3);
 }

 public void changed(){
  revision++;setChanged();
  if(level instanceof ServerLevel server&&server.getBlockEntity(worldPosition)==this)server.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
 }

 public EnclosureDecoration addDecoration(ItemStack stack,EnclosureDecoration.Anchor anchor){var d=new EnclosureDecoration(stack,anchor);decorations.add(d);changed();return d;}
 public ItemStack removeDecoration(int index){if(index<0||index>=decorations.size())return ItemStack.EMPTY;var out=decorations.remove(index).stack.copy();changed();return out;}

 public double collisionPenalty(AABB box){
  double total=0;
  for(var d:decorations){
   float half=Math.max(.08f,d.scale*.45f);double cx=worldPosition.getX()+d.x,cy=worldPosition.getY()+d.y,cz=worldPosition.getZ()+d.z;
   AABB decor=new AABB(cx-half,cy-half,cz-half,cx+half,cy+half,cz+half);
   double dx=Math.max(0,Math.min(box.maxX,decor.maxX)-Math.max(box.minX,decor.minX));
   double dy=Math.max(0,Math.min(box.maxY,decor.maxY)-Math.max(box.minY,decor.minY));
   double dz=Math.max(0,Math.min(box.maxZ,decor.maxZ)-Math.max(box.minZ,decor.minZ));
   total+=dx*dy*dz;
  }
  return total;
 }
 public boolean collides(AABB box){return collisionPenalty(box)>1.0E-7;}

 public void tick(){
  if(!migrated)migrateLegacy();
  if(level instanceof ServerLevel server&&!legacyVisualsCleaned){cleanupLegacyDisplays(server);legacyVisualsCleaned=true;}
 }

 private String ownerPrefix(){return AquariumMod.ID+":decor:"+worldPosition.getX()+","+worldPosition.getY()+","+worldPosition.getZ()+":";}
 private void cleanupLegacyDisplays(ServerLevel server){
  String prefix=ownerPrefix();
  for(var visual:server.getEntitiesOfClass(Display.class,new AABB(worldPosition).inflate(3),d->d.entityTags().stream().anyMatch(t->t.startsWith(prefix))))visual.discard();
 }

 protected void saveAdditional(ValueOutput out){
  super.saveAdditional(out);if(!decoration.isEmpty())out.store("decoration",ItemStack.CODEC,decoration);
  out.putInt("decor_count",decorations.size());for(int i=0;i<decorations.size();i++)decorations.get(i).save(out,"decor_"+i);
 }
 protected void loadAdditional(ValueInput in){
  super.loadAdditional(in);decoration=in.read("decoration",ItemStack.CODEC).orElse(ItemStack.EMPTY);decorations.clear();
  int n=Math.max(0,in.getIntOr("decor_count",0));for(int i=0;i<n;i++){var d=EnclosureDecoration.load(in,"decor_"+i);if(!d.stack.isEmpty())decorations.add(d);}
  migrated=false;legacyVisualsCleaned=false;revision++;
 }

 public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
 public CompoundTag getUpdateTag(HolderLookup.Provider provider){return saveCustomOnly(provider);}

 public void preRemoveSideEffects(BlockPos pos,BlockState state){
  if(level instanceof ServerLevel server)cleanupLegacyDisplays(server);
  super.preRemoveSideEffects(pos,state);
 }
}
