package dev.casz.aquarium;
import java.util.*;
import com.mojang.math.Transformation;
import org.joml.Matrix4f;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
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
 public int revision(){return revision;}
 public void changed(){dirty=true;revision++;setChanged();if(level instanceof ServerLevel server&&server.getBlockEntity(worldPosition)==this)server.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
 public EnclosureDecoration addDecoration(ItemStack stack,EnclosureDecoration.Anchor anchor){var d=new EnclosureDecoration(stack,anchor);decorations.add(d);changed();return d;}
 public ItemStack removeDecoration(int index){if(index<0||index>=decorations.size())return ItemStack.EMPTY;var out=decorations.remove(index).stack.copy();changed();return out;}
 private AABB decorationBox(EnclosureDecoration d){float half=Math.max(.08f,d.scale*.45f);double cx=worldPosition.getX()+d.x,cy=worldPosition.getY()+d.y,cz=worldPosition.getZ()+d.z;return new AABB(cx-half,cy-half,cz-half,cx+half,cy+half,cz+half);}
 public boolean collides(AABB box){for(var d:decorations)if(decorationBox(d).intersects(box))return true;return false;}
 public double collisionPenalty(AABB box){double total=0;for(var d:decorations){AABB obstacle=decorationBox(d);double x=Math.max(0,Math.min(box.maxX,obstacle.maxX)-Math.max(box.minX,obstacle.minX));double y=Math.max(0,Math.min(box.maxY,obstacle.maxY)-Math.max(box.minY,obstacle.minY));double z=Math.max(0,Math.min(box.maxZ,obstacle.maxZ)-Math.max(box.minZ,obstacle.minZ));total+=x*y*z;}return total;}
 public void tick(){if(!migrated)migrateLegacy();if(level instanceof ServerLevel server&&dirty)clearLegacyDisplays(server);}
 private String ownerPrefix(){return AquariumMod.ID+":decor:"+worldPosition.getX()+","+worldPosition.getY()+","+worldPosition.getZ()+":";}
 private void clearLegacyDisplays(ServerLevel server){
  String prefix=ownerPrefix();for(var visual:server.getEntitiesOfClass(Display.class,new AABB(worldPosition).inflate(3),d->d.entityTags().stream().anyMatch(t->t.startsWith(prefix))))visual.discard();dirty=false;
 }
 protected void saveAdditional(ValueOutput out){super.saveAdditional(out);if(!decoration.isEmpty())out.store("decoration",ItemStack.CODEC,decoration);out.putInt("decor_count",decorations.size());for(int i=0;i<decorations.size();i++)decorations.get(i).save(out,"decor_"+i);}
 protected void loadAdditional(ValueInput in){super.loadAdditional(in);decoration=in.read("decoration",ItemStack.CODEC).orElse(ItemStack.EMPTY);decorations.clear();int n=Math.max(0,in.getIntOr("decor_count",0));for(int i=0;i<n;i++){var d=EnclosureDecoration.load(in,"decor_"+i);if(!d.stack.isEmpty())decorations.add(d);}dirty=true;migrated=false;revision++;}
 public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
 public net.minecraft.nbt.CompoundTag getUpdateTag(HolderLookup.Provider provider){return saveCustomOnly(provider);}
 public void preRemoveSideEffects(BlockPos pos,BlockState state){if(level instanceof ServerLevel server)for(var visual:server.getEntitiesOfClass(Display.class,new AABB(pos).inflate(3),d->d.entityTags().stream().anyMatch(t->t.startsWith(ownerPrefix()))))visual.discard();super.preRemoveSideEffects(pos,state);}
}
