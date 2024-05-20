package com.epimorphismmc.eunetwork.common;

import com.epimorphismmc.eunetwork.EUNetwork;
import com.epimorphismmc.eunetwork.api.EUNetValues;
import com.epimorphismmc.eunetwork.config.EUNetConfigHolder;
import com.epimorphismmc.eunetwork.network.eunetwork.MessageHandler;
import com.epimorphismmc.monomorphism.data.worlddata.MOSavedData;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.UUID;

import static com.epimorphismmc.eunetwork.EUNetwork.network;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EUNetworkData extends MOSavedData {

    public static final String NETWORK_DATA = EUNetwork.MODID + "data";

    public static volatile EUNetworkData data;

    private static final String NETWORKS = "networks";

    private static final String UNIQUE_ID = "uniqueID";

    private final Int2ObjectMap<EUNetworkBase> networks = new Int2ObjectOpenHashMap<>();

    private int uniqueID = 0;
    @Getter @Setter
    private boolean dirty = false;

    public EUNetworkData() {/**/}

    public EUNetworkData(@NotNull CompoundTag tag) {
        read(tag);
    }

    @NotNull
    public static EUNetworkData getInstance() {
        if (EUNetworkData.data == null) {
            ServerLevel level = ServerLifecycleHooks.getCurrentServer().overworld();
            EUNetworkData.data = level.getDataStorage()
                    .computeIfAbsent(EUNetworkData::new, EUNetworkData::new, NETWORK_DATA);
            EUNetwork.logger().debug("EUNetworkData has been successfully loaded");
        }
        return EUNetworkData.data;
    }

    // called when the server instance changed, e.g. switching single player saves
    public static void release() {
        if (data != null) {
            data = null;
            EUNetwork.logger().debug("EUNetworkData has been unloaded");
        }
    }

    @Nullable
    public static EUNetworkBase getNetwork(int id) {
        return getInstance().networks.get(id);
    }

    @NotNull
    public static Collection<EUNetworkBase> getAllNetworks() {
        return getInstance().networks.values();
    }

    @Nullable
    public EUNetworkBase createNetwork(@NotNull Player creator, @NotNull String name) {
        final int max = EUNetConfigHolder.INSTANCE.maximumPerPlayer;
        if (max != -1) {
            if (max <= 0) {
                return null;
            }
            final UUID uuid = creator.getUUID();
            int i = 0;
            for (var n : networks.values()) {
                if (n.getOwnerUUID().equals(uuid) && ++i >= max) {
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

    public void deleteNetwork(@NotNull EUNetworkBase network) {
        if (networks.remove(network.getId()) == network) {
            network.onDelete();
            MessageHandler.deleteNetwork(network.getId());
        }
    }

    private void read(@NotNull CompoundTag compound) {
        this.uniqueID = compound.getInt(UNIQUE_ID);
        ListTag list = compound.getList(NETWORKS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            ServerEUNetwork network = new ServerEUNetwork();
            network.readCustomTag(list.getCompound(i), EUNetValues.NBT_SAVE_ALL);
            if (network.getId() > 0) {
                this.networks.put(network.getId(), network);
            }
        }

    }

    @NotNull
    @Override
    public CompoundTag save(@NotNull CompoundTag compound) {
        compound.putInt(UNIQUE_ID, uniqueID);

        ListTag list = new ListTag();
        for (EUNetworkBase network : networks.values()) {
            CompoundTag tag = new CompoundTag();
            network.writeCustomTag(tag, EUNetValues.NBT_SAVE_ALL);
            list.add(tag);
        }
        compound.put(NETWORKS, list);
        return compound;
    }
}
