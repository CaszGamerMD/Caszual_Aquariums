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

 public AquariumScreen(AquariumMenu m,Inventory i,Component t){super(m,i,t,390,310);}
 private void send(int a){if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,a);}
 private Button b(String t,int x,int y,int w,int h,Runnable r){var q=addRenderableWidget(Button.builder(Component.literal(t),z->r.run()).bounds(leftPos+x,topPos+y,w,h).build());pageButtons.add(q);return q;}
 private String title(){return menu.data.get(0)==0?"Aquarium Editor":menu.data.get(0)==1?"Terrarium Editor":"Hostile Terrarium Editor";}
 protected void init(){super.init();rebuild();}

 private void modeTabs(){
  b((page==Page.MOBS?"▶ ":"")+"Mobs",72,32,116,20,()->{page=Page.MOBS;rebuild();});
  b((page==Page.DECOR?"▶ ":"")+"Decorate",194,32,116,20,()->{page=Page.DECOR;rebuild();});
 }

 private void rebuild(){
  for(var q:pageButtons)removeWidget(q);pageButtons.clear();
  if(page==Page.MAIN){
   b("Mobs",58,78,118,32,()->{page=Page.MOBS;rebuild();});
   b("Decorate",214,78,118,32,()->{page=Page.DECOR;rebuild();});
   snapshot();return;
  }

  b("Back",8,8,46,18,()->{page=Page.MAIN;rebuild();});
  modeTabs();

  if(page==Page.MOBS){
   int start=menu.data.get(23)*5,visible=Math.min(5,Math.max(0,menu.data.get(1)-start));
   if(menu.data.get(24)>0){b("<",94,66,22,18,()->send(23));b(">",120,66,22,18,()->send(24));}
   for(int i=0;i<visible;i++){
    final int row=i,index=start+i;int id=menu.data.get(18+row);var type=id<0?null:BuiltInRegistries.ENTITY_TYPE.byId(id);
    String name=type==null?"Mob "+(index+1):Component.translatable(type.getDescriptionId()).getString();
    name=font.plainSubstrByWidth(name,108);
    b((index==menu.data.get(2)?"▶ ":"")+name,14,88+i*22,126,20,()->send(100+row));
   }
   b("Pick up selected",290,96,86,24,()->send(20));
  }else{
   decorPage=menu.data.get(6)>0?menu.data.get(7)/DECOR_ROWS:0;
   b((menu.data.get(8)==0?"▶ ":"")+"Ceiling",110,58,68,20,()->send(200));
   b((menu.data.get(8)==1?"▶ ":"")+"Body",182,58,68,20,()->send(201));
   b((menu.data.get(8)==2?"▶ ":"")+"Floor",254,58,68,20,()->send(202));

   int start=decorPage*DECOR_ROWS,visible=Math.min(DECOR_ROWS,Math.max(0,menu.data.get(6)-start));
   for(int i=0;i<visible;i++){
    final int n=start+i;int id=menu.data.get(25+i);var item=id<0?null:BuiltInRegistries.ITEM.byId(id);
    String name=item==null?"Item":new ItemStack(item).getHoverName().getString();
    name=font.plainSubstrByWidth(name,61);
    b((n==menu.data.get(7)?"▶ ":"")+"#"+(n+1)+" "+name,12,106+i*22,90,20,()->send(300+n));
   }
   if(menu.data.get(6)>DECOR_ROWS){
    b("<",14,194,28,18,()->{int p=Math.max(0,decorPage-1);send(300+p*DECOR_ROWS);});
    b(">",46,194,28,18,()->{int p=Math.min((menu.data.get(6)-1)/DECOR_ROWS,decorPage+1);send(300+p*DECOR_ROWS);});
   }

   String[] move={"X-","X+","Y-","Y+","Z-","Z+"};
   for(int i=0;i<6;i++){final int a=30+i;b(move[i],260+i*19,104,18,18,()->send(a));}
   b("Scale -",260,142,55,18,()->send(36));b("Scale +",319,142,55,18,()->send(37));
   String[] rot={"X-","X+","Y-","Y+","Z-","Z+"};
   for(int i=0;i<6;i++){final int a=38+i;b(rot[i],260+i*19,180,18,18,()->send(a));}
   b("Reset transform",260,198,114,18,()->send(44));

   b("Add",12,276,40,18,()->send(21));
   b("Remove",56,276,42,18,()->send(22));
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
  if(page==Page.DECOR&&event.button()==0&&x>=leftPos+110&&x<leftPos+252&&y>=topPos+102&&y<topPos+194){dragging=true;dragX=x;dragY=y;return true;}
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
   panel(g,48,60,138,72);panel(g,204,60,138,72);
  }else if(page==Page.MOBS){
   panel(g,8,62,138,148);panel(g,150,62,132,148);panel(g,286,62,96,148);
  }else{
   panel(g,8,84,98,130);panel(g,110,84,142,130);panel(g,256,84,126,130);
   int cx=leftPos+181,cy=topPos+145;double yaw=Math.toRadians(previewYaw),pitch=Math.toRadians(previewPitch);
   for(int i=-40;i<=40;i+=8){int ox=(int)(Math.cos(yaw)*i),oy=(int)(Math.sin(pitch)*i*.35);g.fill(cx+ox,cy+oy,cx+ox+2,cy+oy+2,0xFFB7D4D9);}
   if(menu.data.get(6)>0){var selected=BuiltInRegistries.ITEM.byId(menu.data.get(9));if(selected!=null){g.pose().pushMatrix();g.pose().translate(cx-24,cy-24);g.pose().scale(3,3);g.item(new ItemStack(selected),0,0);g.pose().popMatrix();g.outline(cx-27,cy-27,54,54,0xFFFFD778);}}
  }

  panel(g,8,222,92,80);panel(g,106,222,172,80);
  for(var slot:menu.slots){g.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,0xFF102127);g.fill(leftPos+slot.x,topPos+slot.y,leftPos+slot.x+16,topPos+slot.y+16,0xFF91AEB4);}
 }

 protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){
  g.text(font,title(),page==Page.MAIN?8:64,10,0xFFF1FAFA);

  if(page==Page.MAIN){
   g.text(font,"Choose what you want to manage.",111,40,0xFFD7E7E8);
   g.text(font,"Residents",96,66,0xFFEAF4F4);g.text(font,"Decorations",239,66,0xFFEAF4F4);
   g.text(font,"Pick up and manage mobs",62,116,0xFFC9DCDD);g.text(font,"Place, move, scale and rotate",214,116,0xFFC9DCDD);
  }else if(page==Page.MOBS){
   int pages=Math.max(1,menu.data.get(24)+1);
   g.text(font,"Residents",14,70,0xFFEAF4F4);g.text(font,(menu.data.get(23)+1)+" / "+pages,60,70,0xFFC9DCDD);
   int id=menu.data.get(3);var type=id<0?null:BuiltInRegistries.ENTITY_TYPE.byId(id);String selected=type==null?"No resident selected":Component.translatable(type.getDescriptionId()).getString();
   g.text(font,"Selected",156,70,0xFFEAF4F4);g.text(font,font.plainSubstrByWidth(selected,118),156,88,0xFFF1FAFA);
   g.text(font,"Residents: "+menu.data.get(1)+" / "+menu.capacity(),156,108,0xFFC9DCDD);
   g.text(font,"Put the correct empty",156,132,0xFFC9DCDD);g.text(font,"container in Input.",156,144,0xFFC9DCDD);
   g.text(font,"Select a resident, then",156,164,0xFFC9DCDD);g.text(font,"use Pick up selected.",156,176,0xFFC9DCDD);
   g.text(font,"Actions",294,70,0xFFEAF4F4);
  }else{
   g.text(font,"Decor items",14,90,0xFFEAF4F4);
   if(menu.data.get(6)>DECOR_ROWS)g.text(font,"Page "+(decorPage+1)+" / "+(((menu.data.get(6)-1)/DECOR_ROWS)+1),14,202,0xFFC9DCDD);
   g.text(font,"Preview",116,90,0xFFEAF4F4);
   var item=BuiltInRegistries.ITEM.byId(menu.data.get(9));
   if(item!=null&&menu.data.get(6)>0){String name=new ItemStack(item).getHoverName().getString();g.text(font,font.plainSubstrByWidth(name,112),116,198,0xFFEAF4F4);g.text(font,Math.round(previewYaw)+"°",220,198,0xFFC9DCDD);}
   g.text(font,"Move",260,90,0xFFEAF4F4);g.text(font,"Scale",260,128,0xFFEAF4F4);g.text(font,"Rotate",260,166,0xFFEAF4F4);
  }

  g.text(font,page==Page.DECOR?"Decor transfer":"Container transfer",14,228,0xFFD7E7E8);
  g.text(font,"Input",14,244-12,0xFFC9DCDD);g.text(font,"Out",43,244-12,0xFFC9DCDD);
  g.text(font,"Inventory",112,228,0xFFD7E7E8);
 }
}
