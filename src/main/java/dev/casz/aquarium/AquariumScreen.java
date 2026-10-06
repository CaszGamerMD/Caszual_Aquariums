package dev.casz.aquarium;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.*;
public final class AquariumScreen extends AbstractContainerScreen<AquariumMenu> {
 private final List<Button> decorButtons=new ArrayList<>(),fishButtons=new ArrayList<>();private final List<GridButton> cells=new ArrayList<>();private Button tab,add,lower,higher;
 public AquariumScreen(AquariumMenu menu,Inventory inv,Component title){super(menu,inv,title,336,238);}
 private void send(int action){if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,action);}
 private Button button(String text,int x,int y,int w,int action){return addRenderableWidget(Button.builder(Component.literal(text),b->send(action)).bounds(leftPos+x,topPos+y,w,18).build());}
 private Button decor(String text,int x,int y,int w,int action){Button b=button(text,x,y,w,action);decorButtons.add(b);return b;}
 protected void init(){super.init();decorButtons.clear();fishButtons.clear();cells.clear();tab=button("Mobs",264,7,62,0);
  fishButtons.add(button("<",14,34,22,1));fishButtons.add(button(">",300,34,22,2));
  lower=decor("-",12,23,20,12);higher=decor("+",35,23,20,13);
  decor("<",12,154,20,14);decor(">",35,154,20,15);decor("^",58,154,20,16);decor("v",12,176,20,17);decor("Home",35,176,43,18);
  for(int i=0;i<81;i++)cells.add(addRenderableWidget(new GridButton(i)));
  decor("Rotate 45°",222,60,100,3);decor("Model",222,81,48,4);decor("Reset",274,81,48,11);
  decor("Left",146,101,40,5);decor("Right",191,101,40,6);decor("Back",236,101,40,7);decor("Front",281,101,41,8);
  add=button("Place",146,132,60,9);button("Remove",210,132,62,10);updateControls();
 }
 private void updateControls(){boolean fish=menu.data.get(0)==1;tab.setMessage(Component.literal(fish?"Decor":"Mobs"));add.setMessage(Component.literal(fish?"Add":"Place"));for(Button b:decorButtons)b.visible=!fish;for(Button b:fishButtons)b.visible=fish;lower.active=menu.data.get(23)>menu.data.get(24);higher.active=menu.data.get(23)<menu.data.get(25);
  for(GridButton b:cells){int bits=menu.data.get(AquariumMenu.GRID_START+b.cell);b.visible=!fish;b.active=(bits&1)!=0;int x=menu.anchor.getX()+menu.data.get(21)+b.cell%9,y=menu.anchor.getY()+menu.data.get(23),z=menu.anchor.getZ()+menu.data.get(22)+b.cell/9;b.setTooltip(Tooltip.create(Component.literal((bits&1)!=0?"Block "+x+", "+y+", "+z+((bits&8)!=0?" · Decor":"")+((bits&64)!=0?" · Opened here":""):(bits&2)!=0?"Connecting pipe":"Empty space")));}
 }
 protected void containerTick(){super.containerTick();updateControls();}
 public void extractBackground(GuiGraphicsExtractor g,int mouseX,int mouseY,float a){super.extractBackground(g,mouseX,mouseY,a);g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFF162C38);g.fill(leftPos+137,topPos+28,leftPos+329,topPos+151,0xFF243E49);for(var slot:menu.slots){g.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,0xFF172D36);g.fill(leftPos+slot.x,topPos+slot.y,leftPos+slot.x+16,topPos+slot.y+16,0xFF92ADB2);}}
 protected void extractLabels(GuiGraphicsExtractor g,int mouseX,int mouseY){g.text(font,menu.data.get(19)>0?"Linked Terrarium":"Linked Aquarium",8,10,0xFFE7F3F3);g.text(font,"In",287,121,0xFFC2D8DD);g.text(font,"Out",309,121,0xFFC2D8DD);
  if(menu.data.get(0)==1){g.text(font,"Creature "+(menu.data.get(11)==0?0:menu.data.get(14)+1)+" / "+menu.data.get(11),44,39,0xFFE7F3F3);int id=menu.data.get(15);var type=id<0?null:BuiltInRegistries.ENTITY_TYPE.byId(id);g.text(font,type==null?"No inhabitants":Component.translatable(type.getDescriptionId()).getString(),14,65,0xFFE7F3F3);g.text(font,"Capacity: "+menu.data.get(11)+" / "+menu.capacity(),14,84,0xFFC2D8DD);g.text(font,menu.data.get(19)>0?"Add: filled net in In. Remove: empty net.":"Add: fish bucket/egg in In. Remove: bucket.",14,103,0xFFC2D8DD);return;}
  g.text(font,"Layer "+(menu.data.get(23)-menu.data.get(24)+1),61,25,0xFFE7F3F3);g.text(font,"North ^",69,35,0xFFC2D8DD);g.text(font,"Gold: chosen",12,201,0xFFFFD778);g.text(font,"White: open",12,212,0xFFE7F3F3);
  g.text(font,"Selected block",146,34,0xFFE7F3F3);var item=BuiltInRegistries.ITEM.byId(menu.data.get(18));String name=menu.data.get(6)>0&&item!=null?new ItemStack(item).getHoverName().getString():"No decoration";g.text(font,font.plainSubstrByWidth(name,176),146,47,0xFFC2D8DD);
  g.fill(146,61,214,98,0xFF58808B);int cx=180+menu.data.get(9)*7,cy=80+menu.data.get(10)*4;
  if(menu.data.get(6)>0){g.fill(cx-5,cy-3,cx+5,cy+3,0xFFEBDAB7);double angle=Math.toRadians(menu.data.get(8)*45);for(int i=0;i<12;i++){int x=cx+(int)(Math.cos(angle)*i),y=cy+(int)(Math.sin(angle)*i);g.fill(x,y,x+2,y+2,0xFFFFE8A2);}}
 }
 private final class GridButton extends Button {
  final int cell;
  GridButton(int cell){super(leftPos+12+(cell%9)*12,topPos+44+(cell/9)*12,12,12,Component.literal("Select enclosure block"),b->send(32+cell),DEFAULT_NARRATION);this.cell=cell;}
  protected void extractContents(GuiGraphicsExtractor g,int mouseX,int mouseY,float delta){int x=getX(),y=getY(),bits=menu.data.get(AquariumMenu.GRID_START+cell);g.fill(x,y,x+12,y+12,0xFF13262E);
   if((bits&1)!=0){int color=menu.data.get(19)==0?0xFF3B8EA8:menu.data.get(19)==1?0xFF5B8C55:0xFF8667A5;g.fill(x+1,y+1,x+11,y+11,color);if((bits&4)!=0)g.fill(x+2,y+9,x+10,y+11,0xFFD6BC80);if((bits&8)!=0)g.fill(x+4,y+4,x+8,y+8,0xFFEBDAB7);if((bits&16)!=0)g.fill(x+2,y+7,x+10,y+9,0xFF4E9EDD);}
   else if((bits&2)!=0){int links=bits>>>8;g.fill(x+4,y+4,x+8,y+8,0xFF91ADB7);if((links&4)!=0)g.fill(x+4,y,x+8,y+6,0xFF91ADB7);if((links&8)!=0)g.fill(x+4,y+6,x+8,y+12,0xFF91ADB7);if((links&16)!=0)g.fill(x,y+4,x+6,y+8,0xFF91ADB7);if((links&32)!=0)g.fill(x+6,y+4,x+12,y+8,0xFF91ADB7);}
   if((bits&64)!=0)g.outline(x+2,y+2,8,8,0xFFF0F5F5);if((bits&32)!=0)g.outline(x,y,12,12,0xFFFFD778);else if(isHovered()&&active)g.outline(x,y,12,12,0xFFBDDDE7);
  }
 }
}
