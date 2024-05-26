package com.epimorphismmc.eunetwork.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientEUNetwork extends EUNetworkBase {
    public ClientEUNetwork(int ignored) {/**/}

    public ClientEUNetwork fromByteBuf(FriendlyByteBuf buf) {
        return null;
    }
}
