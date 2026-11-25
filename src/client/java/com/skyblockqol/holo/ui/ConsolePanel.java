package com.skyblockqol.holo.ui;

import com.skyblockqol.SkyblockQolClient;

/**
 * Floating control console: timer shortcuts, chat-rule master switch and the
 * visual settings for the whole holographic interface. Follows the player so it
 * always floats naturally in front of them.
 */
public final class ConsolePanel extends HoloPanel {
    public ConsolePanel() {
        super("QOL CONSOLE", 150, 110);
        setFollowPlayer(true);
        setFollowDistance(2.4F);
        setFooter("AIM + CLICK TO USE");

        // top-right close button
        add(new HoloButton(63, -46, 16, 12, "X", () -> SkyblockQolClient.HOLO.toggleConsole()));

        // timer controls
        add(new HoloButton(-37, -30, 66, 14, "+5 MIN",
            () -> SkyblockQolClient.TIMERS.add("5 min", 5 * 60_000L)));
        add(new HoloButton(37, -30, 66, 14, "+30 MIN",
            () -> SkyblockQolClient.TIMERS.add("30 min", 30 * 60_000L)));
        add(new HoloButton(-37, -14, 66, 14, "CLEAR",
            () -> SkyblockQolClient.TIMERS.clear()));
        add(new HoloButton(37, -14, 66, 14, "STATS",
            () -> SkyblockQolClient.HOLO.toggleStats()));

        // chat rule master switch
        add(new HoloToggle(0, 3, 112, 14, "CHAT RULES",
            () -> SkyblockQolClient.CHAT.enabled,
            enabled -> SkyblockQolClient.CHAT.enabled = enabled));

        // visual settings
        add(new HoloSlider(0, 21, 112, 13, "OPACITY", 0.35F, 1.0F,
            () -> SkyblockQolClient.HOLO.opacity,
            value -> SkyblockQolClient.HOLO.opacity = (float) value,
            value -> Math.round(value * 100) + "%"));
        add(new HoloSlider(0, 38, 112, 13, "GLOW", 0.0F, 2.0F,
            () -> SkyblockQolClient.HOLO.glow,
            value -> SkyblockQolClient.HOLO.glow = (float) value,
            value -> String.format("%.1f", value)));
    }
}
