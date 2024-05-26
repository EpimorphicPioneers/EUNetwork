package com.epimorphismmc.eunetwork.network.eunetwork;

import com.epimorphismmc.eunetwork.EUNet;
import com.epimorphismmc.eunetwork.api.EUNetValues;
import com.epimorphismmc.eunetwork.common.EUNetwork;
import com.epimorphismmc.eunetwork.common.EUNetworkBase;
import com.epimorphismmc.eunetwork.common.EUNetworkManager;
import com.epimorphismmc.eunetwork.network.s2c.SPacketEUNetworkPayload;
import com.epimorphismmc.eunetwork.utils.EUNetUtils;
import com.lowdragmc.lowdraglib.networking.IHandlerContext;
import io.netty.handler.codec.DecoderException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.UUID;

import static com.epimorphismmc.eunetwork.EUNet.network;
import static com.epimorphismmc.eunetwork.utils.MessageUtils.getBuffer;

public class MessageHandler {

    /**
     * C->S message indices, must be sequential, 0-based indexing
     */
    protected static final short
            C2S_CREATE_NETWORK = 0,
            C2S_DELETE_NETWORK = 1,
            C2S_EDIT_MEMBER = 2,
            C2S_EDIT_NETWORK = 3,
            C2S_UPDATE_NETWORK = 4;

    /**
     * S->C message indices, must be sequential, 0-based indexing
     */
    protected static final short
            S2C_RESPONSE = 0,
            S2C_UPDATE_NETWORK = 1,
            S2C_DELETE_NETWORK = 2;

    /* -------------------------------------------------- Sender Methods -------------------------------------------------- */

    /**
     * Response to client.
     *
     * @param token the container id
     * @param key   the request key
     * @param code  the response code
     */
    private static void response(int token, int key, int code, ServerPlayer player) {
        var payload = getBuffer();
        payload.writeByte(token);
        payload.writeShort(key);
        payload.writeByte(code);
        network().sendToPlayer(new SPacketEUNetworkPayload(S2C_RESPONSE, payload), player);
    }

    /**
     * Variation of {@link #updateNetwork(Collection, byte)} that updates only one network.
     */
    @Nonnull
    public static SPacketEUNetworkPayload updateNetwork(EUNetwork network, byte type) {
        var payload = getBuffer();
        payload.writeByte(type);
        payload.writeVarInt(1); // size
        payload.writeVarInt(network.getId());
        final var tag = new CompoundTag();
        network.serializeNBT(tag, type);
        payload.writeNbt(tag);
        return new SPacketEUNetworkPayload(S2C_UPDATE_NETWORK, payload);
    }

    @Nonnull
    public static SPacketEUNetworkPayload updateNetwork(Collection<EUNetwork> networks, byte type) {
        var payload = getBuffer();
        payload.writeByte(type);
        payload.writeVarInt(networks.size());
        for (var network : networks) {
            payload.writeVarInt(network.getId());
            final var tag = new CompoundTag();
            network.serializeNBT(tag, type);
            payload.writeNbt(tag);
        }
        return new SPacketEUNetworkPayload(S2C_UPDATE_NETWORK, payload);
    }

    @Nonnull
    private static SPacketEUNetworkPayload updateNetwork(int[] networkIDs, byte type) {
        var payload = getBuffer();
        payload.writeByte(type);
        payload.writeVarInt(networkIDs.length);
        for (var networkID : networkIDs) {
            payload.writeVarInt(networkID);
            final var tag = new CompoundTag();
            EUNetworkManager.getInstance().getNetwork(networkID).serializeNBT(tag, type);
            payload.writeNbt(tag);
        }
        return new SPacketEUNetworkPayload(S2C_UPDATE_NETWORK, payload);
    }

    /**
     * Notify all clients that a network was deleted.
     */
    public static void deleteNetwork(int id) {
        var payload = getBuffer();
        payload.writeVarInt(id);
        network().sendToAll(new SPacketEUNetworkPayload(S2C_DELETE_NETWORK, payload));
    }

    private static void kick(ServerPlayer p, RuntimeException e) {
        if (p != null && p.server.isDedicatedServer()) {
            p.connection.disconnect(Component.translatable("multiplayer.disconnect.invalid_packet"));
            EUNet.logger().info("Received invalid packet from player {}", p.getGameProfile().getName(), e);
        } else {
            EUNet.logger().info("Received invalid packet", e);
        }
    }

    private static void consume(FriendlyByteBuf payload) {
        if (payload.isReadable()) {
            throw new DecoderException("Payload is not fully consumed");
        }
    }

    /* -------------------------------------------------- Handler Methods -------------------------------------------------- */

    public static void handlerMessage(short messageId, FriendlyByteBuf payload, IHandlerContext handler) {
        switch (messageId) {
            case C2S_CREATE_NETWORK -> onCreateNetwork(payload, handler);
            case C2S_DELETE_NETWORK -> onDeleteNetwork(payload, handler);
            case C2S_EDIT_MEMBER -> onEditMember(payload, handler);
            case C2S_EDIT_NETWORK -> onEditNetwork(payload, handler);
            case C2S_UPDATE_NETWORK -> onUpdateNetwork(payload, handler);
            default -> kick(handler.getPlayer(), new RuntimeException("Unidentified message index " + messageId));
        }
    }

