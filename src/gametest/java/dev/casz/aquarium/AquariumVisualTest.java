package dev.casz.aquarium;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
public class AquariumVisualTest implements FabricClientGameTest {
 public void runTest(ClientGameTestContext context){
  context.getInput().resizeWindow(1280,720);
  try(var world=context.worldBuilder().create()){
   var server=world.getServer();var connection=world.getConnection();
   server.runCommand("fill -8 99 -8 28 99 10 minecraft:smooth_quartz");
   server.runOnServer(s->{var level=connection.getServerLevel();level.clockManager().setTotalTicks(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.WORLD_CLOCK).getOrThrow(net.minecraft.world.clock.WorldClocks.OVERWORLD),6000);
    for(int kind=0;kind<3;kind++){
     int base=kind*9;var tank=kind==0?AquariumMod.TANK:kind==1?AquariumMod.PASSIVE_TERRARIUM:AquariumMod.HOSTILE_TERRARIUM;var pipe=kind==0?AquariumMod.TUBE:kind==1?AquariumMod.PASSIVE_PIPE:AquariumMod.HOSTILE_PIPE;
     for(int x=0;x<3;x++)for(int y=0;y<2;y++)for(int z=0;z<2;z++)level.setBlock(new BlockPos(base+x,100+y,z),tank.defaultBlockState(),3);
     for(int x=3;x<5;x++)level.setBlock(new BlockPos(base+x,100,0),pipe.defaultBlockState(),3);
     level.setBlock(new BlockPos(base+5,100,0),tank.defaultBlockState(),3);
     for(int x=0;x<3;x++)for(int z=0;z<2;z++){var p=new BlockPos(base+x,100,z);level.setBlock(p,level.getBlockState(p).setValue(AquariumBlock.SOIL,kind==2?3:1),3);}
     var p=new BlockPos(base+1,100,1);var be=(TankBlockEntity)level.getBlockEntity(p);be.setDecoration(new ItemStack(kind==2?Items.STONE_BUTTON:kind==1?Items.OAK_FENCE:Items.FERN));be.changed();be.tick();
    }
   });
   server.runCommand("gamemode spectator @a");context.getInput().pressKey(290);
   for(int kind=0;kind<3;kind++){
    int base=kind*9;server.runCommand("tp @a "+(base-2)+" 102 -5");connection.waitForClientboundPackets();context.getInput().lookAt(new BlockPos(base+2,100,0));context.waitTicks(5);connection.waitForChunksRender();context.takeScreenshot("enclosure-"+kind);
   }
   server.runCommand("tp @a 4.0 100.3 -3");connection.waitForClientboundPackets();context.getInput().lookAt(new BlockPos(4,100,0));context.waitTicks(5);connection.waitForChunksRender();context.takeScreenshot("tube-water");
   server.runCommand("gamemode creative @a");server.runCommand("tp @a 1.5 100 -2");connection.waitForClientboundPackets();context.getInput().pressKey(290);
   server.runOnServer(s->{var p=connection.getServerPlayer();p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);var pos=new BlockPos(1,100,0);net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.invoker().interact(p,connection.getServerLevel(),InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.NORTH,pos,false));});
   context.waitForScreen(AquariumScreen.class);context.waitTicks(3);context.takeScreenshot("decor-grid");
   double[] cursor=context.computeOnClient(mc->{var w=mc.getWindow();return new double[]{((w.getGuiScaledWidth()-336)/2+12+5*12+6)*w.getGuiScale(),((w.getGuiScaledHeight()-238)/2+44+4*12+6)*w.getGuiScale()};});
   context.getInput().setCursorPos(cursor[0],cursor[1]);context.getInput().pressMouse(0);connection.waitForServerboundPackets();context.waitTicks(2);
   server.runOnServer(s->{var menu=(AquariumMenu)connection.getServerPlayer().containerMenu;if(menu.data.get(3)!=1)throw new AssertionError("Grid pointer click did not select eastern tank");});
   context.clickScreenButton("+");connection.waitForServerboundPackets();context.waitTicks(2);
   server.runOnServer(s->{var menu=(AquariumMenu)connection.getServerPlayer().containerMenu;if(menu.data.get(23)!=1)throw new AssertionError("Layer button failed");});
   context.clickScreenButton("Home");context.clickScreenButton("Mobs");context.waitTicks(3);context.takeScreenshot("residents");context.setScreen(IconPreview::new);context.waitTicks(3);context.takeScreenshot("inventory-icons");context.setScreen(()->null);
  }
 }
 private static class IconPreview extends Screen {
  IconPreview(){super(Component.literal("Enclosure icons"));}
  public void extractRenderState(GuiGraphicsExtractor g,int mouseX,int mouseY,float delta){
   g.fill(0,0,width,height,0xFF202E36);g.centeredText(font,"Enclosure inventory models",width/2,22,0xFFE7F3F3);
   var items=new net.minecraft.world.level.block.Block[]{AquariumMod.TANK,AquariumMod.PASSIVE_TERRARIUM,AquariumMod.HOSTILE_TERRARIUM,AquariumMod.TUBE,AquariumMod.PASSIVE_PIPE,AquariumMod.HOSTILE_PIPE};
   String[] labels={"Aquarium","Passive","Hostile","Swim tube","Passive pipe","Hostile pipe"};
   for(int i=0;i<6;i++){int x=width/2-125+(i%3)*100,y=55+(i/3)*100;g.pose().pushMatrix();g.pose().translate(x,y);g.pose().scale(3,3);g.item(new ItemStack(items[i]),0,0);g.pose().popMatrix();g.text(font,labels[i],x,y+55,0xFFE7F3F3);}
  }
 }
}
