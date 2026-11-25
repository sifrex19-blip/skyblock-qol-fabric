package com.skyblockqol.holo.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.skyblockqol.holo.HoloDraw;
import com.skyblockqol.holo.HoloStyle;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * Emits holographic panel quads into a vertex consumer.
 *
 * <p>The renderer has already applied the panel transform (translate to the
 * world anchor, rotate to face the player, scale pixels to blocks with +Y
 * flipped so panel space is y-down), so every method here works purely in
 * panel pixels.</p>
 */
public final class HoloGeometry implements HoloDraw {
    private static final int SEGMENTS_PER_CORNER = 7;

    private final PoseStack.Pose pose;
    private final VertexConsumer vertices;
    private final float panelW;
    private final float panelH;

    public HoloGeometry(PoseStack.Pose pose, VertexConsumer vertices, float panelW, float panelH) {
        this.pose = pose;
        this.vertices = vertices;
        this.panelW = Math.max(1.0F, panelW);
        this.panelH = Math.max(1.0F, panelH);
    }

    // ------------------------------------------------------------------ HoloDraw

    @Override
    public void fill(float cx, float cy, float w, float h, float z, float radius, int argb) {
        if ((argb >>> 24) == 0 || w <= 0.0F || h <= 0.0F) return;
        if (radius < 0.5F) {
            rect(cx - w * 0.5F, cy - h * 0.5F, cx + w * 0.5F, cy + h * 0.5F, z, argb);
            return;
        }
        float[] path = roundedPath(cx, cy, w, h, radius);
        fan(cx, cy, path, z, argb);
    }

    @Override
    public void outline(float cx, float cy, float w, float h, float radius, float z, float thickness, int argb) {
        if ((argb >>> 24) == 0 || w <= thickness * 2.0F || h <= thickness * 2.0F) return;
        float[] outer = roundedPath(cx, cy, w, h, radius);
        float[] inner = roundedPath(cx, cy, w - thickness * 2.0F, h - thickness * 2.0F,
            Math.max(0.0F, radius - thickness));
        int n = outer.length / 2;
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            quad(
                outer[i * 2], outer[i * 2 + 1], z,
                outer[j * 2], outer[j * 2 + 1], z,
                inner[j * 2], inner[j * 2 + 1], z,
                inner[i * 2], inner[i * 2 + 1], z,
                argb);
        }
    }

    @Override
    public void slab(float cx, float cy, float w, float h, float radius,
                     float zBack, float zFront, int front, int side, int back) {
        if (zFront <= zBack) return;
        if ((back >>> 24) != 0) {
            float[] path = roundedPath(cx, cy, w, h, radius);
            fan(cx, cy, path, zBack, back);
        }
        if ((side >>> 24) != 0) {
            float[] path = roundedPath(cx, cy, w, h, radius);
            walls(path, zBack, zFront, side);
        }
        if ((front >>> 24) != 0) {
            float[] path = roundedPath(cx, cy, w, h, radius);
            fan(cx, cy, path, zFront, front);
        }
    }

    @Override
    public void text(String text, float cx, float cy, float z, int argb) {
        // handled by the text pass
    }

    // ------------------------------------------------------------------ geometry

    /** Closed rounded-rectangle outline, clockwise in y-down space. */
    private static float[] roundedPath(float cx, float cy, float w, float h, float r) {
        r = Math.max(0.0F, Math.min(r, Math.min(w, h) * 0.5F));
        int n = SEGMENTS_PER_CORNER * 4;
        float[] pts = new float[n * 2];
        float l = cx - w * 0.5F;
        float rr = cx + w * 0.5F;
        float t = cy - h * 0.5F;
        float b = cy + h * 0.5F;
        // corner centres with start/end sweep angles (degrees)
        float[][] corners = {
            {rr - r, t + r, -90.0F, 0.0F},
            {rr - r, b - r, 0.0F, 90.0F},
            {l + r, b - r, 90.0F, 180.0F},
            {l + r, t + r, 180.0F, 270.0F},
        };
        int idx = 0;
        for (float[] corner : corners) {
            for (int i = 0; i < SEGMENTS_PER_CORNER; i++) {
                float deg = corner[2] + (corner[3] - corner[2]) * i / (SEGMENTS_PER_CORNER - 1);
                double a = Math.toRadians(deg);
                pts[idx++] = corner[0] + r * (float) Math.cos(a);
                pts[idx++] = corner[1] + r * (float) Math.sin(a);
            }
        }
        return pts;
    }

    /** Triangle-fan style fill of a closed path at a constant depth. */
    private void fan(float cx, float cy, float[] path, float z, int argb) {
        int n = path.length / 2;
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            vertex(cx, cy, z, argb);
            vertex(path[i * 2], path[i * 2 + 1], z, argb);
            vertex(path[j * 2], path[j * 2 + 1], z, argb);
            vertex(path[j * 2], path[j * 2 + 1], z, argb); // degenerate closing vertex
        }
    }

    /** Side walls between two depths around a closed path. */
    private void walls(float[] path, float zBack, float zFront, int argb) {
        int n = path.length / 2;
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            quad(
                path[i * 2], path[i * 2 + 1], zBack,
                path[j * 2], path[j * 2 + 1], zBack,
                path[j * 2], path[j * 2 + 1], zFront,
                path[i * 2], path[i * 2 + 1], zFront,
                argb);
        }
    }

    private void rect(float l, float t, float r, float b, float z, int argb) {
        quad(l, t, z, r, t, z, r, b, z, l, b, z, argb);
    }

    private void quad(float ax, float ay, float az,
                      float bx, float by, float bz,
                      float cx, float cy, float cz,
                      float dx, float dy, float dz,
                      int argb) {
        vertex(ax, ay, az, argb);
        vertex(bx, by, bz, argb);
        vertex(cx, cy, cz, argb);
        vertex(dx, dy, dz, argb);
    }

    private void vertex(float x, float y, float z, int argb) {
        vertices.addVertex(pose, x, y, z)
            .setColor(argb)
            .setUv((x + panelW * 0.5F) / panelW, (y + panelH * 0.5F) / panelH)
            .setLight(HoloStyle.FULLBRIGHT)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }
}
