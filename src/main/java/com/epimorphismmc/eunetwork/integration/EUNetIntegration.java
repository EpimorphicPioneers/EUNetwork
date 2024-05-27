package com.epimorphismmc.eunetwork.integration;

import com.epimorphismmc.eunetwork.api.EUNetValues;
import com.lowdragmc.lowdraglib.Platform;

public class EUNetIntegration {
    public static boolean isFTBTeamsLoaded() {
        return Platform.isModLoaded(EUNetValues.MODID_FTB_TEAMS);
    }
}
