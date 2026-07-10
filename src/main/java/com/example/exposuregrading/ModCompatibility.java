package com.example.exposuregrading;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;

import java.util.List;

public class ModCompatibility {
    private static Boolean modernUILoaded;

    public static boolean isModernUILoaded() {
        if (modernUILoaded == null) {
            modernUILoaded = ModList.get().isLoaded("modernui");
        }
        return modernUILoaded;
    }

    public static List<String> getSelectedResourcePacks() {
        return Minecraft.getInstance().getResourcePackRepository().getSelectedPacks().stream()
                .map(pack -> pack.getId())
                .toList();
    }

    public static boolean isResourcePackActive(String packId) {
        return Minecraft.getInstance().getResourcePackRepository().getSelectedPacks().stream()
                .anyMatch(pack -> pack.getId().equals(packId));
    }

    public static boolean hasCustomTexture(String path) {
        return Minecraft.getInstance().getResourceManager()
                .getResource(ResourceLocation.fromNamespaceAndPath(ExposureGrading.MODID, path))
                .isPresent();
    }
}
