package dev.casz.aquarium;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.core.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;
import dev.casz.aquarium.mixin.ItemLayerRenderStateAccessor;
import dev.casz.aquarium.mixin.ItemStackRenderStateAccessor;

public final class TankDecorationRenderer implements BlockEntityRenderer<TankBlockEntity,TankDecorationRenderer.State>{
 private static final float GLASS_INSET=.035f;
 private static final int[] NO_TINTS=new int[0];

 private record ClipBox(float minX,float minY,float minZ,float maxX,float maxY,float maxZ){}
 private record Vertex(Vector3f p,float u,float v){}
 private static final class DecorRender {
  final ItemStack stack;final float x,y,z,scale,rx,ry,rz;final int seed;final ItemStackRenderState item=new ItemStackRenderState();
  DecorRender(EnclosureDecoration d,int seed){stack=d.stack.copy();x=d.x;y=d.y;z=d.z;scale=d.scale;rx=d.rotX;ry=d.rotY;rz=d.rotZ;this.seed=seed;}
 }
 public static final class State extends BlockEntityRenderState{
  int revision=-1;long clipTick=Long.MIN_VALUE;
  final List<DecorRender> decorations=new ArrayList<>();
  final List<ClipBox> clips=new ArrayList<>();
 }
 private final ItemModelResolver resolver;
 public TankDecorationRenderer(BlockEntityRendererProvider.Context context){resolver=context.itemModelResolver();}
 public State createRenderState(){return new State();}

 public void extractRenderState(TankBlockEntity be,State state,float partial,Vec3 camera,ModelFeatureRenderer.@Nullable CrumblingOverlay breaking){
  BlockEntityRenderer.super.extractRenderState(be,state,partial,camera,breaking);
  Level level=be.getLevel();if(level==null){state.decorations.clear();state.clips.clear();return;}
  if(state.revision!=be.revision()){
   state.revision=be.revision();state.decorations.clear();
   for(int i=0;i<be.decorations.size();i++){var d=be.decorations.get(i);if(!d.stack.isEmpty())state.decorations.add(new DecorRender(d,31*i+be.getBlockPos().hashCode()));}
   for(var d:state.decorations)resolver.updateForTopItem(d.item,d.stack,ItemDisplayContext.FIXED,level,null,d.seed);
  }else{
   for(var d:state.decorations)if(d.item.isAnimated())resolver.updateForTopItem(d.item,d.stack,ItemDisplayContext.FIXED,level,null,d.seed);
  }
  if(state.decorations.isEmpty()){state.clips.clear();state.clipTick=Long.MIN_VALUE;return;}long tick=level.getGameTime();if(state.clipTick==Long.MIN_VALUE||tick-state.clipTick>=20){state.clipTick=tick;scanClipBoxes(level,be.getBlockPos(),state.clips);}
 }

 public void submit(State state,PoseStack pose,SubmitNodeCollector out,CameraRenderState camera){
  if(state.clips.isEmpty())return;
  for(var d:state.decorations){
   pose.pushPose();applyDecoration(pose,d);
   if(hasSpecialLayer(d.item)){d.item.submit(pose,out,state.lightCoords,OverlayTexture.NO_OVERLAY,0);}
   else submitClipped(d.item,d,state.clips,pose,out,state.lightCoords);
   pose.popPose();
  }
 }

 private static void applyDecoration(PoseStack pose,DecorRender d){
  pose.translate(d.x,d.y,d.z);
  pose.mulPose(new Quaternionf().rotationXYZ((float)java.lang.Math.toRadians(d.rx),(float)java.lang.Math.toRadians(d.ry),(float)java.lang.Math.toRadians(d.rz)));
  pose.scale(d.scale,d.scale,d.scale);
  pose.translate(-.5f,-.5f,-.5f);
 }
 private static void applyDecoration(PoseStack.Pose pose,DecorRender d){
  pose.translate(d.x,d.y,d.z);
  pose.rotate(new Quaternionf().rotationXYZ((float)java.lang.Math.toRadians(d.rx),(float)java.lang.Math.toRadians(d.ry),(float)java.lang.Math.toRadians(d.rz)));
  pose.scale(d.scale,d.scale,d.scale);
  pose.translate(-.5f,-.5f,-.5f);
 }

 private static boolean hasSpecialLayer(ItemStackRenderState item){
  var root=(ItemStackRenderStateAccessor)(Object)item;int count=root.linkedAquariums$getActiveLayerCount();var layers=root.linkedAquariums$getLayers();
  for(int i=0;i<count;i++)if(((ItemLayerRenderStateAccessor)(Object)layers[i]).linkedAquariums$getSpecialRenderer()!=null)return true;
  return false;
 }

