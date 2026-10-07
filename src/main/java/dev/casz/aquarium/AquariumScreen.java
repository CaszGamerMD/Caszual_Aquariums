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

 public AquariumScreen(AquariumMenu m,Inventory i,Component t){super(m,i,t,420,240);}
 private void send(int a){if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,a);}
 private Button b(String t,int x,int y,int w,int h,Runnable r){var q=addRenderableWidget(Button.builder(Component.literal(t),z->r.run()).bounds(leftPos+x,topPos+y,w,h).build());pageButtons.add(q);return q;}
 private String title(){return menu.data.get(0)==0?"Aquarium Editor":menu.data.get(0)==1?"Terrarium Editor":"Hostile Terrarium Editor";}
 protected void init(){super.init();rebuild();}

 private void modeTabs(){
  b((page==Page.MOBS?"▶ ":"")+"Mobs",94,30,112,18,()->{page=Page.MOBS;rebuild();});
  b((page==Page.DECOR?"▶ ":"")+"Decorate",212,30,112,18,()->{page=Page.DECOR;rebuild();});
 }

 private void rebuild(){
  for(var q:pageButtons)removeWidget(q);pageButtons.clear();
  if(page==Page.MAIN){
   b("Mobs",72,72,120,28,()->{page=Page.MOBS;rebuild();});
   b("Decorate",228,72,120,28,()->{page=Page.DECOR;rebuild();});
   snapshot();return;
  }

  b("Back",8,7,46,18,()->{page=Page.MAIN;rebuild();});
  modeTabs();

  if(page==Page.MOBS){
   int start=menu.data.get(23)*5,visible=Math.min(5,Math.max(0,menu.data.get(1)-start));
   if(menu.data.get(24)>0){b("<",112,54,20,16,()->send(23));b(">",136,54,20,16,()->send(24));}
   for(int i=0;i<visible;i++){
    final int row=i,index=start+i;int id=menu.data.get(18+row);var type=id<0?null:BuiltInRegistries.ENTITY_TYPE.byId(id);
    String name=type==null?"Mob "+(index+1):Component.translatable(type.getDescriptionId()).getString();
    name=font.plainSubstrByWidth(name,126);
    b((index==menu.data.get(2)?"▶ ":"")+name,12,74+i*18,146,16,()->send(100+row));
   }
   b("Pick up",326,78,80,22,()->send(20));
  }else{
   decorPage=menu.data.get(6)>0?menu.data.get(7)/DECOR_ROWS:0;
   b((menu.data.get(8)==0?"▶ ":"")+"Ceiling",124,52,68,18,()->send(200));
   b((menu.data.get(8)==1?"▶ ":"")+"Body",196,52,68,18,()->send(201));
   b((menu.data.get(8)==2?"▶ ":"")+"Floor",268,52,68,18,()->send(202));

   int start=decorPage*DECOR_ROWS,visible=Math.min(DECOR_ROWS,Math.max(0,menu.data.get(6)-start));
   for(int i=0;i<visible;i++){
    final int n=start+i;int id=menu.data.get(25+i);var item=id<0?null:BuiltInRegistries.ITEM.byId(id);
    String name=item==null?"Item":new ItemStack(item).getHoverName().getString();
    name=font.plainSubstrByWidth(name,106);
    b((n==menu.data.get(7)?"▶ ":"")+"#"+(n+1)+" "+name,12,91+i*15,126,14,()->send(300+n));
   }
   if(menu.data.get(6)>DECOR_ROWS){
    b("<",102,79,16,14,()->{int p=Math.max(0,decorPage-1);send(300+p*DECOR_ROWS);});
    b(">",120,79,16,14,()->{int p=Math.min((menu.data.get(6)-1)/DECOR_ROWS,decorPage+1);send(300+p*DECOR_ROWS);});
   }

   String[] move={"X-","X+","Y-","Y+","Z-","Z+"};
   for(int i=0;i<6;i++){final int a=30+i;b(move[i],264+i*24,90,22,18,()->send(a));}
   String[] rot={"RX-","RX+","RY-","RY+","RZ-","RZ+"};
   for(int i=0;i<6;i++){final int a=38+i;b(rot[i],264+i*24,110,22,18,()->send(a));}
   b("Scale-",264,130,44,18,()->send(36));b("Scale+",310,130,44,18,()->send(37));b("Reset",356,130,44,18,()->send(44));

   b("Add",14,204,40,18,()->send(21));
   b("Remove",58,204,46,18,()->send(22));
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
  if(page==Page.DECOR&&event.button()==0&&x>=leftPos+146&&x<leftPos+258&&y>=topPos+76&&y<topPos+150){dragging=true;dragX=x;dragY=y;return true;}
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

  if(page==Page.MAIN){
   panel(g,62,56,140,58);panel(g,218,56,140,58);
  }else if(page==Page.MOBS){
   panel(g,8,52,154,96);panel(g,166,52,150,96);panel(g,320,52,92,96);
  }else{
   panel(g,8,76,134,76);panel(g,146,76,112,76);panel(g,262,76,150,76);
   int cx=leftPos+202,cy=topPos+111;double yaw=Math.toRadians(previewYaw),pitch=Math.toRadians(previewPitch);
   for(int i=-34;i<=34;i+=7){int ox=(int)(Math.cos(yaw)*i),oy=(int)(Math.sin(pitch)*i*.30);g.fill(cx+ox,cy+oy,cx+ox+2,cy+oy+2,0xFFB7D4D9);}
   if(menu.data.get(6)>0){var selected=BuiltInRegistries.ITEM.byId(menu.data.get(9));if(selected!=null){g.pose().pushMatrix();g.pose().translate(cx-18,cy-18);g.pose().scale(2.25f,2.25f);g.item(new ItemStack(selected),0,0);g.pose().popMatrix();g.outline(cx-21,cy-21,42,42,0xFFFFD778);}}
  }

  panel(g,8,152,104,84);panel(g,116,152,174,84);panel(g,294,152,118,84);
  for(var slot:menu.slots){g.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,0xFF102127);g.fill(leftPos+slot.x,topPos+slot.y,leftPos+slot.x+16,topPos+slot.y+16,0xFF91AEB4);}
 }

 protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){
  g.text(font,title(),page==Page.MAIN?8:62,9,0xFFF1FAFA);

  if(page==Page.MAIN){
   g.text(font,"Choose what you want to manage.",126,36,0xFFD7E7E8);
   g.text(font,"Residents",104,62,0xFFEAF4F4);g.text(font,"Decorations",253,62,0xFFEAF4F4);
   g.text(font,"Mobs and containers",78,104,0xFFC9DCDD);g.text(font,"Place and transform items",228,104,0xFFC9DCDD);
  }else if(page==Page.MOBS){
   int pages=Math.max(1,menu.data.get(24)+1);
   g.text(font,"Residents",14,58,0xFFEAF4F4);g.text(font,"Page "+(menu.data.get(23)+1)+" / "+pages,70,58,0xFFC9DCDD);
   int id=menu.data.get(3);var type=id<0?null:BuiltInRegistries.ENTITY_TYPE.byId(id);String selected=type==null?"No resident selected":Component.translatable(type.getDescriptionId()).getString();
   g.text(font,"Selected",172,58,0xFFEAF4F4);g.text(font,font.plainSubstrByWidth(selected,132),172,74,0xFFF1FAFA);
   g.text(font,"Residents: "+menu.data.get(1)+" / "+menu.capacity(),172,90,0xFFC9DCDD);
   g.text(font,"Correct empty container",172,110,0xFFC9DCDD);g.text(font,"goes in Input.",172,122,0xFFC9DCDD);
   g.text(font,"Actions",326,58,0xFFEAF4F4);
  }else{
   g.text(font,"Decor items",14,82,0xFFEAF4F4);
   if(menu.data.get(6)>DECOR_ROWS)g.text(font,(decorPage+1)+"/"+(((menu.data.get(6)-1)/DECOR_ROWS)+1),78,82,0xFFC9DCDD);
   g.text(font,"Preview",152,82,0xFFEAF4F4);
   var item=BuiltInRegistries.ITEM.byId(menu.data.get(9));
   if(item!=null&&menu.data.get(6)>0){String name=new ItemStack(item).getHoverName().getString();g.text(font,font.plainSubstrByWidth(name,70),152,138,0xFFEAF4F4);g.text(font,Math.round(previewYaw)+"°",226,138,0xFFC9DCDD);}
   g.text(font,"Transform",268,82,0xFFEAF4F4);
  }

  g.text(font,"Transfer",14,158,0xFFD7E7E8);g.text(font,"Input",16,166,0xFFC9DCDD);g.text(font,"Out",48,166,0xFFC9DCDD);
  g.text(font,"Inventory",122,158,0xFFD7E7E8);

  if(page==Page.DECOR&&menu.data.get(6)>0){
   g.text(font,"Transform values",300,158,0xFFD7E7E8);
   g.text(font,"X "+fmt(menu.data.get(10))+"  Y "+fmt(menu.data.get(11)),300,174,0xFFC9DCDD);
   g.text(font,"Z "+fmt(menu.data.get(12))+"  S "+fmt(menu.data.get(13)),300,186,0xFFC9DCDD);
   g.text(font,"RX "+menu.data.get(14)+"  RY "+menu.data.get(15),300,202,0xFFC9DCDD);
   g.text(font,"RZ "+menu.data.get(16),300,214,0xFFC9DCDD);
  }else if(page==Page.MOBS){
   g.text(font,"Network",300,158,0xFFD7E7E8);g.text(font,"Residents",300,174,0xFFC9DCDD);g.text(font,menu.data.get(1)+" / "+menu.capacity(),300,186,0xFFEAF4F4);
  }else{
   g.text(font,"Tip",300,158,0xFFD7E7E8);g.text(font,"Choose Mobs or",300,174,0xFFC9DCDD);g.text(font,"Decorate above.",300,186,0xFFC9DCDD);
  }
 }
 private static String fmt(int hundredths){return String.format(Locale.ROOT,"%.2f",hundredths/100.0);}
}
