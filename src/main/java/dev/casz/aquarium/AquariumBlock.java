package dev.casz.aquarium;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.*;

public class AquariumBlock extends Block {
    public static final MapCodec<AquariumBlock> CODEC = simpleCodec(AquariumBlock::new);
    public static final BooleanProperty[] LINKS = {
        BooleanProperty.create("down"), BooleanProperty.create("up"),
        BooleanProperty.create("north"), BooleanProperty.create("south"),
        BooleanProperty.create("west"), BooleanProperty.create("east")};
    public static final BooleanProperty DOWN=LINKS[0];
    public static final IntegerProperty SOIL = IntegerProperty.create("soil", 0, 19);
    public static final IntegerProperty DECOR = IntegerProperty.create("decor", 0, 32);
    public AquariumBlock(Properties properties) {
        super(properties);
        BlockState state=stateDefinition.any();
        if (!(this instanceof TubeBlock)) {state=state.setValue(SOIL,0);if(this instanceof TerrariumBlock)state=state.setValue(TerrariumBlock.GROUND_WATER,false);else state=state.setValue(DECOR,0);if(this instanceof TankBlock)for(BooleanProperty p:TankBlock.TUBE_LINKS)state=state.setValue(p,false);}
        else for(BooleanProperty p:TubeBlock.TANK_LINKS)state=state.setValue(p,false);
        for(BooleanProperty p:LINKS) state=state.setValue(p,false);
        registerDefaultState(state);
    }
    protected MapCodec<? extends Block> codec() { return CODEC; }
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b) {
        b.add(LINKS);
        if (!(this instanceof TubeBlock)) {b.add(SOIL);if(this instanceof TerrariumBlock)b.add(TerrariumBlock.GROUND_WATER);else b.add(DECOR);if(this instanceof TankBlock)b.add(TankBlock.TUBE_LINKS);}else b.add(TubeBlock.TANK_LINKS);
    }
    public static boolean isModule(BlockState s) { return s.getBlock() instanceof AquariumBlock; }
    public boolean isTube() { return this instanceof TubeBlock; }
    public BlockState getStateForPlacement(BlockPlaceContext c) {
        BlockState s=defaultBlockState();
        for(Direction d:Direction.values()) s=s.setValue(LINKS[d.ordinal()],Enclosures.matches(defaultBlockState(),c.getLevel().getBlockState(c.getClickedPos().relative(d))));
        if(this instanceof TubeBlock)for(Direction d:Direction.values())s=s.setValue(TubeBlock.TANK_LINKS[d.ordinal()],Enclosures.isTank(c.getLevel().getBlockState(c.getClickedPos().relative(d))) && Enclosures.matches(defaultBlockState(),c.getLevel().getBlockState(c.getClickedPos().relative(d))));\n        if(this instanceof TankBlock)for(Direction d:Direction.values())s=s.setValue(TankBlock.TUBE_LINKS[d.ordinal()],c.getLevel().getBlockState(c.getClickedPos().relative(d)).getBlock() instanceof TubeBlock);
        return s;
    }
    protected BlockState updateShape(BlockState s, LevelReader level, ScheduledTickAccess ticks,
       BlockPos pos, Direction d, BlockPos neighbor, BlockState neighborState, RandomSource random) {
        s=s.setValue(LINKS[d.ordinal()],Enclosures.matches(s,neighborState));
        if(this instanceof TubeBlock)return s.setValue(TubeBlock.TANK_LINKS[d.ordinal()],Enclosures.isTank(neighborState) && Enclosures.matches(s,neighborState));\n        if(this instanceof TankBlock)return s.setValue(TankBlock.TUBE_LINKS[d.ordinal()],neighborState.getBlock() instanceof TubeBlock);\n        return s;
    }
    protected void onPlace(BlockState s,Level level,BlockPos pos,BlockState old,boolean moved) {
        if(old.getBlock()==this)return;
        BlockState linked=s;
        for(Direction d:Direction.values()){var neighbor=level.getBlockState(pos.relative(d));linked=linked.setValue(LINKS[d.ordinal()],Enclosures.matches(s,neighbor));if(this instanceof TubeBlock)linked=linked.setValue(TubeBlock.TANK_LINKS[d.ordinal()],Enclosures.isTank(neighbor) && Enclosures.matches(s,neighbor));if(this instanceof TankBlock)linked=linked.setValue(TankBlock.TUBE_LINKS[d.ordinal()],neighbor.getBlock() instanceof TubeBlock);}
        if(linked!=s)level.setBlock(pos,linked,3);
    }
    protected FluidState getFluidState(BlockState state) { return AquariumMod.WATER.getSource(false); }
    protected boolean isPathfindable(BlockState s, PathComputationType type) { return type == PathComputationType.WATER; }
    private static final VoxelShape[] WALLS = new VoxelShape[64];
    protected VoxelShape getCollisionShape(BlockState s, BlockGetter level, BlockPos pos, CollisionContext context) {
        int mask=0; for(Direction d:Direction.values()) if(s.getValue(LINKS[d.ordinal()]))mask |= 1<<d.ordinal();
        if(WALLS[mask]!=null)return WALLS[mask];
        VoxelShape shape=Shapes.empty();
        // Closed outer faces contain real fish. Shared faces are completely open.
        for(Direction d:Direction.values()) if(!s.getValue(LINKS[d.ordinal()])) {
            VoxelShape face=switch(d) {
                case DOWN -> box(0,0,0,16,1,16);
                case UP -> box(0,15,0,16,16,16);
                case NORTH -> box(0,0,0,16,16,1);
                case SOUTH -> box(0,0,15,16,16,16);
                case WEST -> box(0,0,0,1,16,16);
                case EAST -> box(15,0,0,16,16,16);
            };
            shape=Shapes.or(shape,face);
        }
        return WALLS[mask]=shape;
    }
    protected VoxelShape getShape(BlockState s,BlockGetter level,BlockPos pos,CollisionContext context) { return Shapes.block(); }
    protected boolean skipRendering(BlockState s,BlockState neighbor,Direction d) { return Enclosures.matches(s,neighbor); }
}
