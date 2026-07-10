package com.example.exposure_grading;

import com.example.exposure_grading.screen.ReviewTableScreen;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class ClientSetup {
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.REVIEW_TABLE.get(), ReviewTableScreen::new);
    }
}
