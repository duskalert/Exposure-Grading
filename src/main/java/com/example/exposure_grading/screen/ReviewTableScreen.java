package com.example.exposure_grading.screen;

import com.example.exposure_grading.ExposureGrading;
import com.example.exposure_grading.menu.ReviewTableMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ReviewTableScreen extends AbstractContainerScreen<ReviewTableMenu> {
    private static final ResourceLocation BG_DEFAULT = ResourceLocation.fromNamespaceAndPath(ExposureGrading.MODID, "textures/gui/review_table.png");
    private static final ResourceLocation BG_COZY = ResourceLocation.fromNamespaceAndPath(ExposureGrading.MODID, "textures/gui/review_table_cozy.png");

    private ResourceLocation bgTexture;

    public ReviewTableScreen(ReviewTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    private ResourceLocation resolveTexture() {
        try {
            boolean hasCozy = false;
            for (var pack : Minecraft.getInstance().getResourcePackRepository().getSelectedPacks()) {
                String id = pack.getId().toLowerCase();
                if (id.contains("cozy")) {
                    hasCozy = true;
                    break;
                }
            }
            if (hasCozy) {
                boolean exists = Minecraft.getInstance().getResourceManager()
                        .getResource(BG_COZY).isPresent();
                if (exists) return BG_COZY;
            }
        } catch (Exception ignored) {
        }
        return BG_DEFAULT;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("gui.exposure_grading.rate"), button -> onRate())
                .bounds(leftPos + 79, topPos + 34, 60, 20)
                .build());
    }

    private void onRate() {
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        if (bgTexture == null) {
            bgTexture = resolveTexture();
        }
        guiGraphics.blit(bgTexture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