    private static void onCreateNetwork(FriendlyByteBuf payload, IHandlerContext handler) {
        // decode
        final int token = payload.readByte();
        final String name = payload.readUtf(256);

        // validate
        consume(payload);
        if (EUNetUtils.isBadNetworkName(name)) {
            throw new IllegalArgumentException("Invalid network name: " + name);
        }

        final ServerPlayer p = handler.getPlayer();
        if (p == null) {
            return;
        }
        boolean reject = p.containerMenu.containerId != token;
        if (reject) {
            response(token, EUNetValues.REQUEST_CREATE_NETWORK, EUNetValues.RESPONSE_REJECT, p);
            return;
        }
        if (EUNetworkManager.getInstance().createNetwork(p, name) != null) {
            response(token, EUNetValues.REQUEST_CREATE_NETWORK, EUNetValues.RESPONSE_SUCCESS, p);
        } else {
            response(token, EUNetValues.REQUEST_CREATE_NETWORK, EUNetValues.RESPONSE_NO_SPACE, p);
        }
    }

    private static void onDeleteNetwork(FriendlyByteBuf payload, IHandlerContext handler) {
        // decode
        final int token = payload.readByte();
        final int networkID = payload.readVarInt();

        // validate
        consume(payload);

        final ServerPlayer p = handler.getPlayer();
        if (p == null) {
            return;
        }
        final EUNetwork network = EUNetworkManager.getInstance().getNetwork(networkID);

        if (network != null) {
            if (network.getPlayerAccess(p).canDelete()) {
                EUNetworkManager.getInstance().deleteNetwork(network);
                response(token, EUNetValues.REQUEST_DELETE_NETWORK, EUNetValues.RESPONSE_SUCCESS, p);
            } else {
                response(token, EUNetValues.REQUEST_DELETE_NETWORK, EUNetValues.RESPONSE_NO_OWNER, p);
            }
        } else {
            response(token, EUNetValues.REQUEST_DELETE_NETWORK, EUNetValues.RESPONSE_REJECT, p);
        }
    }

    private static void onEditMember(FriendlyByteBuf payload, IHandlerContext handler) {
        // decode
        final int token = payload.readByte();
        final int networkID = payload.readVarInt();
        final UUID targetUUID = payload.readUUID();
        final byte type = payload.readByte();

        // validate
        consume(payload);

        final ServerPlayer p = handler.getPlayer();
        if (p == null) {
            return;
        }
        final EUNetwork network = EUNetworkManager.getInstance().getNetwork(networkID);
        boolean reject = true;
        if (reject) {
            response(token, EUNetValues.REQUEST_EDIT_MEMBER, EUNetValues.RESPONSE_REJECT, p);
            return;
        }

        int code = network.changeMembership(p, targetUUID, type);
        if (code == EUNetValues.RESPONSE_SUCCESS) {
            network().sendToPlayer(updateNetwork(network, EUNetValues.NBT_NET_MEMBERS), p);
        }
        response(token, EUNetValues.REQUEST_EDIT_MEMBER, code, p);
    }

    private static void onEditNetwork(FriendlyByteBuf payload, IHandlerContext handler) {
        // decode
        final int token = payload.readByte();
        final int networkID = payload.readVarInt();
        final String name = payload.readUtf(256);

        // validate
        consume(payload);
        if (EUNetUtils.isBadNetworkName(name)) {
            throw new IllegalArgumentException("Invalid network name: " + name);
        }

        final ServerPlayer p = handler.getPlayer();
        if (p == null) {
            return;
        }
        final EUNetwork network = EUNetworkManager.getInstance().getNetwork(networkID);
        boolean reject = true;
        if (reject) {
            response(token, EUNetValues.REQUEST_EDIT_NETWORK, EUNetValues.RESPONSE_REJECT, p);
            return;
        }
        if (network.getPlayerAccess(p).canEdit()) {
            boolean changed = network.setName(name);
            if (changed) {
                network().sendToAll(updateNetwork(network, EUNetValues.NBT_NET_BASIC));
            }
            response(token, EUNetValues.REQUEST_EDIT_NETWORK, EUNetValues.RESPONSE_SUCCESS, p);
        } else {
            response(token, EUNetValues.REQUEST_EDIT_NETWORK, EUNetValues.RESPONSE_NO_ADMIN, p);
        }
    }

    private static void onUpdateNetwork(FriendlyByteBuf payload, IHandlerContext handler) {
        // decode
        final int token = payload.readByte();
        final int size = payload.readVarInt();
        if (size <= 0) {
            throw new IllegalArgumentException();
        }
        final int[] networkIDs = new int[size];
        for (int i = 0; i < size; i++) {
            networkIDs[i] = payload.readVarInt();
        }
        final byte type = payload.readByte();

        // validate
        consume(payload);

        final ServerPlayer p = handler.getPlayer();
        if (p == null) {
            return;
        }
        boolean reject = true;
        if (reject) {
            response(token, EUNetValues.REQUEST_UPDATE_NETWORK, EUNetValues.RESPONSE_REJECT, p);
        } else {
            // this packet always triggers an event, so no response
            network().sendToPlayer(updateNetwork(networkIDs, type), p);
        }
    }

}
