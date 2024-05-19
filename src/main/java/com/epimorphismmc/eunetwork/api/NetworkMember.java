package com.epimorphismmc.eunetwork.api;

import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public class NetworkMember {
    @Getter
    private UUID playerUUID;
    @Getter
    private String cachedName;
    @Getter
    private AccessLevel accessLevel;

    private NetworkMember(@Nonnull UUID uuid, @Nullable String name, @Nonnull AccessLevel access) {
        playerUUID = Objects.requireNonNull(uuid);
        cachedName = Objects.requireNonNullElse(name, "[Anonymous]");
        accessLevel = access;
    }

    public NetworkMember(@Nonnull CompoundTag tag) {
        readNBT(tag);
    }

    @Nonnull
    public static NetworkMember create(@Nonnull Player player, @Nonnull AccessLevel access) {
        return new NetworkMember(player.getUUID(), player.getGameProfile().getName(), access);
    }

    public boolean setAccessLevel(@Nonnull AccessLevel accessLevel) {
        if (this.accessLevel != accessLevel) {
            this.accessLevel = accessLevel;
            return true;
        }
        return false;
    }

    public void writeNBT(@Nonnull CompoundTag tag) {
        tag.putUUID("playerUUID", playerUUID);
        tag.putString("cachedName", cachedName);
        tag.putByte("accessLevel", accessLevel.getKey());
    }

    public void readNBT(@Nonnull CompoundTag tag) {
        this.playerUUID = tag.getUUID("playerUUID");
        this.cachedName = tag.getString("cachedName");
        this.accessLevel = AccessLevel.fromKey(tag.getByte("accessLevel"));
    }

    @Override
    public String toString() {
        return "NetworkMember{" +
                "uuid=" + playerUUID +
                ", name='" + cachedName + '\'' +
                ", access=" + accessLevel +
                '}';
    }
}
