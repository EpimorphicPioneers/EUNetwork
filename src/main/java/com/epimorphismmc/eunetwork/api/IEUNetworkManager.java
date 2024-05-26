package com.epimorphismmc.eunetwork.api;

import com.epimorphismmc.eunetwork.common.EUNetwork;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public interface IEUNetworkManager {
    Collection<EUNetwork> getAllNetworks();
    @Nullable EUNetwork getNetwork(int id);
    Collection<EUNetwork> getNetworkByUUID(UUID uuid);
    default Collection<EUNetwork> getNetworkByPlayer(ServerPlayer player) {
        return getNetworkByUUID(player.getUUID());
    }
    EUNetwork createNetwork(@NotNull Player creator, @NotNull String name, @NotNull ResourceLocation type);
    void deleteNetwork(@NotNull EUNetwork network);
    void setDirty();
}
