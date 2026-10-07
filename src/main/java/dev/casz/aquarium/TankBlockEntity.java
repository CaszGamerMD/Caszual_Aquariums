package dev.casz.aquarium;
import java.util.*;
import com.mojang.math.Transformation;
import org.joml.Matrix4f;
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
 public boolean collides(AABB box){for(var d:decorations){float base=d.stack.getItem() instanceof BlockItem?.45f:.18f;float half=Math.max(.06f,d.scale*base);double cx=worldPosition.getX()+d.x,cy=worldPosition.getY()+d.y,cz=worldPosition.getZ()+d.z;if(new AABB(cx-half,cy-half,cz-half,cx+half,cy+half,cz+half).intersects(box))return true;}return false;}
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
