package com.skyblockqol;

import java.util.ArrayList;
import java.util.List;

public class SkillTracker {
    public static class Skill {
        public final String name;
        public double xp, maxXp;
        public double rate; // xp/hr
        public long lastGain;
        public Skill(String name) { this.name = name; }
    }

    private final List<Skill> skills = new ArrayList<>();
    private Skill current;

    public SkillTracker() {
        for (String n : new String[]{"Farming","Mining","Foraging","Fishing","Combat","Enchanting","Alchemy","Taming","Carpentry","Runecrafting","Social"}) {
            skills.add(new Skill(n));
        }
    }

    /** Feed an action bar message like "+10.3 Mining (1,234/5,000)". */
    public void onActionBar(String raw) {
        String s = Util.stripCodes(raw);
        // find "+N Skill (a/b)"
        int plus = s.indexOf('+');
        if (plus < 0) return;
        String rest = s.substring(plus + 1).trim();
        // split number and skill
        int space = rest.indexOf(' ');
        if (space < 0) return;
        Double gained = Util.parseNum(rest.substring(0, space));
        if (gained == null) return;
        String after = rest.substring(space + 1);
        // skill name is next token
        int sp2 = after.indexOf(' ');
        String skillName = sp2 < 0 ? after : after.substring(0, sp2);
        Skill sk = find(skillName);
        if (sk == null) return;
        // parse (a/b)
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
        if (sk.lastGain > 0) {
            double dt = (now - sk.lastGain) / 1000.0 / 3600.0;
            if (dt > 0) sk.rate = gained / dt;
        }
        sk.lastGain = now;
        current = sk;
    }

    private Skill find(String name) {
        for (Skill sk : skills) if (sk.name.equalsIgnoreCase(name)) return sk;
        return null;
    }

    public Skill current() { return current; }
    public List<Skill> all() { return skills; }
}
