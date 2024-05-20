package com.epimorphismmc.eunetwork.common;

import com.epimorphismmc.eunetwork.api.AccessLevel;
import com.epimorphismmc.eunetwork.api.EUNetConstants;
import com.epimorphismmc.eunetwork.api.NetworkMember;
import com.epimorphismmc.eunetwork.utils.EPNetUtil;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 *  网络的基类
 *  有两种实现类型：客户端和服务器。
 *  客户端实例是从服务器更新的缓存值，用于 UI 中的预检查。
 *  服务器实例是逻辑网络，负责能量传输。
 *  客户端在运行服务器端网络时，需要进行双侧检查以确保安全性。服务器端数据将与游戏保存一起持久存储
 */
@ParametersAreNonnullByDefault
public class EUNetworkBase {

    /**
     * 无效网络可避免可空性检查，此网络上的任何操作都是无效的。
     * 您可以检查 isValid() 以跳过您的操作。
     * 即使执行了操作，也不会出现错误。
     * 断开连接的设备被视为已连接到此网络。
     */
    public static final EUNetworkBase INVALID = new EUNetworkBase(EUNetConstants.INVALID_NETWORK_ID, "", Util.NIL_UUID);

    public static final int MAX_NETWORK_NAME_LENGTH = 24;

    public static final String NETWORK_NAME = "name";
    public static final String OWNER_UUID = "owner";
    public static final String MEMBERS = "members";
    public static final String CONNECTIONS = "connections";

    //////////////////////////////////////
    //*******       Data        ********//
    //////////////////////////////////////

    int mID;
    String mName;
    UUID mOwnerUUID;

    final NetworkStatistics mStatistics = new NetworkStatistics(this);
    final HashMap<UUID, NetworkMember> mMemberMap = new HashMap<>();

    EUNetworkBase() {/**/}

    private EUNetworkBase(int id, String name, @Nonnull UUID owner) {
        mID = id;
        mName = name;
        mOwnerUUID = owner;
    }

    EUNetworkBase(int id, String name, @Nonnull Player owner) {
        this(id, name, owner.getUUID());
        mMemberMap.put(mOwnerUUID, NetworkMember.create(owner, AccessLevel.OWNER));
    }

    /**
     * 返回此网络的唯一 ID。
     * @return 正整数或 EUNetConstants.INVALID_NETWORK_ID
     */
    public final int getNetworkID() {
        return mID;
    }

    @Nonnull
    public final UUID getOwnerUUID() {
        return mOwnerUUID;
    }

    /**
     * 返回网络名称。对于无效网络，此值为空，客户端应改为显示替代文本。
     * @return 此网络的名称
     */
    @Nonnull
    public final String getNetworkName() {
        return mName;
    }

    public boolean setNetworkName(@Nonnull String name) {
        if (!name.equals(mName) && !EPNetUtil.isBadNetworkName(name)) {
            mName = name;
            return true;
        }
        return false;
    }

    @Nonnull
    public NetworkStatistics getStatistics() {
        return mStatistics;
    }

    //////////////////////////////////////
    //*******      Member       ********//
    //////////////////////////////////////

    @Nullable
    public NetworkMember getMemberByUUID(@Nonnull UUID uuid) {
        return mMemberMap.get(uuid);
    }

    /**
     * 返回包含所有网络成员的集合
     * @return 所有成员
     */
    @Nonnull
    public Collection<NetworkMember> getAllMembers() {
        return mMemberMap.values();
    }

    /**
     * Ticks the server. Server only.
     */
    public void onEndServerTick() {
    }

    /**
     * 当此网络从其管理器中删除时调用。
     */
    public void onDelete() {
        mMemberMap.clear();
    }

