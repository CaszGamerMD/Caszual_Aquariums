package dev.casz.aquarium;

import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.TropicalFishRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Direction;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Render actual vanilla fish and Guardian models, without spawning world entities. */
public final class WearableAquariumRenderer implements BlockEntityRenderer<WearableAquariumBlockEntity,WearableAquariumRenderer.State>{
 public static final class State extends BlockEntityRenderState {
  int revision=-1; final List<Entity> entities=new ArrayList<>();
  final List<EntityRenderState> renderings=new ArrayList<>();final List<Boolean> guardians=new ArrayList<>();float time;Direction facing=Direction.NORTH;
 }
 private final EntityRenderDispatcher dispatcher;
 public WearableAquariumRenderer(BlockEntityRendererProvider.Context c){dispatcher=c.entityRenderer();}
 public State createRenderState(){return new State();}
 private Entity create(WearableAquariumBlockEntity be,ItemStack stack,int slot){
  var level=be.getLevel();if(level==null)return null;
  boolean guardian=WearableAquariumItem.isGuardianNet(stack);
  EntityType<?> type=guardian?EntityTypes.GUARDIAN:
   stack.is(Items.COD_BUCKET)?EntityTypes.COD:
   stack.is(Items.SALMON_BUCKET)?EntityTypes.SALMON:
   stack.is(Items.PUFFERFISH_BUCKET)?EntityTypes.PUFFERFISH:EntityTypes.TROPICAL_FISH;
  var entity=type.create(level,EntitySpawnReason.LOAD);if(entity==null)return null;
  if(guardian){
   var saved=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag()
    .getCompound("terrarium_entity").orElse(null);
   if(saved!=null)try{entity.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));}catch(Exception ignored){}
  }else entity.applyComponentsFromItemStack(stack);
  long hash=be.getBlockPos().asLong()^(0x9e3779b97f4a7c15L*(slot+1L));
  entity.setId(-1-Math.floorMod((int)(hash^(hash>>>32)),Integer.MAX_VALUE-1));
  entity.setPos(be.getBlockPos().getX()+.5,be.getBlockPos().getY()+1,be.getBlockPos().getZ()+.5);
  entity.setCustomNameVisible(false);
  entity.setNoGravity(true);
  entity.setDeltaMovement(Vec3.ZERO);
  return entity;
 }
 public void extractRenderState(WearableAquariumBlockEntity be,State state,float partial,Vec3 camera,ModelFeatureRenderer.@Nullable CrumblingOverlay breaking){
  BlockEntityRenderer.super.extractRenderState(be,state,partial,camera,breaking);state.renderings.clear();state.guardians.clear();
  if(be.getLevel()==null)return;
  state.facing=be.getBlockState().getValue(WearableAquariumBlock.FACING);
  if(state.revision!=be.revision()){
   state.revision=be.revision();state.entities.clear();int i=0;
   for(var stack:be.contents()){var entity=create(be,stack,i++);if(entity!=null)state.entities.add(entity);}
  }
  state.time=be.getLevel().getGameTime()+partial;
  for(int i=0;i<state.entities.size();i++){
   var e=state.entities.get(i);e.tickCount=(int)(state.time*.45f)+i*7;
   e.setDeltaMovement(Vec3.ZERO);
   // Guardians face out through the tank's head, rather than spinning.
   float yaw=e.getType()==EntityTypes.GUARDIAN?state.facing.toYRot():
       (float)Math.sin(state.time*.035+i*1.8)*110;
   e.setYRot(yaw);e.yRotO=yaw;
   if(e instanceof Mob mob){mob.setYBodyRot(yaw);mob.setYHeadRot(yaw);}
   try{EntityRenderState render=dispatcher.extractEntity(e,partial);
    if(render instanceof TropicalFishRenderState fish)fish.isInWater=true;
    state.renderings.add(render);state.guardians.add(e.getType()==EntityTypes.GUARDIAN);
   }catch(RuntimeException ignored){}
  }
 }
 public void submit(State state,PoseStack pose,SubmitNodeCollector out,CameraRenderState camera){
  int guardianCount=0;
  for(boolean isGuardian:state.guardians)if(isGuardian)guardianCount++;
  int guardianOrdinal=0;
  for(int i=0;i<state.renderings.size();i++){
   boolean guardian=state.guardians.get(i);float t=state.time;
   float x,y,z,size;
   if(guardian){
    // One Guardian centered in the head; two balanced side-by-side as eyes.
    // Rotate the layout with the placed block so both remain on its front.
    float offset=WearableAquariumItem.guardianHeadOffset(guardianOrdinal++,guardianCount);
    x=.5f-offset*state.facing.getStepZ()+state.facing.getStepX()*.065f;
    z=.5f+offset*state.facing.getStepX()+state.facing.getStepZ()*.065f;
    y=1.75f;
    size=guardianCount==1?.22f:.13f;
   }else{
    float phase=i*2.17f;
    y=1.19f+(float)Math.sin(t*.010f+phase)*.40f;
    x=.5f+(float)Math.sin(t*.017f+phase)*.075f;
    z=.5f+(float)Math.cos(t*.013f+phase)*.038f;
    size=.30f;
   }
   pose.pushPose();pose.translate(x,y,z);
   pose.scale(size,size,size);
   dispatcher.submit(state.renderings.get(i),camera,0,0,0,pose,out);
   pose.popPose();
  }
 }
 public boolean shouldRenderOffScreen(){return true;}
 public int getViewDistance(){return 96;}
}
