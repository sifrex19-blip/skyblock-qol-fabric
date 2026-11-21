package com.skyblockqol.mixin;

import com.skyblockqol.SkyblockQolClient;
import com.skyblockqol.SkillTracker;
import com.skyblockqol.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

/** Draws an animated 3D skill-gain HUD, only while XP is being earned. */
@Mixin(Hud.class)
public class HudMixin {
    private static final Random RNG = new Random();

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void skyblockqol$drawHud(GuiGraphicsExtractor g, DeltaTracker delta, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        SkillTracker tracker = SkyblockQolClient.SKILLS;
        if (!tracker.recentlyActive(5000)) return; // popup only while farming XP

        SkillTracker.Skill sk = tracker.current();
        if (sk == null) return;
        Font font = mc.font;
        long now = System.currentTimeMillis();
        float secs = (now % 100000) / 1000f;

        int x = 6, y = 6, w = 200, h = 118, depth = 3;
        int accent = skillColor(sk.name);

        // ---- 3D slab depth ----
        for (int i = depth; i > 0; i--) {
            g.fill(x + i, y + h, x + w + i, y + h + i, 0x66000000);
            g.fill(x + w, y + i, x + w + i, y + h + i, 0x66000000);
        }
        // ---- outer glow, pulsing ----
        int pulse = (int) (18 + 12 * Math.sin(secs * 3.0));
        for (int i = 5; i > 0; i--) g.fill(x - i, y - i, x + w + i, y + h + i, (pulse << 24) | (accent & 0xFFFFFF));
        // ---- glass body ----
        g.fill(x, y, x + w, y + h, 0xEA0B0D17);
        // header wash + accent line
        g.fillGradient(x + 1, y + 1, x + w - 1, y + 24, 0x55FFFFFF | (accent & 0xFFFFFF), 0x00000000);
        g.fill(x + 3, y, x + w - 3, y + 1, 0xFF000000 | (accent & 0xFFFFFF));
        g.fillGradient(x, y + 3, x + 2, y + h - 3, 0xFF000000 | (accent & 0xFFFFFF), 0xFFA77BFF);

        // ---- title + level ----
        g.text(font, Component.literal("\u00a7l" + sk.name.toUpperCase()), x + 10, y + 7, 0xFFFFFFFF);
        int lvl = (int) (sk.progress * 100);
        g.text(font, Component.literal(lvl + "%"), x + w - 10 - font.width(lvl + "%"), y + 7, 0xFF000000 | (accent & 0xFFFFFF));

        // ---- big progress bar with shimmer ----
        int by = y + 30;
        g.fill(x + 10, by, x + w - 10, by + 8, 0x662A2F4A);
        int fw = (int) ((w - 20) * sk.progress);
        if (fw > 1) {
            g.fillGradient(x + 10, by + 1, x + 10 + fw, by + 7, 0xFF000000 | (accent & 0xFFFFFF), 0xFFA77BFF);
            g.fill(x + 10, by + 1, x + 10 + fw, by + 2, 0x55FFFFFF);
        }
        // sweeping shimmer
        int sweep = (int) (((secs * 0.35) % 1.0) * (w - 20));
        if (sweep < fw) {
            g.fill(x + 10 + sweep, by + 1, Math.min(x + 10 + sweep + 8, x + 10 + fw), by + 7, 0x66FFFFFF);
        }

        // ---- stat rows ----
        int ry = y + 44;
        stat(g, font, x, ry, accent, "XP/hr", sk.rate > 0 ? Util.fmtCompact(sk.rate) : "...");
        ry += 14;
        stat(g, font, x, ry, accent, "NL in", sk.etaSeconds() > 0 ? Util.fmtDuration(sk.etaSeconds()) : "...");
        ry += 14;
        stat(g, font, x, ry, accent, "Wheat/hr", Util.fmtCompact(tracker.wheatPerHour));
        ry += 14;
        stat(g, font, x, ry, accent, "Wheat/s", Util.fmtCompact(tracker.wheatPerHour / 3600.0));

        // ---- sparkles rising along the panel ----
        drawSparkles(g, x, y, w, h, accent, secs);
    }

    private void stat(GuiGraphicsExtractor g, Font font, int x, int y, int accent, String label, String value) {
        g.fill(x + 10, y + 2, x + 12, y + 9, 0xFF000000 | (accent & 0xFFFFFF));
        g.fill(x + 10, y + 9, x + 12, y + 10, 0x88000000);
        g.text(font, Component.literal("\u00a77" + label), x + 16, y, 0xFF8A90B8);
        g.text(font, Component.literal("\u00a7f" + value), x + 190 - font.width(value), y, 0xFFF2F4FF);
    }

    private void drawSparkles(GuiGraphicsExtractor g, int x, int y, int w, int h, int accent, float secs) {
        for (int i = 0; i < 14; i++) {
            float t = ((secs * 0.6f) + i * 0.37f) % 1.0f;
            float px = x + 8 + ((i * 37) % (w - 16)) + (float) Math.sin(secs * 2 + i) * 4f;
            float py = y + h - t * h;
            int a = (int) (200 * (1 - t));
            if (a <= 0) continue;
            int c = (a << 24) | (accent & 0xFFFFFF);
            int s = (i % 3 == 0) ? 2 : 1;
            g.fill((int) px, (int) py, (int) px + s, (int) py + s, c);
            if (s == 2) {
                g.fill((int) px - 1, (int) py + 1, (int) px + 1, (int) py + 2, c);
                g.fill((int) px + 2, (int) py + 1, (int) px + 4, (int) py + 2, c);
            }
        }
    }

    private static int skillColor(String name) {
        return switch (name) {
            case "Farming" -> 0x5AFF9E;
            case "Mining" -> 0x7CB8FF;
            case "Combat" -> 0xFF5E6C;
            case "Foraging" -> 0x8BFF5A;
            case "Fishing" -> 0x5AE8FF;
            case "Enchanting" -> 0xC77BFF;
            case "Alchemy" -> 0xFF7BE0;
            case "Taming" -> 0xFFB05A;
            default -> 0x7CF0FF;
        };
    }
}