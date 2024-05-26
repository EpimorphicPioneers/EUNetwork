package com.epimorphismmc.eunetwork.api;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public interface IClientEUNetworkManager {
    Collection<IEUNetwork> getAllNetworks();
    @Nullable IEUNetwork getNetwork(int id);
    Collection<IEUNetwork> getNetworkByUUID(UUID uuid);
    default Collection<IEUNetwork> getNetworkByPlayer(ServerPlayer player) {
        return getNetworkByUUID(player.getUUID());
    }
}
