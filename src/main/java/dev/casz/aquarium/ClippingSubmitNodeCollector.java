package dev.casz.aquarium;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.dispatch.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.gizmos.DrawableGizmoPrimitives;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/**
 * Delegates normal rendering while clipping baked block/item quads to one or more
 * enclosure-interior boxes.  The original pose is preserved, so lighting, UVs,
 * tinting, rotation and scaling continue to use Minecraft's standard renderer.
 */
final class ClippingSubmitNodeCollector implements SubmitNodeCollector {
 private static final float EPS=1.0E-5f;
 private final SubmitNodeCollector root;
 private final OrderedSubmitNodeCollector delegate;
 private final Matrix4f inverseBase;
 private final List<AABB> boxes;

 ClippingSubmitNodeCollector(SubmitNodeCollector delegate,Matrix4fc basePose,List<AABB> boxes){
  this.root=delegate;this.delegate=delegate;this.inverseBase=new Matrix4f(basePose).invert();this.boxes=List.copyOf(boxes);
 }
 private ClippingSubmitNodeCollector(SubmitNodeCollector root,OrderedSubmitNodeCollector delegate,Matrix4f inverseBase,List<AABB> boxes){
  this.root=root;this.delegate=delegate;this.inverseBase=inverseBase;this.boxes=boxes;
 }
 public OrderedSubmitNodeCollector order(int order){return new ClippingSubmitNodeCollector(root,root.order(order),inverseBase,boxes);}

 private Matrix4f localToOwner(PoseStack pose){return new Matrix4f(inverseBase).mul(pose.last().pose());}

 private record V(Vector3f model,Vector3f owner,float u,float v){}
 private static V lerp(V a,V b,float t){
  return new V(new Vector3f(a.model).lerp(b.model,t),new Vector3f(a.owner).lerp(b.owner,t),a.u+(b.u-a.u)*t,a.v+(b.v-a.v)*t);
 }
 private static float coord(V v,int axis){return axis==0?v.owner.x:axis==1?v.owner.y:v.owner.z;}
 private static boolean inside(float value,float bound,boolean greater){return greater?value>=bound-EPS:value<=bound+EPS;}
 private static List<V> clipPlane(List<V> input,int axis,float bound,boolean greater){
  if(input.isEmpty())return input;
  ArrayList<V> out=new ArrayList<>(input.size()+2);V prev=input.getLast();float pv=coord(prev,axis);boolean pin=inside(pv,bound,greater);
  for(V cur:input){
   float cv=coord(cur,axis);boolean cin=inside(cv,bound,greater);
   if(cin!=pin){float denom=cv-pv;if(Math.abs(denom)>1.0E-8f){float t=Math.clamp((bound-pv)/denom,0,1);out.add(lerp(prev,cur,t));}}
   if(cin)out.add(cur);prev=cur;pv=cv;pin=cin;
  }
  return out;
 }
 private static List<V> clipBox(List<V> poly,AABB b){
  poly=clipPlane(poly,0,(float)b.minX,true);poly=clipPlane(poly,0,(float)b.maxX,false);
  poly=clipPlane(poly,1,(float)b.minY,true);poly=clipPlane(poly,1,(float)b.maxY,false);
  poly=clipPlane(poly,2,(float)b.minZ,true);poly=clipPlane(poly,2,(float)b.maxZ,false);
  return poly;
 }
 private static boolean contained(List<V> poly,AABB b){
  for(V v:poly)if(v.owner.x<b.minX-EPS||v.owner.x>b.maxX+EPS||v.owner.y<b.minY-EPS||v.owner.y>b.maxY+EPS||v.owner.z<b.minZ-EPS||v.owner.z>b.maxZ+EPS)return false;
  return true;
 }
 private static BakedQuad triangle(V a,V b,V c,BakedQuad source){
  return new BakedQuad(a.model,b.model,c.model,c.model,UVPair.pack(a.u,a.v),UVPair.pack(b.u,b.v),UVPair.pack(c.u,c.v),UVPair.pack(c.u,c.v),source.direction(),source.materialInfo());
 }
 private List<BakedQuad> clipQuads(List<BakedQuad> source,Matrix4fc transform){
  if(source.isEmpty()||boxes.isEmpty())return List.of();
  ArrayList<BakedQuad> out=new ArrayList<>();
  for(BakedQuad q:source){
   ArrayList<V> original=new ArrayList<>(4);
   for(int i=0;i<4;i++){Vector3f model=new Vector3f(q.position(i)),owner=transform.transformPosition(model,new Vector3f());long uv=q.packedUV(i);original.add(new V(model,owner,UVPair.unpackU(uv),UVPair.unpackV(uv)));}
   boolean whole=false;for(AABB box:boxes)if(contained(original,box)){out.add(q);whole=true;break;}if(whole)continue;
   for(AABB box:boxes){
    List<V> clipped=clipBox(original,box);if(clipped.size()<3)continue;
    V first=clipped.getFirst();for(int i=1;i+1<clipped.size();i++)out.add(triangle(first,clipped.get(i),clipped.get(i+1),q));
   }
  }
  return out;
 }

