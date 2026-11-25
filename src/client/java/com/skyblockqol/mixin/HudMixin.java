package com.skyblockqol.mixin;

import com.skyblockqol.SkyblockQolClient;
import com.skyblockqol.SkillTracker;
import com.skyblockqol.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draws an animated skill panel anchored in the 3D world while XP is earned. */
@Mixin(Hud.class)
public class HudMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void skyblockqol$drawHud(GuiGraphicsExtractor g, DeltaTracker delta, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!SkyblockQolClient.HUD_ENABLED) return;
        SkillTracker tracker = SkyblockQolClient.SKILLS;
        if (!tracker.recentlyActive(5000)) return;

        SkillTracker.Skill sk = tracker.current();
        if (sk == null) return;

        // ---- project a world point in front of + above the player to screen ----
        int[] p = project(mc, 1.8);
        if (p == null) return;
        int x = p[0], y = p[1];
        int w = 200, h = 128, depth = 3;
        int accent = skillColor(sk.name);
        Font font = mc.font;
        float secs = (System.currentTimeMillis() % 100000) / 1000f;

        // scale the panel a little with distance for a 3D feel
        // (kept simple: fixed size, positioned in world)

        // ---- 3D slab depth ----
        for (int i = depth; i > 0; i--) {
            g.fill(x + i, y + h, x + w + i, y + h + i, 0x66000000);
            g.fill(x + w, y + i, x + w + i, y + h + i, 0x66000000);
        }
        // ---- pulsing outer glow ----
        int pulse = (int) (18 + 12 * Math.sin(secs * 3.0));
        for (int i = 5; i > 0; i--) g.fill(x - i, y - i, x + w + i, y + h + i, (pulse << 24) | (accent & 0xFFFFFF));
        // ---- glass body ----
        g.fill(x, y, x + w, y + h, 0xEA0B0D17);
        g.fillGradient(x + 1, y + 1, x + w - 1, y + 24, 0x55FFFFFF | (accent & 0xFFFFFF), 0x00000000);
        g.fill(x + 3, y, x + w - 3, y + 1, 0xFF000000 | (accent & 0xFFFFFF));
        g.fillGradient(x, y + 3, x + 2, y + h - 3, 0xFF000000 | (accent & 0xFFFFFF), 0xFFA77BFF);

        // ---- title, level, percent ----
        g.text(font, Component.literal("\u00a7l" + sk.name.toUpperCase()), x + 10, y + 7, 0xFFFFFFFF);
        int pct = (int) Math.round(sk.progress * 100);
        String right = "Lv " + sk.level + "  " + pct + "%";
        g.text(font, Component.literal(right), x + w - 10 - font.width(right), y + 7, 0xFF000000 | (accent & 0xFFFFFF));

        // ---- progress bar ----
        int by = y + 30;
        g.fill(x + 10, by, x + w - 10, by + 8, 0x662A2F4A);
        int fw = (int) ((w - 20) * sk.progress);
        if (fw > 1) {
            g.fillGradient(x + 10, by + 1, x + 10 + fw, by + 7, 0xFF000000 | (accent & 0xFFFFFF), 0xFFA77BFF);
            g.fill(x + 10, by + 1, x + 10 + fw, by + 2, 0x55FFFFFF);
        }
        int sweep = (int) (((secs * 0.35) % 1.0) * (w - 20));
        if (sweep < fw) g.fill(x + 10 + sweep, by + 1, Math.min(x + 10 + sweep + 8, x + 10 + fw), by + 7, 0x66FFFFFF);

        // ---- stat rows ----
        int ry = y + 44;
        stat(g, font, x, ry, accent, "XP/hr", sk.rate > 0 ? Util.fmtCompact(sk.rate) : "...");
        ry += 14;
        stat(g, font, x, ry, accent, "Next level in", sk.etaSeconds() > 0 ? Util.fmtDuration(sk.etaSeconds()) : "...");
        ry += 14;
        stat(g, font, x, ry, accent, "Wheat/hr", Util.fmtCompact(tracker.wheatPerHour));
        ry += 14;
        stat(g, font, x, ry, accent, "Wheat/s", Util.fmtCompact(tracker.wheatPerHour / 3600.0));

        // ---- sparkles ----
        drawSparkles(g, x, y, w, h, accent, secs);
    }

    /** Project a point `height` blocks above the player, `dist` blocks forward, to screen. */
    private int[] project(Minecraft mc, double height) {
        Camera cam = mc.gameRenderer.mainCamera();
        Vec3 camPos = cam.position();
        Quaternionf rot = cam.rotation();
        Vec3 target = mc.player.position().add(0, height, 0);
        // push it a little forward of the player so it floats ahead
        Vec3 look = mc.player.getViewVector(1.0f);
        target = target.add(look.scale(1.2));
        Vector3f d = new Vector3f((float) (target.x - camPos.x), (float) (target.y - camPos.y), (float) (target.z - camPos.z));
        rot.transformInverse(d);
        if (d.z > -0.1f) return null; // behind camera
        float z = -d.z;
        double fov = mc.options.fov().get();
        double f = 1.0 / Math.tan(Math.toRadians(fov) / 2.0);
        double aspect = (double) mc.getWindow().getGuiScaledWidth() / mc.getWindow().getGuiScaledHeight();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        double sx = (d.x * f / aspect) / z;
        double sy = (d.y * f) / z;
        int px = (int) ((sw / 2.0) * (1 + sx)) - 100;
        int py = (int) ((sh / 2.0) * (1 - sy)) - 64;
        // keep on screen
        px = Math.max(4, Math.min(sw - 204, px));
        py = Math.max(4, Math.min(sh - 132, py));
        return new int[]{px, py};
    }

    private void stat(GuiGraphicsExtractor g, Font font, int x, int y, int accent, String label, String value) {
        g.fill(x + 10, y + 2, x + 12, y + 9, 0xFF000000 | (accent & 0xFFFFFF));
        g.fill(x + 10, y + 9, x + 12, y + 10, 0x88000000);
        g.text(font, Component.literal("\u00a77" + label), x + 16, y, 0xFF8A90B8);
        g.text(font, Component.literal("\u00a7f" + value), x + 190 - font.width(value), y, 0xFFF2F4FF);
    }

    private void drawSparkles(GuiGraphicsExtractor g, int x, int y, int w, int h, int accent, float secs) {
        for (int i = 0; i < 16; i++) {
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