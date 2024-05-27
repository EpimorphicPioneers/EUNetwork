package com.epimorphismmc.eunetwork.common;

import com.epimorphismmc.eunetwork.api.AccessLevel;
import com.epimorphismmc.eunetwork.api.EUNetValues;
import com.epimorphismmc.eunetwork.api.IEUNetwork;
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
public abstract class EUNetworkBase implements IEUNetwork {

    public static final int MAX_NETWORK_NAME_LENGTH = 24;

    public static final String NETWORK_NAME = "name";
    public static final String OWNER_UUID = "owner";
    public static final String MEMBERS = "members";
    public static final String STORAGE = "storage";

    //////////////////////////////////////
    //*******       Data        ********//
    //////////////////////////////////////

    @Getter
    protected int id; // 正整数或 EUNetConstants.INVALID_NETWORK_ID
    @Getter
    protected String name;
    @Getter
    protected UUID owner;

    protected final NetworkStatistics mStatistics = new NetworkStatistics(this);
    protected final HashMap<UUID, NetworkMember> mMemberMap = new HashMap<>();

    EUNetworkBase() {/**/}

    private EUNetworkBase(int id, String name, @Nonnull UUID owner) {
        this.id = id;
        this.name = name;
        this.owner = owner;
    }

    EUNetworkBase(int id, String name, @Nonnull Player owner) {
        this(id, name, owner.getUUID());
        mMemberMap.put(getOwner(), NetworkMember.create(owner, AccessLevel.OWNER));
    }

    @Override
    public boolean setName(@Nonnull String name) {
        if (!this.name.equals(name) && !EUNetUtils.isBadNetworkName(name)) {
            this.name = name;
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public NetworkStatistics getStatistics() {
        return mStatistics;
    }

    //////////////////////////////////////
    //*******      Member       ********//
    //////////////////////////////////////

    @Nullable
    @Override
    public NetworkMember getMemberByUUID(@Nonnull UUID uuid) {
        return mMemberMap.get(uuid);
    }

    @Nonnull
    @Override
    public Collection<NetworkMember> getAllMembers() {
        return mMemberMap.values();
    }

    @Override
    public void onDelete() {
        mMemberMap.clear();
    }

    @Override
    public String toString() {
        return "FluxNetwork{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", owner=" + owner +
                '}';
    }
}
