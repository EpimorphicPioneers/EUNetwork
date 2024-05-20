package com.epimorphismmc.eunetwork.network.c2s;

import com.epimorphismmc.eunetwork.network.eunetwork.MessageHandler;
import com.lowdragmc.lowdraglib.networking.IHandlerContext;
import com.lowdragmc.lowdraglib.networking.IPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.NoArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;

@NoArgsConstructor
public class CPacketEUNetworkPayload implements IPacket {

    private short messageId;
    private FriendlyByteBuf payload;

    public CPacketEUNetworkPayload(short messageId, FriendlyByteBuf payload) {
        this.messageId = messageId;
        this.payload = payload;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(payload.readableBytes());
        buf.writeBytes(payload);

        buf.writeShort(messageId);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        ByteBuf directSliceBuffer = buf.readBytes(buf.readVarInt());
        ByteBuf copiedDataBuffer = Unpooled.copiedBuffer(directSliceBuffer);
        directSliceBuffer.release();
        this.payload = new FriendlyByteBuf(copiedDataBuffer);

        this.messageId = buf.readShort();
    }

    @Override
    public void execute(IHandlerContext handler) {
        MessageHandler.handlerMessage(messageId, payload, handler);
    }

}
