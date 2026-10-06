package dev.casz.aquarium;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;

public final class TropicalFishEditorMenu extends AbstractContainerMenu {
 public final BlockPos pos;
 public final ContainerData data=new SimpleContainerData(4);
 private final ServerLevel level;
 public TropicalFishEditorMenu(int id,Inventory inv,BlockPos pos){this(id,inv,null,pos);}
 public TropicalFishEditorMenu(int id,Inventory inv,ServerLevel level,BlockPos pos){super(AquariumMod.FISH_EDITOR_MENU,id);this.level=level;this.pos=pos.immutable();addDataSlots(data);refresh();}
 private TropicalFishEditorBlockEntity be(){return level!=null&&level.getBlockEntity(pos) instanceof TropicalFishEditorBlockEntity be?be:null;}
 private void refresh(){var be=be();data.set(0,be!=null&&be.occupied()?1:0);data.set(1,be==null||!be.occupied()?0:be.pattern().ordinal());data.set(2,be==null||!be.occupied()?0:be.baseColor().getId());data.set(3,be==null||!be.occupied()?0:be.patternColor().getId());}
 public boolean stillValid(Player player){return level==null||level.getBlockState(pos).is(AquariumMod.FISH_EDITOR)&&player.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=64;}
 public void broadcastChanges(){refresh();super.broadcastChanges();}
 public boolean clickMenuButton(Player player,int action){
  var be=be();if(be==null||!be.occupied())return false;boolean changed=false;
  if(action>=0&&action<TropicalFish.Pattern.values().length)changed=be.setPattern(action);
  else if(action>=100&&action<116)changed=be.setBaseColor(action-100);
  else if(action>=120&&action<136)changed=be.setPatternColor(action-120);
  else if(action==200){be.randomize();changed=true;}
  else if(action==201){be.swapColors();changed=true;}
  if(changed)refresh();return changed;
 }
 public ItemStack quickMoveStack(Player player,int index){return ItemStack.EMPTY;}
}
