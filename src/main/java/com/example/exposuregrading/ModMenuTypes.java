package com.example.exposuregrading;

import com.example.exposuregrading.menu.ReviewTableMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ExposureGrading.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<ReviewTableMenu>> REVIEW_TABLE = MENUS.register("review_table",
            () -> new MenuType<>(ReviewTableMenu::new, FeatureFlags.VANILLA_SET));
}
