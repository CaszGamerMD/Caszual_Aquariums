package dev.casz.aquarium;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import org.joml.Matrix3x2fStack;
public final class MobitatRenderer implements BlockEntityRenderer<MobitatBlockEntity>{
 private final EntityRenderDispatcher dispatcher;
 public MobitatRenderer(BlockEntityRendererProvider.Context c){dispatcher=c.entityRenderer();}
 public void submit(MobitatBlockEntity be,float partial,Matrix3x2fStack pose,SubmitNodeCollector out,CameraRenderState camera){
  Level level=be.getLevel();if(level==null||be.empty())return;
  float[][] spots={{.32f,.28f},{.68f,.30f},{.50f,.52f},{.30f,.70f},{.70f,.70f}};
  for(int i=0;i<be.size()&&i<5;i++){var data=be.residentData(i);String raw=data.getString("type").orElse("");Identifier id=Identifier.tryParse(raw);if(id==null)continue;var type=BuiltInRegistries.ENTITY_TYPE.getValue(id);if(type==null)continue;Entity e=type.create(level,EntitySpawnReason.LOAD);if(!(e instanceof Mob mob))continue;data.getCompound("entity").ifPresent(tag->{try{mob.load(tag);}catch(Exception ignored){}});mob.setPos(be.getBlockPos().getX()+spots[i][0],be.getBlockPos().getY()+.24,be.getBlockPos().getZ()+spots[i][1]);mob.tickCount=(int)(level.getGameTime()+i*9);pose.pushMatrix();pose.translate(spots[i][0]*16f,4.0f+((i&1)*.35f),spots[i][1]*16f);pose.scale(.10f,.10f);dispatcher.submit(mob,0,partial,pose,out,camera);pose.popMatrix();}
 }
}