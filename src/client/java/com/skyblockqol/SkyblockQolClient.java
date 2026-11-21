package com.skyblockqol;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class SkyblockQolClient implements ClientModInitializer {
    public static final SkillTracker SKILLS = new SkillTracker();
    public static final TimerBoard TIMERS = new TimerBoard();
    public static final ChatRules CHAT = new ChatRules();

    @Override
    public void onInitializeClient() {
        CHAT.addRule("sea creature");
        CHAT.addRule("slayer");

        ClientTickEvents.END_CLIENT_TICK.register(client -> TIMERS.tick());
    }
}