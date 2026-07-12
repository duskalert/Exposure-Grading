package com.example.exposure_grading;

import com.example.exposure_grading.screen.ReviewTableScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.text.DecimalFormat;

public class ClientSetup {
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.REVIEW_TABLE.get(), ReviewTableScreen::new);
    }

    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.has(ModDataComponents.PHOTO_RATING.get())) {
            var r = stack.get(ModDataComponents.PHOTO_RATING.get());
            if (r != null) {
                var df = new DecimalFormat("#.#");
                event.getToolTip().add(Component.literal("§e§l评分:"));
                event.getToolTip().add(Component.literal(" §e构图 " + stars(r.composition())));
                event.getToolTip().add(Component.literal(" §e影调 " + stars(r.tone())));
                event.getToolTip().add(Component.literal(" §e创意 " + stars(r.creativity())));
                event.getToolTip().add(Component.literal(" §e内容 " + stars(r.content())));
            }
        }
    }

    private static String stars(float score) {
        int full = (int) (score / 2);
        float rem = score / 2 - full;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < full && i < 5; i++) sb.append("★");
        if (rem >= 0.25f && full < 5) sb.append("⯪");
        for (int i = sb.length(); i < 5; i++) sb.append("☆");
        return sb.toString();
    }
}
