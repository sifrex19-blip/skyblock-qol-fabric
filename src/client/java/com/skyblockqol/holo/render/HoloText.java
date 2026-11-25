package com.skyblockqol.holo.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.skyblockqol.holo.HoloDraw;
import com.skyblockqol.holo.HoloStyle;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/**
 * Emits holographic panel text through the level submit collector.
 *
 * <p>Geometry calls are ignored; only {@link #text} produces output, and it
 * pushes the requested depth onto the pose so labels always sit in front of the
 * glass they belong to.</p>
 */
public final class HoloText implements HoloDraw {
    private final SubmitNodeCollector collector;
    private final PoseStack pose;
    private final Font font;

    public HoloText(SubmitNodeCollector collector, PoseStack pose, Font font) {
        this.collector = collector;
        this.pose = pose;
        this.font = font;
    }

    @Override
    public void fill(float cx, float cy, float w, float h, float z, float radius, int argb) {
        // geometry pass only
    }

    @Override
    public void outline(float cx, float cy, float w, float h, float radius, float z, float thickness, int argb) {
        // geometry pass only
    }

    @Override
    public void slab(float cx, float cy, float w, float h, float radius,
                     float zBack, float zFront, int front, int side, int back) {
        // geometry pass only
    }

    @Override
    public void text(String text, float cx, float cy, float z, int argb) {
        if (text == null || text.isEmpty() || (argb >>> 24) == 0) return;
        Component literal = Component.literal(text);
        FormattedCharSequence ordered = literal.getVisualOrderText();
        float x = cx - font.width(text) * 0.5F;
        float y = cy - font.lineHeight * 0.5F + 0.5F;
        pose.pushPose();
        pose.translate(0.0, 0.0, z);
        collector.submitText(pose, x, y, ordered, false, Font.DisplayMode.NORMAL,
            HoloStyle.FULLBRIGHT, argb, 0, 0);
        pose.popPose();
    }
}
