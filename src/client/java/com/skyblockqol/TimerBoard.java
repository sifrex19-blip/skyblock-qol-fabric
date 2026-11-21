package com.skyblockqol;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class TimerBoard {
    public void tick() { }
    public void render(DrawContext ctx, int x, int y) {
        ctx.fill(x, y, x + 130, y + 40, 0xAA150C08);
        ctx.drawText(MinecraftClient.getInstance().textRenderer, Text.literal("Timers"), x + 6, y + 6, 0xFFB05A, false);
        ctx.drawText(MinecraftClient.getInstance().textRenderer, Text.literal("No active timers"), x + 6, y + 20, 0xB88F78, false);
    }
}