    /**
     * 帮助程序方法，用于获取玩家对此网络的访问级别，即使该玩家不是网络中的成员
     *
     * @param player the server player
     * @return access level
     */
    @Nonnull
    public AccessLevel getPlayerAccess(@Nonnull Player player) {
        final UUID uuid = player.getUUID();
        if (mOwnerUUID.equals(uuid)) {
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
    public final boolean canPlayerAccess(@Nonnull Player player) {
        return getPlayerAccess(player).canUse();
    }

    /**
     * 一个总和值，用于限制流向设备缓冲区的能量。仅限服务器
     *
     * @return buffer limit
     */
    public long getBufferLimiter() {
        return 0;
    }

    /**
     * 更改目标的成员身份。首先检查有效。
     *
     * @param player     the player performing this action
     * @param targetUUID the UUID of the player to change
     * @param type       the operation type, e.g. {@link EUNetConstants#MEMBERSHIP_SET_USER}
     * @return a response code
     */
    public int changeMembership(Player player, UUID targetUUID, byte type) {
        throw new IllegalStateException();
    }

    /**
     * 返回此网络是否为有效网络。无效网络实际上是空网络，但我们使用单例来避免可空性检查。
     *
     * @return {@code true} if it is valid, {@code false} otherwise
     * @see EUNetConstants#INVALID_NETWORK_ID
     */
    public boolean isValid() {
        return false;
    }

    public void writeCustomTag(@Nonnull CompoundTag tag, byte type) {
        if (type == EUNetConstants.NBT_NET_BASIC || type == EUNetConstants.NBT_SAVE_ALL) {
            tag.putInt(EUNetConstants.NETWORK_ID, mID);
            tag.putString(NETWORK_NAME, mName);
            tag.putUUID(OWNER_UUID, mOwnerUUID);
        }
        if (type == EUNetConstants.NBT_SAVE_ALL) {
            Collection<NetworkMember> members = getAllMembers();
            if (!members.isEmpty()) {
                ListTag list = new ListTag();
                for (NetworkMember m : members) {
                    CompoundTag subTag = new CompoundTag();
                    m.writeNBT(subTag);
                    list.add(subTag);
                }
                tag.put(MEMBERS, list);
            }

        }
        if (type == EUNetConstants.NBT_NET_MEMBERS) {
            Collection<NetworkMember> members = getAllMembers();
            ListTag list = new ListTag();
            if (!members.isEmpty()) {
                for (NetworkMember m : members) {
                    CompoundTag subTag = new CompoundTag();
                    m.writeNBT(subTag);
                    list.add(subTag);
                }
            }
            List<ServerPlayer> players = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers();
            for (ServerPlayer p : players) {
                if (getMemberByUUID(p.getUUID()) == null) {
                    CompoundTag subTag = new CompoundTag();
                    NetworkMember.create(p, AccessLevel.BLOCKED)
                            .writeNBT(subTag);
                    list.add(subTag);
                }
            }
            tag.put(MEMBERS, list);
        }

        if (type == EUNetConstants.NBT_NET_STATISTICS) {
            mStatistics.writeNBT(tag);
        }
    }

    public void readCustomTag(@Nonnull CompoundTag tag, byte type) {
        if (type == EUNetConstants.NBT_NET_BASIC || type == EUNetConstants.NBT_SAVE_ALL) {
            mID = tag.getInt(EUNetConstants.NETWORK_ID);
            mName = tag.getString(NETWORK_NAME);
            mOwnerUUID = tag.getUUID(OWNER_UUID);
        }
        if (type == EUNetConstants.NBT_SAVE_ALL) {
            ListTag list = tag.getList(MEMBERS, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag c = list.getCompound(i);
                NetworkMember m = new NetworkMember(c);
                mMemberMap.put(m.getPlayerUUID(), m);
            }
        }
        if (type == EUNetConstants.NBT_NET_MEMBERS) {
            mMemberMap.clear();
            ListTag list = tag.getList(MEMBERS, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag c = list.getCompound(i);
                NetworkMember m = new NetworkMember(c);
                mMemberMap.put(m.getPlayerUUID(), m);
            }
        }

        if (type == EUNetConstants.NBT_NET_STATISTICS) {
            mStatistics.readNBT(tag);
        }
    }

    @Override
    public String toString() {
        return "FluxNetwork{" +
                "id=" + mID +
                ", name='" + mName + '\'' +
                ", owner=" + mOwnerUUID +
                '}';
    }
}
