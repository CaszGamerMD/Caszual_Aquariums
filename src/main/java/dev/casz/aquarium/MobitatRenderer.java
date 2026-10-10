package dev.casz.aquarium;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.TropicalFishRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The residents are miniature render-only previews, never spawned into the
 * world. Each follows a smooth slow route within the Mobitat's interior, with
 * matching heading and walking animation. Existing Mobitat inventory and
 * serialized residents are unchanged.
 */
public final class MobitatRenderer implements BlockEntityRenderer<MobitatBlockEntity, MobitatRenderer.State> {
    private static final Logger LOGGER=LoggerFactory.getLogger("LinkedAquariums/MobitatRenderer");
    private static final Set<String> WARNED_TYPES=ConcurrentHashMap.newKeySet();

    private static int previewId(BlockPos pos,int slot) {
        long hash=pos.asLong()^(0x9E3779B97F4A7C15L*(slot+1L));
        return -1-Math.floorMod((int)(hash^(hash>>>32)),Integer.MAX_VALUE-1);
    }

    private record Preview(EntityRenderState render,MobitatPreviewMovement.Position position) {}

    public static final class State extends BlockEntityRenderState {
        int revision=-1;
        long previousTick=Long.MIN_VALUE;
        final List<Entity> entities=new ArrayList<>();
        final List<Integer> slots=new ArrayList<>();
        final List<Preview> residents=new ArrayList<>();
    }

    private final EntityRenderDispatcher dispatcher;
    public MobitatRenderer(BlockEntityRendererProvider.Context context) {
        dispatcher=context.entityRenderer();
    }
    public State createRenderState(){return new State();}

    private static boolean airborne(Entity entity) {
        return (entity instanceof Mob mob&&Terrestrial.flying(mob))
            || entity.getType().getCategory().name().contains("WATER");
    }

    @Override
    public void extractRenderState(MobitatBlockEntity be,State state,float partial,
                                   Vec3 camera,ModelFeatureRenderer.@Nullable CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(be,state,partial,camera,breaking);
        state.residents.clear();
        var level=be.getLevel();
        if(level==null)return;

        if(state.revision!=be.revision()) {
            state.revision=be.revision();
            state.entities.clear();
            state.slots.clear();
            state.previousTick=Long.MIN_VALUE;
            for(int slot=0;slot<be.size()&&slot<MobitatBlockEntity.CAPACITY;slot++) {
                var saved=be.residentData(slot);
                Identifier id=Identifier.tryParse(saved.getString("type").orElse(""));
                if(id==null)continue;
                var type=BuiltInRegistries.ENTITY_TYPE.getValue(id);
                if(type==null)continue;
                Entity entity=type.create(level,EntitySpawnReason.LOAD);
                if(entity==null)continue;
                var tag=saved.getCompound("entity").orElse(null);
                if(tag!=null)try {
                    entity.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),tag));
                }catch(Exception ex) {
                    LOGGER.debug("Could not apply Mobitat preview data for {}",id,ex);
                }
                entity.setId(previewId(be.getBlockPos(),slot));
                entity.setCustomNameVisible(false);
                entity.setNoGravity(true);
                entity.setPos(be.getBlockPos().getX()+.5,be.getBlockPos().getY()+.20,be.getBlockPos().getZ()+.5);
                state.entities.add(entity);
                state.slots.add(slot);
            }
        }

        long tick=level.getGameTime();
        float time=tick+partial;
        boolean stepAnimation=state.previousTick!=tick;
        state.previousTick=tick;
        BlockPos origin=be.getBlockPos();
        long seed=origin.asLong();

        for(int i=0;i<state.entities.size();i++) {
            Entity entity=state.entities.get(i);
            int slot=state.slots.get(i);
            var path=MobitatPreviewMovement.sample(time,slot,seed,airborne(entity),
                    entity.getBbWidth(),entity.getBbHeight());

            // Keep the synthetic entity's render state consistent with the
            // small path movement, but never add it to the world or tick AI.
            entity.setPos(origin.getX()+path.x(),origin.getY()+path.y(),origin.getZ()+path.z());
            entity.setDeltaMovement(path.vx(),0,path.vz());
            entity.tickCount=(int)tick+slot*9;

            float yaw=(float)Math.toDegrees(Math.atan2(-path.vx(),path.vz()));
            if(path.horizontalSpeed()>.00005f) {
                entity.setYRot(yaw);
                entity.yRotO=yaw;
                if(entity instanceof Mob mob) {
                    mob.setYBodyRot(yaw);
                    mob.yBodyRotO=yaw;
                    mob.setYHeadRot(yaw);
                    mob.yHeadRotO=yaw;
                }
            }
            if(entity instanceof Mob mob&&stepAnimation) {
                // Walk/wing cycles should match 10%-size motion, not normal
                // full-sized movement. Once per tick, not once per frame.
                mob.walkAnimation.update(Math.min(.12f,path.horizontalSpeed()*12f),
                        1f,1f);
            }

            try {
                EntityRenderState render=dispatcher.extractEntity(entity,partial);
                if(render instanceof TropicalFishRenderState fish)fish.isInWater=true;
                state.residents.add(new Preview(render,path));
            }catch(RuntimeException ex) {
                String key=BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
                if(WARNED_TYPES.add(key))
                    LOGGER.warn("Skipping Mobitat preview for {} because its renderer failed on a render-only entity",key,ex);
            }
        }
    }

    @Override
    public void submit(State state,PoseStack pose,SubmitNodeCollector collector,
                       CameraRenderState camera) {
        for(Preview resident:state.residents) {
            pose.pushPose();
            var pos=resident.position();
            pose.translate(pos.x(),pos.y(),pos.z());
            pose.scale(MobitatPreviewMovement.SCALE,MobitatPreviewMovement.SCALE,MobitatPreviewMovement.SCALE);
            dispatcher.submit(resident.render(),camera,0,0,0,pose,collector);
            pose.popPose();
        }
    }
}
