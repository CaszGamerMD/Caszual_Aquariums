package dev.casz.aquarium;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
public final class AquariumClient implements ClientModInitializer {
 public void onInitializeClient(){
  net.minecraft.client.gui.screens.MenuScreens.register(AquariumMod.MENU,AquariumScreen::new);
  net.minecraft.client.gui.screens.MenuScreens.register(AquariumMod.MOBITAT_MENU,MobitatScreen::new);
  BlockEntityRenderers.register(AquariumMod.MOBITAT_ENTITY,MobitatRenderer::new);
  BlockColorRegistry.register(List.of(BlockTintSources.constant(0xFF78AD42)),AquariumMod.TANK,AquariumMod.PASSIVE_TERRARIUM,AquariumMod.HOSTILE_TERRARIUM,AquariumMod.DECOR_MODEL);
  Material invisible=new Material(Identifier.fromNamespaceAndPath(AquariumMod.ID,"block/invisible_water"));
  FluidRenderingRegistry.register(AquariumMod.WATER,new FluidModel.Unbaked(invisible,invisible,invisible,BlockTintSources.constant(-1)),
   new net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler(){
    @Override public void renderFluid(net.minecraft.client.renderer.block.FluidRenderer renderer,net.minecraft.core.BlockPos pos,net.minecraft.client.renderer.block.BlockAndTintGetter level,net.minecraft.client.renderer.block.FluidRenderer.Output output,net.minecraft.world.level.block.state.BlockState block,net.minecraft.world.level.material.FluidState fluid){}
   });
 }
}
