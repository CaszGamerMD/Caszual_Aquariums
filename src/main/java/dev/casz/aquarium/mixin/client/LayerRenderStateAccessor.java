package dev.casz.aquarium.mixin.client;

import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemStackRenderState.LayerRenderState.class)
public interface LayerRenderStateAccessor {
 @Accessor("quads") List<BakedQuad> linkedAquariums$getQuads();
 @Accessor("itemTransform") ItemTransform linkedAquariums$getItemTransform();
 @Accessor("localTransform") Matrix4f linkedAquariums$getLocalTransform();
 @Accessor("foilType") ItemStackRenderState.FoilType linkedAquariums$getFoilType();
 @Accessor("tintLayers") IntList linkedAquariums$getTintLayers();
 @Accessor("specialRenderer") SpecialModelRenderer linkedAquariums$getSpecialRenderer();
 @Accessor("argumentForSpecialRendering") Object linkedAquariums$getSpecialArgument();
}
