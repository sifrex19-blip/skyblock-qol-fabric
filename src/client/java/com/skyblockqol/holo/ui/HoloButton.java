package com.skyblockqol.holo.ui;

import com.skyblockqol.holo.HoloDraw;
import com.skyblockqol.holo.HoloFrame;
import com.skyblockqol.holo.HoloStyle;

/**
 * A raised, extruded 3D button. Hovering lifts it further out of the glass and
 * inverts its palette; clicking runs the action and plays a UI click.
 */
public final class HoloButton extends HoloComponent {
    private final String label;
    private final Runnable action;

    public HoloButton(float x, float y, float width, float height, String label, Runnable action) {
        super(x, y, width, height);
        this.label = label;
        this.action = action;
    }

    @Override
    public boolean onClick(float localX, float localY) {
        playClick();
        if (action != null) action.run();
        return true;
    }

    @Override
    public void render(HoloDraw d, HoloFrame f) {
        float a = f.alpha();
        float hov = hover(f.now());
        float half = frontZ();
        float lift = 1.1F + 0.8F * hov;

        int bg = HoloStyle.mix(HoloStyle.BUTTON_BG, HoloStyle.BUTTON_BG_HOVER, hov);
        int fg = HoloStyle.mix(HoloStyle.BUTTON_FG, HoloStyle.BUTTON_FG_HOVER, hov);

        d.slab(x(), y(), width(), height(), 3.0F, half, half + lift,
            HoloStyle.alpha(bg, a),
            HoloStyle.alpha(HoloStyle.BUTTON_SIDE, a),
            0);
        d.outline(x(), y(), width(), height(), 3.0F, half + lift + 0.1F, 1.1F,
            HoloStyle.alpha(HoloStyle.ACCENT, a * (0.55F + 0.45F * hov) * f.glow()));
        d.text(label, x(), y(), half + lift + 0.4F, HoloStyle.alpha(fg, a));
    }
}
