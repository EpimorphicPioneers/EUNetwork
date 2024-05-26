package com.epimorphismmc.eunetwork.api;

import com.epimorphismmc.eunetwork.common.EUNetwork;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public interface IEUNetworkFactory<T extends EUNetwork> {
    T createEUNetwork(CompoundTag tag, byte type);

    ResourceLocation getType();
}
