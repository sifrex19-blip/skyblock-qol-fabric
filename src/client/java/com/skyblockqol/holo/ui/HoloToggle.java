package com.skyblockqol.holo.ui;

import com.skyblockqol.holo.HoloDraw;
import com.skyblockqol.holo.HoloFrame;
import com.skyblockqol.holo.HoloStyle;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * A 3D toggle: recessed track with a knob that slides between off and on.
 * The knob animation is time based so it stays smooth at any frame rate.
 */
public final class HoloToggle extends HoloComponent {
    private static final float TRACK_W = 26.0F;
    private static final float TRACK_H = 11.0F;
    private static final float KNOB = 9.0F;

    private final String label;
    private final BooleanSupplier getter;
    private final Consumer<Boolean> setter;

    private boolean shown;
    private float animFrom;
    private long animStart;

    public HoloToggle(float x, float y, float width, float height, String label,
                      BooleanSupplier getter, Consumer<Boolean> setter) {
        super(x, y, width, height);
        this.label = label;
        this.getter = getter;
        this.setter = setter;
        this.shown = getter.getAsBoolean();
        this.animFrom = shown ? 1.0F : 0.0F;
        this.animStart = System.currentTimeMillis() - HoloStyle.TOGGLE_MS;
    }

    @Override
    public boolean onClick(float localX, float localY) {
        playClick();
        setter.accept(!getter.getAsBoolean());
        return true;
    }

    @Override
    public void render(HoloDraw d, HoloFrame f) {
        float a = f.alpha();
        float half = frontZ();
        float t = knob(f.now());
        boolean on = getter.getAsBoolean();

        float trackCx = x() + width() * 0.30F;
        d.slab(trackCx, y(), TRACK_W, TRACK_H, 5.5F, half, half + 0.9F,
            HoloStyle.alpha(on ? HoloStyle.TRACK_ON : HoloStyle.TRACK_OFF, a),
            HoloStyle.alpha(HoloStyle.BUTTON_SIDE, a),
            0);

        float knobCx = trackCx - (TRACK_W - KNOB) * 0.5F + t * (TRACK_W - KNOB);
        int knobColor = HoloStyle.mix(HoloStyle.KNOB_OFF, HoloStyle.KNOB_ON, t);
        d.slab(knobCx, y(), KNOB, KNOB, 4.5F, half + 0.9F, half + 2.6F,
            HoloStyle.alpha(knobColor, a),
            HoloStyle.alpha(HoloStyle.SIDE, a),
            0);

        d.text(label, x() - width() * 0.14F, y(), half + 0.6F,
            HoloStyle.alpha(HoloStyle.LABEL, a));
    }

    /** Current knob position (0 off .. 1 on), retargeting when the value changes. */
    private float knob(long now) {
        boolean target = getter.getAsBoolean();
        if (target != shown) {
            animFrom = knobValue(now);
            shown = target;
            animStart = now;
        }
        return knobValue(now);
    }

    private float knobValue(long now) {
        float t = Math.min(1.0F, (now - animStart) / (float) HoloStyle.TOGGLE_MS);
        t = 1.0F - (1.0F - t) * (1.0F - t);
        return animFrom + ((shown ? 1.0F : 0.0F) - animFrom) * t;
    }
}
