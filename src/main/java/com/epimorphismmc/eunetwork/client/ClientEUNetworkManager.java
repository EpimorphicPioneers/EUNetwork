package com.epimorphismmc.eunetwork.client;

import com.epimorphismmc.eunetwork.EUNet;
import com.epimorphismmc.eunetwork.api.IClientEUNetworkManager;
import com.epimorphismmc.eunetwork.api.IEUNetwork;
import com.epimorphismmc.eunetwork.common.ClientEUNetwork;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

/**
 * Main thread only.
 */
public final class ClientEUNetworkManager implements IClientEUNetworkManager {

    private static final ClientEUNetworkManager INSTANCE = new ClientEUNetworkManager();

    private static final int MAX_RECENT_PASSWORD_COUNT = 5;

    private final Int2ObjectOpenHashMap<ClientEUNetwork> sNetworks =
            new Int2ObjectOpenHashMap<>();

    private ClientEUNetworkManager() {
    }

    public static ClientEUNetworkManager getInstance() {
        return INSTANCE;
    }

    /**
     * Release buffers and view models.
     */
    public void release() {
        sNetworks.clear();
        sNetworks.trim(); // rehash
        EUNet.logger().info("Released client EUNetworks cache");
    }

    /**
     * Cleanup members and connections cache.
     */
    public void cleanup() {
        sNetworks.values().forEach(IEUNetwork::onDelete);
    }

    public void updateNetwork(@Nonnull Int2ObjectMap<CompoundTag> map, byte type) {
        for (var e : map.int2ObjectEntrySet()) {
            sNetworks.computeIfAbsent(e.getIntKey(), ClientEUNetwork::new)
                    .fromNetwork(e.getValue(), type);
        }
    }

    @Nullable
    public IEUNetwork getNetwork(int id) {
        return sNetworks.get(id);
    }

    @Override
    public Collection<ClientEUNetwork> getNetworkByUUID(UUID uuid) {
        return null;
    }

    @Nonnull
    public Collection<ClientEUNetwork> getAllNetworks() {
        return sNetworks.values();
    }

    public void deleteNetwork(int id) {
        sNetworks.remove(id);
    }
}
