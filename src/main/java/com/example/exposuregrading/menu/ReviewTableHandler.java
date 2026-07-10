package com.example.exposuregrading.menu;

import com.example.exposuregrading.block.ReviewTableBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class ReviewTableHandler implements IItemHandler {
    @Nullable
    private final ReviewTableBlockEntity blockEntity;

    public ReviewTableHandler(@Nullable ReviewTableBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return blockEntity != null ? blockEntity.getPhotograph() : ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (blockEntity != null && !blockEntity.hasPhotograph()) {
            if (!simulate) {
                blockEntity.setPhotograph(stack);
            }
            return ItemStack.EMPTY;
        }
        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (blockEntity == null) return ItemStack.EMPTY;
        ItemStack current = blockEntity.getPhotograph();
        if (current.isEmpty()) return ItemStack.EMPTY;
        if (!simulate) {
            blockEntity.setPhotograph(ItemStack.EMPTY);
        }
        return current.copyWithCount(1);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return !stack.isEmpty();
    }
}
