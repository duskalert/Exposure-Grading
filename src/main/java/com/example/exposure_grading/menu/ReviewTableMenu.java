package com.example.exposure_grading.menu;

import com.example.exposure_grading.ModMenuTypes;
import com.example.exposure_grading.block.ReviewTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ReviewTableMenu extends AbstractContainerMenu {
    private final ReviewTableBlockEntity blockEntity;

    public ReviewTableMenu(int containerId, Inventory playerInventory) {
        super(ModMenuTypes.REVIEW_TABLE.get(), containerId);
        this.blockEntity = null;
        addSlots(playerInventory);
    }

    public ReviewTableMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, (ReviewTableBlockEntity) playerInventory.player.level().getBlockEntity(pos));
    }

    public ReviewTableMenu(int containerId, Inventory playerInventory, ReviewTableBlockEntity blockEntity) {
        super(ModMenuTypes.REVIEW_TABLE.get(), containerId);
        this.blockEntity = blockEntity;
        addSlots(playerInventory);
    }

    private void addSlots(Inventory playerInventory) {
        addSlot(new SlotItemHandler(new ReviewTableHandler(blockEntity), 0, 49, 40) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !stack.isEmpty();
            }

            @Override
            public void setChanged() {
                super.setChanged();
                ReviewTableMenu.this.slotsChanged(this.container);
            }
        });

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public ReviewTableBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            if (index == 0) {
                if (!moveItemStackTo(stack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!moveItemStackTo(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
