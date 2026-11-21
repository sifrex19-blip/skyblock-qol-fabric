package com.skyblockqol;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class SkyblockQolClient implements ClientModInitializer {
    private final SkillTracker skills = new SkillTracker();
    private final TimerBoard timers = new TimerBoard();

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            int w = MinecraftClient.getInstance().getWindow().getScaledWidth();
            int h = MinecraftClient.getInstance().getWindow().getScaledHeight();
            // Render skill panel top-left
            skills.render(drawContext, 6, 6);
            timers.render(drawContext, w - 140, 6);
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Tick timers
            timers.tick();
        });
    }
}