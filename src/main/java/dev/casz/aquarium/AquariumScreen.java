package dev.casz.aquarium;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class AquariumScreen extends AbstractContainerScreen<AquariumMenu>{
 private static final int DECOR_ROWS=4;
 private enum Page{MAIN,MOBS,DECOR}
 private Page page=Page.MAIN;
 private final List<Button> pageButtons=new ArrayList<>();
 private int decorPage,uiMobCount=-1,uiDecorCount=-1,uiSelectedMob=-1,uiSelectedDecor=-1,uiAnchor=-1,uiMobPage=-1;
 private float previewYaw=25,previewPitch=20;
 private boolean dragging;
 private double dragX,dragY;

 public AquariumScreen(AquariumMenu m,Inventory i,Component t){super(m,i,t,390,280);}
 private void send(int a){if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,a);}
 private Button b(String t,int x,int y,int w,int h,Runnable r){var q=addRenderableWidget(Button.builder(Component.literal(t),z->r.run()).bounds(leftPos+x,topPos+y,w,h).build());pageButtons.add(q);return q;}
 private String title(){return menu.data.get(0)==0?"Aquarium Editor":menu.data.get(0)==1?"Terrarium Editor":"Hostile Terrarium Editor";}
 protected void init(){super.init();rebuild();}

 private void modeTabs(){
  b((page==Page.MOBS?"▶ ":"")+"Mobs",62,32,118,20,()->{page=Page.MOBS;rebuild();});
  b((page==Page.DECOR?"▶ ":"")+"Decorate",186,32,118,20,()->{page=Page.DECOR;rebuild();});
 }

 private void rebuild(){
  for(var q:pageButtons)removeWidget(q);pageButtons.clear();
  if(page==Page.MAIN){
   b("Mobs",74,76,110,34,()->{page=Page.MOBS;rebuild();});
   b("Decorate",206,76,110,34,()->{page=Page.DECOR;rebuild();});
   snapshot();return;
  }
  b("Back",8,8,46,18,()->{page=Page.MAIN;rebuild();});
  modeTabs();
  if(page==Page.MOBS){
   int start=menu.data.get(23)*5,visible=Math.min(5,Math.max(0,menu.data.get(1)-start));
   for(int i=0;i<visible;i++){
    final int row=i,index=start+i;int id=menu.data.get(18+row);var type=id<0?null:BuiltInRegistries.ENTITY_TYPE.byId(id);
    String name=type==null?"Mob "+(index+1):Component.translatable(type.getDescriptionId()).getString();
    name=font.plainSubstrByWidth(name,110);
    b((index==menu.data.get(2)?"▶ ":"")+name,14,72+i*22,126,20,()->send(100+row));
   }
   if(menu.data.get(24)>0){b("<",14,184,28,18,()->send(23));b(">",46,184,28,18,()->send(24));}
   b("Pick up selected",286,128,90,24,()->send(20));
  }else{
   decorPage=menu.data.get(6)>0?menu.data.get(7)/DECOR_ROWS:0;
   b((menu.data.get(8)==0?"▶ ":"")+"Ceiling",108,58,68,20,()->send(200));
   b((menu.data.get(8)==1?"▶ ":"")+"Body",180,58,68,20,()->send(201));
   b((menu.data.get(8)==2?"▶ ":"")+"Floor",252,58,68,20,()->send(202));
   int start=decorPage*DECOR_ROWS,visible=Math.min(DECOR_ROWS,Math.max(0,menu.data.get(6)-start));
   for(int i=0;i<visible;i++){
    final int n=start+i;int id=menu.data.get(25+i);var item=id<0?null:BuiltInRegistries.ITEM.byId(id);
    String name=item==null?"Item":new ItemStack(item).getHoverName().getString();
    name=font.plainSubstrByWidth(name,64);
    b((n==menu.data.get(7)?"▶ ":"")+"#"+(n+1)+" "+name,12,82+i*22,88,20,()->send(300+n));
   }
   if(menu.data.get(6)>DECOR_ROWS){
    b("<",12,172,28,18,()->{int p=Math.max(0,decorPage-1);send(300+p*DECOR_ROWS);});
    b(">",44,172,28,18,()->{int p=Math.min((menu.data.get(6)-1)/DECOR_ROWS,decorPage+1);send(300+p*DECOR_ROWS);});
   }
   b("Add",12,192,42,18,()->send(21));b("Remove",58,192,42,18,()->send(22));
   String[] move={"X-","X+","Y-","Y+","Z-","Z+"};
   for(int i=0;i<6;i++){final int a=30+i;b(move[i],260+i*19,96,18,18,()->send(a));}
   b("Scale -",260,130,55,18,()->send(36));b("Scale +",319,130,55,18,()->send(37));
   String[] rot={"X-","X+","Y-","Y+","Z-","Z+"};
   for(int i=0;i<6;i++){final int a=38+i;b(rot[i],260+i*19,164,18,18,()->send(a));}
   b("Reset transform",260,184,114,18,()->send(44));
  }
  snapshot();
 }

 private void snapshot(){uiMobCount=menu.data.get(1);uiDecorCount=menu.data.get(6);uiSelectedMob=menu.data.get(2);uiMobPage=menu.data.get(23);uiSelectedDecor=menu.data.get(7);uiAnchor=menu.data.get(8);}
 protected void containerTick(){
  super.containerTick();
  if(page==Page.MOBS&&(uiMobCount!=menu.data.get(1)||uiSelectedMob!=menu.data.get(2)||uiMobPage!=menu.data.get(23)))rebuild();
  else if(page==Page.DECOR&&(uiDecorCount!=menu.data.get(6)||uiSelectedDecor!=menu.data.get(7)||uiAnchor!=menu.data.get(8)))rebuild();
 }
 public boolean mouseClicked(MouseButtonEvent event,boolean doubleClick){
  double x=event.x(),y=event.y();
  if(page==Page.DECOR&&event.button()==0&&x>=leftPos+108&&x<leftPos+250&&y>=topPos+82&&y<topPos+180){dragging=true;dragX=x;dragY=y;return true;}
  return super.mouseClicked(event,doubleClick);
 }
 public boolean mouseDragged(MouseButtonEvent event,double dx,double dy){
  if(dragging&&page==Page.DECOR){double x=event.x(),y=event.y();previewYaw+=(float)(x-dragX);previewPitch=Math.clamp(previewPitch+(float)(y-dragY),-80,80);dragX=x;dragY=y;return true;}
  return super.mouseDragged(event,dx,dy);
 }
 public boolean mouseReleased(MouseButtonEvent event){dragging=false;return super.mouseReleased(event);}

 private void panel(GuiGraphicsExtractor g,int x,int y,int w,int h){g.fill(leftPos+x,topPos+y,leftPos+x+w,topPos+y+h,0xB5102127);g.outline(leftPos+x,topPos+y,w,h,0xFF6F9299);}
 public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float a){
  super.extractBackground(g,mx,my,a);
  int kind=menu.data.get(0),bg=kind==0?0xFF163846:kind==1?0xFF29452C:0xFF40283F;
  g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,bg);
  if(page==Page.MOBS){panel(g,8,64,138,140);panel(g,148,64,132,116);panel(g,284,64,98,116);}
  else if(page==Page.DECOR){
   panel(g,8,76,96,136);panel(g,106,80,146,104);panel(g,256,80,122,124);
   int cx=leftPos+179,cy=topPos+132;double yaw=Math.toRadians(previewYaw),pitch=Math.toRadians(previewPitch);
   for(int i=-38;i<=38;i+=8){int ox=(int)(Math.cos(yaw)*i),oy=(int)(Math.sin(pitch)*i*.35);g.fill(cx+ox,cy+oy,cx+ox+2,cy+oy+2,0xFFB7D4D9);}
   if(menu.data.get(6)>0){var selected=BuiltInRegistries.ITEM.byId(menu.data.get(9));if(selected!=null){g.pose().pushMatrix();g.pose().translate(cx-24,cy-24);g.pose().scale(3,3);g.item(new ItemStack(selected),0,0);g.pose().popMatrix();g.outline(cx-27,cy-27,54,54,0xFFFFD778);}}
  }
  if(page!=Page.MAIN)panel(g,106,196,172,80);
  for(var slot:menu.slots){g.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,0xFF102127);g.fill(leftPos+slot.x,topPos+slot.y,leftPos+slot.x+16,topPos+slot.y+16,0xFF91AEB4);}
 }
 protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){
  g.text(font,title(),page==Page.MAIN?8:64,10,0xFFF1FAFA);
  if(page==Page.MAIN){g.text(font,"Choose what you want to manage.",111,52,0xFFD7E7E8);g.text(font,"Residents, containers and naming",64,120,0xFFC9DCDD);g.text(font,"Items, placement, scale and rotation",202,120,0xFFC9DCDD);return;}
  g.text(font,"Inventory",112,188,0xFFD7E7E8);g.text(font,"Input",14,214,0xFFC9DCDD);g.text(font,"Out",43,214,0xFFC9DCDD);
  if(page==Page.MOBS){
   int pages=Math.max(1,menu.data.get(24)+1);g.text(font,"Residents",14,58,0xFFEAF4F4);g.text(font,"Page "+(menu.data.get(23)+1)+" / "+pages,78,189,0xFFC9DCDD);
   int id=menu.data.get(3);var type=id<0?null:BuiltInRegistries.ENTITY_TYPE.byId(id);String selected=type==null?"No resident selected":Component.translatable(type.getDescriptionId()).getString();
   g.text(font,font.plainSubstrByWidth(selected,118),156,76,0xFFEAF4F4);
   g.text(font,"Residents: "+menu.data.get(1)+" / "+menu.capacity(),156,94,0xFFC9DCDD);
   g.text(font,"Place the correct empty",156,116,0xFFC9DCDD);g.text(font,"container in Input.",156,128,0xFFC9DCDD);
   g.text(font,"Then select a resident",156,146,0xFFC9DCDD);g.text(font,"and pick it up.",156,158,0xFFC9DCDD);
   g.text(font,"Actions",294,76,0xFFEAF4F4);return;
  }
  g.text(font,"Decor items",14,64,0xFFEAF4F4);g.text(font,"Preview",112,68,0xFFEAF4F4);
  g.text(font,"Move",260,84,0xFFEAF4F4);g.text(font,"Scale",260,118,0xFFEAF4F4);g.text(font,"Rotate",260,152,0xFFEAF4F4);
  var item=BuiltInRegistries.ITEM.byId(menu.data.get(9));
  if(item!=null&&menu.data.get(6)>0){String name=new ItemStack(item).getHoverName().getString();g.text(font,font.plainSubstrByWidth(name,96),108,186,0xFFEAF4F4);g.text(font,Math.round(previewYaw)+"°",220,186,0xFFC9DCDD);}
 }
}
