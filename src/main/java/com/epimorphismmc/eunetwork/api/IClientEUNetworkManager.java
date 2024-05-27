package com.epimorphismmc.eunetwork.api;

import com.epimorphismmc.eunetwork.common.ClientEUNetwork;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public interface IClientEUNetworkManager {
    Collection<ClientEUNetwork> getAllNetworks();
    @Nullable IEUNetwork getNetwork(int id);
    Collection<ClientEUNetwork> getNetworkByUUID(UUID uuid);
    default Collection<ClientEUNetwork> getNetworkByPlayer(ServerPlayer player) {
        return getNetworkByUUID(player.getUUID());
    }
}
