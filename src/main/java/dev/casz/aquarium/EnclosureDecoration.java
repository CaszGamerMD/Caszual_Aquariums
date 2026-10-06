package dev.casz.aquarium;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
public final class EnclosureDecoration {
 public enum Anchor {CEILING,BODY,FLOOR}
 public ItemStack stack=ItemStack.EMPTY;public Anchor anchor=Anchor.BODY;
 public float x=.5f,y=.5f,z=.5f,scale=1,rotX,rotY,rotZ;
 public EnclosureDecoration(){}
 public EnclosureDecoration(ItemStack s,Anchor a){stack=s.copyWithCount(1);anchor=a;y=a==Anchor.CEILING?1:a==Anchor.FLOOR?0:.5f;}
 public void save(ValueOutput out,String key){out.store(key+"_item",ItemStack.CODEC,stack);out.putInt(key+"_anchor",anchor.ordinal());out.putFloat(key+"_x",x);out.putFloat(key+"_y",y);out.putFloat(key+"_z",z);out.putFloat(key+"_scale",scale);out.putFloat(key+"_rx",rotX);out.putFloat(key+"_ry",rotY);out.putFloat(key+"_rz",rotZ);}
 public static EnclosureDecoration load(ValueInput in,String key){var d=new EnclosureDecoration();d.stack=in.read(key+"_item",ItemStack.CODEC).orElse(ItemStack.EMPTY);d.anchor=Anchor.values()[Math.clamp(in.getIntOr(key+"_anchor",1),0,2)];d.x=in.getFloatOr(key+"_x",.5f);d.y=in.getFloatOr(key+"_y",.5f);d.z=in.getFloatOr(key+"_z",.5f);d.scale=in.getFloatOr(key+"_scale",1);d.rotX=in.getFloatOr(key+"_rx",0);d.rotY=in.getFloatOr(key+"_ry",0);d.rotZ=in.getFloatOr(key+"_rz",0);return d;}
}