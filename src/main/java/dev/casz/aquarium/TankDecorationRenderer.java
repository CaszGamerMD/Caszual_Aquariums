package dev.casz.aquarium;

import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;
import dev.casz.aquarium.mixin.client.ItemStackRenderStateAccessor;
import dev.casz.aquarium.mixin.client.LayerRenderStateAccessor;

public final class TankDecorationRenderer implements BlockEntityRenderer<TankBlockEntity,TankDecorationRenderer.State>{
 private static final float GLASS_INSET=.0325f;
 private final ItemModelResolver itemResolver;

 private static final class Prepared{
  final ItemStack stack;final Matrix4f transform;final ItemStackRenderState itemState;final boolean blockItem;
  Prepared(ItemStack stack,Matrix4f transform,ItemStackRenderState itemState,boolean blockItem){this.stack=stack;this.transform=transform;this.itemState=itemState;this.blockItem=blockItem;}
 }
 private record V(float x,float y,float z,float u,float v){}
 public static final class State extends BlockEntityRenderState{
  int revision=-1;long clipTick=Long.MIN_VALUE;
  final List<Prepared> prepared=new ArrayList<>();
  final List<AABB> clips=new ArrayList<>();
 }

 public TankDecorationRenderer(BlockEntityRendererProvider.Context c){itemResolver=c.itemModelResolver();}
 public State createRenderState(){return new State();}

 public void extractRenderState(TankBlockEntity be,State state,float partial,Vec3 camera,ModelFeatureRenderer.@Nullable CrumblingOverlay breaking){
  BlockEntityRenderer.super.extractRenderState(be,state,partial,camera,breaking);
  if(be.getLevel()==null)return;
  if(state.revision!=be.revision()){
   state.revision=be.revision();state.prepared.clear();int seed=0;
   for(var d:be.decorations){
    ItemStack stack=d.stack.copy();boolean blockItem=stack.getItem() instanceof BlockItem;
    Matrix4f transform=new Matrix4f().translate(d.x,d.y,d.z).rotateXYZ((float)java.lang.Math.toRadians(d.rotX),(float)java.lang.Math.toRadians(d.rotY),(float)java.lang.Math.toRadians(d.rotZ)).scale(d.scale);
    ItemStackRenderState item=new ItemStackRenderState();
    itemResolver.updateForTopItem(item,stack,blockItem?ItemDisplayContext.NONE:ItemDisplayContext.FIXED,be.getLevel(),null,be.getBlockPos().hashCode()+seed++);
    if(!blockItem)transform.rotateY((float)java.lang.Math.PI);
    state.prepared.add(new Prepared(stack,transform,item,blockItem));
   }
  }
  if(state.prepared.isEmpty()){state.clips.clear();state.clipTick=Long.MIN_VALUE;return;}
  long now=be.getLevel().getGameTime();if(state.clipTick==Long.MIN_VALUE||now-state.clipTick>=10){state.clipTick=now;scanClips(be,state.clips);}
 }

 private static void scanClips(TankBlockEntity be,List<AABB> out){
  out.clear();var level=be.getLevel();if(level==null)return;BlockPos origin=be.getBlockPos();BlockState start=level.getBlockState(origin);
  Set<BlockPos> modules=new HashSet<>(),tanks=new HashSet<>(),seen=new HashSet<>();ArrayDeque<BlockPos> q=new ArrayDeque<>();q.add(origin);
  while(!q.isEmpty()&&modules.size()<4096){BlockPos p=q.removeFirst();if(!seen.add(p)||!level.hasChunkAt(p))continue;BlockState s=level.getBlockState(p);if(!Enclosures.matches(start,s))continue;modules.add(p.immutable());if(Enclosures.isTank(s))tanks.add(p.immutable());for(Direction d:Direction.values())q.addLast(p.relative(d));}
  for(BlockPos p:tanks){int rx=p.getX()-origin.getX(),ry=p.getY()-origin.getY(),rz=p.getZ()-origin.getZ();
   double x0=rx+(tanks.contains(p.west())?0:GLASS_INSET),x1=rx+1-(tanks.contains(p.east())?0:GLASS_INSET);
   double y0=ry+(tanks.contains(p.below())?0:GLASS_INSET),y1=ry+1-(tanks.contains(p.above())?0:GLASS_INSET);
   double z0=rz+(tanks.contains(p.north())?0:GLASS_INSET),z1=rz+1-(tanks.contains(p.south())?0:GLASS_INSET);
   out.add(new AABB(x0,y0,z0,x1,y1,z1));
  }
 }

