package dev.casz.aquarium.mixin.client;

import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemStackRenderState.LayerRenderState.class)
public interface LayerRenderStateAccessor {
 @Accessor("quads") List<BakedQuad> linkedAquariums$getQuads();
 @Accessor("transform") ItemTransform linkedAquariums$getTransform();
 @Accessor("renderType") RenderType linkedAquariums$getRenderType();
 @Accessor("foilType") ItemStackRenderState.FoilType linkedAquariums$getFoilType();
 @Accessor("tintLayers") int[] linkedAquariums$getTintLayers();
 @Accessor("specialRenderer") SpecialModelRenderer linkedAquariums$getSpecialRenderer();
 @Accessor("argumentForSpecialRendering") Object linkedAquariums$getSpecialArgument();
}
