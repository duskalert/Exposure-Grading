package com.example.exposure_grading.network;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModNetworking {
    public static void onRegisterPayload(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1.0");
        registrar.playToServer(C2SRatingPacket.TYPE, new C2SRatingPacket.Codec(), C2SRatingPacket::handle);
        registrar.playToClient(S2CTriggerRatingPacket.TYPE, new S2CTriggerRatingPacket.Codec(), S2CTriggerRatingPacket::handle);
    }
}
