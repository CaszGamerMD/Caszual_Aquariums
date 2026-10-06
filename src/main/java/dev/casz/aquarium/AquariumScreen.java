package dev.casz.aquarium;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
public final class AquariumScreen extends AbstractContainerScreen<AquariumMenu>{
 private enum Page{MAIN,MOBS,DECOR}private Page page=Page.MAIN;private final List<Button> pageButtons=new ArrayList<>();private int decorPage;private float previewYaw=25,previewPitch=20;private boolean dragging;private double dragX,dragY;
 public AquariumScreen(AquariumMenu m,Inventory i,Component t){super(m,i,t,320,258);}
 private void send(int a){if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,a);}
 private Button b(String t,int x,int y,int w,int h,Runnable r){var q=addRenderableWidget(Button.builder(Component.literal(t),z->r.run()).bounds(leftPos+x,topPos+y,w,h).build());pageButtons.add(q);return q;}
 protected void init(){super.init();rebuild();}
 private void rebuild(){for(var b:pageButtons)removeWidget(b);pageButtons.clear();if(page==Page.MAIN){b("Mobs",52,72,96,32,()->{page=Page.MOBS;rebuild();});b("Decorate",172,72,96,32,()->{page=Page.DECOR;rebuild();});return;}b("Back",8,8,46,18,()->{page=Page.MAIN;rebuild();});
  if(page==Page.MOBS){for(int i=0;i<Math.min(5,menu.data.get(1));i++){final int n=i;b("Mob "+(i+1),18,42+i*25,116,21,()->send(100+n));}b("Pick up selected",18,174,116,20,()->send(20));}
  else {b("Ceiling",8,32,70,18,()->send(200));b("Body",82,32,70,18,()->send(201));b("Floor",156,32,70,18,()->send(202));b("Add item",230,32,70,18,()->send(21));for(int i=0;i<Math.min(8,menu.data.get(6));i++){final int n=i;b("#"+(i+1),8,58+i*20,54,18,()->send(300+n));}b("Remove",66,218,58,18,()->send(22));String[] names={"X-","X+","Y-","Y+","Z-","Z+","Scale-","Scale+","RX-","RX+","RY-","RY+","RZ-","RZ+","Reset"};for(int i=0;i<names.length;i++){final int a=30+i;b(names[i],190+(i%3)*38,58+(i/3)*20,36,18,()->send(a));}}
 }
 protected void containerTick(){super.containerTick();if(page==Page.MOBS||page==Page.DECOR){}}
 public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float a){super.extractBackground(g,mx,my,a);int kind=menu.data.get(0),bg=kind==0?0xFF163846:kind==1?0xFF29452C:0xFF40283F;g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,bg);if(page==Page.DECOR){g.fill(leftPos+68,topPos+58,leftPos+184,topPos+202,0xFF102127);g.outline(leftPos+68,topPos+58,116,144,0xFF9CBAC0);int cx=leftPos+126,cy=topPos+130;double yaw=Math.toRadians(previewYaw),pitch=Math.toRadians(previewPitch);for(int i=-35;i<=35;i+=7){int ox=(int)(Math.cos(yaw)*i),oy=(int)(Math.sin(pitch)*i*.35);g.fill(cx+ox,cy+oy,cx+ox+2,cy+oy+2,0xFFB7D4D9);}}for(var slot:menu.slots){g.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,0xFF102127);g.fill(leftPos+slot.x,topPos+slot.y,leftPos+slot.x+16,topPos+slot.y+16,0xFF91AEB4);}}
 protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){String title=menu.data.get(0)==0?"Aquarium Editor":menu.data.get(0)==1?"Terrarium Editor":"Hostile Terrarium Editor";g.text(font,title,8,8,0xFFF1FAFA);if(page==Page.MAIN){g.text(font,"Choose what to edit",105,45,0xFFD7E7E8);return;}if(page==Page.MOBS){g.text(font,"Mobs  "+menu.data.get(1)+" / "+menu.capacity(),18,28,0xFFEAF4F4);int id=menu.data.get(3);var type=id<0?null:BuiltInRegistries.ENTITY_TYPE.byId(id);g.text(font,type==null?"No inhabitants":Component.translatable(type.getDescriptionId()).getString(),150,46,0xFFEAF4F4);g.text(font,"Put the correct empty container in the input slot,",150,70,0xFFC9DCDD);g.text(font,"then right-click/select the resident to pick it up.",150,82,0xFFC9DCDD);return;}g.text(font,"Decorate · "+(menu.data.get(6)==0?"no items":"item "+(menu.data.get(7)+1)+" / "+menu.data.get(6)),68,44,0xFFEAF4F4);var item=BuiltInRegistries.ITEM.byId(menu.data.get(9));if(item!=null&&menu.data.get(6)>0)g.text(font,font.plainSubstrByWidth(new ItemStack(item).getHoverName().getString(),110),68,207,0xFFEAF4F4);g.text(font,"Drag preview to rotate",68,220,0xFFC9DCDD);g.text(font,"Input",14,188,0xFFC9DCDD);g.text(font,"Out",39,188,0xFFC9DCDD);}
}