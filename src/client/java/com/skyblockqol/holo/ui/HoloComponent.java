package com.skyblockqol.holo.ui;

import com.skyblockqol.holo.HoloDraw;
import com.skyblockqol.holo.HoloFrame;
import com.skyblockqol.holo.HoloStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

/**
 * Base class for everything that can live on a panel.
 *
 * <p>Components are pure UI logic: they know their bounds in panel pixels, how
 * to react to crosshair interaction and how to describe themselves through
 * {@link HoloDraw}. Hover and state animations are time based, so they stay
 * smooth regardless of frame or tick rate.</p>
 */
public abstract class HoloComponent {
    private final float x;
    private final float y;
    private final float width;
    private final float height;

    private boolean hovered;
    private long hoverChangedAt = System.currentTimeMillis();

    protected HoloComponent(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    /** Panel-space hit test with a small forgiveness margin. */
    public boolean contains(float localX, float localY) {
        float m = 1.5F;
        return localX >= x - width * 0.5F - m && localX <= x + width * 0.5F + m
            && localY >= y - height * 0.5F - m && localY <= y + height * 0.5F + m;
    }

    public final void setHovered(boolean value) {
        if (hovered == value) return;
        hovered = value;
        hoverChangedAt = System.currentTimeMillis();
        onHoverChanged(value);
    }

    public final boolean isHovered() {
        return hovered;
    }

    /** Smooth 0..1 hover ramp used by renderers. */
    public final float hover(long now) {
        float t = Math.min(1.0F, (now - hoverChangedAt) / (float) HoloStyle.HOVER_MS);
        if (!hovered) t = 1.0F - t;
        return t * t * (3.0F - 2.0F * t);
    }

    /** Called whenever the crosshair enters or leaves the component. */
    protected void onHoverChanged(boolean nowHovered) {
        if (nowHovered) playHover();
    }

    /** Handles a crosshair click; return {@code true} when the click was consumed. */
    public boolean onClick(float localX, float localY) {
        return false;
    }

    /** Sliders and similar controls return {@code true} to keep receiving drag events. */
    public boolean wantsDrag() {
        return false;
    }

    public void onDrag(float localX, float localY) {}

    public void onRelease() {}

    /** Describes this component for both the geometry and the text pass. */
    public abstract void render(HoloDraw d, HoloFrame f);

    // ------------------------------------------------------------------ sounds

    protected static void playClick() {
        Minecraft mc = Minecraft.getInstance();
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    protected static void playHover() {
        Minecraft mc = Minecraft.getInstance();
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 0.45F));
    }

    // -------------------------------------------------------- shared render bits

    /** Depth of the panel's front face in pixels. */
    protected static float frontZ() {
        return HoloStyle.DEPTH * 0.5F;
    }

    /** Clamps a value into the 0..1 range. */
    protected static float clamp01(float v) {
        return Math.min(1.0F, Math.max(0.0F, v));
    }
}