 private static void submitClipped(ItemStackRenderState item,DecorRender decor,List<ClipBox> clips,PoseStack pose,SubmitNodeCollector out,int light){
  var root=(ItemStackRenderStateAccessor)(Object)item;int count=root.linkedAquariums$getActiveLayerCount();var layers=root.linkedAquariums$getLayers();
  for(int layerIndex=0;layerIndex<count;layerIndex++){
   var layer=layers[layerIndex];var access=(ItemLayerRenderStateAccessor)(Object)layer;
   List<BakedQuad> source=access.linkedAquariums$getQuads();if(source.isEmpty())continue;
   pose.pushPose();access.linkedAquariums$getItemTransform().apply(ItemDisplayContext.FIXED.leftHand(),pose.last());pose.mulPose(access.linkedAquariums$getLocalTransform());

   PoseStack local=new PoseStack();applyDecoration(local.last(),decor);access.linkedAquariums$getItemTransform().apply(ItemDisplayContext.FIXED.leftHand(),local.last());local.mulPose(access.linkedAquariums$getLocalTransform());
   Matrix4f toOwner=new Matrix4f(local.last().pose()),fromOwner=new Matrix4f(toOwner).invert();

   ArrayList<BakedQuad> clipped=new ArrayList<>();
   for(BakedQuad quad:source)for(ClipBox box:clips)clipQuad(quad,toOwner,fromOwner,box,clipped);
   if(!clipped.isEmpty()){
    IntList tintList=access.linkedAquariums$getTintLayers();int[] tints=tintList==null?NO_TINTS:tintList.toArray(NO_TINTS);
    out.submitItem(pose,ItemDisplayContext.FIXED,light,OverlayTexture.NO_OVERLAY,0,tints,clipped,access.linkedAquariums$getFoilType());
   }
   pose.popPose();
  }
 }

 private static void clipQuad(BakedQuad quad,Matrix4f toOwner,Matrix4f fromOwner,ClipBox box,List<BakedQuad> out){
  ArrayList<Vertex> poly=new ArrayList<>(4);
  for(int i=0;i<4;i++){
   Vector3f p=toOwner.transformPosition(quad.position(i),new Vector3f());long uv=quad.packedUV(i);
   poly.add(new Vertex(p,UVPair.unpackU(uv),UVPair.unpackV(uv)));
  }
  poly=clip(poly,0,box.minX,true);poly=clip(poly,0,box.maxX,false);
  poly=clip(poly,1,box.minY,true);poly=clip(poly,1,box.maxY,false);
  poly=clip(poly,2,box.minZ,true);poly=clip(poly,2,box.maxZ,false);
  if(poly.size()<3)return;
  Vertex first=poly.getFirst();
  for(int i=1;i+1<poly.size();i++)out.add(triangle(quad,first,poly.get(i),poly.get(i+1),fromOwner));
 }

 private static ArrayList<Vertex> clip(List<Vertex> input,int axis,float bound,boolean greater){
  ArrayList<Vertex> out=new ArrayList<>();if(input.isEmpty())return out;
  Vertex prev=input.getLast();boolean prevInside=inside(prev,axis,bound,greater);
  for(Vertex cur:input){boolean curInside=inside(cur,axis,bound,greater);
   if(curInside!=prevInside)out.add(intersection(prev,cur,axis,bound));
   if(curInside)out.add(cur);
   prev=cur;prevInside=curInside;
  }
  return out;
 }
 private static boolean inside(Vertex v,int axis,float bound,boolean greater){float c=coord(v.p,axis);return greater?c>=bound-1.0E-5f:c<=bound+1.0E-5f;}
 private static Vertex intersection(Vertex a,Vertex b,int axis,float bound){
  float av=coord(a.p,axis),bv=coord(b.p,axis),den=bv-av,t=java.lang.Math.abs(den)<1.0E-8f?0:(bound-av)/den;t=java.lang.Math.clamp(t,0,1);
  return new Vertex(new Vector3f(a.p).lerp(b.p,t),a.u+(b.u-a.u)*t,a.v+(b.v-a.v)*t);
 }
 private static float coord(Vector3f p,int axis){return axis==0?p.x:axis==1?p.y:p.z;}
 private static BakedQuad triangle(BakedQuad source,Vertex a,Vertex b,Vertex c,Matrix4f inverse){
  Vector3f p0=inverse.transformPosition(a.p,new Vector3f()),p1=inverse.transformPosition(b.p,new Vector3f()),p2=inverse.transformPosition(c.p,new Vector3f());
  long u0=UVPair.pack(a.u,a.v),u1=UVPair.pack(b.u,b.v),u2=UVPair.pack(c.u,c.v);
  return new BakedQuad(p0,p1,p2,new Vector3f(p2),u0,u1,u2,u2,source.direction(),source.materialInfo());
 }

 private static void scanClipBoxes(Level level,BlockPos origin,List<ClipBox> out){
  out.clear();var originState=level.getBlockState(origin);HashSet<BlockPos> network=new HashSet<>(),seen=new HashSet<>(),tanks=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();queue.add(origin);
  while(!queue.isEmpty()&&network.size()<4096){BlockPos p=queue.removeFirst();if(!seen.add(p)||!level.hasChunkAt(p))continue;var state=level.getBlockState(p);if(!Enclosures.matches(originState,state))continue;network.add(p.immutable());if(Enclosures.isTank(state))tanks.add(p.immutable());for(Direction d:Direction.values()){BlockPos next=p.relative(d);if(!seen.contains(next))queue.addLast(next);}}
  for(BlockPos p:tanks){float x=p.getX()-origin.getX(),y=p.getY()-origin.getY(),z=p.getZ()-origin.getZ();
   float minX=x+(tanks.contains(p.west())?0:GLASS_INSET),maxX=x+1-(tanks.contains(p.east())?0:GLASS_INSET);
   float minY=y+(tanks.contains(p.below())?0:GLASS_INSET),maxY=y+1-(tanks.contains(p.above())?0:GLASS_INSET);
   float minZ=z+(tanks.contains(p.north())?0:GLASS_INSET),maxZ=z+1-(tanks.contains(p.south())?0:GLASS_INSET);
   out.add(new ClipBox(minX,minY,minZ,maxX,maxY,maxZ));
  }
 }
}
