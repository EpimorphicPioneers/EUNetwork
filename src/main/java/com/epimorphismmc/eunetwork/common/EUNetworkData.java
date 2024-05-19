package com.epimorphismmc.eunetwork.common;//package cn.gtcommunity.epimorphism.common.eunetwork;
//
//import cn.gtcommunity.epimorphism.Epimorphism;
//import cn.gtcommunity.epimorphism.api.data.worlddata.EPSavedData;
//import cn.gtcommunity.epimorphism.api.eunetwork.EUNetConstants;
//import cn.gtcommunity.epimorphism.config.EPConfigHolder;
//import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
//import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
//import net.minecraft.MethodsReturnNonnullByDefault;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.nbt.ListTag;
//import net.minecraft.nbt.Tag;
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.world.entity.player.Player;
//import net.minecraftforge.server.ServerLifecycleHooks;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//import javax.annotation.ParametersAreNonnullByDefault;
//import java.util.Collection;
//import java.util.UUID;
//
//@ParametersAreNonnullByDefault
//@MethodsReturnNonnullByDefault
//public class EUNetworkData extends EPSavedData {
//
//    public static final String NETWORK_DATA = Epimorphism.MOD_ID + "data";
//
//    public static volatile EUNetworkData data;
//
//    private static final String NETWORKS = "networks";
//
//    private static final String UNIQUE_ID = "uniqueID";
//
//    private final Int2ObjectMap<EUNetwork> networks = new Int2ObjectOpenHashMap<>();
//
//    private int uniqueID = 0;
//
//    public EUNetworkData() {/**/}
//
//    public EUNetworkData(@Nonnull CompoundTag tag) {
//        read(tag);
//    }
//
//    @Nonnull
//    public static EUNetworkData getInstance() {
//        if (EUNetworkData.data == null) {
//            ServerLevel level = ServerLifecycleHooks.getCurrentServer().overworld();
//            EUNetworkData.data = level.getDataStorage()
//                    .computeIfAbsent(EUNetworkData::new, EUNetworkData::new, NETWORK_DATA);
//            Epimorphism.LOGGER.debug("EUNetworkData has been successfully loaded");
//        }
//        return EUNetworkData.data;
//    }
//
//    // called when the server instance changed, e.g. switching single player saves
//    public static void release() {
//        if (data != null) {
//            data = null;
//            Epimorphism.LOGGER.debug("EUNetworkData has been unloaded");
//        }
//    }
//
//    @Nonnull
//    public static EUNetwork getNetwork(int id) {
//        return getInstance().networks.getOrDefault(id, EUNetwork.INVALID);
//    }
//
//    @Nonnull
//    public static Collection<EUNetwork> getAllNetworks() {
//        return getInstance().networks.values();
//    }
//
//    @Nullable
//    public EUNetwork createNetwork(@Nonnull Player creator, @Nonnull String name) {
//        final int max = EPConfigHolder.INSTANCE.EUNetworks.maximumPerPlayer;
//        if (max != -1) {
//            if (max <= 0) {
//                return null;
//            }
//            final UUID uuid = creator.getUUID();
//            int i = 0;
//            for (var n : networks.values()) {
//                if (n.getOwnerUUID().equals(uuid) && ++i >= max) {
//                    return null;
//                }
//            }
//        }
//        do {
//            uniqueID++;
//        } while (networks.containsKey(uniqueID));
//
//        final ServerEUNetwork network = new ServerEUNetwork(uniqueID, name, creator);
//
//        networks.put(network.getNetworkID(), network);
//        Channel.get().sendToAll(Messages.updateNetwork(network, EUNetConstants.NBT_NET_BASIC));
//        return network;
//    }
//
//    public void deleteNetwork(@Nonnull EUNetwork network) {
//        if (networks.remove(network.getNetworkID()) == network) {
//            network.onDelete();
//            Messages.deleteNetwork(network.getNetworkID());
//        }
//    }
//
//    @Override
//    public boolean isDirty() {
//        // always dirty as a convenience
//        return true;
//    }
//
//    private void read(@Nonnull CompoundTag compound) {
//        this.uniqueID = compound.getInt(UNIQUE_ID);
//        ListTag list = compound.getList(NETWORKS, Tag.TAG_COMPOUND);
//        for (int i = 0; i < list.size(); i++) {
//            ServerEUNetwork network = new ServerEUNetwork();
//            network.readCustomTag(list.getCompound(i), EUNetConstants.NBT_SAVE_ALL);
//            if (network.getNetworkID() > 0) {
//                this.networks.put(network.getNetworkID(), network);
//            }
//        }
//
//    }
//
//    @Nonnull
//    @Override
//    public CompoundTag save(@Nonnull CompoundTag compound) {
//        compound.putInt(UNIQUE_ID, uniqueID);
//
//        ListTag list = new ListTag();
//        for (EUNetwork network : networks.values()) {
//            CompoundTag tag = new CompoundTag();
//            network.writeCustomTag(tag, EUNetConstants.NBT_SAVE_ALL);
//            list.add(tag);
//        }
//        compound.put(NETWORKS, list);
//        return compound;
//    }
//}
