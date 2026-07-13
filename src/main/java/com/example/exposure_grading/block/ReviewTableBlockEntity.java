package com.example.exposure_grading.block;

import com.example.exposure_grading.ModBlockEntities;
import com.example.exposure_grading.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ReviewTableBlockEntity extends BlockEntity {
    private static final long STALE_TIMEOUT_MS = 120_000;

    private ItemStack photograph = ItemStack.EMPTY;

    public ReviewTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REVIEW_TABLE.value(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        clearStaleRating();
    }

    public void clearStaleRating() {
        if (photograph.isEmpty()) return;
        String state = photograph.get(ModDataComponents.RATING_STATE.get());
        if (!"rating".equals(state)) return;
        if (photograph.has(ModDataComponents.PHOTO_RATING.get())) return;

        Long startTime = photograph.get(ModDataComponents.RATING_START_TIME.get());
        if (startTime == null || System.currentTimeMillis() - startTime > STALE_TIMEOUT_MS) {
            photograph.remove(ModDataComponents.RATING_STATE.get());
            photograph.remove(ModDataComponents.RATING_START_TIME.get());
            setChanged();
        }
    }

    public ItemStack getPhotograph() {
        return photograph;
    }

    public void setPhotograph(ItemStack stack) {
        this.photograph = stack.copyWithCount(1);
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean hasPhotograph() {
        return !photograph.isEmpty();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!photograph.isEmpty()) {
            tag.put("photograph", photograph.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("photograph")) {
            photograph = ItemStack.parse(registries, tag.getCompound("photograph")).orElse(ItemStack.EMPTY);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        if (!photograph.isEmpty()) {
            tag.put("photograph", photograph.save(registries));
        }
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
