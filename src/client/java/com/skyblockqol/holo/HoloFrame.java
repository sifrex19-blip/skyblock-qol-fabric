package com.skyblockqol.holo;

/**
 * Per-frame values shared by every part of a panel while it renders.
 *
 * @param now   wall clock milliseconds, used for all animation maths
 * @param alpha overall opacity of the panel (open animation x pulse x settings)
 * @param glow  glow intensity multiplier from the console slider (0..2)
 */
public record HoloFrame(long now, float alpha, float glow) {}
