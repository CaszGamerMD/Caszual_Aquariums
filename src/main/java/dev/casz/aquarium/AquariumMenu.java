package dev.casz.aquarium;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;

public final class AquariumMenu extends AbstractContainerMenu {
 public static final int GRID_SIZE=9, GRID_START=27;
 public final BlockPos anchor;
 private int gridX,gridY,gridZ;private BlockPos selectedPos;
 public final ContainerData data=new SimpleContainerData(108);
 public final SimpleContainer transfer=new SimpleContainer(2);
 private final ServerLevel level;
 private List<BlockPos> tanks=List.of();private List<Mob> residents=List.of();private int tankIndex,fishIndex;private long refreshed=-1;
 public AquariumMenu(int id,Inventory inventory,BlockPos pos){this(id,inventory,null,pos);}
 public AquariumMenu(int id,Inventory inventory,ServerLevel level,BlockPos pos){
  super(AquariumMod.MENU,id);this.level=level;this.anchor=pos.immutable();selectedPos=anchor;gridX=pos.getX()-4;gridZ=pos.getZ()-4;gridY=pos.getY();
  addSlot(new Slot(transfer,0,286,132));addSlot(new Slot(transfer,1,310,132){public boolean mayPlace(ItemStack s){return false;}});
  for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inventory,col+row*9+9,86+col*18,154+row*18));
  for(int col=0;col<9;col++)addSlot(new Slot(inventory,col,86+col*18,212));addDataSlots(data);refresh(true);
 }
 public int capacity(){return (data.get(12)&65535)|(data.get(13)<<16);}
 private void refresh(boolean force){
  if(level==null)return;if(!force&&refreshed==level.getGameTime())return;refreshed=level.getGameTime();
  var net=Network.scan(level,anchor);tanks=net.cells().stream().filter(p->Enclosures.isTank(level.getBlockState(p))).sorted(Comparator.<BlockPos>comparingInt(p -> p.getY()).thenComparingInt(BlockPos::getZ).thenComparingInt(BlockPos::getX)).toList();
  residents=net.residents(level).stream().sorted(Comparator.comparing(m->m.getUUID().toString())).toList();tankIndex=Math.max(0,tanks.indexOf(selectedPos));if(!tanks.isEmpty())selectedPos=tanks.get(tankIndex);fishIndex=Math.clamp(fishIndex,0,Math.max(0,residents.size()-1));
  data.set(19,Enclosures.kind(level.getBlockState(anchor)));data.set(1,tankIndex);data.set(2,tanks.size());data.set(11,residents.size());data.set(12,net.capacity()&65535);data.set(13,net.capacity()>>>16);data.set(14,fishIndex);data.set(15,residents.isEmpty()?-1:BuiltInRegistries.ENTITY_TYPE.getId(residents.get(fishIndex).getType()));data.set(16,net.complete()?1:0);
  int minY=tanks.stream().mapToInt(BlockPos::getY).min().orElse(anchor.getY()),maxY=tanks.stream().mapToInt(BlockPos::getY).max().orElse(anchor.getY());
  gridY=Math.clamp(gridY,minY,maxY);data.set(21,gridX-anchor.getX());data.set(22,gridZ-anchor.getZ());data.set(23,gridY-anchor.getY());data.set(24,minY-anchor.getY());data.set(25,maxY-anchor.getY());
  for(int cell=0;cell<81;cell++){
   BlockPos p=new BlockPos(gridX+cell%9,gridY,gridZ+cell/9);int bits=0;
   if(net.cells().contains(p)){
    var state=level.getBlockState(p);boolean tank=Enclosures.isTank(state);bits=tank?1:2;
    if(tank){if(state.getValue(AquariumBlock.SOIL)>0)bits|=4;if(level.getBlockEntity(p) instanceof TankBlockEntity t&&!t.decoration.isEmpty())bits|=8;if(Enclosures.isLand(state)&&state.getValue(TerrariumBlock.GROUND_WATER))bits|=16;}
    else for(Direction d:Direction.values())if(state.getValue(AquariumBlock.LINKS[d.ordinal()]))bits|=1<<(8+d.ordinal());
    if(p.equals(selectedPos))bits|=32;if(p.equals(anchor))bits|=64;
   }
   data.set(GRID_START+cell,bits);
  }
  var be=selected();if(be!=null){be.migrateLegacy();BlockPos p=be.getBlockPos();data.set(3,p.getX()-anchor.getX());data.set(4,p.getY()-anchor.getY());data.set(5,p.getZ()-anchor.getZ());data.set(6,be.kind());data.set(7,be.variant);data.set(8,be.rotation);data.set(9,be.offsetX);data.set(10,be.offsetZ);data.set(18,BuiltInRegistries.ITEM.getId(be.decoration.getItem()));data.set(17,be.getBlockState().getValue(AquariumBlock.SOIL));data.set(20,Enclosures.isLand(be.getBlockState())&&be.getBlockState().getValue(TerrariumBlock.GROUND_WATER)?1:0);}else for(int i=3;i<=10;i++)data.set(i,0);
 }
 private TankBlockEntity selected(){return !tanks.isEmpty()&&level.getBlockEntity(tanks.get(tankIndex)) instanceof TankBlockEntity be?be:null;}
 public boolean stillValid(Player player){return level==null||Enclosures.isTank(level.getBlockState(anchor))&&player.distanceToSqr(anchor.getX()+.5,anchor.getY()+.5,anchor.getZ()+.5)<=64;}
 public void broadcastChanges(){refresh(false);super.broadcastChanges();}
 private boolean outputFree(){return transfer.getItem(1).isEmpty();}
 private void output(ItemStack stack){if(!stack.isEmpty())transfer.setItem(1,stack);}
 public boolean clickMenuButton(Player player,int action){
  if(level==null||!stillValid(player))return false;refresh(true);var be=selected();
  if(action>=32&&action<113){int cell=action-32;BlockPos p=new BlockPos(gridX+cell%9,gridY,gridZ+cell/9);if(data.get(0)!=0||!tanks.contains(p))return false;selectedPos=p;refresh(true);broadcastChanges();return true;}
  if(action>=12&&action<=18){
   if(data.get(0)!=0)return false;
   switch(action){case 12->gridY=tanks.stream().mapToInt(BlockPos::getY).filter(y->y<gridY).max().orElse(gridY);case 13->gridY=tanks.stream().mapToInt(BlockPos::getY).filter(y->y>gridY).min().orElse(gridY);case 14->gridX-=4;case 15->gridX+=4;case 16->gridZ-=4;case 17->gridZ+=4;case 18->{gridX=anchor.getX()-4;gridZ=anchor.getZ()-4;gridY=anchor.getY();selectedPos=anchor;}}
   if(action==12||action==13){selectedPos=tanks.stream().filter(p->p.getY()==gridY).min(Comparator.comparingDouble(p->p.distSqr(selectedPos))).orElse(selectedPos);if(selectedPos.getX()<gridX||selectedPos.getX()>=gridX+9||selectedPos.getZ()<gridZ||selectedPos.getZ()>=gridZ+9){gridX=selectedPos.getX()-4;gridZ=selectedPos.getZ()-4;}}
   gridX=Math.clamp(gridX,tanks.stream().mapToInt(BlockPos::getX).min().orElse(gridX)-4,tanks.stream().mapToInt(BlockPos::getX).max().orElse(gridX));
   gridZ=Math.clamp(gridZ,tanks.stream().mapToInt(BlockPos::getZ).min().orElse(gridZ)-4,tanks.stream().mapToInt(BlockPos::getZ).max().orElse(gridZ));
   refresh(true);broadcastChanges();return true;
  }
  if(action==0){data.set(0,1-data.get(0));return true;}
  if(action==1||action==2){int delta=action==1?-1:1;if(data.get(0)==0){tankIndex=Math.floorMod(tankIndex+delta,Math.max(1,tanks.size()));if(!tanks.isEmpty())selectedPos=tanks.get(tankIndex);}else fishIndex=Math.floorMod(fishIndex+delta,Math.max(1,residents.size()));refresh(true);return true;}
  if(!outputFree()&&(action==9||action==10))return false;
  if(data.get(0)==1){
   var input=transfer.getItem(0);
   if(action==9&&be!=null&&data.get(19)>0){String error=Terrestrial.add(level,be.getBlockPos(),input);if(error!=null){player.sendOverlayMessage(net.minecraft.network.chat.Component.literal(error));return false;}transfer.setChanged();}
   else if(action==10&&data.get(19)>0&&!residents.isEmpty()&&input.is(AquariumMod.MOB_NET)&&!Terrestrial.filled(input)){var stack=Terrestrial.capture(level,residents.get(fishIndex));input.shrink(1);output(stack);transfer.setChanged();}
   else if(action==9&&be!=null){var result=Inhabitants.add(level,be.getBlockPos(),input,player);if(!result.success())return false;output(result.returned());transfer.setChanged();}
   else if(action==10&&data.get(19)==0&&!residents.isEmpty()&&(input.is(Items.BUCKET)||input.is(Items.WATER_BUCKET))){var stack=Inhabitants.capture(level,residents.get(fishIndex));if(!player.getAbilities().instabuild)input.shrink(1);output(stack);transfer.setChanged();}
   else return false;
  }else{
   if(be==null)return false;
   if(action>=3&&action<=8&&!be.decoration.isEmpty())switch(action){case 3->be.rotation=(be.rotation+1)%8;case 4->be.variant=(be.variant+1)%4;case 5->be.offsetX=Math.max(-3,be.offsetX-1);case 6->be.offsetX=Math.min(3,be.offsetX+1);case 7->be.offsetZ=Math.max(-3,be.offsetZ-1);case 8->be.offsetZ=Math.min(3,be.offsetZ+1);}
   else if(action==11){be.rotation=be.variant=be.offsetX=be.offsetZ=0;}
   else if(action==9){
    var input=transfer.getItem(0);int soil=Palette.find(Palette.SOILS,input.getItem());
    if(data.get(19)>0&&(input.is(Items.WATER_BUCKET)||input.is(Items.BUCKET))){var state=be.getBlockState();if(!Terrestrial.groundEntry(level,be.getBlockPos()))return false;boolean add=input.is(Items.WATER_BUCKET);if(state.getValue(TerrariumBlock.GROUND_WATER)==add)return false;level.setBlock(be.getBlockPos(),state.setValue(TerrariumBlock.GROUND_WATER,add),3);if(!player.getAbilities().instabuild){input.shrink(1);output(new ItemStack(add?Items.BUCKET:Items.WATER_BUCKET));}}
    else if(soil>0){var state=be.getBlockState();if(state.getValue(AquariumBlock.DOWN))return false;int old=state.getValue(AquariumBlock.SOIL);if(old==soil)return false;level.setBlock(be.getBlockPos(),state.setValue(AquariumBlock.SOIL,soil),3);if(old>0)output(new ItemStack(Palette.item(Palette.SOILS.get(old))));if(!player.getAbilities().instabuild)input.shrink(1);}
    else if(AquariumMod.isDecoration(input)){if(ItemStack.isSameItemSameComponents(input,be.decoration))return false;ItemStack old=be.decoration.copy();be.setDecoration(input);if(!player.getAbilities().instabuild)input.shrink(1);output(old);AquariumMod.tridentChance(level,be);}
    else return false;transfer.setChanged();
   }else if(action==10){if(!be.decoration.isEmpty()){output(be.decoration.copy());be.decoration=ItemStack.EMPTY;}else{int soil=be.getBlockState().getValue(AquariumBlock.SOIL);if(soil==0)return false;output(new ItemStack(Palette.item(Palette.SOILS.get(soil))));level.setBlock(be.getBlockPos(),be.getBlockState().setValue(AquariumBlock.SOIL,0),3);}}
   else return false;be.changed();be.tick();
  }
  refresh(true);broadcastChanges();return true;
 }
 public ItemStack quickMoveStack(Player player,int index){var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;ItemStack item=slot.getItem(),original=item.copy();if(index<2){if(!moveItemStackTo(item,2,38,true))return ItemStack.EMPTY;}else if(!moveItemStackTo(item,0,1,false))return ItemStack.EMPTY;if(item.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();return original;}
 public void removed(Player player){super.removed(player);if(level!=null)clearContainer(player,transfer);}
}
