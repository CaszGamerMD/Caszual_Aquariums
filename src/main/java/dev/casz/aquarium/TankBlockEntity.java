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
 public ItemStack decoration=ItemStack.EMPTY;
 public int variant,rotation,offsetX,offsetZ;
 private int chestOpenTicks;
 private UUID display;
 private boolean dirty=true;
 public TankBlockEntity(BlockPos p,BlockState s){super(AquariumMod.TANK_ENTITY,p,s);}
 public void migrateLegacy(){
  if(level==null)return;var state=getBlockState();int old=state.hasProperty(AquariumBlock.DECOR)?state.getValue(AquariumBlock.DECOR):0;
  if(old>0){if(decoration.isEmpty())decoration=new ItemStack(Palette.item(Palette.DECORS.get(old)));level.setBlock(worldPosition,state.setValue(AquariumBlock.DECOR,0),3);changed();}
 }
 public int kind(){if(decoration.isEmpty())return 0;if(decoration.is(Items.TRIDENT))return 33;if(decoration.is(Items.CHEST)||decoration.is(Items.TRAPPED_CHEST)||decoration.is(Items.ENDER_CHEST))return 34;int k=Palette.find(Palette.DECORS,decoration.getItem());return k>0?k:35;}
 public void changed(){dirty=true;setChanged();}
 public void setDecoration(ItemStack stack){decoration=stack.copyWithCount(1);variant=rotation=offsetX=offsetZ=0;changed();}
 public void openChest(){chestOpenTicks=80;changed();}
 public boolean isChestOpen(){return chestOpenTicks>0;}
 public void tick(){
  migrateLegacy();if(!(level instanceof ServerLevel server))return;
  if(kind()==34){if(chestOpenTicks>0){chestOpenTicks--;if(chestOpenTicks==0)dirty=true;}else if(server.getRandom().nextInt(600)==0){openChest();server.sendParticles(net.minecraft.core.particles.ParticleTypes.BUBBLE,worldPosition.getX()+.5+offsetX*.04,worldPosition.getY()+.4,worldPosition.getZ()+.5+offsetZ*.04,8,.06,.08,.06,.015);}}
  if(dirty||server.getGameTime()%40==0)updateDisplay(server);
 }
 private String ownerTag(){return AquariumMod.ID+":decor:"+worldPosition.getX()+","+worldPosition.getY()+","+worldPosition.getZ();}
 private void updateDisplay(ServerLevel server){
  boolean generic=kind()==35;
  Display visual=display!=null && server.getEntity(display) instanceof Display d?d:null;
  if(visual==null){
   var found=server.getEntitiesOfClass(Display.class,new AABB(worldPosition).inflate(1),d->d.entityTags().contains(ownerTag()));
   if(!found.isEmpty()){visual=found.getFirst();for(int i=1;i<found.size();i++)found.get(i).discard();}
  }
  if(visual!=null && (generic != (visual instanceof Display.ItemDisplay))){visual.discard();visual=null;}
  if(decoration.isEmpty()){if(visual!=null)visual.discard();display=null;dirty=false;return;}
  if(visual==null){visual=generic?EntityTypes.ITEM_DISPLAY.create(server,EntitySpawnReason.TRIGGERED):EntityTypes.BLOCK_DISPLAY.create(server,EntitySpawnReason.TRIGGERED);if(visual==null)return;visual.addTag(ownerTag());server.addFreshEntity(visual);}
  display=visual.getUUID();visual.setPos(worldPosition.getX(),worldPosition.getY(),worldPosition.getZ());
  int kind=kind();
  if(visual instanceof Display.ItemDisplay item){item.setItemStack(decoration.copy());item.setItemTransform(ItemDisplayContext.FIXED);}
  else if(visual instanceof Display.BlockDisplay block)block.setBlockState(AquariumMod.DECOR_MODEL.defaultBlockState().setValue(DecorModelBlock.KIND,kind).setValue(DecorModelBlock.MODEL,kind==34?(variant/2)*2+(chestOpenTicks>0?1:0):variant));
  visual.setTransformation(new Transformation(new Matrix4f().translate(.5f+offsetX*.04f,generic?.35f:.08f,.5f+offsetZ*.04f).rotateY((float)Math.toRadians(rotation*45)).scale(generic?.45f:.6f).translate(generic?0:-.5f,0,generic?0:-.5f)));
  visual.setWidth(2);visual.setHeight(2);visual.setViewRange(1);dirty=false;setChanged();
 }
 protected void saveAdditional(ValueOutput out){super.saveAdditional(out);if(!decoration.isEmpty())out.store("decoration",ItemStack.CODEC,decoration);out.putInt("variant",variant);out.putInt("rotation",rotation);out.putInt("offset_x",offsetX);out.putInt("offset_z",offsetZ);if(display!=null)out.putString("display",display.toString());}
 protected void loadAdditional(ValueInput in){super.loadAdditional(in);decoration=in.read("decoration",ItemStack.CODEC).orElse(ItemStack.EMPTY);variant=Math.floorMod(in.getIntOr("variant",0),4);rotation=Math.floorMod(in.getIntOr("rotation",0),8);offsetX=Math.clamp(in.getIntOr("offset_x",0),-3,3);offsetZ=Math.clamp(in.getIntOr("offset_z",0),-3,3);try{display=UUID.fromString(in.getStringOr("display",""));}catch(IllegalArgumentException ex){display=null;}dirty=true;}
 public void preRemoveSideEffects(BlockPos pos,BlockState state){if(level instanceof ServerLevel server && display!=null){var visual=server.getEntity(display);if(visual!=null)visual.discard();}super.preRemoveSideEffects(pos,state);}
}