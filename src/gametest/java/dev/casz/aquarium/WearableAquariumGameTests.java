package dev.casz.aquarium;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

/** GameTests verifying the entire wearable stores only four saved fish. */
public final class WearableAquariumGameTests {
    @GameTest
    public void wearableKeepsFourFishAndPreservesEditedTropicalVariants(GameTestHelper h) {
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wearer = new ItemStack(AquariumMod.WEARABLE_AQUARIUM);
        player.setItemInHand(InteractionHand.MAIN_HAND, wearer);
        var item = (WearableAquariumItem) wearer.getItem();

        ItemStack tropical = new ItemStack(Items.TROPICAL_FISH_BUCKET);
        tropical.set(DataComponents.TROPICAL_FISH_BASE_COLOR, DyeColor.BLUE);
        tropical.set(DataComponents.TROPICAL_FISH_PATTERN_COLOR, DyeColor.YELLOW);
        for (ItemStack fish : new ItemStack[]{
            new ItemStack(Items.COD_BUCKET),
            new ItemStack(Items.SALMON_BUCKET),
            new ItemStack(Items.PUFFERFISH_BUCKET),
            tropical
        }) {
            player.setItemInHand(InteractionHand.OFF_HAND, fish);
            h.assertTrue(item.use(h.getLevel(), player, InteractionHand.MAIN_HAND).consumesAction(),
                "Inserting a fish bucket should succeed");
            h.assertTrue(player.getOffhandItem().is(Items.BUCKET),
                "Insertion must return an empty bucket");
        }
        h.assertTrue(WearableAquariumItem.fish(wearer).size() == 4,
            "The entire wearable must have exactly four capacity slots");

        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.COD_BUCKET));
        h.assertTrue(item.use(h.getLevel(), player, InteractionHand.MAIN_HAND) == InteractionResult.FAIL,
            "A fifth fish must be rejected");
        h.assertTrue(player.getOffhandItem().is(Items.COD_BUCKET),
            "Rejecting the fifth fish must not consume its bucket");

        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.BUCKET));
        h.assertTrue(item.use(h.getLevel(), player, InteractionHand.MAIN_HAND).consumesAction(),
            "Retrieval should succeed");
        ItemStack output = player.getOffhandItem();
        h.assertTrue(output.is(Items.TROPICAL_FISH_BUCKET), "Last inserted bucket must be retrieved first");
        h.assertTrue(output.get(DataComponents.TROPICAL_FISH_BASE_COLOR) == DyeColor.BLUE
            && output.get(DataComponents.TROPICAL_FISH_PATTERN_COLOR) == DyeColor.YELLOW,
            "Edited tropical fish colors must survive wearable storage");
        h.assertTrue(WearableAquariumItem.fish(wearer).size() == 3,
            "Retrieval must free only one slot");
        h.succeed();
    }

    @GameTest
    public void wearableRejectsNonFishAndClampsStoredData(GameTestHelper h) {
        ItemStack wearer = new ItemStack(AquariumMod.WEARABLE_AQUARIUM);
        WearableAquariumItem.setFish(wearer, java.util.List.of(
            new ItemStack(Items.COD_BUCKET),
            new ItemStack(Items.AXOLOTL_BUCKET),
            new ItemStack(Items.SALMON_BUCKET),
            new ItemStack(Items.PUFFERFISH_BUCKET),
            new ItemStack(Items.TROPICAL_FISH_BUCKET),
            new ItemStack(Items.COD_BUCKET)
        ));
        h.assertTrue(WearableAquariumItem.fish(wearer).size() == 4,
            "Non-fish buckets must be filtered and data clamped to four fish");
        h.assertTrue(WearableAquariumItem.fish(wearer).stream()
            .noneMatch(s -> s.is(Items.AXOLOTL_BUCKET)), "Only fish buckets are accepted");
        h.succeed();
    }
}
