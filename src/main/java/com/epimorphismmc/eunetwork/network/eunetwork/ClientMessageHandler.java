package com.epimorphismmc.eunetwork.network.eunetwork;

import com.epimorphismmc.eunetwork.client.ClientCache;
import com.epimorphismmc.eunetwork.common.EUNetworkBase;
import com.epimorphismmc.eunetwork.network.c2s.CPacketEUNetworkPayload;
import com.epimorphismmc.monomorphism.client.utils.ClientUtils;
import com.lowdragmc.lowdraglib.networking.IHandlerContext;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Collection;
import java.util.UUID;

import static com.epimorphismmc.eunetwork.EUNetwork.network;
import static com.epimorphismmc.eunetwork.network.eunetwork.MessageHandler.*;
import static com.epimorphismmc.eunetwork.utils.MessageUtils.getBuffer;

@OnlyIn(Dist.CLIENT)
public class ClientMessageHandler {


    /* -------------------------------------------------- Sender Methods -------------------------------------------------- */

    /**
     * Request to create a new network.
     *
     * @param token must be valid
     */
    public static void createNetwork(int token, String name) {
        var payload = getBuffer();
        payload.writeByte(token);
        payload.writeUtf(name, 256);
        sendMessage(C2S_CREATE_NETWORK, payload);
    }

    /**
     * Request to delete an existing network.
     *
     * @param token must be valid
     */
    public static void deleteNetwork(int token, EUNetworkBase network) {
        var payload = getBuffer();
        payload.writeByte(token);
        payload.writeVarInt(network.getId());
        sendMessage(C2S_DELETE_NETWORK, payload);
    }

    /**
     * Request the server to update all certain data of a network.
     *
     * @param token a valid token
     */
    public static void updateNetwork(int token, EUNetworkBase network, byte type) {
        var payload = getBuffer();
        payload.writeByte(token);
        payload.writeVarInt(1); // size
        payload.writeVarInt(network.getId());
        payload.writeByte(type);
        sendMessage(C2S_UPDATE_NETWORK, payload);
    }

    /**
     * Request the server to update all certain data of networks.
     *
     * @param token a valid token
     */
    public static void updateNetwork(int token, Collection<EUNetworkBase> networks, byte type) {
        if (networks.isEmpty()) {
            return;
        }
        var payload = getBuffer();
        payload.writeByte(token);
        payload.writeVarInt(networks.size());
        for (var network : networks) {
            payload.writeVarInt(network.getId());
        }
        payload.writeByte(type);
        sendMessage(C2S_UPDATE_NETWORK, payload);
    }

    public static void editMember(int token, EUNetworkBase network, UUID uuid, byte type) {
        var payload = getBuffer();
        payload.writeByte(token);
        payload.writeVarInt(network.getId());
        payload.writeUUID(uuid);
        payload.writeByte(type);
        sendMessage(C2S_EDIT_MEMBER, payload);
    }

    public static void editNetwork(int token, EUNetworkBase network, String name) {
        var payload = getBuffer();
        payload.writeByte(token);
        payload.writeVarInt(network.getId());
        payload.writeUtf(name, 256);
        sendMessage(C2S_EDIT_NETWORK, payload);
    }

    /* -------------------------------------------------- Handler Methods -------------------------------------------------- */

    public static void handlerMessage(short messageId, FriendlyByteBuf payload, IHandlerContext handler) {
        Minecraft minecraft = Minecraft.getInstance();
        switch (messageId) {
            case MessageHandler.S2C_RESPONSE -> onResponse(payload, handler);
            case MessageHandler.S2C_UPDATE_NETWORK -> onUpdateNetwork(payload, handler);
            case MessageHandler.S2C_DELETE_NETWORK -> onDeleteNetwork(payload, handler);
        }
    }

    private static void onResponse(FriendlyByteBuf payload, IHandlerContext handler) {
        final int token = payload.readByte();
        final int key = payload.readShort();
        final int code = payload.readByte();

        LocalPlayer p = ClientUtils.getMC().player;
        if (p == null) {
            return;
        }

    }

    private static void onUpdateNetwork(FriendlyByteBuf payload, IHandlerContext handler) {
        final byte type = payload.readByte();
        final int size = payload.readVarInt();
        final Int2ObjectMap<CompoundTag> map = new Int2ObjectArrayMap<>(size);
        for (int i = 0; i < size; i++) {
            final int id = payload.readVarInt();
            final CompoundTag tag = payload.readNbt();
            assert tag != null;
            map.put(id, tag);
        }

        LocalPlayer p = ClientUtils.getMC().player;
        if (p == null) {
            return;
        }
        ClientCache.updateNetwork(map, type);

    }

    private static void onDeleteNetwork(FriendlyByteBuf payload, IHandlerContext handler) {
        final int id = payload.readVarInt();

        LocalPlayer p = ClientUtils.getMC().player;
        if (p == null) {
            return;
        }
        ClientCache.deleteNetwork(id);

    }

    private static void sendMessage(short messageId, FriendlyByteBuf payload) {
        network().sendToServer(new CPacketEUNetworkPayload(messageId, payload));
    }

}
