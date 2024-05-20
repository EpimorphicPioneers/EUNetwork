package com.epimorphismmc.eunetwork.client;

import com.epimorphismmc.eunetwork.common.CommonProxy;
import com.epimorphismmc.monomorphism.proxy.base.IClientProxyBase;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy implements IClientProxyBase {
    public ClientProxy() {
        super();
    }

}
