package dev.casz.aquarium.mixin;

import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemStackRenderState.LayerRenderState.class)
public interface ItemLayerRenderStateAccessor {
 @Accessor("quads") List<BakedQuad> linkedAquariums$getQuads();
 @Accessor("itemTransform") ItemTransform linkedAquariums$getItemTransform();
 @Accessor("localTransform") Matrix4f linkedAquariums$getLocalTransform();
 @Accessor("foilType") ItemStackRenderState.FoilType linkedAquariums$getFoilType();
 @Accessor("tintLayers") IntList linkedAquariums$getTintLayers();
 @Accessor("specialRenderer") SpecialModelRenderer<Object> linkedAquariums$getSpecialRenderer();
}
