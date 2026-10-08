package dev.casz.aquarium.mixin.client;

import dev.casz.aquarium.Enclosures;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class AquariumSquidScaleMixin {
 @Inject(method="extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",at=@At("TAIL"))
 private void linkedAquariums$miniSquids(LivingEntity entity,LivingEntityRenderState state,float partialTicks,CallbackInfo ci){
  if((entity.getType()==EntityTypes.SQUID||entity.getType()==EntityTypes.GLOW_SQUID)&&Enclosures.isAquatic(entity.level().getBlockState(entity.blockPosition())))state.scale*=.25f;
 }
}
