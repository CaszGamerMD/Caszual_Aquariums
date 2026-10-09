package dev.casz.aquarium;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

/** Fish-only item inventory; Caszual Additions' cosmetic layer renders its contents. */
public final class WearableAquariumItem extends Item {
    public static final int CAPACITY = 4;

    public WearableAquariumItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static boolean isFishBucket(ItemStack stack) {
        return stack.is(Items.COD_BUCKET) || stack.is(Items.SALMON_BUCKET)
            || stack.is(Items.TROPICAL_FISH_BUCKET) || stack.is(Items.PUFFERFISH_BUCKET);
    }

    /** Copies retain fish type, bucket data and custom tropical fish variants. */
    public static List<ItemStack> fish(ItemStack aquarium) {
        if (!(aquarium.getItem() instanceof WearableAquariumItem)) return List.of();
        return aquarium.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
            .nonEmptyItemCopyStream().filter(WearableAquariumItem::isFishBucket)
            .limit(CAPACITY).map(s -> s.copyWithCount(1)).toList();
    }

    public static void setFish(ItemStack aquarium, List<ItemStack> contents) {
        if (!(aquarium.getItem() instanceof WearableAquariumItem)) return;
        var accepted = contents.stream().filter(WearableAquariumItem::isFishBucket)
            .limit(CAPACITY).map(s -> s.copyWithCount(1)).toList();
        if (accepted.isEmpty()) aquarium.remove(DataComponents.CONTAINER);
        else aquarium.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(accepted));
    }

    /**
     * Main hand: wearable aquarium. Offhand: a fish bucket to insert, empty bucket
     * to retrieve the most recently inserted fish. Works in air; saved on the item.
     */
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        ItemStack aquarium = player.getItemInHand(hand), offhand = player.getOffhandItem();
        boolean adding = isFishBucket(offhand), removing = offhand.is(Items.BUCKET);
        if (!adding && !removing) return InteractionResult.PASS;
        List<ItemStack> saved = new ArrayList<>(fish(aquarium));
        if (adding && saved.size() >= CAPACITY) {
            if (!level.isClientSide()) player.sendOverlayMessage(Component.literal("Wearable Aquarium is full (4/4 fish)."));
            return InteractionResult.FAIL;
        }
        if (removing && saved.isEmpty()) {
            if (!level.isClientSide()) player.sendOverlayMessage(Component.literal("Wearable Aquarium is empty."));
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ItemStack returned;
        if (adding) {
            saved.add(offhand.copyWithCount(1));
            returned = new ItemStack(Items.BUCKET);
        } else returned = saved.remove(saved.size() - 1);
        setFish(aquarium, saved);
        if (!player.getAbilities().instabuild) {
            offhand.shrink(1);
            if (offhand.isEmpty()) player.setItemInHand(InteractionHand.OFF_HAND, returned);
            else if (!player.getInventory().add(returned)) player.drop(returned, false);
        } else if (removing && !player.getInventory().add(returned)) player.drop(returned, false);
        player.sendOverlayMessage(Component.literal("Wearable Aquarium: " + saved.size() + "/" + CAPACITY + " fish."));
        return InteractionResult.SUCCESS;
    }
}
