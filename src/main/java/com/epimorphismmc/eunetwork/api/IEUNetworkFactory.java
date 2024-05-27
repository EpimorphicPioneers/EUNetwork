package com.epimorphismmc.eunetwork.api;

import com.epimorphismmc.eunetwork.common.EUNetwork;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

public interface IEUNetworkFactory<T extends EUNetwork> {

    T createEUNetwork(int id, String name,  @Nonnull Player owner);

    T deserialize(CompoundTag tag, byte type);

    String getType();
}
