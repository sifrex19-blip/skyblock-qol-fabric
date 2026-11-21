package com.skyblockqol;

public final class Util {
    private Util() {}

    public static String stripCodes(String s) {
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\u00a7' && i + 1 < s.length()) { i++; }
            else out.append(c);
        }
        return out.toString();
    }

    public static Double parseNum(String s) {
        try { return Double.parseDouble(s.replace(",", "")); }
        catch (NumberFormatException e) { return null; }
    }

    public static String fmtDuration(double secs) {
        long s = Math.max(0, Math.round(secs));
        long h = s / 3600, m = (s % 3600) / 60, sec = s % 60;
        if (h > 0) return h + "h " + String.format("%02d", m) + "m";
        if (m > 0) return m + "m " + String.format("%02d", sec) + "s";
        return sec + "s";
    }

    public static String fmtCompact(double n) {
        double a = Math.abs(n);
        if (a >= 1e9) return String.format("%.2fB", n / 1e9);
        if (a >= 1e6) return String.format("%.2fM", n / 1e6);
        if (a >= 1e3) return String.format("%.1fK", n / 1e3);
        return String.format("%.0f", n);
    }
}