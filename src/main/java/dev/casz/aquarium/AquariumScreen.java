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
 private enum Page{MAIN,MOBS,DECOR}
 private static final int PANEL=0xFF102127,PANEL_EDGE=0xFF75949A,TEXT=0xFFEAF4F4,MUTED=0xFFC9DCDD,SELECT=0xFFFFD778;
 private Page page=Page.MAIN;
 private final List<Button> pageButtons=new ArrayList<>();
 private int decorPage,uiMobCount=-1,uiDecorCount=-1,uiSelectedMob=-1,uiSelectedDecor=-1,uiAnchor=-1,uiMobPage=-1;
 private float previewYaw=25,previewPitch=20;
 private boolean dragging;
 private double dragX,dragY;

 public AquariumScreen(AquariumMenu m,Inventory i,Component t){super(m,i,t,368,340);}
 private void send(int a){if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,a);}
 private Button b(String t,int x,int y,int w,int h,Runnable r){
  var q=addRenderableWidget(Button.builder(Component.literal(t),z->r.run()).bounds(leftPos+x,topPos+y,w,h).build());
  pageButtons.add(q);return q;
 }
 protected void init(){super.init();rebuild();}
 private void clearPageButtons(){for(var button:pageButtons)removeWidget(button);pageButtons.clear();}
 private void nav(){
  b("Back",8,8,44,18,()->{page=Page.MAIN;rebuild();});
  b(page==Page.MOBS?"▶ Mobs":"Mobs",104,32,72,20,()->{page=Page.MOBS;rebuild();});
  b(page==Page.DECOR?"▶ Decorate":"Decorate",184,32,80,20,()->{page=Page.DECOR;rebuild();});
 }
 private void rebuild(){
  clearPageButtons();
  if(page==Page.MAIN){
   b("Mobs",78,78,96,34,()->{page=Page.MOBS;rebuild();});
   b("Decorate",194,78,96,34,()->{page=Page.DECOR;rebuild();});
   snapshot();return;
  }
  nav();
  if(page==Page.MOBS)buildMobs();else buildDecor();
  snapshot();
 }
 private void buildMobs(){
  int pageIndex=menu.data.get(23),start=pageIndex*5,visible=Math.min(5,Math.max(0,menu.data.get(1)-start));
  for(int i=0;i<visible;i++){
   final int row=i,index=start+i;
   int id=menu.data.get(18+row);
   var type=id<0?null:BuiltInRegistries.ENTITY_TYPE.byId(id);
   String name=type==null?"Mob "+(index+1):Component.translatable(type.getDescriptionId()).getString();
   b((index==menu.data.get(2)?"▶ ":"")+font.plainSubstrByWidth(name,108),14,76+i*26,124,22,()->send(100+row));
  }
  int maxPage=menu.data.get(24);
  if(maxPage>0){
   b("<",58,208,28,18,()->send(23));
   b(">",90,208,28,18,()->send(24));
  }
  b("Pick Up",286,78,68,22,()->send(20));
 }
 private void buildDecor(){
  b((menu.data.get(8)==0?"▶ ":"")+"Ceiling",14,62,68,20,()->send(200));
  b((menu.data.get(8)==1?"▶ ":"")+"Body",86,62,68,20,()->send(201));
  b((menu.data.get(8)==2?"▶ ":"")+"Floor",158,62,68,20,()->send(202));
  b("Add Item",282,62,72,20,()->send(21));

  int pageSize=4,start=Math.min(decorPage*pageSize,Math.max(0,menu.data.get(6)-1));
  if(menu.data.get(6)>0){
   b("<",14,103,22,16,()->{decorPage=Math.max(0,decorPage-1);send(300+decorPage*pageSize);rebuild();});
   b(">",40,103,22,16,()->{decorPage=Math.min((menu.data.get(6)-1)/pageSize,decorPage+1);send(300+decorPage*pageSize);rebuild();});
  }
  for(int i=0;i<Math.min(pageSize,menu.data.get(6)-start);i++){
   final int n=start+i;int itemId=menu.data.get(25+i);var rowItem=itemId<0?null:BuiltInRegistries.ITEM.byId(itemId);String rowName=rowItem==null?"Item "+(n+1):new ItemStack(rowItem).getHoverName().getString();String prefix=n==menu.data.get(7)?"▶ ":"";
   b(prefix+font.plainSubstrByWidth(rowName,prefix.isEmpty()?62:54),14,123+i*24,72,20,()->send(300+n));
  }
  b("Remove",14,219,72,17,()->send(22));

  b("X−",244,105,48,18,()->send(30));b("X+",298,105,48,18,()->send(31));
  b("Y−",244,126,48,18,()->send(32));b("Y+",298,126,48,18,()->send(33));
  b("Z−",244,147,48,18,()->send(34));b("Z+",298,147,48,18,()->send(35));
  b("−",244,177,48,18,()->send(36));b("+",298,177,48,18,()->send(37));
  b("X−",244,216,17,17,()->send(38));b("X+",263,216,17,17,()->send(39));b("Y−",282,216,17,17,()->send(40));b("Y+",301,216,17,17,()->send(41));b("Z−",320,216,17,17,()->send(42));b("Z+",339,216,17,17,()->send(43));
  b("Reset",304,197,52,16,()->send(44));
 }
 private void snapshot(){
  uiMobCount=menu.data.get(1);uiDecorCount=menu.data.get(6);uiSelectedMob=menu.data.get(2);uiMobPage=menu.data.get(23);uiSelectedDecor=menu.data.get(7);uiAnchor=menu.data.get(8);
 }
 protected void containerTick(){
  super.containerTick();
  if(page==Page.MOBS&&(uiMobCount!=menu.data.get(1)||uiSelectedMob!=menu.data.get(2)||uiMobPage!=menu.data.get(23)))rebuild();
  else if(page==Page.DECOR&&(uiDecorCount!=menu.data.get(6)||uiSelectedDecor!=menu.data.get(7)||uiAnchor!=menu.data.get(8))){
   if(uiDecorCount!=menu.data.get(6)&&menu.data.get(6)>0)decorPage=menu.data.get(7)/4;
   rebuild();
  }
 }
 public boolean mouseClicked(MouseButtonEvent event,boolean doubleClick){
  double x=event.x(),y=event.y();
  if(page==Page.DECOR&&event.button()==0&&x>=leftPos+100&&x<leftPos+230&&y>=topPos+92&&y<topPos+222){
   dragging=true;dragX=x;dragY=y;return true;
  }
  return super.mouseClicked(event,doubleClick);
 }
 public boolean mouseDragged(MouseButtonEvent event,double dx,double dy){
  if(dragging&&page==Page.DECOR){
   double x=event.x(),y=event.y();previewYaw+=(float)(x-dragX);previewPitch=Math.clamp(previewPitch+(float)(y-dragY),-80,80);dragX=x;dragY=y;return true;
  }
  return super.mouseDragged(event,dx,dy);
 }
 public boolean mouseReleased(MouseButtonEvent event){dragging=false;return super.mouseReleased(event);}

 private void panel(GuiGraphicsExtractor g,int x,int y,int w,int h){g.fill(leftPos+x,topPos+y,leftPos+x+w,topPos+y+h,PANEL);g.outline(leftPos+x,topPos+y,w,h,PANEL_EDGE);}
 protected void extractBackground(GuiGraphicsExtractor g,int mx,int my,float a){
  super.extractBackground(g,mx,my,a);
  int kind=menu.data.get(0),bg=kind==0?0xFF163846:kind==1?0xFF29452C:0xFF40283F;
  g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,bg);
  if(page!=Page.MAIN){
   if(page==Page.MOBS){panel(g,8,64,136,168);panel(g,150,64,126,168);panel(g,282,64,78,168);}
   else{panel(g,8,86,82,154);panel(g,96,86,134,154);panel(g,236,86,124,154);}
  }
  panel(g,8,246,64,42);panel(g,94,246,172,88);
  if(page==Page.DECOR){
   int cx=leftPos+163,cy=topPos+155;double yaw=Math.toRadians(previewYaw),pitch=Math.toRadians(previewPitch);
   for(int i=-39;i<=39;i+=6){int ox=(int)(Math.cos(yaw)*i),oy=(int)(Math.sin(pitch)*i*.34);g.fill(cx+ox,cy+oy,cx+ox+2,cy+oy+2,0xFFB7D4D9);}
   if(menu.data.get(6)>0){
    var selected=BuiltInRegistries.ITEM.byId(menu.data.get(9));
    if(selected!=null){g.pose().pushMatrix();g.pose().translate(cx-24,cy-24);g.pose().scale(3,3);g.item(new ItemStack(selected),0,0);g.pose().popMatrix();g.outline(cx-27,cy-27,54,54,SELECT);}
   }
  }
  for(var slot:menu.slots){g.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,PANEL);g.fill(leftPos+slot.x,topPos+slot.y,leftPos+slot.x+16,topPos+slot.y+16,0xFF91AEB4);}
 }
 protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){
  String title=menu.data.get(0)==0?"Aquarium Editor":menu.data.get(0)==1?"Terrarium Editor":"Hostile Terrarium Editor";
  g.text(font,title,60,10,TEXT);
  if(page==Page.MAIN){g.text(font,"Choose what to edit",132,54,MUTED);g.text(font,"Mobs manage residents. Decorate edits the enclosure.",62,128,MUTED);g.text(font,"Input",14,249,MUTED);g.text(font,"Output",43,249,MUTED);g.text(font,"Player Inventory",103,239,MUTED);return;}
  if(page==Page.MOBS){
   int currentPage=menu.data.get(23)+1,totalPages=menu.data.get(24)+1;
   g.text(font,"Residents",14,68,TEXT);g.text(font,"Page "+currentPage+" / "+totalPages,14,211,MUTED);
   int id=menu.data.get(3);var type=id<0?null:BuiltInRegistries.ENTITY_TYPE.byId(id);
   String selected=type==null?"No inhabitants":Component.translatable(type.getDescriptionId()).getString();
   g.text(font,font.plainSubstrByWidth(selected,112),158,76,TEXT);
   g.text(font,"Residents: "+menu.data.get(1)+" / "+menu.capacity(),158,94,MUTED);
   g.text(font,"To remove a resident:",158,119,MUTED);
   g.text(font,"1. Put the correct empty",158,135,MUTED);
   g.text(font,"   container in Input.",158,147,MUTED);
   g.text(font,"2. Select the resident.",158,165,MUTED);
   g.text(font,"3. Press Pick Up.",158,177,MUTED);
   g.text(font,"Input",14,249,MUTED);g.text(font,"Output",43,249,MUTED);
   g.text(font,"Player Inventory",103,239,MUTED);
   return;
  }
  int count=menu.data.get(6),selectedIndex=menu.data.get(7);
  g.text(font,"Items",14,92,TEXT);
  g.text(font,"Preview",104,92,TEXT);
  g.text(font,count==0?"No decorations":"Item "+(selectedIndex+1)+" / "+count,104,104,MUTED);
  g.text(font,"Drag preview to rotate",104,116,MUTED);
  var item=BuiltInRegistries.ITEM.byId(menu.data.get(9));
  if(item!=null&&count>0)g.text(font,font.plainSubstrByWidth(new ItemStack(item).getHoverName().getString(),120),104,218,TEXT);
  g.text(font,"View "+Math.round(previewYaw)+"°",104,230,MUTED);

  g.text(font,"Move",244,92,TEXT);
  g.text(font,"Scale",244,165,TEXT);
  g.text(font,"Rotate",244,202,TEXT);
  g.text(font,"Input",14,249,MUTED);g.text(font,"Output",43,249,MUTED);
  g.text(font,"Player Inventory",103,239,MUTED);
 }
}
