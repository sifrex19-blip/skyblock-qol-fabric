package com.skyblockqol.holo;

import com.skyblockqol.holo.ui.ConsolePanel;
import com.skyblockqol.holo.ui.HoloComponent;
import com.skyblockqol.holo.ui.HoloPanel;
import com.skyblockqol.holo.ui.StatsPanel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Owns every floating panel plus the crosshair interaction loop.
 *
 * <p>The manager is the only place where input (clicks, drags, hover) meets the
 * UI logic; rendering lives in {@link com.skyblockqol.holo.render.HoloRenderer}
 * and geometry maths in {@link HoloRaycast}. Everything here is client side and
 * sends no packets, so it behaves identically in multiplayer.</p>
 */
public final class HoloManager {
    /** Panel opacity driven by the console slider. */
    public float opacity = 0.9F;
    /** Glow intensity driven by the console slider (0..2). */
    public float glow = 1.0F;

    private final List<HoloPanel> panels = new ArrayList<>();
    private ConsolePanel console;
    private StatsPanel stats;
    private HoloComponent dragging;

    public List<HoloPanel> panels() {
        return Collections.unmodifiableList(panels);
    }

    /** Visible panels ordered far-to-front for correct translucent blending. */
    public List<HoloPanel> renderList(Vec3 camera) {
        List<HoloPanel> out = new ArrayList<>(panels.size());
        for (HoloPanel panel : panels) {
            if (panel.shouldRender()) out.add(panel);
        }
        if (out.size() > 1) {
            out.sort((a, b) -> Double.compare(b.distanceSq(camera), a.distanceSq(camera)));
        }
        return out;
    }

    // ------------------------------------------------------------------- tick

    public void tick(Minecraft mc) {
        long now = System.currentTimeMillis();

        if (mc.player == null) {
            releaseDrag();
            for (HoloPanel panel : panels) panel.tick(now);
            return;
        }

        // Move follow-panels first so the raycast sees fresh positions.
        for (HoloPanel panel : panels) {
            if (panel.followPlayer()) {
                panel.placeInFrontOf(mc.player, panel.followDistance());
            }
            panel.tick(now);
            for (HoloComponent component : panel.components()) component.setHovered(false);
        }

        HoloRaycast.Hit hit = pick(mc);
        if (hit != null && hit.component() != null) hit.component().setHovered(true);

        if (dragging != null) {
            if (!mc.options.keyAttack.isDown()) {
                releaseDrag();
            } else if (hit != null && hit.component() == dragging) {
                dragging.onDrag(hit.localX(), hit.localY());
            }
        }
    }

    private void releaseDrag() {
        if (dragging != null) {
            dragging.onRelease();
            dragging = null;
        }
    }

    // ------------------------------------------------------------- interaction

    /**
     * Resolves the crosshair target, but only when the panel is closer than
     * whatever the vanilla raycast found - that way walking right up to a block
     * still lets you mine through a floating panel.
     */
    public HoloRaycast.Hit pick(Minecraft mc) {
        if (mc.player == null || mc.gui.screen() != null) return null;
        Vec3 eye = mc.gameRenderer.mainCamera().position();
        HoloRaycast.Hit hit = HoloRaycast.pick(panels, eye, mc.player.getViewVector(1.0F), System.currentTimeMillis());
        if (hit == null) return null;

        HitResult world = mc.hitResult;
        if (world != null && world.getType() != HitResult.Type.MISS
            && eye.distanceTo(world.getLocation()) <= hit.distance()) {
            return null;
        }
        return hit;
    }

    /**
     * Called from the input mixin when the player left/right clicks.
     *
     * @return {@code true} when a panel took the click and the world action
     *         (attack/use) must be suppressed
     */
    public boolean tryClick(Minecraft mc, boolean primary) {
        HoloRaycast.Hit hit = pick(mc);
        if (hit == null) return false;
        HoloComponent component = hit.component();
        if (component != null && component.onClick(hit.localX(), hit.localY())
            && primary && component.wantsDrag()) {
            dragging = component;
        }
        return true;
    }

    /** {@code true} while a panel should swallow the held attack action. */
    public boolean isPanelTargeted(Minecraft mc) {
        if (dragging != null) return true;
        return pick(mc) != null;
    }

    // ---------------------------------------------------------------- panels

    public void toggleConsole() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (console == null) {
            console = new ConsolePanel();
            panels.add(console);
        }
        long now = System.currentTimeMillis();
        if (console.isOpenOrOpening()) {
            console.close(now);
        } else {
            console.placeInFrontOf(mc.player, console.followDistance());
            console.open(now);
        }
    }

    public void toggleStats() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (stats == null) {
            stats = new StatsPanel();
            panels.add(stats);
        }
        long now = System.currentTimeMillis();
        if (stats.isOpenOrOpening()) {
            stats.close(now);
        } else {
            stats.placeInFrontOf(mc.player, stats.followDistance());
            stats.open(now);
        }
    }
}
