package com.skyblockqol.holo.ui;

import com.skyblockqol.holo.HoloDraw;
import com.skyblockqol.holo.HoloFrame;
import com.skyblockqol.holo.HoloStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * A floating holographic panel: an anchored slab of world geometry plus a list
 * of {@link HoloComponent}s laid out in panel pixels.
 *
 * <p>Panels own their placement, orientation, open/close animation and render
 * order, but know nothing about how they are submitted to the GPU - that is
 * {@link com.skyblockqol.holo.render.HoloRenderer}'s job.</p>
 */
public class HoloPanel {
    public enum State { CLOSED, OPENING, OPEN, CLOSING }

    private final String title;
    private final float width;
    private final float height;
    private final List<HoloComponent> components = new ArrayList<>();

    private double x;
    private double y;
    private double z;
    private float yaw;
    private boolean followPlayer;
    private float followDistance = 2.4F;
    private String footer;

    private State state = State.CLOSED;
    private long animStart;
    private float animFrom;

    protected HoloPanel(String title, float width, float height) {
        this.title = title;
        this.width = width;
        this.height = height;
    }

    // ---------------------------------------------------------------- geometry

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    /** Entity-style yaw: the front face normal is {@code (-sin, 0, cos)}. */
    public float yaw() {
        return yaw;
    }

    public void setPos(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public double distanceSq(Vec3 point) {
        double dx = x - point.x;
        double dy = y - point.y;
        double dz = z - point.z;
        return dx * dx + dy * dy + dz * dz;
    }

    /** When true the panel glides along in front of the player every tick. */
    public void setFollowPlayer(boolean followPlayer) {
        this.followPlayer = followPlayer;
    }

    public boolean followPlayer() {
        return followPlayer;
    }

    public void setFollowDistance(float followDistance) {
        this.followDistance = followDistance;
    }

    public float followDistance() {
        return followDistance;
    }

    /** Anchors the panel {@code distance} blocks ahead of the player, facing them. */
    public void placeInFrontOf(LocalPlayer player, float distance) {
        Vec3 look = player.getViewVector(1.0F);
        double dx = look.x;
        double dz = look.z;
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 1.0E-4) {
            dx = 0.0;
            dz = 1.0;
        } else {
            dx /= len;
            dz /= len;
        }
        Vec3 eye = player.getEyePosition();
        setPos(eye.x + dx * distance, eye.y - 0.35, eye.z + dz * distance);
        // A panel standing in front of the player faces back along their look vector.
        setYaw(player.getYRot() + 180.0F);
    }

    // ------------------------------------------------------------------ state

    public State state() {
        return state;
    }

    public boolean shouldRender() {
        return state != State.CLOSED;
    }

    public boolean isOpenOrOpening() {
        return state == State.OPEN || state == State.OPENING;
    }

    public void open(long now) {
        if (state == State.OPEN || state == State.OPENING) return;
        animFrom = openFactor(now);
        animStart = now;
        state = State.OPENING;
    }

    public void close(long now) {
        if (state == State.CLOSED || state == State.CLOSING) return;
        animFrom = openFactor(now);
        animStart = now;
        state = State.CLOSING;
    }

    public void toggle(long now) {
        if (isOpenOrOpening()) close(now);
        else open(now);
    }

    /** Advances the open/close animation; called once per client tick. */
    public void tick(long now) {
        if (state == State.OPENING && now - animStart >= HoloStyle.ANIM_MS) {
            state = State.OPEN;
        } else if (state == State.CLOSING && now - animStart >= HoloStyle.ANIM_MS) {
            state = State.CLOSED;
        }
    }

    /** Ease-out cubic progress of the current animation, 0 when closed, 1 when open. */
    public float openFactor(long now) {
        if (state == State.OPEN) return 1.0F;
        if (state == State.CLOSED) return 0.0F;
        float t = Math.min(1.0F, (now - animStart) / (float) HoloStyle.ANIM_MS);
        float eased = 1.0F - (float) Math.pow(1.0 - t, 3.0);
        if (state == State.CLOSING) return animFrom * (1.0F - eased);
        return animFrom + (1.0F - animFrom) * eased;
    }