 public void submit(State state,PoseStack pose,SubmitNodeCollector out,CameraRenderState camera){
  if(state.clips.isEmpty())return;
  for(Prepared p:state.prepared)submitItem(p,state,pose,out);
 }
 private void submitItem(Prepared p,State state,PoseStack pose,SubmitNodeCollector out){
  var root=(ItemStackRenderStateAccessor)(Object)p.itemState;int count=root.linkedAquariums$getActiveLayerCount();var layers=root.linkedAquariums$getLayers();
  for(int i=0;i<count;i++){var layer=layers[i];var a=(LayerRenderStateAccessor)(Object)layer;List<BakedQuad> quads=a.linkedAquariums$getQuads();
   PoseStack.Pose lp=new PoseStack.Pose();a.linkedAquariums$getItemTransform().apply(false,lp);lp.mulPose(a.linkedAquariums$getLocalTransform());Matrix4f transform=new Matrix4f(p.transform).mul(lp.pose());

   if(!quads.isEmpty()){var tints=a.linkedAquariums$getTintLayers();for(BakedQuad q:quads){var rt=q.materialInfo().itemRenderType();int color=0xFFFFFFFF;int ti=q.materialInfo().tintIndex();if(tints!=null&&ti>=0&&ti<tints.size()){int tint=tints.getInt(ti);if(tint!=-1)color=tint;}final int quadColor=color;out.submitCustomGeometry(pose,rt,(base,buffer)->emitClipped(base,buffer,q,transform,state.clips,quadColor,state.lightCoords));}}
   SpecialModelRenderer special=a.linkedAquariums$getSpecialRenderer();if(special!=null&&fullyInside(p.itemState.getModelBoundingBox(),p.transform,state.clips)){pose.pushPose();pose.mulPose(p.transform);a.linkedAquariums$getItemTransform().apply(false,pose.last());pose.last().mulPose(a.linkedAquariums$getLocalTransform());special.submit(a.linkedAquariums$getSpecialArgument(),pose,out,state.lightCoords,OverlayTexture.NO_OVERLAY,a.linkedAquariums$getFoilType()!=ItemStackRenderState.FoilType.NONE,0);pose.popPose();}
  }
 }

 private static void emitClipped(PoseStack.Pose base,com.mojang.blaze3d.vertex.VertexConsumer buffer,BakedQuad quad,Matrix4f transform,List<AABB> clips,int color,int light){
  ArrayList<V> source=new ArrayList<>(4);for(int i=0;i<4;i++){Vector3f pos=transform.transformPosition(quad.position(i),new Vector3f());long uv=quad.packedUV(i);source.add(new V(pos.x,pos.y,pos.z,UVPair.unpackU(uv),UVPair.unpackV(uv)));}
  Matrix4f inverse=new Matrix4f(transform).invert();PoseStack.Pose renderPose=base.copy();renderPose.mulPose(transform);
  QuadInstance instance=new QuadInstance();instance.setColor(color);instance.setLightCoords(light);instance.setOverlayCoords(OverlayTexture.NO_OVERLAY);
  for(AABB box:clips){
   List<V> poly=clipBox(source,box);if(poly.size()<3)continue;V root=poly.getFirst();
   for(int i=1;i+1<poly.size();i++)emitTriangle(buffer,renderPose,quad,inverse,root,poly.get(i),poly.get(i+1),instance);
  }
 }
 private static void emitTriangle(com.mojang.blaze3d.vertex.VertexConsumer buffer,PoseStack.Pose renderPose,BakedQuad source,Matrix4f inverse,V a,V b,V c,QuadInstance instance){
  Vector3f pa=inverse.transformPosition(a.x,a.y,a.z,new Vector3f()),pb=inverse.transformPosition(b.x,b.y,b.z,new Vector3f()),pc=inverse.transformPosition(c.x,c.y,c.z,new Vector3f());
  BakedQuad clipped=new BakedQuad(pa,pb,pc,new Vector3f(pc),UVPair.pack(a.u,a.v),UVPair.pack(b.u,b.v),UVPair.pack(c.u,c.v),UVPair.pack(c.u,c.v),source.direction(),source.materialInfo());
  buffer.putBakedQuad(renderPose,clipped,instance);
 }
 private static List<V> clipBox(List<V> source,AABB b){List<V> p=source;p=clip(p,0,(float)b.minX,true);p=clip(p,0,(float)b.maxX,false);p=clip(p,1,(float)b.minY,true);p=clip(p,1,(float)b.maxY,false);p=clip(p,2,(float)b.minZ,true);return clip(p,2,(float)b.maxZ,false);}
 private static List<V> clip(List<V> in,int axis,float bound,boolean greater){if(in.isEmpty())return List.of();ArrayList<V> out=new ArrayList<>();V prev=in.getLast();boolean prevIn=inside(prev,axis,bound,greater);for(V cur:in){boolean curIn=inside(cur,axis,bound,greater);if(curIn!=prevIn)out.add(intersect(prev,cur,axis,bound));if(curIn)out.add(cur);prev=cur;prevIn=curIn;}return out;}
 private static boolean inside(V v,int axis,float b,boolean greater){float c=axis==0?v.x:axis==1?v.y:v.z;return greater?c>=b-1e-5f:c<=b+1e-5f;}
 private static V intersect(V a,V b,int axis,float bound){float av=axis==0?a.x:axis==1?a.y:a.z,bv=axis==0?b.x:axis==1?b.y:b.z;float den=bv-av,t=java.lang.Math.abs(den)<1e-7f?0:(bound-av)/den;t=java.lang.Math.clamp(t,0,1);return new V(a.x+(b.x-a.x)*t,a.y+(b.y-a.y)*t,a.z+(b.z-a.z)*t,a.u+(b.u-a.u)*t,a.v+(b.v-a.v)*t);}
 private static boolean fullyInside(AABB box,Matrix4f transform,List<AABB> clips){for(int ix=0;ix<2;ix++)for(int iy=0;iy<2;iy++)for(int iz=0;iz<2;iz++){Vector3f p=transform.transformPosition((float)(ix==0?box.minX:box.maxX),(float)(iy==0?box.minY:box.maxY),(float)(iz==0?box.minZ:box.maxZ),new Vector3f());boolean inside=false;for(AABB c:clips)if(p.x>=c.minX&&p.x<=c.maxX&&p.y>=c.minY&&p.y<=c.maxY&&p.z>=c.minZ&&p.z<=c.maxZ){inside=true;break;}if(!inside)return false;}return true;}
 public boolean shouldRenderOffScreen(){return true;}
}
