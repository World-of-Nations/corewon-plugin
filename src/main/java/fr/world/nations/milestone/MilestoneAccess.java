package fr.world.nations.milestone;

import lombok.Getter;

public enum MilestoneAccess {
    FIRST(1, 50, false),
    SECOND(2, 50, false),
    THIRD(3, 50, false),
    FOURTH(4, 50, false),
    FIFTH(5, 50, true);

    @Getter
    final int level;
    @Getter
    final boolean expandAccess;
    @Getter
    final int powerReward;

    MilestoneAccess(int level, int powerReward, boolean expandAccess) {
        this.level = level;
        this.powerReward = powerReward;
        this.expandAccess = expandAccess;
    }

    // Returns by default level 1
    public static MilestoneAccess fromLevel(int level) {
        for (MilestoneAccess access : values()) {
            if (access.level == level) return access;
        }
        return MilestoneAccess.FIRST;
    }

    public static int getMinimumLevelForExpand() {
        for (int i = 1; i < values().length + 1; i++) {
            if (fromLevel(i).expandAccess) return i;
        }
        return -1;
    }
}
