package com.epimorphismmc.eunetwork.integration.ftbteams;

import com.epimorphismmc.eunetwork.api.AccessLevel;
import dev.ftb.mods.ftbteams.api.TeamRank;
import org.jetbrains.annotations.NotNull;

public class TeamUtils {

    public static AccessLevel rankToAccessLevel(@NotNull TeamRank rank) {
        if (rank == TeamRank.OWNER) return AccessLevel.OWNER;
        if (rank.isMemberOrBetter()) return AccessLevel.USER;
        return AccessLevel.BLOCKED;
    }

}
