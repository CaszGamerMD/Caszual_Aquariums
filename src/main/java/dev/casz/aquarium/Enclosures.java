package dev.casz.aquarium;
import net.minecraft.world.level.block.state.BlockState;
public final class Enclosures {
 public static int kind(BlockState s){if(s.getBlock() instanceof TerrariumBlock t)return t.hostile?2:1;if(s.getBlock() instanceof TerrariumPipeBlock t)return t.hostile?2:1;return AquariumBlock.isModule(s)?0:-1;}
 public static boolean matches(BlockState a,BlockState b){return kind(a)>=0&&kind(a)==kind(b);}
 public static boolean isTank(BlockState s){return s.getBlock() instanceof TankBlock;}
 public static boolean isLand(BlockState s){return kind(s)>0;}
 public static boolean isAquatic(BlockState s){return kind(s)==0;}
}