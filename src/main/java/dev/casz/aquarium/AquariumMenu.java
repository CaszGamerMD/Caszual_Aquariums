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
 public final BlockPos anchor;public final ContainerData data=new SimpleContainerData(32);public final SimpleContainer transfer=new SimpleContainer(2);
 private final ServerLevel level;private List<BlockPos> tanks=List.of();private List<Mob> residents=List.of();private int selectedMob,selectedDecor,anchorMode=1;private long refreshed=-1;
 public AquariumMenu(int id,Inventory inv,BlockPos p){this(id,inv,null,p);}
 public AquariumMenu(int id,Inventory inv,ServerLevel l,BlockPos p){super(AquariumMod.MENU,id);level=l;anchor=p.immutable();addSlot(new Slot(transfer,0,16,198));addSlot(new Slot(transfer,1,38,198){public boolean mayPlace(ItemStack s){return false;}});for(int r=0;r<3;r++)for(int c=0;c<9;c++)addSlot(new Slot(inv,c+r*9+9,82+c*18,176+r*18));for(int c=0;c<9;c++)addSlot(new Slot(inv,c,82+c*18,234));addDataSlots(data);refresh(true);}
 public int capacity(){return (data.get(4)&65535)|(data.get(5)<<16);}
 private TankBlockEntity owner(){
  TankBlockEntity owner=null;for(var p:tanks)if(level.getBlockEntity(p) instanceof TankBlockEntity be){if(owner==null)owner=be;if(!be.decorations.isEmpty()){owner=be;break;}}
  if(owner==null)return null;
  for(var p:tanks)if(level.getBlockEntity(p) instanceof TankBlockEntity other&&other!=owner&&!other.decorations.isEmpty()){
   BlockPos from=other.getBlockPos(),to=owner.getBlockPos();for(var d:new ArrayList<>(other.decorations)){d.x+=from.getX()-to.getX();d.y+=from.getY()-to.getY();d.z+=from.getZ()-to.getZ();owner.decorations.add(d);}other.decorations.clear();other.changed();owner.changed();
  }
  return owner;
 }
 private void refresh(boolean force){if(level==null)return;if(!force&&refreshed==level.getGameTime())return;refreshed=level.getGameTime();var net=Network.scan(level,anchor);tanks=net.cells().stream().filter(p->Enclosures.isTank(level.getBlockState(p))).sorted(Comparator.comparingLong(BlockPos::asLong)).toList();residents=net.residents(level).stream().sorted(Comparator.comparing(m->m.getUUID().toString())).toList();var be=owner();if(be!=null)be.migrateLegacy();selectedMob=Math.clamp(selectedMob,0,Math.max(0,residents.size()-1));selectedDecor=Math.clamp(selectedDecor,0,Math.max(0,be==null?0:be.decorations.size()-1));data.set(0,Enclosures.kind(level.getBlockState(anchor)));data.set(1,residents.size());data.set(2,selectedMob);data.set(3,residents.isEmpty()?-1:BuiltInRegistries.ENTITY_TYPE.getId(residents.get(selectedMob).getType()));data.set(4,net.capacity()&65535);data.set(5,net.capacity()>>>16);data.set(6,be==null?0:be.decorations.size());data.set(7,selectedDecor);data.set(8,anchorMode);if(be!=null&&!be.decorations.isEmpty()){var d=be.decorations.get(selectedDecor);data.set(9,BuiltInRegistries.ITEM.getId(d.stack.getItem()));data.set(10,Math.round(d.x*100));data.set(11,Math.round(d.y*100));data.set(12,Math.round(d.z*100));data.set(13,Math.round(d.scale*100));data.set(14,Math.round(d.rotX));data.set(15,Math.round(d.rotY));data.set(16,Math.round(d.rotZ));data.set(17,d.anchor.ordinal());}}
 public boolean stillValid(Player p){return level==null||Enclosures.isTank(level.getBlockState(anchor))&&p.distanceToSqr(anchor.getX()+.5,anchor.getY()+.5,anchor.getZ()+.5)<=64;}
 public void broadcastChanges(){refresh(false);super.broadcastChanges();}
 private void giveOut(ItemStack s){if(!s.isEmpty())transfer.setItem(1,s);}
 public boolean clickMenuButton(Player player,int a){if(level==null||!stillValid(player))return false;refresh(true);var be=owner();
  if(a>=100&&a<105){selectedMob=a-100;if(selectedMob>=residents.size())return false;refresh(true);return true;}
  if(a>=200&&a<203){anchorMode=a-200;data.set(8,anchorMode);return true;}
  if(a>=300&&a<10000){selectedDecor=a-300;if(be==null||selectedDecor>=be.decorations.size())return false;refresh(true);return true;}
  if(a==20){if(residents.isEmpty())return false;var input=transfer.getItem(0);var mob=residents.get(selectedMob);ItemStack out=ItemStack.EMPTY;if(Enclosures.isLand(level.getBlockState(anchor))&&input.is(AquariumMod.MOB_NET)&&!Terrestrial.filled(input)){out=Terrestrial.capture(level,mob);if(!player.getAbilities().instabuild)input.shrink(1);}else if(Enclosures.isAquatic(level.getBlockState(anchor))&&(input.is(Items.BUCKET)||input.is(Items.WATER_BUCKET))){out=Inhabitants.capture(level,mob);if(!player.getAbilities().instabuild)input.shrink(1);}else return false;giveOut(out);refresh(true);return true;}
  if(a==21){if(be==null||transfer.getItem(0).isEmpty())return false;var in=transfer.getItem(0);be.addDecoration(in,EnclosureDecoration.Anchor.values()[anchorMode]);if(!player.getAbilities().instabuild)in.shrink(1);selectedDecor=be.decorations.size()-1;refresh(true);return true;}
  if(a==22){if(be==null||be.decorations.isEmpty()||!transfer.getItem(1).isEmpty())return false;giveOut(be.removeDecoration(selectedDecor));refresh(true);return true;}
  if(be==null||be.decorations.isEmpty())return false;var d=be.decorations.get(selectedDecor);
  float move=.1f,rot=15f;if(a==30)d.x-=move;if(a==31)d.x+=move;if(a==32)d.y-=move;if(a==33)d.y+=move;if(a==34)d.z-=move;if(a==35)d.z+=move;if(a==36)d.scale=Math.max(.1f,d.scale-.1f);if(a==37)d.scale=Math.min(4,d.scale+.1f);if(a==38)d.rotX-=rot;if(a==39)d.rotX+=rot;if(a==40)d.rotY-=rot;if(a==41)d.rotY+=rot;if(a==42)d.rotZ-=rot;if(a==43)d.rotZ+=rot;if(a==44){d.x=.5f;d.z=.5f;d.y=d.anchor==EnclosureDecoration.Anchor.CEILING?1:d.anchor==EnclosureDecoration.Anchor.FLOOR?0:.5f;d.scale=1;d.rotX=d.rotY=d.rotZ=0;}
  int minX=tanks.stream().mapToInt(BlockPos::getX).min().orElse(anchor.getX()),maxX=tanks.stream().mapToInt(BlockPos::getX).max().orElse(anchor.getX());int minY=tanks.stream().mapToInt(BlockPos::getY).min().orElse(anchor.getY()),maxY=tanks.stream().mapToInt(BlockPos::getY).max().orElse(anchor.getY());int minZ=tanks.stream().mapToInt(BlockPos::getZ).min().orElse(anchor.getZ()),maxZ=tanks.stream().mapToInt(BlockPos::getZ).max().orElse(anchor.getZ());BlockPos o=be.getBlockPos();d.x=Math.clamp(d.x,minX-o.getX()-1f,maxX-o.getX()+2f);d.y=Math.clamp(d.y,minY-o.getY()-1f,maxY-o.getY()+2f);d.z=Math.clamp(d.z,minZ-o.getZ()-1f,maxZ-o.getZ()+2f);be.changed();refresh(true);return true;
 }
 public ItemStack quickMoveStack(Player p,int i){return ItemStack.EMPTY;}public void removed(Player p){super.removed(p);if(level!=null)clearContainer(p,transfer);}
}