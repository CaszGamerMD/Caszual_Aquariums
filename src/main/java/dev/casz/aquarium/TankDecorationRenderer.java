package dev.casz.aquarium;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.*;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.*;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

/** Client renderer for enclosure decorations with true tank-volume clipping. */
public final class TankDecorationRenderer implements BlockEntityRenderer<TankBlockEntity,TankDecorationRenderer.State>{
 private static final double GLASS_INSET=.035;
 private static final BlockDisplayContext BLOCK_CONTEXT=BlockDisplayContext.create();

 static final class DecorationState{
  final Matrix4f transform;final @Nullable BlockModelRenderState block;final @Nullable ItemStackRenderState item;
  DecorationState(Matrix4f transform,@Nullable BlockModelRenderState block,@Nullable ItemStackRenderState item){this.transform=transform;this.block=block;this.item=item;}
 }
 public static final class State extends BlockEntityRenderState{
  int revision=-1,shapeHash=Integer.MIN_VALUE;long nextShapeCheck;
  final List<DecorationState> decorations=new ArrayList<>();List<AABB> clipBoxes=List.of();
 }
 private final BlockModelResolver blockModels;private final ItemModelResolver itemModels;

 public TankDecorationRenderer(BlockEntityRendererProvider.Context context){blockModels=context.blockModelResolver();itemModels=context.itemModelResolver();}
 public State createRenderState(){return new State();}

 public void extractRenderState(TankBlockEntity be,State state,float partial,Vec3 camera,ModelFeatureRenderer.@Nullable CrumblingOverlay breaking){
  BlockEntityRenderer.super.extractRenderState(be,state,partial,camera,breaking);if(be.getLevel()==null)return;
  if(state.revision!=be.revision()){
   state.revision=be.revision();state.decorations.clear();
   int seed=be.getBlockPos().hashCode();
   for(int i=0;i<be.decorations.size();i++){
    var d=be.decorations.get(i);if(d.stack.isEmpty())continue;
    Matrix4f transform=new Matrix4f().translate(d.x,d.y,d.z).rotateXYZ((float)Math.toRadians(d.rotX),(float)Math.toRadians(d.rotY),(float)Math.toRadians(d.rotZ)).scale(d.scale).translate(-.5f,-.5f,-.5f);
    if(d.stack.getItem() instanceof BlockItem bi){
     var block=new BlockModelRenderState();blockModels.update(block,bi.getBlock().defaultBlockState(),BLOCK_CONTEXT);state.decorations.add(new DecorationState(transform,block,null));
    }else{
     var item=new ItemStackRenderState();itemModels.updateForTopItem(item,d.stack,ItemDisplayContext.FIXED,be.getLevel(),null,seed+i);state.decorations.add(new DecorationState(transform,null,item));
    }
   }
  }
  long now=be.getLevel().getGameTime();
  if(state.clipBoxes.isEmpty()||state.revision!=be.revision()||now>=state.nextShapeCheck){
   List<AABB> boxes=clipBoxes(be);int hash=boxes.hashCode();if(hash!=state.shapeHash){state.shapeHash=hash;state.clipBoxes=boxes;}state.nextShapeCheck=now+10;
  }
 }

 private static List<AABB> clipBoxes(TankBlockEntity be){
  var level=be.getLevel();if(level==null)return List.of();BlockPos origin=be.getBlockPos();var start=level.getBlockState(origin);
  Set<BlockPos> cells=new HashSet<>(),tanks=new HashSet<>(),seen=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();queue.add(origin);
  while(!queue.isEmpty()&&cells.size()<4096){
   BlockPos p=queue.removeFirst();if(!seen.add(p)||!level.hasChunkAt(p))continue;var s=level.getBlockState(p);if(!Enclosures.matches(start,s))continue;
   cells.add(p.immutable());if(Enclosures.isTank(s))tanks.add(p.immutable());for(Direction d:Direction.values())queue.addLast(p.relative(d));
  }
  if(tanks.isEmpty())return List.of();
  ArrayList<AABB> result=new ArrayList<>(tanks.size());
  for(BlockPos p:tanks){
   double x=p.getX()-origin.getX(),y=p.getY()-origin.getY(),z=p.getZ()-origin.getZ();
   double x0=x+(tanks.contains(p.west())?0:GLASS_INSET),x1=x+1-(tanks.contains(p.east())?0:GLASS_INSET);
   double y0=y+(tanks.contains(p.below())?0:GLASS_INSET),y1=y+1-(tanks.contains(p.above())?0:GLASS_INSET);
   double z0=z+(tanks.contains(p.north())?0:GLASS_INSET),z1=z+1-(tanks.contains(p.south())?0:GLASS_INSET);
   result.add(new AABB(x0,y0,z0,x1,y1,z1));
  }
  result.sort(Comparator.comparingDouble((AABB b)->b.minY).thenComparingDouble(b->b.minZ).thenComparingDouble(b->b.minX));
  return List.copyOf(result);
 }

 public void submit(State state,PoseStack pose,SubmitNodeCollector out,CameraRenderState camera){
  if(state.decorations.isEmpty()||state.clipBoxes.isEmpty())return;
  var clipped=new ClippingSubmitNodeCollector(out,new Matrix4f(pose.last().pose()),state.clipBoxes);
  for(var d:state.decorations){
   pose.pushPose();pose.mulPose(d.transform);
   if(d.block!=null)d.block.submit(pose,clipped,state.lightCoords,OverlayTexture.NO_OVERLAY,0);
   else if(d.item!=null){pose.mulPose(Axis.YP.rotation((float)Math.PI));d.item.submit(pose,clipped,state.lightCoords,OverlayTexture.NO_OVERLAY,0);}
   pose.popPose();
  }
 }
 public boolean shouldRenderOffScreen(){return true;}
 public int getViewDistance(){return 128;}
}
