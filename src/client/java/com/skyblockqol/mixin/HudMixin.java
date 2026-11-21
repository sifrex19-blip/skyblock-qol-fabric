package com.skyblockqol.mixin;

import com.skyblockqol.SkyblockQolClient;
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

/** Draws the Skyblock QoL 3D-style HUD after the vanilla HUD is extracted. */
@Mixin(Hud.class)
public class HudMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void skyblockqol$drawHud(GuiGraphicsExtractor g, DeltaTracker delta, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        Font font = mc.font;

        // ---- panel geometry ----
        int x = 6, y = 6, w = 160;
        int h = 96;
        int depth = 3;

        // 3D depth faces (bottom + right), darker
        for (int i = depth; i > 0; i--) {
            int a = 0x60000000;
            g.fill(x + i, y + h - 1, x + w + i, y + h + i, a);       // bottom face
            g.fill(x + w, y + i, x + w + i, y + h + i, a);           // right face
        }

        // glass body + soft outer glow (layered translucent frames)
        for (int i = 4; i > 0; i--) {
            g.fill(x - i, y - i, x + w + i, y + h + i, 0x107CF0FF);
        }
        g.fill(x, y, x + w, y + h, 0xE60B0D17);

        // header wash (gradient) and top accent line
        g.fillGradient(x + 1, y + 1, x + w - 1, y + 22, 0x557CF0FF, 0x00000000);
        g.fill(x + 3, y, x + w - 3, y + 1, 0xFF7CF0FF);
        // left accent bar, cyan to violet
        g.fillGradient(x, y + 3, x + 2, y + h - 3, 0xFF7CF0FF, 0xFFA77BFF);

        // ---- title ----
        g.text(font, Component.literal("\u00a7b\u00a7lSKYBLOCK QoL"), x + 9, y + 7, 0xFFFFFFFF);
        g.text(font, Component.literal("\u00a78v0.1"), x + w - 26, y + 7, 0xFF8A90B8);

        // ---- stat rows with tiny 3D icon blocks ----
        int ry = y + 26;
        ry = row(g, font, x, ry, 0x7CF0FF, "Skills", "0", 0.0f);
        ry = row(g, font, x, ry, 0x5AFF9E, "Purse", "0", 0.0f);
        row(g, font, x, ry, 0xFFB05A, "Timers", "0", 0.0f);

        // ---- progress bar (shimmering gradient with gloss) ----
        int by = y + h - 14;
        g.fill(x + 9, by, x + w - 9, by + 6, 0x662A2F4A);
        g.fillGradient(x + 10, by + 1, x + 80, by + 5, 0xFF7CF0FF, 0xFFA77BFF);
        g.fill(x + 10, by + 1, x + 80, by + 2, 0x66FFFFFF); // gloss line
    }

    private int row(GuiGraphicsExtractor g, Font font, int x, int y, int accent, String label, String value, float frac) {
        // 3D icon: accent block + darker underside
        g.fill(x + 9, y, x + 13, y + 4, 0xFF000000 | accent);
        g.fill(x + 9, y + 4, x + 13, y + 5, 0x88000000);
        g.text(font, Component.literal(label), x + 18, y - 1, 0xFF8A90B8);
        g.text(font, Component.literal(value), x + 150 - font.width(value), y - 1, 0xFFF2F4FF);
        // thin progress under the row
        g.fill(x + 18, y + 10, x + 152, y + 11, 0x552A2F4A);
        g.fill(x + 18, y + 10, x + 18 + (int) (134 * frac), y + 11, 0xFF000000 | accent);
        return y + 18;
    }
}