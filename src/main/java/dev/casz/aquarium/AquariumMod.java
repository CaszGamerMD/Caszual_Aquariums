package dev.casz.aquarium;
import java.util.*;
import java.nio.file.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.menu.v1.*;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.*;
import org.slf4j.LoggerFactory;

public final class AquariumMod implements ModInitializer {
 public static final String ID="linked_aquariums",MANAGED=ID+":managed";
 public static int fishPerBlock=1;
 public static final net.minecraft.world.level.material.FlowingFluid WATER=Registry.register(BuiltInRegistries.FLUID,id("contained_water"),new ContainedWaterFluid());
 public static final Block TANK=register("aquarium",false),TUBE=register("swim_tube",true);
 public static final Block PASSIVE_TERRARIUM=registerLand("passive_terrarium",false,false),HOSTILE_TERRARIUM=registerLand("hostile_terrarium",false,true),PASSIVE_PIPE=registerLand("passive_pipe",true,false),HOSTILE_PIPE=registerLand("hostile_pipe",true,true);
 public static final Block MOBITAT=Registry.register(BuiltInRegistries.BLOCK,id("mobitat"),new MobitatBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(ResourceKey.create(Registries.BLOCK,id("mobitat"))).noOcclusion()));
 public static final Item MOBITAT_ITEM=Registry.register(BuiltInRegistries.ITEM,id("mobitat"),new BlockItem(MOBITAT,new Item.Properties().setId(ResourceKey.create(Registries.ITEM,id("mobitat"))).stacksTo(1)));
 public static final Item MOB_NET=Registry.register(BuiltInRegistries.ITEM,id("mob_net"),new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM,id("mob_net"))).stacksTo(1)));
 public static final Block DECOR_MODEL=Registry.register(BuiltInRegistries.BLOCK,id("decor_model"),new DecorModelBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,id("decor_model"))).noCollision().noOcclusion()));
 public static final BlockEntityType<MobitatBlockEntity> MOBITAT_ENTITY=Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,id("mobitat"),FabricBlockEntityTypeBuilder.create(MobitatBlockEntity::new,MOBITAT).build());
 public static final BlockEntityType<TankBlockEntity> TANK_ENTITY=Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,id("aquarium"),FabricBlockEntityTypeBuilder.create(TankBlockEntity::new,TANK,PASSIVE_TERRARIUM,HOSTILE_TERRARIUM).build());
 public static final MenuType<MobitatMenu> MOBITAT_MENU=Registry.register(BuiltInRegistries.MENU,id("mobitat"),new ExtendedMenuType<>(MobitatMenu::new,BlockPos.STREAM_CODEC));
 public static final MenuType<AquariumMenu> MENU=Registry.register(BuiltInRegistries.MENU,id("aquarium"),new ExtendedMenuType<>(AquariumMenu::new,BlockPos.STREAM_CODEC));
 public static final Item CREATURE_BUCKET=Registry.register(BuiltInRegistries.ITEM,id("creature_bucket"),new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM,id("creature_bucket"))).stacksTo(1)));
 public static Identifier id(String s){return Identifier.fromNamespaceAndPath(ID,s);}
 private static Block register(String name,boolean tube){var id=id(name);var key=ResourceKey.create(Registries.BLOCK,id);var props=BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(key).noOcclusion().isViewBlocking((s,l,p)->false).isSuffocating((s,l,p)->false);Block block=tube?new TubeBlock(props):new TankBlock(props);Registry.register(BuiltInRegistries.BLOCK,key,block);Registry.register(BuiltInRegistries.ITEM,ResourceKey.create(Registries.ITEM,id),new BlockItem(block,new Item.Properties().setId(ResourceKey.create(Registries.ITEM,id)).useBlockDescriptionPrefix()));return block;}
 private static Block registerLand(String name,boolean pipe,boolean hostile){var id=id(name);var key=ResourceKey.create(Registries.BLOCK,id);var props=BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(key).noOcclusion().isViewBlocking((s,l,p)->false).isSuffocating((s,l,p)->false);Block block=pipe?new TerrariumPipeBlock(props,hostile):new TerrariumBlock(props,hostile);Registry.register(BuiltInRegistries.BLOCK,key,block);Registry.register(BuiltInRegistries.ITEM,ResourceKey.create(Registries.ITEM,id),new BlockItem(block,new Item.Properties().setId(ResourceKey.create(Registries.ITEM,id)).useBlockDescriptionPrefix()));return block;}
 public void onInitialize(){
  loadConfig();CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(out->{out.accept(TANK);out.accept(TUBE);out.accept(PASSIVE_TERRARIUM);out.accept(HOSTILE_TERRARIUM);out.accept(PASSIVE_PIPE);out.accept(HOSTILE_PIPE);out.accept(MOB_NET);out.accept(MOBITAT);});
  UseEntityCallback.EVENT.register((player,level,hand,entity,hit)->{
   var held=player.getItemInHand(hand);if(!held.is(MOB_NET)||player.isSpectator())return InteractionResult.PASS;
   if(!(entity instanceof net.minecraft.world.entity.Mob mob)||!(Terrestrial.supported(mob)||Inhabitants.supported(mob.getType()))||mob.isPassenger()||mob.isVehicle()||!mob.isAlive()||Terrestrial.filled(held))return InteractionResult.FAIL;
   if(!level.isClientSide())player.setItemInHand(hand,Terrestrial.capture((ServerLevel)level,mob));return InteractionResult.SUCCESS;
  });
  UseBlockCallback.EVENT.register((player,level,hand,hit)->{
   if(player.isSpectator())return InteractionResult.PASS;var held=player.getItemInHand(hand);var state=level.getBlockState(hit.getBlockPos());
   if(held.is(MOBITAT_ITEM)&&!level.isClientSide())return useMobitat(player,(ServerLevel)level,hand,hit);
   if(state.is(MOBITAT)&&level.getBlockEntity(hit.getBlockPos()) instanceof MobitatBlockEntity mb){if(level.isClientSide())return InteractionResult.SUCCESS;if(held.is(MOB_NET)){if(Terrestrial.filled(held)){if(!mb.addNet(held))player.sendOverlayMessage(Component.literal("Mobitat holds up to 5 mobs of one type."));return InteractionResult.SUCCESS;}if(mb.empty())return InteractionResult.FAIL;player.setItemInHand(hand,mb.takeNet(mb.size()-1));return InteractionResult.SUCCESS;}if(held.isEmpty()&&player instanceof ServerPlayer sp){BlockPos mp=hit.getBlockPos();sp.openMenu(new ExtendedMenuProvider<BlockPos>(){public BlockPos getScreenOpeningData(ServerPlayer q){return mp;}public Component getDisplayName(){return Component.literal("Mobitat");}public MobitatMenu createMenu(int id,Inventory inv,Player q){return new MobitatMenu(id,inv,(ServerLevel)level,mp);}});return InteractionResult.SUCCESS;}}
   if(!AquariumBlock.isModule(state)){
    if(!Terrestrial.filled(held))return InteractionResult.PASS;
    if(level.isClientSide())return InteractionResult.SUCCESS;String error=Terrestrial.release((ServerLevel)level,hit.getBlockPos().relative(hit.getDirection()),held);if(error!=null){player.sendOverlayMessage(Component.literal(error));return InteractionResult.FAIL;}return InteractionResult.SUCCESS;
   }
   if(level.isClientSide())return InteractionResult.SUCCESS;return interact(player,(ServerLevel)level,hand,hit);
  });ServerTickEvents.END_LEVEL_TICK.register(l->{Inhabitants.tick(l);Terrestrial.tick(l);});
 }
 private static InteractionResult useMobitat(Player player,ServerLevel level,InteractionHand hand,BlockHitResult hit){
  ItemStack held=player.getItemInHand(hand);MobitatBlockEntity box=new MobitatBlockEntity(BlockPos.ZERO,MOBITAT.defaultBlockState());box.setLevel(level);box.fromItem(held);if(box.empty())return InteractionResult.PASS;
  BlockPos target=hit.getBlockPos();BlockState state=level.getBlockState(target);int moved=0;
  for(int i=box.size()-1;i>=0;i--){ItemStack net=box.peekNet(i);boolean ok=false;
   if(AquariumBlock.isModule(state)&&Enclosures.isTank(state)){
    if(Enclosures.isLand(state)){ok=Terrestrial.add(level,target,net)==null;}
    else {var mob=Terrestrial.load(level,net);if(mob!=null&&Inhabitants.supported(mob.getType())){var nw=Network.scan(level,target);if(nw.complete()&&nw.residents(level).size()<nw.capacity()){Inhabitants.configure(mob);mob.setPos(target.getX()+.5,target.getY()+.4,target.getZ()+.5);ok=level.addFreshEntity(mob);}}}
   }else{ok=Terrestrial.release(level,target.relative(hit.getDirection()),net)==null;}
   if(ok){box.remove(i);moved++;}
  }
  if(moved==0){player.sendOverlayMessage(Component.literal(AquariumBlock.isModule(state)?"No stored mobs are valid here, or the enclosure is full.":"There is not enough room to release these mobs."));return InteractionResult.FAIL;}
  player.setItemInHand(hand,box.asItem());return InteractionResult.SUCCESS;
 }

 private static void loadConfig(){var path=FabricLoader.getInstance().getConfigDir().resolve("linked-aquariums.properties");try{Properties p=new Properties();if(Files.exists(path))try(var in=Files.newInputStream(path)){p.load(in);}else{p.setProperty("fish-per-tank-block","1");try(var out=Files.newOutputStream(path)){p.store(out,"Each aquarium inhabitant takes one capacity slot. Tubes add none.");}}fishPerBlock=Math.clamp(Integer.parseInt(p.getProperty("fish-per-tank-block","1")),1,16);}catch(Exception ex){LoggerFactory.getLogger(ID).warn("Cannot read aquarium configuration; using one creature per tank block",ex);fishPerBlock=1;}}
 static void give(Player p,ItemStack stack){if(!stack.isEmpty()&&!p.getInventory().add(stack))p.drop(stack,false);}
 public static boolean isDecoration(ItemStack s){return s.is(Items.TRIDENT)||(s.getItem() instanceof BlockItem b && !(b.getBlock() instanceof AquariumBlock));}
 static void tridentChance(ServerLevel level,TankBlockEntity be){if(Enclosures.isAquatic(be.getBlockState())&&be.decoration.is(Items.TRIDENT)&&level.getRandom().nextInt(50)==0)Inhabitants.summonDrowned(level,be.getBlockPos());}
 private static InteractionResult interact(Player p,ServerLevel level,InteractionHand hand,BlockHitResult hit){
  BlockPos pos=hit.getBlockPos();ItemStack held=p.getItemInHand(hand);var state=level.getBlockState(pos);TankBlockEntity be=level.getBlockEntity(pos) instanceof TankBlockEntity b?b:null;if(be!=null){be.migrateLegacy();state=level.getBlockState(pos);}
  if(p.isShiftKeyDown()&&held.isEmpty()&&be!=null){if(!be.decoration.isEmpty()){give(p,be.decoration.copy());be.decoration=ItemStack.EMPTY;be.changed();be.tick();}else{int soil=state.getValue(AquariumBlock.SOIL);if(soil>0){give(p,new ItemStack(Palette.item(Palette.SOILS.get(soil))));level.setBlock(pos,state.setValue(AquariumBlock.SOIL,0),3);}}return InteractionResult.SUCCESS;}
  if(Enclosures.isLand(state)){
   if(held.is(MOB_NET)){if(Terrestrial.filled(held)){String error=Terrestrial.add(level,pos,held);if(error!=null){p.sendOverlayMessage(Component.literal(error));return InteractionResult.FAIL;}return InteractionResult.SUCCESS;}var mob=Network.scan(level,pos).residents(level).stream().filter(m->m.entityTags().contains(Terrestrial.MANAGED)).min(Comparator.comparingDouble(m->m.distanceToSqr(Vec3.atCenterOf(pos)))).orElse(null);if(mob==null)return InteractionResult.FAIL;p.setItemInHand(hand,Terrestrial.capture(level,mob));return InteractionResult.SUCCESS;}
   if(be!=null&&(held.is(Items.WATER_BUCKET)||held.is(Items.BUCKET))){if(!Terrestrial.groundEntry(level,pos))return InteractionResult.FAIL;boolean water=state.getValue(TerrariumBlock.GROUND_WATER);boolean add=held.is(Items.WATER_BUCKET);if(water==add)return InteractionResult.SUCCESS;level.setBlock(pos,state.setValue(TerrariumBlock.GROUND_WATER,add),3);if(!p.getAbilities().instabuild){held.shrink(1);var returned=new ItemStack(add?Items.BUCKET:Items.WATER_BUCKET);if(held.isEmpty())p.setItemInHand(hand,returned);else give(p,returned);}return InteractionResult.SUCCESS;}
   if(held.getItem() instanceof SpawnEggItem||Inhabitants.accepts(held))return InteractionResult.FAIL;
  }
  if(Enclosures.isAquatic(state)&&Inhabitants.accepts(held)){var result=Inhabitants.add(level,pos,held,p);if(!result.success()){p.sendOverlayMessage(Component.literal("No room, or aquarium network not fully loaded."));return InteractionResult.FAIL;}if(!result.returned().isEmpty()){if(held.isEmpty())p.setItemInHand(hand,result.returned());else give(p,result.returned());}return InteractionResult.SUCCESS;}
  if(held.getItem() instanceof SpawnEggItem)return InteractionResult.FAIL;
  if(Enclosures.isAquatic(state)&&(held.is(Items.BUCKET)||held.is(Items.WATER_BUCKET))){var mob=Network.scan(level,pos).residents(level).stream().min(Comparator.comparingDouble(m->m.distanceToSqr(Vec3.atCenterOf(pos)))).orElse(null);if(mob==null)return InteractionResult.FAIL;var stack=Inhabitants.capture(level,mob);if(!p.getAbilities().instabuild){held.shrink(1);if(held.isEmpty())p.setItemInHand(hand,stack);else give(p,stack);}else give(p,stack);return InteractionResult.SUCCESS;}
  int soil=Palette.find(Palette.SOILS,held.getItem());if(be!=null&&soil>0){if(state.getValue(AquariumBlock.DOWN))return InteractionResult.FAIL;int old=state.getValue(AquariumBlock.SOIL);if(old==soil)return InteractionResult.SUCCESS;level.setBlock(pos,state.setValue(AquariumBlock.SOIL,soil),3);if(old>0)give(p,new ItemStack(Palette.item(Palette.SOILS.get(old))));if(!p.getAbilities().instabuild)held.shrink(1);return InteractionResult.SUCCESS;}
  if(be!=null&&isDecoration(held)){if(ItemStack.isSameItemSameComponents(held,be.decoration))return InteractionResult.SUCCESS;ItemStack old=be.decoration.copy();be.setDecoration(held);if(!p.getAbilities().instabuild)held.shrink(1);give(p,old);tridentChance(level,be);be.tick();return InteractionResult.SUCCESS;}
  if(held.isEmpty()&&be!=null){if(p instanceof ServerPlayer server)p.openMenu(new ExtendedMenuProvider<BlockPos>(){public BlockPos getScreenOpeningData(ServerPlayer player){return pos;}public Component getDisplayName(){return Component.literal(Enclosures.isLand(level.getBlockState(pos))?"Linked Terrarium":"Linked Aquarium");}public AquariumMenu createMenu(int id,Inventory inv,Player player){return new AquariumMenu(id,inv,level,pos);}});return InteractionResult.SUCCESS;}
  return held.getItem() instanceof BlockItem?InteractionResult.PASS:InteractionResult.FAIL;
 }
}
