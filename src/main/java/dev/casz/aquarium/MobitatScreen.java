package dev.casz.aquarium;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public final class MobitatScreen extends AbstractContainerScreen<MobitatMenu>{
 public MobitatScreen(MobitatMenu m,Inventory i,Component t){super(m,i,t,250,188);}private void send(int i){if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,i);}
 protected void init(){super.init();for(int i=0;i<5;i++){final int n=i;addRenderableWidget(Button.builder(Component.literal("Name "+(i+1)),b->send(n)).bounds(leftPos+90,topPos+18+i*18,70,16).build());}}
 public void extractBackground(GuiGraphicsExtractor g,int x,int y,float a){super.extractBackground(g,x,y,a);g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFF20343B);}
 protected void extractLabels(GuiGraphicsExtractor g,int x,int y){g.text(font,"Mobitat · "+menu.data.get(0)+"/5",8,7,0xFFEAF5F5);g.text(font,"Renamed Name Tag",8,58,0xFFC4D9DD);}
}