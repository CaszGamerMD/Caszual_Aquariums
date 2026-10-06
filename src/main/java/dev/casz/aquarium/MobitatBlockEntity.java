package dev.casz.aquarium;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
public final class MobitatBlockEntity extends BlockEntity {
 public static final int CAPACITY=5; private final List<CompoundTag> mobs=new ArrayList<>();
 public MobitatBlockEntity(BlockPos p,BlockState s){super(AquariumMod.MOBITAT_ENTITY,p,s);}
 public int size(){return mobs.size();} public boolean empty(){return mobs.isEmpty();}
 public String type(){return mobs.isEmpty()?"":mobs.getFirst().getString("type").orElse("");}
 public Component name(int i){if(i<0||i>=mobs.size())return Component.empty();return Component.literal(mobs.get(i).getString("name").orElse("Mob"));}
 public boolean addNet(ItemStack net){
  CompoundTag root=net.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();String t=root.getString("terrarium_type").orElse("");
  if(t.isEmpty()||mobs.size()>=CAPACITY||!mobs.isEmpty()&&!type().equals(t))return false;
  CompoundTag e=new CompoundTag();e.putString("type",t);e.put("entity",root.getCompound("terrarium_entity").orElse(new CompoundTag()).copy());e.putString("name",net.getHoverName().getString().replace(" in Mob Net",""));mobs.add(e);net.set(DataComponents.CUSTOM_DATA,CustomData.EMPTY);net.remove(DataComponents.CUSTOM_NAME);setChanged();return true;
 }
 public ItemStack peekNet(int i){\n  if(i<0||i>=mobs.size())return ItemStack.EMPTY;CompoundTag e=mobs.get(i),root=new CompoundTag();root.putString("terrarium_type",e.getString("type").orElse(""));root.put("terrarium_entity",e.getCompound("entity").orElse(new CompoundTag()).copy());ItemStack net=new ItemStack(AquariumMod.MOB_NET);net.set(DataComponents.CUSTOM_DATA,CustomData.of(root));net.set(DataComponents.CUSTOM_NAME,Component.literal(e.getString("name").orElse("Mob")+" in Mob Net"));return net;\n }\n public void remove(int i){if(i>=0&&i<mobs.size()){mobs.remove(i);setChanged();}}\n public ItemStack takeNet(int i){
  if(i<0||i>=mobs.size())return ItemStack.EMPTY;CompoundTag e=mobs.remove(i),root=new CompoundTag();root.putString("terrarium_type",e.getString("type").orElse(""));root.put("terrarium_entity",e.getCompound("entity").orElse(new CompoundTag()).copy());
  ItemStack net=new ItemStack(AquariumMod.MOB_NET);net.set(DataComponents.CUSTOM_DATA,CustomData.of(root));net.set(DataComponents.CUSTOM_NAME,Component.literal(e.getString("name").orElse("Mob")+" in Mob Net"));setChanged();return net;
 }
 public boolean rename(int i,ItemStack tag){if(i<0||i>=mobs.size()||!tag.is(Items.NAME_TAG)||!tag.has(DataComponents.CUSTOM_NAME))return false;String n=tag.getHoverName().getString();mobs.get(i).putString("name",n);var data=mobs.get(i).getCompound("entity").orElse(new CompoundTag());data.putString("CustomName",Component.Serializer.toJson(Component.literal(n),level.registryAccess()));setChanged();return true;}
 public ItemStack asItem(){ItemStack s=new ItemStack(AquariumMod.MOBITAT_ITEM);CompoundTag root=new CompoundTag();root.put("mobitat_mobs",saveList());s.set(DataComponents.CUSTOM_DATA,CustomData.of(root));if(!mobs.isEmpty())s.set(DataComponents.CUSTOM_NAME,Component.literal("Mobitat ("+mobs.size()+"/5 "+name(0).getString()+")"));return s;}
 public void fromItem(ItemStack s){mobs.clear();var root=s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();loadList(root.getList("mobitat_mobs").orElse(new ListTag()));setChanged();}
 private ListTag saveList(){ListTag l=new ListTag();for(var e:mobs)l.add(e.copy());return l;}
 private void loadList(ListTag l){for(int i=0;i<Math.min(CAPACITY,l.size());i++)l.getCompound(i).ifPresent(t->mobs.add(t.copy()));}
 protected void saveAdditional(ValueOutput out){super.saveAdditional(out);out.store("mobs",CompoundTag.CODEC,wrap());}
 protected void loadAdditional(ValueInput in){super.loadAdditional(in);mobs.clear();loadList(in.read("mobs",CompoundTag.CODEC).orElse(new CompoundTag()).getList("list").orElse(new ListTag()));}
 private CompoundTag wrap(){CompoundTag t=new CompoundTag();t.put("list",saveList());return t;}
}