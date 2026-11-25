package com.skyblockqol.holo.ui;

import com.skyblockqol.holo.HoloDraw;
import com.skyblockqol.holo.HoloFrame;
import com.skyblockqol.holo.HoloStyle;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoubleSupplier;

/**
 * A draggable 3D slider: grooved track, glowing fill and an extruded knob.
 * The crosshair both sets the value on click and drags it while held.
 */
public final class HoloSlider extends HoloComponent {
    private final String label;
    private final float min;
    private final float max;
    private final DoubleSupplier getter;
    private final DoubleConsumer setter;
    private final DoubleFunction<String> format;

    public HoloSlider(float x, float y, float width, float height, String label,
                      float min, float max, DoubleSupplier getter, DoubleConsumer setter,
                      DoubleFunction<String> format) {
        super(x, y, width, height);
        this.label = label;
        this.min = min;
        this.max = max;
        this.getter = getter;
        this.setter = setter;
        this.format = format;
    }

    @Override
    public boolean onClick(float localX, float localY) {
        playClick();
        setFromLocal(localX);
        return true;
    }

    @Override
    public boolean wantsDrag() {
        return true;
    }

    @Override
    public void onDrag(float localX, float localY) {
        setFromLocal(localX);
    }

    private void setFromLocal(float localX) {
        float trackW = width() - 6.0F;
        float rel = (localX - (x() - trackW * 0.5F)) / trackW;
        setter.accept(min + clamp01(rel) * (max - min));
    }

    private float fraction() {
        float span = max - min;
        if (span <= 1.0E-6F) return 0.0F;
        return clamp01(((float) getter.getAsDouble() - min) / span);
    }

    @Override
    public void render(HoloDraw d, HoloFrame f) {
        float a = f.alpha();
        float half = frontZ();
        float t = fraction();
        float value = min + t * (max - min);
        float trackW = width() - 6.0F;
        float trackCy = y() + 4.5F;

        d.text(label + "  " + format.apply(value), x(), y() - 5.5F, half + 0.6F,
            HoloStyle.alpha(HoloStyle.LABEL, a));

        d.fill(x(), trackCy, trackW, 4.5F, half + 0.15F, 2.2F,
            HoloStyle.alpha(HoloStyle.GROOVE, a));

        float fillW = trackW * t;
        if (fillW > 1.0F) {
            int fill = f.glow() <= 1.0F
                ? HoloStyle.mix(HoloStyle.FILL_DIM, HoloStyle.FILL, f.glow())
                : HoloStyle.mix(HoloStyle.FILL, 0xFFFFFFFF, (f.glow() - 1.0F) * 0.6F);
            d.fill(x() - trackW * 0.5F + fillW * 0.5F, trackCy, fillW, 4.5F, half + 0.35F, 2.2F,
                HoloStyle.alpha(fill, a));
        }

        float knobCx = x() - trackW * 0.5F + fillW;
        d.slab(knobCx, trackCy, 6.5F, 11.0F, 3.2F, half + 0.2F, half + 1.9F,
            HoloStyle.alpha(HoloStyle.KNOB_ON, a),
            HoloStyle.alpha(HoloStyle.SIDE, a),
            0);
    }
}
