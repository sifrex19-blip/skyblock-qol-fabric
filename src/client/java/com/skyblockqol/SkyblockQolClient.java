package com.skyblockqol;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class SkyblockQolClient implements ClientModInitializer {
    public static final SkillTracker SKILLS = new SkillTracker();
    public static final TimerBoard TIMERS = new TimerBoard();
    public static final ChatRules CHAT = new ChatRules();

    private int tickCounter = 0;

    @Override
    public void onInitializeClient() {
        CHAT.addRule("sea creature");
        CHAT.addRule("slayer");

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tickCounter++;
            if (tickCounter % 20 == 0 && client.player != null) {
                client.player.sendOverlayMessage(
                    Component.literal("\u00a7bSkyblock QoL\u00a7r | loaded"));
            }
        });
    }
}