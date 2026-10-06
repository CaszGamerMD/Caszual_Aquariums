package dev.casz.aquarium;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;

public final class TropicalFishEditorScreen extends AbstractContainerScreen<TropicalFishEditorMenu>{
 private static final int[] COLORS={0xFFF9FFFE,0xFFF9801D,0xFFC74EBD,0xFF3AB3DA,0xFFFED83D,0xFF80C71F,0xFFF38BAA,0xFF474F52,0xFF9D9D97,0xFF169C9C,0xFF8932B8,0xFF3C44AA,0xFF835432,0xFF5E7C16,0xFFB02E26,0xFF1D1D21};
 private final List<Button> controls=new ArrayList<>();private int lastOccupied=-1,lastPattern=-1,lastBase=-1,lastAccent=-1;
 public TropicalFishEditorScreen(TropicalFishEditorMenu menu,Inventory inv,Component title){super(menu,inv,title,350,236);}
 private void send(int action){if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,action);}
 private Button button(String text,int x,int y,int w,int h,Runnable run){var b=addRenderableWidget(Button.builder(Component.literal(text),q->run.run()).bounds(leftPos+x,topPos+y,w,h).build());controls.add(b);return b;}
 protected void init(){super.init();rebuild();}
 private void rebuild(){for(var b:controls)removeWidget(b);controls.clear();boolean active=menu.data.get(0)==1;var patterns=TropicalFish.Pattern.values();
  for(int i=0;i<patterns.length;i++){final int n=i;String label=(menu.data.get(1)==i?"▶ ":"")+patterns[i].displayName().getString();var b=button(label,10+(i%3)*92,38+(i/3)*22,88,20,()->send(n));b.active=active;}
  var swap=button("Swap colors",290,72,52,38,()->send(201));var random=button("Random",290,114,52,38,()->send(200));swap.active=random.active=active;snapshot();
 }
 private void snapshot(){lastOccupied=menu.data.get(0);lastPattern=menu.data.get(1);lastBase=menu.data.get(2);lastAccent=menu.data.get(3);}
 protected void containerTick(){super.containerTick();if(lastOccupied!=menu.data.get(0)||lastPattern!=menu.data.get(1)||lastBase!=menu.data.get(2)||lastAccent!=menu.data.get(3))rebuild();}
 private int paletteIndex(double x,double y,int ox,int oy){double lx=x-(leftPos+ox),ly=y-(topPos+oy);if(lx<0||ly<0)return-1;int col=(int)(lx/16),row=(int)(ly/16);if(col<0||col>=8||row<0||row>=2||lx-col*16>=14||ly-row*16>=14)return-1;return row*8+col;}
 public boolean mouseClicked(MouseButtonEvent event,boolean doubleClick){if(menu.data.get(0)==1&&event.button()==0){int base=paletteIndex(event.x(),event.y(),10,166);if(base>=0){send(100+base);return true;}int accent=paletteIndex(event.x(),event.y(),174,166);if(accent>=0){send(120+accent);return true;}}return super.mouseClicked(event,doubleClick);}
 private void palette(GuiGraphicsExtractor g,int ox,int oy,int selected){for(int i=0;i<16;i++){int x=leftPos+ox+(i%8)*16,y=topPos+oy+(i/8)*16;g.fill(x,y,x+14,y+14,COLORS[i]);g.outline(x-1,y-1,16,16,i==selected?0xFFFFFFFF:0xFF49636B);}}
 public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial){super.extractBackground(g,mx,my,partial);g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFF153945);g.outline(leftPos+6,topPos+30,280,98,0xFF6A9DA8);g.outline(leftPos+6,topPos+158,140,40,0xFF6A9DA8);g.outline(leftPos+170,topPos+158,140,40,0xFF6A9DA8);if(menu.data.get(0)==1){palette(g,10,166,menu.data.get(2));palette(g,174,166,menu.data.get(3));}}
 protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){g.text(font,"Tropical Fish Editor",10,10,0xFFF1FAFA);if(menu.data.get(0)==0){g.text(font,"Insert a Tropical Fish Bucket into the tank.",10,22,0xFFD2E8EB);g.text(font,"The fish will appear inside the block when loaded.",10,44,0xFFB9D7DC);return;}var pattern=TropicalFish.Pattern.values()[Math.floorMod(menu.data.get(1),TropicalFish.Pattern.values().length)];String size=pattern.base()==TropicalFish.Base.SMALL?"Small body":"Large body";g.text(font,"Pattern: "+pattern.displayName().getString()+" · "+size,10,22,0xFFD2E8EB);g.text(font,"Body color · "+DyeColor.byId(menu.data.get(2)).getName(),10,148,0xFFEAF5F5);g.text(font,"Pattern color · "+DyeColor.byId(menu.data.get(3)).getName(),174,148,0xFFEAF5F5);g.text(font,"Use an empty bucket on the block to take the edited fish.",10,210,0xFFB9D7DC);}
}
