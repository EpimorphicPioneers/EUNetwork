package com.epimorphismmc.eunetwork.common;

import com.epimorphismmc.eunetwork.api.AccessLevel;
import com.epimorphismmc.eunetwork.api.EUNetValues;
import com.epimorphismmc.eunetwork.api.NetworkMember;
import com.epimorphismmc.eunetwork.utils.EUNetUtils;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.math.BigInteger;
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

    public static final int MAX_NETWORK_NAME_LENGTH = 24;

    public static final String NETWORK_NAME = "name";
    public static final String OWNER_UUID = "owner";
    public static final String MEMBERS = "members";
    public static final String STORAGE = "storage";

    //////////////////////////////////////
    //*******       Data        ********//
    //////////////////////////////////////

    @Getter
    int id; // 正整数或 EUNetConstants.INVALID_NETWORK_ID
    @Getter
    String name;
    @Getter
    UUID ownerUUID;

    final NetworkStatistics mStatistics = new NetworkStatistics(this);
    final HashMap<UUID, NetworkMember> mMemberMap = new HashMap<>();

    EUNetworkBase() {/**/}

    private EUNetworkBase(int id, String name, @Nonnull UUID owner) {
        this.id = id;
        this.name = name;
        this.ownerUUID = owner;
    }

    EUNetworkBase(int id, String name, @Nonnull Player owner) {
        this(id, name, owner.getUUID());
        mMemberMap.put(ownerUUID, NetworkMember.create(owner, AccessLevel.OWNER));
    }

    public boolean setNetworkName(@Nonnull String name) {
        if (!this.name.equals(name) && !EUNetUtils.isBadNetworkName(name)) {
            this.name = name;
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
        if (ownerUUID.equals(uuid)) {
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
     * 更改目标的成员身份。首先检查有效。
     *
     * @param player     the player performing this action
     * @param targetUUID the UUID of the player to change
     * @param type       the operation type, e.g. {@link EUNetValues#MEMBERSHIP_SET_USER}
     * @return a response code
     */
    public int changeMembership(Player player, UUID targetUUID, byte type) {
        throw new IllegalStateException();
    }

    public long addEnergy(long energyToAdd) {
        return 0L;
    }

    public long removeEnergy(long energyToRemove) {
        return 0L;
    }

    public BigInteger addEnergy(BigInteger energyToAdd) {
        return BigInteger.ZERO;
    }

    public BigInteger removeEnergy(BigInteger energyToRemove) {
        return BigInteger.ZERO;
    }

    public BigInteger getStorage() {
        return BigInteger.ZERO;
    }

    public void setStorage(BigInteger storage) {
    }

    public void writeCustomTag(@Nonnull CompoundTag tag, byte type) {
        if (type == EUNetValues.NBT_NET_BASIC || type == EUNetValues.NBT_SAVE_ALL) {
            tag.putInt(EUNetValues.NETWORK_ID, id);
            tag.putString(NETWORK_NAME, name);
            tag.putUUID(OWNER_UUID, ownerUUID);
            tag.putString(STORAGE, getStorage().toString());
        }
        if (type == EUNetValues.NBT_SAVE_ALL) {
            Collection<NetworkMember> members = getAllMembers();
            if (!members.isEmpty()) {
                ListTag list = new ListTag();
                for (NetworkMember member : members) {
                    CompoundTag subTag = new CompoundTag();
                    member.writeNBT(subTag);
                    list.add(subTag);
                }
                tag.put(MEMBERS, list);
            }

        }
        if (type == EUNetValues.NBT_NET_MEMBERS) {
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

        if (type == EUNetValues.NBT_NET_STATISTICS) {
            mStatistics.writeNBT(tag);
        }
    }

    public void readCustomTag(@Nonnull CompoundTag tag, byte type) {
        if (type == EUNetValues.NBT_NET_BASIC || type == EUNetValues.NBT_SAVE_ALL) {
            this.id = tag.getInt(EUNetValues.NETWORK_ID);
            this.name = tag.getString(NETWORK_NAME);
            this.ownerUUID = tag.getUUID(OWNER_UUID);
            setStorage(new BigInteger(tag.getString(STORAGE)));
        }
        if (type == EUNetValues.NBT_SAVE_ALL) {
            ListTag list = tag.getList(MEMBERS, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag c = list.getCompound(i);
                NetworkMember m = new NetworkMember(c);
                mMemberMap.put(m.getPlayerUUID(), m);
            }
        }
        if (type == EUNetValues.NBT_NET_MEMBERS) {
            mMemberMap.clear();
            ListTag list = tag.getList(MEMBERS, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag c = list.getCompound(i);
                NetworkMember m = new NetworkMember(c);
                mMemberMap.put(m.getPlayerUUID(), m);
            }
        }

        if (type == EUNetValues.NBT_NET_STATISTICS) {
            mStatistics.readNBT(tag);
        }
    }

    @Override
    public String toString() {
        return "FluxNetwork{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", owner=" + ownerUUID +
                '}';
    }
}
