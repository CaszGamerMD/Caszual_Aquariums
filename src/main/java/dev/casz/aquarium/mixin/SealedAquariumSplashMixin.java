package dev.casz.aquarium.mixin;

import dev.casz.aquarium.AquariumWaterEffects;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Only suppress the false particle/sound burst when an external mob brushes
 * the FULL fluid state of a sealed aquarium or swim tube through its glass.
 *
 * All fluid checks, fish physics, oxygen, rendering and entity movement stay
 * untouched. Real open water continues to produce ordinary splashes.
 */
@Mixin(Entity.class)
public abstract class SealedAquariumSplashMixin {
    @Inject(method="doWaterSplashEffect",at=@At("HEAD"),cancellable=true)
    private void linkedAquariums$preventFalseExteriorSplash(CallbackInfo ci) {
        Entity entity=(Entity)(Object)this;
        if(AquariumWaterEffects.shouldSuppressSplash(
                entity.level(),entity.getBoundingBox(),entity.position())) {
            ci.cancel();
        }
    }
}
