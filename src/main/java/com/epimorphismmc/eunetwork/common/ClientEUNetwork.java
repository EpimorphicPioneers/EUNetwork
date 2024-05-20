package com.epimorphismmc.eunetwork.common;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientEUNetwork extends EUNetworkBase {
    public ClientEUNetwork(int ignored) {/**/}

    @Override
    public void onEndServerTick() {
        throw new IllegalStateException();
    }
}
