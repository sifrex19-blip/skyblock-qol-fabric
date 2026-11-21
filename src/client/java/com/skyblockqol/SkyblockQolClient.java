package com.skyblockqol;

import net.fabricmc.api.ClientModInitializer;

public class SkyblockQolClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Skyblock QoL suite - HUD rendering wired once Fabric's 26.3
        // HudRenderCallback is available. Core logic lives in Util/SkillTracker.
    }
}