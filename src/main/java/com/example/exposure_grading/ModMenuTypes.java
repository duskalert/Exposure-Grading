package com.example.exposure_grading;

import com.example.exposure_grading.menu.ReviewTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ExposureGrading.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<ReviewTableMenu>> REVIEW_TABLE = MENUS.register("review_table",
            () -> IMenuTypeExtension.create((containerId, inventory, data) -> {
                BlockPos pos = data.readBlockPos();
                return new ReviewTableMenu(containerId, inventory, pos);
            }));
}
