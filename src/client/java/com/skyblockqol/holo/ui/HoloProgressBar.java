package com.skyblockqol.holo.ui;

import com.skyblockqol.holo.HoloDraw;
import com.skyblockqol.holo.HoloFrame;
import com.skyblockqol.holo.HoloStyle;
import java.util.function.DoubleSupplier;

/**
 * A live progress bar (skill level progress, cooldowns, ...) drawn as a
 * recessed groove with a glowing fill.
 */
public final class HoloProgressBar extends HoloComponent {
    private final DoubleSupplier value;

    public HoloProgressBar(float x, float y, float width, float height, DoubleSupplier value) {
        super(x, y, width, height);
        this.value = value;
    }

    @Override
    public void render(HoloDraw d, HoloFrame f) {
        float a = f.alpha();
        float half = frontZ();
        float t = clamp01((float) value.getAsDouble());

        d.fill(x(), y(), width(), height(), half + 0.15F, 2.5F,
            HoloStyle.alpha(HoloStyle.GROOVE, a));

        float fillW = (width() - 2.0F) * t;
        if (fillW > 1.0F) {
            int fill = f.glow() <= 1.0F
                ? HoloStyle.mix(HoloStyle.FILL_DIM, HoloStyle.FILL, f.glow())
                : HoloStyle.mix(HoloStyle.FILL, 0xFFFFFFFF, (f.glow() - 1.0F) * 0.6F);
            d.fill(x() - (width() - 2.0F) * 0.5F + fillW * 0.5F, y(), fillW, height(),
                half + 0.35F, 2.5F, HoloStyle.alpha(fill, a));
        }

        d.outline(x(), y(), width(), height(), 2.5F, half + 0.55F, 0.8F,
            HoloStyle.alpha(HoloStyle.BAR_EDGE, a * f.glow()));
    }
}
