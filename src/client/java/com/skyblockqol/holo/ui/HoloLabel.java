package com.skyblockqol.holo.ui;

import com.skyblockqol.holo.HoloDraw;
import com.skyblockqol.holo.HoloFrame;
import com.skyblockqol.holo.HoloStyle;
import java.util.function.Supplier;

/**
 * A dynamic, non-interactive readout bound to a {@link Supplier} so live data
 * (skill XP, rates, timers...) updates every frame without extra plumbing.
 */
public final class HoloLabel extends HoloComponent {
    private final Supplier<String> text;
    private final int color;

    public HoloLabel(float x, float y, float width, float height, Supplier<String> text, int color) {
        super(x, y, width, height);
        this.text = text;
        this.color = color;
    }

    @Override
    public void render(HoloDraw d, HoloFrame f) {
        String value = text.get();
        if (value == null || value.isEmpty()) return;
        d.text(value, x(), y(), frontZ() + 0.5F, HoloStyle.alpha(color, f.alpha()));
    }
}
