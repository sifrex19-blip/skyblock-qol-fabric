package com.skyblockqol;

import java.util.ArrayList;
import java.util.List;

public class ChatRules {
    /** Master switch controlled by the holographic console toggle. */
    public boolean enabled = true;

    public static class Rule {
        public final String contains;
        public int count;
        public Rule(String contains) { this.contains = contains; }
    }
    private final List<Rule> rules = new ArrayList<>();

    public void addRule(String contains) { rules.add(new Rule(contains)); }

    /** Returns the rule that matched, or null. */
    public Rule onChat(String msg) {
        if (!enabled) return null;
        String s = Util.stripCodes(msg).toLowerCase();
        for (Rule r : rules) {
            if (s.contains(r.contains.toLowerCase())) { r.count++; return r; }
        }
        return null;
    }

    public List<Rule> all() { return rules; }
}