package com.epimorphismmc.eunetwork.utils;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

public class MessageUtils {

    public static FriendlyByteBuf getBuffer() {
        return new FriendlyByteBuf(Unpooled.buffer());
    }

}
