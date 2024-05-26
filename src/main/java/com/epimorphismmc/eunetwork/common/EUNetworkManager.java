package com.epimorphismmc.eunetwork.common;

import com.epimorphismmc.eunetwork.EUNet;
import com.epimorphismmc.eunetwork.api.EUNetValues;
import com.epimorphismmc.eunetwork.api.IEUNetwork;
import com.epimorphismmc.eunetwork.api.IEUNetworkFactory;
import com.epimorphismmc.eunetwork.api.IEUNetworkManager;
import com.epimorphismmc.eunetwork.config.EUNetConfigHolder;
import com.epimorphismmc.eunetwork.network.eunetwork.MessageHandler;
import com.epimorphismmc.monomorphism.data.worlddata.MOSavedData;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.epimorphismmc.eunetwork.EUNet.network;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EUNetworkManager extends MOSavedData implements IEUNetworkManager {

    public static final String NETWORK_DATA = EUNet.MODID + "data";

    public static volatile IEUNetworkManager data;

    private static final String NETWORKS = "networks";

    private static final String UNIQUE_ID = "uniqueID";

    private static final Map<ResourceLocation, IEUNetworkFactory<? extends EUNetwork>> factories = new HashMap<>();

    private final Int2ObjectMap<EUNetwork> networks = new Int2ObjectOpenHashMap<>();

    private int uniqueID = 0;

    public EUNetworkManager() {/**/}

    public EUNetworkManager(@NotNull CompoundTag tag) {
        read(tag);
    }

    @NotNull
    public static IEUNetworkManager getInstance() {
        if (EUNetworkManager.data == null) {
            ServerLevel level = ServerLifecycleHooks.getCurrentServer().overworld();
            EUNetworkManager.data = level.getDataStorage()
                    .computeIfAbsent(EUNetworkManager::new, EUNetworkManager::new, NETWORK_DATA);
            EUNet.logger().debug("EUNetworkData has been successfully loaded");
        }
        return EUNetworkManager.data;
    }

    // called when the server instance changed, e.g. switching single player saves
    public static void release() {
        if (data != null) {
            data = null;
            EUNet.logger().debug("EUNetworkData has been unloaded");
        }
    }

    @Override
    public Collection<EUNetwork> getAllNetworks() {
        return networks.values();
    }

    @Override
    public @Nullable EUNetwork getNetwork(int id) {
        return networks.get(id);
    }

    @Override
    public Collection<EUNetwork> getNetworkByUUID(UUID uuid) {
        return networks.values().stream()
                .filter(n -> n.getOwner().equals(uuid))
                .collect(Collectors.toSet());
    }

    public EUNetwork createNetwork(@NotNull Player creator, @NotNull String name) {
        final int max = EUNetConfigHolder.INSTANCE.maximumPerPlayer;
        if (max != -1) {
            if (max <= 0) {
                return null;
            }
            final UUID uuid = creator.getUUID();
            int i = 0;
            for (var n : networks.values()) {
                if (n.getOwner().equals(uuid) && ++i >= max) {
                    return null;
                }
            }
        }
        do {
            uniqueID++;
        } while (networks.containsKey(uniqueID));

        final ServerEUNetwork network = new ServerEUNetwork(uniqueID, name, creator);

        networks.put(network.getId(), network);
        network().sendToAll(MessageHandler.updateNetwork(network, EUNetValues.NBT_NET_BASIC));
        return network;
    }

    @Override
    public void deleteNetwork(@NotNull EUNetwork network) {
        if (networks.remove(network.getId()) == network) {
            network.onDelete();
            MessageHandler.deleteNetwork(network.getId());
        }
    }

    public static void registerFactory(IEUNetworkFactory<? extends IEUNetwork> factory) {
        factories.put(factory.getType(), factory);
    }

    private void read(@NotNull CompoundTag compound) {
        this.uniqueID = compound.getInt(UNIQUE_ID);
        ListTag list = compound.getList(NETWORKS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            var tag = list.getCompound(i);
            if (tag.contains("type", Tag.TAG_STRING)) {
                var type = ResourceLocation.tryParse(tag.getString("type"));
                var factory = factories.get(type);
                if (factory != null) {
                    var network = factory.createEUNetwork(list.getCompound(i), EUNetValues.NBT_SAVE_ALL);
                    if (network.getId() > 0) {
                        this.networks.put(network.getId(), network);
                    }
                }
            }
        }

    }

    @NotNull
    @Override
    public CompoundTag save(@NotNull CompoundTag compound) {
        compound.putInt(UNIQUE_ID, uniqueID);

        ListTag list = new ListTag();
        for (EUNetwork network : networks.values()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("type", network.getFactory().getType().toString());
            network.serializeNBT(tag, EUNetValues.NBT_SAVE_ALL);
            list.add(tag);
        }
        compound.put(NETWORKS, list);
        return compound;
    }
}
