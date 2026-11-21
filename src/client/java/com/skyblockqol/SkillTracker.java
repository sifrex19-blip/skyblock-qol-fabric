package com.skyblockqol;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class SkillTracker {
    // Minimal: shows a placeholder panel until skill parsing is wired.
    public void render(DrawContext ctx, int x, int y) {
        ctx.fill(x, y, x + 150, y + 60, 0xAA0B0D17);
        ctx.drawText(MinecraftClient.getInstance().textRenderer, Text.literal("Skyblock QoL - Skills"), x + 6, y + 6, 0x7CF0FF, false);
        ctx.drawText(MinecraftClient.getInstance().textRenderer, Text.literal("Waiting for action bar..."), x + 6, y + 20, 0x8A90B8, false);
    }
}