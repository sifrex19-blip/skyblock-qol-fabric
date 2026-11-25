package com.skyblockqol.holo.ui;

import com.skyblockqol.SkillTracker;
import com.skyblockqol.SkyblockQolClient;
import com.skyblockqol.Util;
import com.skyblockqol.holo.HoloStyle;
import java.util.Locale;

/**
 * World-anchored skill readout. The panel stays where it was opened so the
 * player can walk around it, while the labels re-read the live trackers every
 * frame.
 */
public final class StatsPanel extends HoloPanel {
    public StatsPanel() {
        super("SKILL STATS", 146, 118);
        setFollowPlayer(false);
        setFollowDistance(2.8F);

        SkillTracker tracker = SkyblockQolClient.SKILLS;

        add(new HoloButton(61, -50, 16, 12, "X", () -> SkyblockQolClient.HOLO.toggleStats()));

        add(new HoloLabel(0, -33, 130, 10, () -> skillLine(tracker), HoloStyle.ACCENT));
        add(new HoloLabel(0, -20, 130, 10, () -> xpLine(tracker), HoloStyle.LABEL));
        add(new HoloProgressBar(0, -7, 126, 7, () -> progress(tracker)));
        add(new HoloLabel(0, 6, 130, 10, () -> rateLine(tracker), HoloStyle.LABEL));
        add(new HoloLabel(0, 19, 130, 10, () -> etaLine(tracker), HoloStyle.LABEL));
        add(new HoloLabel(0, 32, 130, 10, () -> wheatLine(tracker), HoloStyle.LABEL));

        add(new HoloToggle(0, 47, 116, 13, "HUD PANEL",
            () -> SkyblockQolClient.HUD_ENABLED,
            enabled -> SkyblockQolClient.HUD_ENABLED = enabled));
    }

    private static SkillTracker.Skill skill(SkillTracker tracker) {
        return tracker.current();
    }

    private static String skillLine(SkillTracker tracker) {
        SkillTracker.Skill sk = skill(tracker);
        if (sk == null) return "NO SKILL DATA YET";
        return (sk.name + "   LV " + sk.level).toUpperCase(Locale.ROOT);
    }

    private static String xpLine(SkillTracker tracker) {
        SkillTracker.Skill sk = skill(tracker);
        if (sk == null || sk.maxXp <= 0) return "XP  -";
        return "XP  " + Util.fmtCompact(sk.xp) + " / " + Util.fmtCompact(sk.maxXp);
    }

    private static float progress(SkillTracker tracker) {
        SkillTracker.Skill sk = skill(tracker);
        return sk == null ? 0.0F : (float) sk.progress;
    }

    private static String rateLine(SkillTracker tracker) {
        SkillTracker.Skill sk = skill(tracker);
        if (sk == null || sk.rate <= 0) return "RATE  ...";
        return "RATE  " + Util.fmtCompact(sk.rate) + "/HR";
    }

    private static String etaLine(SkillTracker tracker) {
        SkillTracker.Skill sk = skill(tracker);
        if (sk == null) return "NEXT LEVEL  ...";
        double eta = sk.etaSeconds();
        if (eta <= 0) return "NEXT LEVEL  ...";
        return "NEXT LEVEL  " + Util.fmtDuration(eta);
    }

    private static String wheatLine(SkillTracker tracker) {
        return "WHEAT  " + Util.fmtCompact(tracker.wheatPerHour) + "/HR";
    }
}
