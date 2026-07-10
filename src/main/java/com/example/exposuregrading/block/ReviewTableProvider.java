package com.example.exposuregrading.block;

import com.example.exposuregrading.menu.ReviewTableMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class ReviewTableProvider implements MenuProvider {
    private final ReviewTableBlockEntity blockEntity;

    public ReviewTableProvider(ReviewTableBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.exposuregrading.review_table");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ReviewTableMenu(containerId, inventory, blockEntity);
    }
}
