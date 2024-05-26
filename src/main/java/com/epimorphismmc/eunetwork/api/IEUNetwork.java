package com.epimorphismmc.eunetwork.api;

import com.epimorphismmc.eunetwork.common.NetworkStatistics;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

public interface IEUNetwork {

    //////////////////////////////////////
    //*******       Data        ********//
    //////////////////////////////////////

    int getId();
    String getName();
    UUID getOwner();
    boolean setName(@Nonnull String name);

    @Nonnull NetworkStatistics getStatistics();

    //////////////////////////////////////
    //*******      Member       ********//
    //////////////////////////////////////

    @Nullable NetworkMember getMemberByUUID(@Nonnull UUID uuid);

    /**
     * 返回包含所有网络成员的集合
     * @return 所有成员
     */
    @Nonnull Collection<NetworkMember> getAllMembers();

    /**
     * 帮助程序方法，用于获取玩家对此网络的访问级别，即使该玩家不是网络中的成员
     *
     * @param player the server player
     * @return access level
     */
    @Nonnull
    default AccessLevel getPlayerAccess(@Nonnull Player player) {
        return getPlayerAccess(player.getUUID());
    }

    @Nonnull
    default AccessLevel getPlayerAccess(@Nonnull UUID uuid) {
        if (getOwner().equals(uuid)) {
            return AccessLevel.OWNER;
        }
        final NetworkMember member = getMemberByUUID(uuid);
        if (member != null) {
            return member.getAccessLevel();
        }
        return AccessLevel.BLOCKED;
    }

    /**
     * 玩家可以逻辑地访问这个网络吗？
     *
     * @param player the player
     * @return has permission or not
     */
    default boolean canPlayerAccess(@Nonnull Player player) {
        return canPlayerAccess(player.getUUID());
    }

    default boolean canPlayerAccess(@Nonnull UUID uuid) {
        return getPlayerAccess(uuid).canUse();
    }

    default boolean isClientEUNetwork() {
        return false;
    }

    /**
     * 当此网络从其管理器中删除时调用。
     */
    default void onDelete() {
    }
}
