package com.epimorphismmc.eunetwork.integration;

import com.epimorphismmc.eunetwork.api.EUNetValues;
import com.gregtechceu.gtceu.utils.SupplierMemoizer;
import com.lowdragmc.lowdraglib.Platform;

public class EUNetIntegration {
    public static boolean isFTBTeamsLoaded() {
        return SupplierMemoizer.memoize(() -> Platform.isModLoaded(EUNetValues.MODID_FTB_TEAMS)).get();
    }
}
