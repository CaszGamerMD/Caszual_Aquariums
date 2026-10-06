package dev.casz.aquarium;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class QualityGameTests {
 @GameTest public void creativeBreakingMobitatKeepsResident(GameTestHelper h){
  h.setBlock(1,1,1,AquariumMod.MOBITAT);BlockPos pos=h.absolutePos(new BlockPos(1,1,1));var level=h.getLevel();var be=(MobitatBlockEntity)level.getBlockEntity(pos);
  var cow=h.spawn(EntityTypes.COW,3.5f,1.1f,1.5f);var net=Terrestrial.capture(level,cow);h.assertTrue(be.addNet(net),"Mobitat must accept captured resident");
  var player=h.makeMockPlayer(GameType.CREATIVE);player.setPos(Vec3.atCenterOf(pos));AquariumMod.MOBITAT.playerWillDestroy(level,pos,level.getBlockState(pos),player);level.removeBlock(pos,false);
  var drops=level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(2),e->e.getItem().is(AquariumMod.MOBITAT_ITEM));h.assertTrue(drops.size()==1,"Creative break must drop exactly one Mobitat");
  var restored=new MobitatBlockEntity(BlockPos.ZERO,AquariumMod.MOBITAT.defaultBlockState());restored.setLevel(level);restored.fromItem(drops.getFirst().getItem());h.assertTrue(restored.size()==1&&restored.type().equals("minecraft:cow"),"Dropped Mobitat must retain its resident");h.succeed();
 }
 @GameTest public void editorFindsExistingDecorationOwner(GameTestHelper h){
  h.setBlock(0,1,1,AquariumMod.TANK);h.setBlock(1,1,1,AquariumMod.TANK);BlockPos first=h.absolutePos(new BlockPos(0,1,1)),second=h.absolutePos(new BlockPos(1,1,1));var level=h.getLevel();
  var owner=(TankBlockEntity)level.getBlockEntity(second);owner.addDecoration(new net.minecraft.world.item.ItemStack(Items.DIAMOND_BLOCK),EnclosureDecoration.Anchor.BODY);
  var player=h.makeMockPlayer(GameType.SURVIVAL);player.setPos(Vec3.atCenterOf(first));var menu=new AquariumMenu(1,player.getInventory(),level,first);
  h.assertTrue(menu.clickMenuButton(player,22),"Editor must find decoration data stored on another connected tank");h.assertTrue(menu.transfer.getItem(1).is(Items.DIAMOND_BLOCK)&&owner.decorations.isEmpty(),"Editor must return the existing decoration without loss");h.succeed();
 }
}
