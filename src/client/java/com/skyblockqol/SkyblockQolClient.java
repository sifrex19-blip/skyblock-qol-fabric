package com.skyblockqol;

import net.fabricmc.api.ClientModInitializer;

public class SkyblockQolClient implements ClientModInitializer {
    public static final SkillTracker SKILLS = new SkillTracker();
    public static final TimerBoard TIMERS = new TimerBoard();
    public static final ChatRules CHAT = new ChatRules();

    @Override
    public void onInitializeClient() {
        CHAT.addRule("sea creature");
        CHAT.addRule("slayer");
    }
}