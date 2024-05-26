package com.epimorphismmc.eunetwork.client;

import com.epimorphismmc.eunetwork.EUNet;
import com.epimorphismmc.eunetwork.common.ClientEUNetwork;
import com.epimorphismmc.eunetwork.common.EUNetworkBase;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Main thread only.
 */
@OnlyIn(Dist.CLIENT)
public final class ClientCache {

    private static final int MAX_RECENT_PASSWORD_COUNT = 5;

    private static final Int2ObjectOpenHashMap<EUNetworkBase> sNetworks =
            new Int2ObjectOpenHashMap<>();

    private ClientCache() {
    }

    /**
     * Release buffers and view models.
     */
    public static void release() {
        sNetworks.clear();
        sNetworks.trim(); // rehash
        EUNet.logger().info("Released client Flux Networks cache");
    }

    /**
     * Cleanup members and connections cache.
     */
    public static void cleanup() {
        sNetworks.values().forEach(EUNetworkBase::onDelete);
    }

    public static void updateNetwork(@Nonnull Int2ObjectMap<CompoundTag> map, byte type) {
//        for (var e : map.int2ObjectEntrySet()) {
//            sNetworks.computeIfAbsent(e.getIntKey(), ClientEUNetwork::new)
//                    .readCustomTag(e.getValue(), type);
//        }
    }

    @Nullable
    public static EUNetworkBase getNetwork(int id) {
        return sNetworks.get(id);
    }

    @Nonnull
    public static Collection<EUNetworkBase> getAllNetworks() {
        return sNetworks.values();
    }

    public static void deleteNetwork(int id) {
        sNetworks.remove(id);
    }
}
