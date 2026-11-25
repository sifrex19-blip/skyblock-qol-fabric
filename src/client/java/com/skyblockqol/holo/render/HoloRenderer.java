package com.skyblockqol.holo.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.skyblockqol.SkyblockQolClient;
import com.skyblockqol.holo.HoloFrame;
import com.skyblockqol.holo.HoloManager;
import com.skyblockqol.holo.HoloStyle;
import com.skyblockqol.holo.ui.HoloPanel;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.world.phys.Vec3;

/**
 * Bridges the holo UI into Minecraft's world rendering.
 *
 * <p>Every visible panel is submitted during {@code COLLECT_SUBMITS} as world
 * geometry: it is translated to its world anchor, rotated so the front face
 * points at the player and scaled from panel pixels to blocks. Surfaces go
 * through {@link HoloGeometry}, labels through {@link HoloText}; both share the
 * exact same panel code via {@link com.skyblockqol.holo.HoloDraw}.</p>
 *
 * <p>Panels are drawn back-to-front so translucent blending stays correct when
 * several panels overlap.</p>
 */
public final class HoloRenderer {
    private static final RenderType SURFACE = RenderTypes.entityTranslucent(HoloStyle.TEXTURE);
    private static boolean textureReady;

    private HoloRenderer() {}

    /** Subscribes to the level render pipeline. Safe to call during mod init. */
    public static void register() {
        LevelRenderEvents.COLLECT_SUBMITS.register(HoloRenderer::collect);
    }

    private static void collect(LevelRenderContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        HoloManager manager = SkyblockQolClient.HOLO;
        Vec3 camera = ctx.levelState().cameraRenderState.pos;
        List<HoloPanel> panels = manager.renderList(camera);
        if (panels.isEmpty()) return;

        if (!textureReady) {
            mc.getTextureManager().registerAndLoad(HoloStyle.TEXTURE, new SimpleTexture(HoloStyle.TEXTURE));
            textureReady = true;
        }

        SubmitNodeCollector collector = ctx.submitNodeCollector();
        PoseStack poseStack = ctx.poseStack();
        long now = System.currentTimeMillis();

        for (HoloPanel panel : panels) {
            float open = panel.openFactor(now);
            float alpha = panel.alpha(now) * manager.opacity;
            if (open <= 0.002F || alpha <= 0.02F) continue;

            HoloFrame frame = new HoloFrame(now, alpha, manager.glow);

            poseStack.pushPose();
            poseStack.translate(panel.x() - camera.x, panel.y() - camera.y, panel.z() - camera.z);
            // Panel yaw follows entity conventions (front normal = (-sin, 0, cos)),
            // so the geometry rotation is the negated yaw.
            poseStack.rotateDegrees(Axis.YP, -panel.yaw());
            float scale = HoloStyle.SCALE * open;
            poseStack.scale(scale, -scale, scale);

            collector.submitCustomGeometry(poseStack, SURFACE, (pose, vertices) ->
                panel.render(new HoloGeometry(pose, vertices, panel.width(), panel.height()), frame));
            panel.render(new HoloText(collector, poseStack, mc.font), frame);

            poseStack.popPose();
        }
    }
}
