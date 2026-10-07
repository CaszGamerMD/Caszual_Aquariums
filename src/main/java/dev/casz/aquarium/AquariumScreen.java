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
 private Page page=Page.MAIN;
 private final List<Button> pageButtons=new ArrayList<>();
 private int uiMobCount=-1,uiDecorCount=-1,uiSelectedMob=-1,uiSelectedDecor=-1,uiAnchor=-1,uiMobPage=-1,uiDecorPage=-1;
 private float previewYaw=25,previewPitch=20;
 private boolean dragging;
 private double dragX,dragY;

 public AquariumScreen(AquariumMenu m,Inventory i,Component t){super(m,i,t,380,300);}
 private void send(int a){if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,a);}
 private Button b(String t,int x,int y,int w,int h,Runnable r){var q=addRenderableWidget(Button.builder(Component.literal(t),z->r.run()).bounds(leftPos+x,topPos+y,w,h).build());pageButtons.add(q);return q;}
 protected void init(){super.init();rebuild();}

 private void clearPageButtons(){for(var button:pageButtons)removeWidget(button);pageButtons.clear();}
 private String itemName(int raw,int width){
  var item=raw<0?null:BuiltInRegistries.ITEM.byId(raw);
  return item==null?"Empty":font.plainSubstrByWidth(new ItemStack(item).getHoverName().getString(),width);
 }
 private void addPrimaryTabs(){
  b((page==Page.MOBS?"▶ ":"")+"Mobs",96,32,88,20,()->{page=Page.MOBS;rebuild();});
  b((page==Page.DECOR?"▶ ":"")+"Decorate",190,32,94,20,()->{page=Page.DECOR;rebuild();});
 }
 private void rebuild(){
  clearPageButtons();
  if(page==Page.MAIN){
   b("Mobs",84,88,96,34,()->{page=Page.MOBS;rebuild();});
   b("Decorate",200,88,96,34,()->{page=Page.DECOR;rebuild();});
   snapshot();return;
  }

  b("Back",8,6,48,20,()->{page=Page.MAIN;rebuild();});
  addPrimaryTabs();

  if(page==Page.MOBS){
   int start=menu.data.get(23)*5;
   int visible=Math.min(5,Math.max(0,menu.data.get(1)-start));
   for(int row=0;row<visible;row++){
    final int local=row,index=start+row;
    int id=menu.data.get(18+row);
    var type=id<0?null:BuiltInRegistries.ENTITY_TYPE.byId(id);
    String name=type==null?"Mob "+(index+1):Component.translatable(type.getDescriptionId()).getString();
    b((index==menu.data.get(2)?"▶ ":"")+font.plainSubstrByWidth(name,104),12,82+row*20,120,18,()->send(100+local));
   }
   b("<",12,182,28,18,()->send(23));
   b(">",104,182,28,18,()->send(24));
   b("Pick Up",288,98,76,22,()->send(20));
  }else{
   b((menu.data.get(8)==0?"▶ ":"")+"Ceiling",94,60,50,18,()->send(200));
   b((menu.data.get(8)==1?"▶ ":"")+"Body",148,60,48,18,()->send(201));
   b((menu.data.get(8)==2?"▶ ":"")+"Floor",200,60,48,18,()->send(202));

   int start=menu.data.get(25)*5;
   for(int row=0;row<5;row++){
    int id=menu.data.get(27+row);if(id<0)continue;
    final int index=start+row;
    b((index==menu.data.get(7)?"▶ ":"")+itemName(id,58),12,104+row*18,72,16,()->send(300+index));
   }
   b("<",12,86,22,16,()->send(25));
   b(">",62,86,22,16,()->send(26));
   b("Add",12,194,34,16,()->send(21));
   b("Remove",48,194,36,16,()->send(22));

   b("Reset",330,86,36,16,()->send(44));
   String[] move={"X-","X+","Y-","Y+","Z-","Z+"};
   for(int i=0;i<6;i++){final int action=30+i;int col=i%2,row=i/2;b(move[i],258+col*54,104+row*20,48,18,()->send(action));}
   b("Scale -",258,166,48,18,()->send(36));b("Scale +",312,166,48,18,()->send(37));
   String[] rot={"X-","X+","Y-","Y+","Z-","Z+"};
   for(int i=0;i<6;i++){final int action=38+i;b(rot[i],258+i*18,194,17,16,()->send(action));}
  }
  snapshot();
 }

 private void snapshot(){
  uiMobCount=menu.data.get(1);uiDecorCount=menu.data.get(6);uiSelectedMob=menu.data.get(2);uiMobPage=menu.data.get(23);
  uiSelectedDecor=menu.data.get(7);uiAnchor=menu.data.get(8);uiDecorPage=menu.data.get(25);
 }
 protected void containerTick(){
  super.containerTick();
  if(page==Page.MOBS&&(uiMobCount!=menu.data.get(1)||uiSelectedMob!=menu.data.get(2)||uiMobPage!=menu.data.get(23)))rebuild();
  else if(page==Page.DECOR&&(uiDecorCount!=menu.data.get(6)||uiSelectedDecor!=menu.data.get(7)||uiAnchor!=menu.data.get(8)||uiDecorPage!=menu.data.get(25)))rebuild();
 }

 private boolean inPreview(double x,double y){return x>=leftPos+94&&x<leftPos+248&&y>=topPos+84&&y<topPos+210;}
 public boolean mouseClicked(MouseButtonEvent event,boolean doubleClick){
  if(page==Page.DECOR&&event.button()==0&&inPreview(event.x(),event.y())){dragging=true;dragX=event.x();dragY=event.y();return true;}
  return super.mouseClicked(event,doubleClick);
 }
 public boolean mouseDragged(MouseButtonEvent event,double dx,double dy){
  if(dragging&&page==Page.DECOR){previewYaw+=(float)(event.x()-dragX);previewPitch=Math.clamp(previewPitch+(float)(event.y()-dragY),-80,80);dragX=event.x();dragY=event.y();return true;}
  return super.mouseDragged(event,dx,dy);
 }
 public boolean mouseReleased(MouseButtonEvent event){dragging=false;return super.mouseReleased(event);}

 private void panel(GuiGraphicsExtractor g,int x,int y,int w,int h){
  g.fill(leftPos+x,topPos+y,leftPos+x+w,topPos+y+h,0xB9102127);
  g.outline(leftPos+x,topPos+y,w,h,0xFF6F929A);
 }
 public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float a){
  super.extractBackground(g,mx,my,a);
  int kind=menu.data.get(0),bg=kind==0?0xFF163846:kind==1?0xFF29452C:0xFF40283F;
  g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,bg);

  if(page!=Page.MAIN){
   if(page==Page.MOBS){panel(g,8,58,130,144);panel(g,144,58,130,144);panel(g,280,58,92,144);}
   else{panel(g,8,82,80,130);panel(g,94,82,154,130);panel(g,254,82,118,130);}
   g.fill(leftPos+4,topPos+208,leftPos+376,topPos+298,0x70102127);
   g.outline(leftPos+4,topPos+208,372,90,0xFF58747A);
  }

  if(page==Page.DECOR){
   int cx=leftPos+171,cy=topPos+143;
   double yaw=Math.toRadians(previewYaw),pitch=Math.toRadians(previewPitch);
   for(int i=-42;i<=42;i+=7){int ox=(int)(Math.cos(yaw)*i),oy=(int)(Math.sin(pitch)*i*.35);g.fill(cx+ox,cy+oy,cx+ox+2,cy+oy+2,0xFFB7D4D9);}
   if(menu.data.get(6)>0){
    var selected=BuiltInRegistries.ITEM.byId(menu.data.get(9));
    if(selected!=null){g.pose().pushMatrix();g.pose().translate(cx-24,cy-24);g.pose().scale(3,3);g.item(new ItemStack(selected),0,0);g.pose().popMatrix();g.outline(cx-27,cy-27,54,54,0xFFFFD778);}
   }
  }

  for(var slot:menu.slots){
   g.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,0xFF102127);
   g.fill(leftPos+slot.x,topPos+slot.y,leftPos+slot.x+16,topPos+slot.y+16,0xFF91AEB4);
  }
 }

 protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){
  String title=menu.data.get(0)==0?"Aquarium Editor":menu.data.get(0)==1?"Terrarium Editor":"Hostile Terrarium Editor";
  g.text(font,title,page==Page.MAIN?8:64,10,0xFFF1FAFA);

  if(page==Page.MAIN){
   g.text(font,"Choose what you want to manage.",112,58,0xFFD7E7E8);
   g.text(font,"Mobs",118,130,0xFFC9DCDD);g.text(font,"Decorations",224,130,0xFFC9DCDD);
   return;
  }

  g.text(font,"Input",20,218,0xFFC9DCDD);g.text(font,"Output",48,218,0xFFC9DCDD);g.text(font,"Inventory",109,202,0xFFEAF4F4);

  if(page==Page.MOBS){
   g.text(font,"Residents",14,64,0xFFEAF4F4);
   g.text(font,"Page "+(menu.data.get(23)+1)+" / "+(menu.data.get(24)+1),47,186,0xFFC9DCDD);
   g.text(font,menu.data.get(1)+" / "+menu.capacity()+" occupied",150,64,0xFFC9DCDD);

   int id=menu.data.get(3);var type=id<0?null:BuiltInRegistries.ENTITY_TYPE.byId(id);
   String selected=type==null?"No inhabitants":Component.translatable(type.getDescriptionId()).getString();
   g.text(font,font.plainSubstrByWidth(selected,118),150,82,0xFFFFFFFF);
   g.text(font,"Selected resident",150,98,0xFF9FC0C6);
   g.text(font,"Place the correct empty",150,124,0xFFC9DCDD);
   g.text(font,"container in Input, then",150,136,0xFFC9DCDD);
   g.text(font,"use Pick Up.",150,148,0xFFC9DCDD);

   g.text(font,"Actions",286,64,0xFFEAF4F4);
   g.text(font,"Moves the selected",286,128,0xFFC9DCDD);
   g.text(font,"resident to Output.",286,140,0xFFC9DCDD);
   return;
  }

  g.text(font,"Decor "+(menu.data.get(6)==0?"0 / 0":(menu.data.get(7)+1)+" / "+menu.data.get(6)),34,88,0xFFEAF4F4);
  g.text(font,"Preview",100,88,0xFFEAF4F4);
  g.text(font,"Transform",260,88,0xFFEAF4F4);
  g.text(font,"Position",258,96,0xFF9FC0C6);
  g.text(font,"Scale "+menu.data.get(13)+"%",258,156,0xFF9FC0C6);
  g.text(font,"Rotation",258,184,0xFF9FC0C6);

  if(menu.data.get(6)>0){
   var item=BuiltInRegistries.ITEM.byId(menu.data.get(9));
   if(item!=null)g.text(font,font.plainSubstrByWidth(new ItemStack(item).getHoverName().getString(),142),100,184,0xFFEAF4F4);
  }else g.text(font,"Add an item from Input",100,184,0xFFC9DCDD);
  g.text(font,"Drag preview to rotate · "+Math.round(previewYaw)+"°",100,198,0xFF9FC0C6);
 }
}
