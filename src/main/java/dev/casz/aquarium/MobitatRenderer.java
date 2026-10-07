package dev.casz.aquarium;
import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ConcurrentHashMap;
public final class MobitatRenderer implements BlockEntityRenderer<MobitatBlockEntity,MobitatRenderer.State>{
 private static final Logger LOGGER=LoggerFactory.getLogger("LinkedAquariums/MobitatRenderer");
 private static final Set<String> WARNED_TYPES=ConcurrentHashMap.newKeySet();
 private static int previewId(net.minecraft.core.BlockPos pos,int slot){long h=pos.asLong()^(0x9E3779B97F4A7C15L*(slot+1L));int mixed=(int)(h^(h>>>32));return -1-Math.floorMod(mixed,Integer.MAX_VALUE-1);}
 public static final class State extends BlockEntityRenderState{int revision=-1;final List<Entity> entities=new ArrayList<>();final List<EntityRenderState> residents=new ArrayList<>();}
 private final EntityRenderDispatcher dispatcher;
 public MobitatRenderer(BlockEntityRendererProvider.Context c){dispatcher=c.entityRenderer();}
 public State createRenderState(){return new State();}
 public void extractRenderState(MobitatBlockEntity be,State state,float partial,Vec3 camera,ModelFeatureRenderer.@Nullable CrumblingOverlay breaking){
  BlockEntityRenderer.super.extractRenderState(be,state,partial,camera,breaking);state.residents.clear();if(be.getLevel()==null)return;
  if(state.revision!=be.revision()){state.revision=be.revision();state.entities.clear();for(int i=0;i<be.size()&&i<5;i++){var resident=be.residentData(i);String raw=resident.getString("type").orElse("");Identifier id=Identifier.tryParse(raw);if(id==null)continue;var type=BuiltInRegistries.ENTITY_TYPE.getValue(id);if(type==null)continue;Entity e=type.create(be.getLevel(),EntitySpawnReason.LOAD);if(e!=null){var saved=resident.getCompound("entity").orElse(null);if(saved!=null)try{e.load(TagValueInput.create(ProblemReporter.DISCARDING,be.getLevel().registryAccess(),saved));}catch(Exception ignored){}e.setId(previewId(be.getBlockPos(),i));e.setPos(be.getBlockPos().getX()+.5,be.getBlockPos().getY()+.25,be.getBlockPos().getZ()+.5);e.setCustomNameVisible(false);state.entities.add(e);}}}
  for(int i=0;i<state.entities.size();i++){Entity e=state.entities.get(i);e.tickCount=(int)(be.getLevel().getGameTime()+i*9);try{state.residents.add(dispatcher.extractEntity(e,partial));}catch(RuntimeException ex){String key=BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString();if(WARNED_TYPES.add(key))LOGGER.warn("Skipping Mobitat preview for {} because its renderer failed on a render-only entity",key,ex);}}
 }
 public void submit(State state,PoseStack pose,SubmitNodeCollector out,CameraRenderState camera){
  float[][] spots={{.32f,.28f},{.68f,.30f},{.50f,.52f},{.30f,.70f},{.70f,.70f}};
  for(int i=0;i<state.residents.size()&&i<5;i++){pose.pushPose();pose.translate(spots[i][0],.24+((i&1)*.02),spots[i][1]);pose.scale(.10f,.10f,.10f);dispatcher.submit(state.residents.get(i),camera,0,0,0,pose,out);pose.popPose();}
 }
}