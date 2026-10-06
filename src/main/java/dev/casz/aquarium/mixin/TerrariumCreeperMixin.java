package dev.casz.aquarium.mixin;
import dev.casz.aquarium.Terrestrial;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.network.syncher.EntityDataAccessor;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Creeper.class)
public abstract class TerrariumCreeperMixin {
 @Shadow private int swell;
 @Shadow private int oldSwell;
 @Shadow @Final private static EntityDataAccessor<Boolean> DATA_IS_IGNITED;
 @Inject(method="tick",at=@At("HEAD"))
 private void keepResidentCalm(CallbackInfo ci){Creeper creeper=(Creeper)(Object)this;if(creeper.entityTags().contains(Terrestrial.MANAGED)){swell=oldSwell=0;creeper.setSwellDir(-1);creeper.getEntityData().set(DATA_IS_IGNITED,false);}}
}