package dev.casz.aquarium;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
public final class MobitatMenu extends AbstractContainerMenu {
 public final BlockPos pos;public final ContainerData data=new SimpleContainerData(6);public final SimpleContainer tag=new SimpleContainer(1);private final ServerLevel level;
 public MobitatMenu(int id,Inventory inv,BlockPos p){this(id,inv,null,p);}public MobitatMenu(int id,Inventory inv,ServerLevel l,BlockPos p){super(AquariumMod.MOBITAT_MENU,id);pos=p;level=l;addSlot(new Slot(tag,0,44,70){public boolean mayPlace(ItemStack s){return s.is(Items.NAME_TAG);}});for(int r=0;r<3;r++)for(int c=0;c<9;c++)addSlot(new Slot(inv,c+r*9+9,44+c*18,104+r*18));for(int c=0;c<9;c++)addSlot(new Slot(inv,c,44+c*18,162));addDataSlots(data);refresh();}
 private MobitatBlockEntity be(){return level!=null&&level.getBlockEntity(pos) instanceof MobitatBlockEntity b?b:null;}private void refresh(){var b=be();data.set(0,b==null?0:b.size());}
 public boolean stillValid(Player p){return level==null||level.getBlockState(pos).is(AquariumMod.MOBITAT)&&p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=64;}
 public boolean clickMenuButton(Player p,int a){var b=be();if(b==null||a<0||a>=b.size())return false;ItemStack t=tag.getItem(0);if(!b.rename(a,t))return false;if(!p.getAbilities().instabuild)t.shrink(1);refresh();return true;}
 public void broadcastChanges(){refresh();super.broadcastChanges();}public ItemStack quickMoveStack(Player p,int i){return ItemStack.EMPTY;}public void removed(Player p){super.removed(p);if(level!=null)clearContainer(p,tag);}
}