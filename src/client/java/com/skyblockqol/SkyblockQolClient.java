package com.skyblockqol;

import com.skyblockqol.holo.HoloManager;
import com.skyblockqol.holo.render.HoloRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class SkyblockQolClient implements ClientModInitializer {
    public static final SkillTracker SKILLS = new SkillTracker();
    public static final TimerBoard TIMERS = new TimerBoard();
    public static final ChatRules CHAT = new ChatRules();
    /** Floating holographic interface manager (3D world panels). */
    public static final HoloManager HOLO = new HoloManager();
    /** Whether the classic 2D skill HUD overlay is drawn. */
    public static boolean HUD_ENABLED = true;

    private static KeyMapping keyConsole;
    private static KeyMapping keyStats;

    @Override
    public void onInitializeClient() {
        CHAT.addRule("sea creature");
        CHAT.addRule("slayer");

        // Holographic interface keybinds (GLFW key codes: H = 72, O = 79).
        KeyMapping.Category category = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("skyblockqol", "holo"));
        keyConsole = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.skyblockqol.holo_console", 72, category));
        keyStats = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.skyblockqol.holo_stats", 79, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            TIMERS.tick();
            HOLO.tick(client);
            while (keyConsole.consumeClick()) HOLO.toggleConsole();
            while (keyStats.consumeClick()) HOLO.toggleStats();
        });

        // World-space panel rendering (surfaces + labels are submitted as geometry).
        HoloRenderer.register();
    }
}
