package com.skyblockqol.holo;

import net.minecraft.resources.Identifier;

/**
 * Shared constants for the holographic panel system: sizes, timing and palette.
 *
 * <p>Panel space is pixel based with the origin at the panel centre, +X to the
 * right, +Y <b>down</b> and +Z coming out of the front face. {@link #SCALE}
 * converts panel pixels to world blocks.</p>
 */
public final class HoloStyle {
    private HoloStyle() {}

    /** Blocks per panel pixel. */
    public static final float SCALE = 0.016F;
    /** Panel thickness in pixels. */
    public static final float DEPTH = 3.0F;
    /** Body corner radius in pixels. */
    public static final float CORNER = 6.0F;
    /** Fullbright light value (block 15 / sky 15). */
    public static final int FULLBRIGHT = 15728880;

    /** Open/close animation length in milliseconds. */
    public static final long ANIM_MS = 260;
    /** Hover animation length in milliseconds. */
    public static final long HOVER_MS = 130;
    /** Toggle knob slide animation length in milliseconds. */
    public static final long TOGGLE_MS = 170;

    // ------------------------------------------------------------------ palette
    public static final int GLASS          = 0x5A0A1626;
    public static final int GLASS_BACK     = 0x46060D18;
    public static final int SIDE           = 0x9913293F;
    public static final int FRAME          = 0xFF7DE9FF;
    public static final int GLOW           = 0x5535E0FF;
    public static final int TITLE_BG       = 0x66173A57;
    public static final int TITLE_FG       = 0xFFCFF6FF;
    public static final int LABEL          = 0xFFA9C6DA;
    public static final int ACCENT         = 0xFF35E0FF;
    public static final int BUTTON_BG      = 0x7A0E2438;
    public static final int BUTTON_BG_HOVER = 0xB035E0FF;
    public static final int BUTTON_FG      = 0xFFDFF9FF;
    public static final int BUTTON_FG_HOVER = 0xFF04121E;
    public static final int BUTTON_SIDE    = 0x991E4A66;
    public static final int TRACK_OFF      = 0x880A1428;
    public static final int TRACK_ON       = 0x991E7A55;
    public static final int KNOB_OFF       = 0xFF8FA6B8;
    public static final int KNOB_ON        = 0xFF7DFFC8;
    public static final int GROOVE         = 0x99050B14;
    public static final int FILL           = 0xFF35E0FF;
    public static final int FILL_DIM       = 0xFF1E6C7E;
    public static final int BAR_EDGE       = 0x997DE9FF;
    public static final int STRIPE         = 0x2E000000;
    public static final int SWEEP          = 0x667DE9FF;

    /** Solid white texture; all colour comes from vertex colours. */
    public static final Identifier TEXTURE =
        Identifier.fromNamespaceAndPath("skyblock-qol", "textures/gui/holo.png");

    /** Scales the alpha channel of an ARGB colour. */
    public static int alpha(int argb, float factor) {
        float f = Math.min(1.0F, Math.max(0.0F, factor));
        int a = (int) (((argb >>> 24) & 0xFF) * f);
        return (a << 24) | (argb & 0x00FFFFFF);
    }

    /** Linear blend between two ARGB colours. */
    public static int mix(int from, int to, float t) {
        float k = Math.min(1.0F, Math.max(0.0F, t));
        int a = chan(from, 24) + (int) ((chan(to, 24) - chan(from, 24)) * k);
        int r = chan(from, 16) + (int) ((chan(to, 16) - chan(from, 16)) * k);
        int g = chan(from, 8) + (int) ((chan(to, 8) - chan(from, 8)) * k);
        int b = chan(from, 0) + (int) ((chan(to, 0) - chan(from, 0)) * k);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int chan(int argb, int shift) {
        return (argb >>> shift) & 0xFF;
    }
}
