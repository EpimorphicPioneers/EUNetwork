package com.epimorphismmc.eunetwork;

import com.epimorphismmc.eunetwork.common.data.EUNetItems;
import com.epimorphismmc.monomorphism.MOGTAddon;
import com.gregtechceu.gtceu.api.addon.GTAddon;

@GTAddon
public class EUNetGTAddon extends MOGTAddon {
    public EUNetGTAddon() {
        super(EUNet.MODID);
    }

    @Override
    public void initializeAddon() {
        EUNetItems.init();
    }
}
