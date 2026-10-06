package dev.casz.aquarium;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class TropicalFishEditorRenderer implements BlockEntityRenderer<TropicalFishEditorBlockEntity,TropicalFishEditorRenderer.State>{
 public static final class State extends BlockEntityRenderState{int revision=-1;TropicalFish fish;EntityRenderState rendered;}
 private final EntityRenderDispatcher dispatcher;
 public TropicalFishEditorRenderer(BlockEntityRendererProvider.Context context){dispatcher=context.entityRenderer();}
 public State createRenderState(){return new State();}
 public void extractRenderState(TropicalFishEditorBlockEntity be,State state,float partial,Vec3 camera,ModelFeatureRenderer.@Nullable CrumblingOverlay breaking){
  BlockEntityRenderer.super.extractRenderState(be,state,partial,camera,breaking);state.rendered=null;if(be.getLevel()==null)return;
  if(state.revision!=be.revision()){state.revision=be.revision();state.fish=null;if(be.occupied()){TropicalFish fish=EntityTypes.TROPICAL_FISH.create(be.getLevel(),EntitySpawnReason.LOAD);if(fish!=null){fish.applyComponentsFromItemStack(be.fish());fish.setNoAi(true);fish.setCustomNameVisible(false);fish.setPos(be.getBlockPos().getX()+.5,be.getBlockPos().getY()+.5,be.getBlockPos().getZ()+.5);state.fish=fish;}}}
  if(state.fish!=null){state.fish.tickCount=(int)be.getLevel().getGameTime();state.fish.setYRot((float)((be.getLevel().getGameTime()*1.6)%360));state.rendered=dispatcher.extractEntity(state.fish,partial);}
 }
 public void submit(State state,PoseStack pose,SubmitNodeCollector out,CameraRenderState camera){
  if(state.rendered==null)return;pose.pushPose();pose.translate(.5,.46,.5);pose.scale(.72f,.72f,.72f);dispatcher.submit(state.rendered,camera,0,0,0,pose,out);pose.popPose();
 }
}