 private final class ClippedPart implements BlockStateModelPart {
  private final BlockStateModelPart source;private final EnumMap<Direction,List<BakedQuad>> sides=new EnumMap<>(Direction.class);private final List<BakedQuad> unculled;
  ClippedPart(BlockStateModelPart source,Matrix4fc transform){
   this.source=source;for(Direction d:Direction.values())sides.put(d,clipQuads(source.getQuads(d),transform));unculled=clipQuads(source.getQuads(null),transform);
  }
  boolean empty(){if(!unculled.isEmpty())return false;for(var q:sides.values())if(!q.isEmpty())return false;return true;}
  public List<BakedQuad> getQuads(@Nullable Direction d){return d==null?unculled:sides.getOrDefault(d,List.of());}
  public boolean useAmbientOcclusion(){return source.useAmbientOcclusion();}
  public Material.Baked particleMaterial(){return source.particleMaterial();}
  public int materialFlags(){return source.materialFlags();}
 }

 public void submitBlockModel(PoseStack pose,RenderType type,List<BlockStateModelPart> parts,int[] tints,int light,int overlay,int outline){
  Matrix4f transform=localToOwner(pose);ArrayList<BlockStateModelPart> clipped=new ArrayList<>(parts.size());
  for(var part:parts){var c=new ClippedPart(part,transform);if(!c.empty())clipped.add(c);}
  if(!clipped.isEmpty())delegate.submitBlockModel(pose,type,clipped,tints,light,overlay,outline);
 }
 public void submitItem(PoseStack pose,ItemDisplayContext context,int light,int overlay,int outline,int[] tints,List<BakedQuad> quads,ItemStackRenderState.FoilType foil){
  var clipped=clipQuads(quads,localToOwner(pose));if(!clipped.isEmpty())delegate.submitItem(pose,context,light,overlay,outline,tints,clipped,foil);
 }

 public void submitShadow(PoseStack p,float r,List<EntityRenderState.ShadowPiece> v){delegate.submitShadow(p,r,v);}
 public void submitNameTag(PoseStack p,@Nullable Vec3 a,int o,Component n,boolean s,int l,CameraRenderState c){delegate.submitNameTag(p,a,o,n,s,l,c);}
 public void submitText(PoseStack p,float x,float y,FormattedCharSequence s,boolean sh,Font.DisplayMode m,int l,int c,int b,int o){delegate.submitText(p,x,y,s,sh,m,l,c,b,o);}
 public void submitFlame(PoseStack p,EntityRenderState s,Quaternionf q){delegate.submitFlame(p,s,q);}
 public void submitLeash(PoseStack p,EntityRenderState.LeashState s){delegate.submitLeash(p,s);}
 public <S> void submitModel(Model<? super S> m,S s,PoseStack p,RenderType r,int l,int o,int tint,@Nullable TextureAtlasSprite sprite,int outline,ModelFeatureRenderer.@Nullable CrumblingOverlay breaking){delegate.submitModel(m,s,p,r,l,o,tint,sprite,outline,breaking);}
 public void submitMovingBlock(PoseStack p,MovingBlockRenderState s,int outline){delegate.submitMovingBlock(p,s,outline);}
 public void submitBreakingBlockModel(PoseStack p,List<BlockStateModelPart> parts,int progress){delegate.submitBreakingBlockModel(p,parts,progress);}
 public void submitShapeOutline(PoseStack p,VoxelShape s,RenderType r,int c,float w,boolean after){delegate.submitShapeOutline(p,s,r,c,w,after);}
 public void submitCustomGeometry(PoseStack p,RenderType r,SubmitNodeCollector.CustomGeometryRenderer g){delegate.submitCustomGeometry(p,r,g);}
 public void submitQuadParticleGroup(QuadParticleRenderState p){delegate.submitQuadParticleGroup(p);}
 public void submitGizmoPrimitives(DrawableGizmoPrimitives.Group group,CameraRenderState camera,boolean onTop){delegate.submitGizmoPrimitives(group,camera,onTop);}
}