    /** Panel opacity including a slow holographic pulse. */
    public float alpha(long now) {
        float open = openFactor(now);
        float pulse = 0.94F + 0.06F * (float) Math.sin(now / 420.0);
        return Math.min(1.0F, open * 1.25F) * pulse;
    }

    // ------------------------------------------------------------- components

    protected <T extends HoloComponent> T add(T component) {
        components.add(component);
        return component;
    }

    public List<HoloComponent> components() {
        return Collections.unmodifiableList(components);
    }

    /** Topmost component under the given panel-space point, or {@code null}. */
    public HoloComponent componentAt(float localX, float localY) {
        for (int i = components.size() - 1; i >= 0; i--) {
            HoloComponent component = components.get(i);
            if (component.contains(localX, localY)) return component;
        }
        return null;
    }

    /** Panel-space hit test with a small margin around the glass. */
    public boolean contains(float localX, float localY) {
        float m = 2.5F;
        return localX >= -width * 0.5F - m && localX <= width * 0.5F + m
            && localY >= -height * 0.5F - m && localY <= height * 0.5F + m;
    }

    protected void setFooter(String footer) {
        this.footer = footer;
    }

    // ----------------------------------------------------------------- render

    /**
     * Describes the whole panel against a {@link HoloDraw}. Called once for
     * geometry and once for text; both passes run identical code.
     */
    public void render(HoloDraw d, HoloFrame f) {
        float a = f.alpha();
        float half = HoloStyle.DEPTH * 0.5F;
        float top = -height * 0.5F;

        // main glass slab (front, walls, rear)
        d.slab(0, 0, width, height, HoloStyle.CORNER, -half, half,
            HoloStyle.alpha(HoloStyle.GLASS, a),
            HoloStyle.alpha(HoloStyle.SIDE, a),
            HoloStyle.alpha(HoloStyle.GLASS_BACK, a));

        // layered glow around the frame
        d.outline(0, 0, width + 5.0F, height + 5.0F, HoloStyle.CORNER + 2.5F, half + 0.35F, 2.6F,
            HoloStyle.alpha(HoloStyle.GLOW, a * 0.6F * f.glow()));
        d.outline(0, 0, width + 2.4F, height + 2.4F, HoloStyle.CORNER + 1.2F, half + 0.5F, 1.3F,
            HoloStyle.alpha(HoloStyle.FRAME, a * (0.55F + 0.45F * Math.min(1.0F, f.glow()))));

        // faint CRT stripes + a slow sweeping highlight
        float stripeTop = top + 19.0F;
        float stripeBottom = height * 0.5F - 4.0F;
        float range = Math.max(1.0F, stripeBottom - stripeTop);
        for (float sy = stripeTop; sy < stripeBottom; sy += 6.0F) {
            d.fill(0, sy, width - 8.0F, 1.0F, half + 0.12F, 0.5F,
                HoloStyle.alpha(HoloStyle.STRIPE, a));
        }
        float sweepY = stripeTop + ((f.now() % 1600L) * (range / 1600.0F));
        d.fill(0, sweepY, width - 8.0F, 2.2F, half + 0.16F, 1.0F,
            HoloStyle.alpha(HoloStyle.SWEEP, a * Math.min(1.0F, f.glow())));

        // title bar
        d.fill(0, top + 9.0F, width - 6.0F, 13.0F, half + 0.2F, 4.0F,
            HoloStyle.alpha(HoloStyle.TITLE_BG, a));
        d.fill(0, top + 16.5F, width - 12.0F, 1.0F, half + 0.3F, 0.5F,
            HoloStyle.alpha(HoloStyle.ACCENT, a * 0.7F * f.glow()));
        d.text(title, -4.0F, top + 9.0F, half + 0.7F, HoloStyle.alpha(HoloStyle.TITLE_FG, a));

        if (footer != null) {
            d.text(footer, 0, height * 0.5F - 6.0F, half + 0.7F,
                HoloStyle.alpha(HoloStyle.LABEL, a * 0.85F));
        }

        for (HoloComponent component : components) {
            component.render(d, f);
        }
    }
}
