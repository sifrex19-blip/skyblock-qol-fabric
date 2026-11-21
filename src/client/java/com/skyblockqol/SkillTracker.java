package com.skyblockqol;

import java.util.ArrayList;
import java.util.List;

/** Tracks Skyblock skill XP from action bar lines and computes live rates. */
public class SkillTracker {
    public static class Skill {
        public final String name;
        public double xp, maxXp;      // from (a/b)
        public double gained;          // total since tracking
        public double rate;            // xp/hr (EMA)
        public long lastGainAt;        // millis
        public double progress;        // 0..1 through level
        public Skill(String name) { this.name = name; }
        public double etaSeconds() {
            if (rate <= 1e-6) return -1;
            double left = Math.max(0, maxXp - xp);
            return left / rate * 3600.0;
        }
    }

    private final List<Skill> skills = new ArrayList<>();
    private Skill current;
    private long lastEventAt;

    // crop counters
    public double wheatCount;
    public double wheatPerHour;
    private long cropStart = System.currentTimeMillis();

    public SkillTracker() {
        for (String n : new String[]{"Farming","Mining","Foraging","Fishing","Combat","Enchanting","Alchemy","Taming","Carpentry","Runecrafting","Social"}) {
            skills.add(new Skill(n));
        }
    }

    /** Parse "+10.3 Farming (1,234/5,000)" (colour codes already stripped). */
    public void onActionBar(String raw) {
        if (raw == null) return;
        String s = Util.stripCodes(raw);
        int plus = s.indexOf('+');
        if (plus < 0) return;
        String rest = s.substring(plus + 1).trim();
        int space = rest.indexOf(' ');
        if (space < 0) return;
        Double gained = Util.parseNum(rest.substring(0, space));
        if (gained == null) return;
        String after = rest.substring(space + 1).trim();
        int sp2 = after.indexOf(' ');
        String skillName = sp2 < 0 ? after : after.substring(0, sp2);
        // strip a trailing "(" if present
        if (skillName.endsWith("(")) skillName = skillName.substring(0, skillName.length() - 1);
        Skill sk = find(skillName);
        if (sk == null) return;

        int lp = s.indexOf('('), rp = s.indexOf(')');
        if (lp >= 0 && rp > lp) {
            String inside = s.substring(lp + 1, rp);
            String[] parts = inside.split("/");
            if (parts.length == 2) {
                Double a = Util.parseNum(parts[0].trim());
                Double b = Util.parseNum(parts[1].trim());
                if (a != null && b != null) { sk.xp = a; sk.maxXp = b; }
            }
        }
        long now = System.currentTimeMillis();
        if (sk.lastGainAt > 0) {
            double dtHr = (now - sk.lastGainAt) / 3600000.0;
            if (dtHr > 0 && dtHr < 0.5) {
                double inst = gained / dtHr;
                sk.rate = sk.rate <= 0 ? inst : sk.rate * 0.8 + inst * 0.2;
            }
        }
        sk.gained += gained;
        sk.progress = sk.maxXp > 0 ? Math.min(1.0, sk.xp / sk.maxXp) : 0;
        sk.lastGainAt = now;
        lastEventAt = now;
        current = sk;
    }

    /** Count crop drops from chat, e.g. "+1 Wheat" or "Wheat +3". */
    public void onChat(String raw) {
        String s = Util.stripCodes(raw);
        String lower = s.toLowerCase();
        if (!lower.contains("wheat")) return;
        for (String tok : s.split("\\s+")) {
            Double n = Util.parseNum(tok);
            if (n != null) { addWheat(n); break; }
        }
        int plus = s.indexOf('+');
        if (plus >= 0) {
            String rest = s.substring(plus + 1).trim();
            int sp = rest.indexOf(' ');
            if (sp > 0) {
                Double n = Util.parseNum(rest.substring(0, sp));
                if (n != null) addWheat(n);
            }
        }
    }

    private void addWheat(double n) {
        wheatCount += n;
        double hours = Math.max(1.0 / 3600.0, (System.currentTimeMillis() - cropStart) / 3600000.0);
        wheatPerHour = wheatCount / hours;
    }

    private Skill find(String name) {
        for (Skill sk : skills) if (sk.name.equalsIgnoreCase(name)) return sk;
        return null;
    }

    public Skill current() { return current; }
    public boolean recentlyActive(long windowMs) {
        return current != null && System.currentTimeMillis() - lastEventAt <= windowMs;
    }
    public List<Skill> all() { return skills; }
}