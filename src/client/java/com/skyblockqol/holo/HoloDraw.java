package com.skyblockqol.holo;

/**
 * Surface that panels and components draw against.
 *
 * <p>The renderer implements this interface twice per panel per frame: once to
 * emit world geometry and once to emit text. Panel and component code therefore
 * never has to know about submission passes - it simply describes its visuals
 * and both passes pick out the parts they own.</p>
 *
 * <p>All coordinates are panel pixels (origin at the panel centre, +Y down);
 * {@code z} is the depth in pixels, positive towards the viewer.</p>
 */
public interface HoloDraw {

    /** Filled, optionally rounded rectangle centred at {@code cx}/{@code cy}. */
    void fill(float cx, float cy, float w, float h, float z, float radius, int argb);

    /** Hollow frame of the given thickness around a rounded rectangle. */
    void outline(float cx, float cy, float w, float h, float radius, float z, float thickness, int argb);

    /**
     * Rounded slab extruded between two depths. Produces the back face, the side
     * walls and the front face so buttons feel like solid objects.
     *
     * <p>Pass an alpha of {@code 0} for {@code back} when the slab sits directly
     * on another surface and the rear face would be hidden.</p>
     */
    void slab(float cx, float cy, float w, float h, float radius, float zBack, float zFront,
              int front, int side, int back);

    /** Centre-aligned label at the given depth (no-op while emitting geometry). */
    void text(String text, float cx, float cy, float z, int argb);
}
