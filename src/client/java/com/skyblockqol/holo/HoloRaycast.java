package com.skyblockqol.holo;

import com.skyblockqol.holo.ui.HoloComponent;
import com.skyblockqol.holo.ui.HoloPanel;
import java.util.List;
import net.minecraft.world.phys.Vec3;

/**
 * Crosshair ray casting against the floating panels.
 *
 * <p>The panel transform is known analytically (anchor + yaw + animated uniform
 * scale), so instead of inverting matrices the ray is intersected with the
 * panel's front plane and the hit point is projected back into panel pixels.</p>
 */
public final class HoloRaycast {
    private HoloRaycast() {}

    /** A resolved crosshair hit. */
    public record Hit(HoloPanel panel, HoloComponent component, double distance, float localX, float localY) {}

    /**
     * Picks the nearest panel (and component) under the ray.
     *
     * @param panels panels to test
     * @param eye    ray origin (camera position)
     * @param dir    normalised ray direction
     * @param now    wall clock milliseconds, for the open animation scale
     */
    public static Hit pick(List<HoloPanel> panels, Vec3 eye, Vec3 dir, long now) {
        Hit best = null;
        for (HoloPanel panel : panels) {
            if (!panel.shouldRender()) continue;
            Hit hit = rayPanel(panel, eye, dir, now);
            if (hit != null && (best == null || hit.distance() < best.distance())) {
                best = hit;
            }
        }
        return best;
    }

    private static Hit rayPanel(HoloPanel panel, Vec3 eye, Vec3 dir, long now) {
        float scale = HoloStyle.SCALE * panel.openFactor(now);
        if (scale <= 1.0E-5F) return null;

        double yaw = Math.toRadians(panel.yaw());
        // outward normal of the front face for an entity-style yaw
        double nx = -Math.sin(yaw);
        double nz = Math.cos(yaw);
        double denom = dir.x * nx + dir.z * nz;
        if (denom >= -1.0E-6) return null; // ray points along/away from the front face

        double plane = HoloStyle.DEPTH * 0.5 * scale;
        double cx = panel.x() - eye.x;
        double cy = panel.y() - eye.y;
        double cz = panel.z() - eye.z;
        double t = (plane + (cx * nx + cz * nz)) / denom;
        if (t <= 0.0) return null;

        double vx = eye.x + dir.x * t - panel.x();
        double vy = eye.y + dir.y * t - panel.y();
        double vz = eye.z + dir.z * t - panel.z();

        // right vector of the panel: (cos, 0, sin)
        double rx = Math.cos(yaw);
        double rz = Math.sin(yaw);
        float localX = (float) ((vx * rx + vz * rz) / scale);
        float localY = (float) (-vy / scale);

        if (!panel.contains(localX, localY)) return null;
        return new Hit(panel, panel.componentAt(localX, localY), t, localX, localY);
    }
}
