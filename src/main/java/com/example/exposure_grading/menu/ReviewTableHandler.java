package com.example.exposure_grading.menu;

import com.example.exposure_grading.block.ReviewTableBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

public class ReviewTableHandler implements IItemHandlerModifiable {
    @Nullable
    private final ReviewTableBlockEntity blockEntity;
    private ItemStack stack = ItemStack.EMPTY;

    public ReviewTableHandler(@Nullable ReviewTableBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        if (blockEntity != null) {
            this.stack = blockEntity.getPhotograph();
        }
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return stack;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (!this.stack.isEmpty()) return stack;
        if (!simulate) {
            this.stack = stack.copyWithCount(1);
            if (blockEntity != null) {
                blockEntity.setPhotograph(this.stack);
                this.stack = blockEntity.getPhotograph(); // keep same reference as BE
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (this.stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = this.stack.copyWithCount(1);
        if (!simulate) {
            this.stack = ItemStack.EMPTY;
            if (blockEntity != null) {
                blockEntity.setPhotograph(ItemStack.EMPTY);
                this.stack = blockEntity.getPhotograph();
            }
        }
        return result;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        this.stack = stack.copyWithCount(1);
        if (blockEntity != null) {
            blockEntity.setPhotograph(this.stack);
            this.stack = blockEntity.getPhotograph(); // keep same reference as BE
        }
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return !stack.isEmpty();
    }
}
