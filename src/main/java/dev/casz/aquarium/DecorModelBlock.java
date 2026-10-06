package dev.casz.aquarium;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
/** Render-only block states used by ordinary Minecraft block-display entities. */
public final class DecorModelBlock extends Block {
 public static final IntegerProperty KIND=IntegerProperty.create("kind",0,34),MODEL=IntegerProperty.create("model",0,3);
 public DecorModelBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,0).setValue(MODEL,0));}
 protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,MODEL);}
}