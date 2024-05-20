package com.epimorphismmc.eunetwork.utils;

import com.epimorphismmc.eunetwork.common.EUNetworkBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class EPNetUtil {
    public static void writeGlobalPos(@Nonnull CompoundTag tag, @Nonnull GlobalPos pos) {
        BlockPos p = pos.pos();
        tag.putInt("x", p.getX());
        tag.putInt("y", p.getY());
        tag.putInt("z", p.getZ());
        tag.putString("dim", pos.dimension().location().toString());
    }

    @Nonnull
    public static GlobalPos readGlobalPos(@Nonnull CompoundTag tag) {
        return GlobalPos.of(ResourceKey.create(Registries.DIMENSION,
                        new ResourceLocation(tag.getString("dim"))),
                new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")));
    }

    public static void writeGlobalPos(@Nonnull FriendlyByteBuf buffer, @Nonnull GlobalPos pos) {
        buffer.writeResourceLocation(pos.dimension().location());
        buffer.writeBlockPos(pos.pos());
    }

    @Nonnull
    public static GlobalPos readGlobalPos(@Nonnull FriendlyByteBuf buffer) {
        return GlobalPos.of(ResourceKey.create(Registries.DIMENSION,
                buffer.readResourceLocation()), buffer.readBlockPos());
    }

    @Nonnull
    public static String getDisplayPos(@Nonnull GlobalPos pos) {
        BlockPos p = pos.pos();
        return "X: " + p.getX() + " Y: " + p.getY() + " Z: " + p.getZ();
    }

    @Nonnull
    public static String getDisplayDim(@Nonnull GlobalPos pos) {
        return pos.dimension().location().toString();
    }

    public static boolean isBadNetworkName(@Nonnull String s) {
        return s.isEmpty() || s.length() > EUNetworkBase.MAX_NETWORK_NAME_LENGTH;
    }
}
