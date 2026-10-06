package dev.casz.aquarium;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;

public final class TropicalFishEditorScreen extends AbstractContainerScreen<TropicalFishEditorMenu>{
 private static final int[] COLORS={0xFFF9FFFE,0xFFF9801D,0xFFC74EBD,0xFF3AB3DA,0xFFFED83D,0xFF80C71F,0xFFF38BAA,0xFF474F52,0xFF9D9D97,0xFF169C9C,0xFF8932B8,0xFF3C44AA,0xFF835432,0xFF5E7C16,0xFFB02E26,0xFF1D1D21};
 private final List<Button> controls=new ArrayList<>();private int lastOccupied=-1,lastPattern=-1,lastBase=-1,lastAccent=-1;
 public TropicalFishEditorScreen(TropicalFishEditorMenu menu,Inventory inv,Component title){super(menu,inv,title,330,220);}
 private void send(int action){if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,action);}
 private Button button(String text,int x,int y,int w,int h,Runnable run){var b=addRenderableWidget(Button.builder(Component.literal(text),q->run.run()).bounds(leftPos+x,topPos+y,w,h).build());controls.add(b);return b;}
 protected void init(){super.init();rebuild();}
 private void rebuild(){for(var b:controls)removeWidget(b);controls.clear();boolean active=menu.data.get(0)==1;var patterns=TropicalFish.Pattern.values();for(int i=0;i<patterns.length;i++){final int n=i;String label=(menu.data.get(1)==i?"▶ ":"")+patterns[i].displayName().getString();var b=button(label,10+(i%3)*86,38+(i/3)*22,82,20,()->send(n));b.active=active;}
  var b1=button("Body ◀",10,136,72,20,()->send(100+Math.floorMod(menu.data.get(2)-1,16)));var b2=button("Body ▶",86,136,72,20,()->send(100+Math.floorMod(menu.data.get(2)+1,16)));
  var p1=button("Pattern ◀",10,160,72,20,()->send(120+Math.floorMod(menu.data.get(3)-1,16)));var p2=button("Pattern ▶",86,160,72,20,()->send(120+Math.floorMod(menu.data.get(3)+1,16)));
  var swap=button("Swap colors",174,136,92,20,()->send(201));var random=button("Randomize",174,160,92,20,()->send(200));for(var b:List.of(b1,b2,p1,p2,swap,random))b.active=active;snapshot();}
 private void snapshot(){lastOccupied=menu.data.get(0);lastPattern=menu.data.get(1);lastBase=menu.data.get(2);lastAccent=menu.data.get(3);}
 protected void containerTick(){super.containerTick();if(lastOccupied!=menu.data.get(0)||lastPattern!=menu.data.get(1)||lastBase!=menu.data.get(2)||lastAccent!=menu.data.get(3))rebuild();}
 public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial){super.extractBackground(g,mx,my,partial);g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFF153945);g.fill(leftPos+270,topPos+36,leftPos+320,topPos+184,0xFF0B252E);g.outline(leftPos+270,topPos+36,50,148,0xFF85C3D1);if(menu.data.get(0)==1){int base=COLORS[Math.floorMod(menu.data.get(2),16)],accent=COLORS[Math.floorMod(menu.data.get(3),16)];g.fill(leftPos+282,topPos+70,leftPos+308,topPos+96,base);g.outline(leftPos+280,topPos+68,30,30,0xFFFFFFFF);g.fill(leftPos+282,topPos+126,leftPos+308,topPos+152,accent);g.outline(leftPos+280,topPos+124,30,30,0xFFFFFFFF);}}
 protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){g.text(font,"Tropical Fish Editor",10,10,0xFFF1FAFA);if(menu.data.get(0)==0){g.text(font,"Insert a Tropical Fish Bucket into the tank.",10,24,0xFFD2E8EB);return;}var pattern=TropicalFish.Pattern.values()[Math.floorMod(menu.data.get(1),TropicalFish.Pattern.values().length)];g.text(font,"Pattern: "+pattern.displayName().getString(),10,24,0xFFD2E8EB);g.text(font,"Body",278,54,0xFFEAF5F5);g.text(font,DyeColor.byId(menu.data.get(2)).getName(),274,101,0xFFD2E8EB);g.text(font,"Pattern",274,110,0xFFEAF5F5);g.text(font,DyeColor.byId(menu.data.get(3)).getName(),274,157,0xFFD2E8EB);g.text(font,"Use an empty bucket on the block to take the edited fish.",10,192,0xFFB9D7DC);}
}
