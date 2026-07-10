package com.example.exposuregrading;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;

import java.util.List;

public class ModCompatibility {
    private static Boolean modernUILoaded;
    private static Boolean polaroidLoaded;
    private static Boolean expandedLoaded;

    public static boolean isModernUILoaded() {
        if (modernUILoaded == null) {
            modernUILoaded = ModList.get().isLoaded("modernui");
        }
        return modernUILoaded;
    }

    public static boolean isPolaroidLoaded() {
        if (polaroidLoaded == null) {
            polaroidLoaded = ModList.get().isLoaded("exposure_polaroid");
        }
        return polaroidLoaded;
    }

    public static boolean isExpandedLoaded() {
        if (expandedLoaded == null) {
            expandedLoaded = ModList.get().isLoaded("exposure_expanded");
        }
        return expandedLoaded;
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
