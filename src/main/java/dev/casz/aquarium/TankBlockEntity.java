package dev.casz.aquarium;
import java.util.*;
import com.mojang.math.Transformation;
import org.joml.Matrix4f;
import net.minecraft.core.BlockPos;
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

 private boolean dirty=true,migrated;
 public TankBlockEntity(BlockPos p,BlockState s){super(AquariumMod.TANK_ENTITY,p,s);}
 public void migrateLegacy(){
  if(migrated||level==null)return;migrated=true;var state=getBlockState();int old=state.hasProperty(AquariumBlock.DECOR)?state.getValue(AquariumBlock.DECOR):0;
  if(old>0&&decoration.isEmpty())decoration=new ItemStack(Palette.item(Palette.DECORS.get(old)));
  if(!decoration.isEmpty()){decorations.add(new EnclosureDecoration(decoration,EnclosureDecoration.Anchor.FLOOR));decoration=ItemStack.EMPTY;changed();}
  if(old>0)level.setBlock(worldPosition,state.setValue(AquariumBlock.DECOR,0),3);
 }
 public void changed(){dirty=true;setChanged();}
 public EnclosureDecoration addDecoration(ItemStack stack,EnclosureDecoration.Anchor anchor){var d=new EnclosureDecoration(stack,anchor);decorations.add(d);changed();return d;}
 public ItemStack removeDecoration(int index){if(index<0||index>=decorations.size())return ItemStack.EMPTY;var out=decorations.remove(index).stack.copy();changed();return out;}
 public boolean collides(AABB box){for(var d:decorations){float base=d.stack.getItem() instanceof BlockItem?.45f:.28f;float half=Math.max(.06f,d.scale*base);double cx=worldPosition.getX()+d.x,cy=worldPosition.getY()+d.y,cz=worldPosition.getZ()+d.z;if(new AABB(cx-half,cy-half,cz-half,cx+half,cy+half,cz+half).intersects(box))return true;}return false;}
 public void tick(){if(!migrated)migrateLegacy();if(level instanceof ServerLevel server&&dirty)updateDisplays(server);}
 private String ownerPrefix(){return AquariumMod.ID+":decor:"+worldPosition.getX()+","+worldPosition.getY()+","+worldPosition.getZ()+":";}
 private void updateDisplays(ServerLevel server){
  String prefix=ownerPrefix();Map<Integer,Display> existing=new HashMap<>();
  for(var visual:server.getEntitiesOfClass(Display.class,new AABB(worldPosition).inflate(3),d->d.entityTags().stream().anyMatch(t->t.startsWith(prefix)))){
   Integer index=null;for(String tag:visual.entityTags())if(tag.startsWith(prefix))try{index=Integer.parseInt(tag.substring(prefix.length()));}catch(NumberFormatException ignored){}
   if(index==null||existing.putIfAbsent(index,visual)!=null)visual.discard();
  }
  for(int i=0;i<decorations.size();i++){var d=decorations.get(i);boolean blockItem=d.stack.getItem() instanceof BlockItem;Display visual=existing.remove(i);
   if(visual!=null&&blockItem!=(visual instanceof Display.BlockDisplay)){visual.discard();visual=null;}
   if(visual==null){if(blockItem){var block=EntityTypes.BLOCK_DISPLAY.create(server,EntitySpawnReason.TRIGGERED);if(block==null)continue;visual=block;}else{var item=EntityTypes.ITEM_DISPLAY.create(server,EntitySpawnReason.TRIGGERED);if(item==null)continue;visual=item;}visual.addTag(prefix+i);server.addFreshEntity(visual);}
   if(visual instanceof Display.BlockDisplay block&&d.stack.getItem() instanceof BlockItem bi)block.setBlockState(bi.getBlock().defaultBlockState());
   else if(visual instanceof Display.ItemDisplay item){item.setItemStack(d.stack.copy());item.setItemTransform(ItemDisplayContext.FIXED);}
   visual.setPos(worldPosition.getX(),worldPosition.getY(),worldPosition.getZ());
   visual.setTransformation(new Transformation(new Matrix4f().translate(d.x,d.y,d.z).rotateXYZ((float)Math.toRadians(d.rotX),(float)Math.toRadians(d.rotY),(float)Math.toRadians(d.rotZ)).scale(d.scale).translate(-.5f,-.5f,-.5f)));
   visual.setWidth(3);visual.setHeight(3);visual.setViewRange(1);
  }
  for(var stale:existing.values())stale.discard();dirty=false;
 }
 protected void saveAdditional(ValueOutput out){super.saveAdditional(out);if(!decoration.isEmpty())out.store("decoration",ItemStack.CODEC,decoration);out.putInt("decor_count",decorations.size());for(int i=0;i<decorations.size();i++)decorations.get(i).save(out,"decor_"+i);}
 protected void loadAdditional(ValueInput in){super.loadAdditional(in);decoration=in.read("decoration",ItemStack.CODEC).orElse(ItemStack.EMPTY);decorations.clear();int n=Math.max(0,in.getIntOr("decor_count",0));for(int i=0;i<n;i++){var d=EnclosureDecoration.load(in,"decor_"+i);if(!d.stack.isEmpty())decorations.add(d);}dirty=true;migrated=false;}
 public void preRemoveSideEffects(BlockPos pos,BlockState state){if(level instanceof ServerLevel server)for(var visual:server.getEntitiesOfClass(Display.class,new AABB(pos).inflate(3),d->d.entityTags().stream().anyMatch(t->t.startsWith(ownerPrefix()))))visual.discard();super.preRemoveSideEffects(pos,state);}
}
