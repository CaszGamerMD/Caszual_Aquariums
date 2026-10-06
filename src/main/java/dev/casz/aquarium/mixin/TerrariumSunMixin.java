package dev.casz.aquarium.mixin;
import dev.casz.aquarium.*;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Mob.class)
public abstract class TerrariumSunMixin {
 @Inject(method="isSunBurnTick",at=@At("HEAD"),cancellable=true)
 private void protectTerrariumResident(CallbackInfoReturnable<Boolean> cir){Mob mob=(Mob)(Object)this;if(mob.entityTags().contains(Terrestrial.MANAGED)&&Enclosures.kind(mob.level().getBlockState(mob.blockPosition()))==2)cir.setReturnValue(false);}
}
