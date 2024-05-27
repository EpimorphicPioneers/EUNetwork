package com.epimorphismmc.eunetwork.common;

import com.epimorphismmc.eunetwork.api.*;
import com.epimorphismmc.eunetwork.common.data.EUNetworkTypes;
import com.epimorphismmc.monomorphism.utility.BigIntStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class ServerEUNetwork extends EUNetwork {

    public static final IEUNetworkFactory<ServerEUNetwork> FACTORY = new IEUNetworkFactory<>() {

        @Override
        public ServerEUNetwork createEUNetwork(int id, String name, @NotNull Player owner) {
            return new ServerEUNetwork(id, name, owner);
        }

        @Override
        public ServerEUNetwork deserialize(CompoundTag tag, byte type) {
            var network = new ServerEUNetwork();
            network.deserialize(tag, type);
            return network;
        }

        @Override
        public String getType() {
            return EUNetworkTypes.BUILT;
        }
    };

    private final BigIntStorage storage;

    protected ServerEUNetwork() {
        this.storage = new BigIntStorage();
    }

    protected ServerEUNetwork(int id, String name,  @Nonnull Player owner) {
        super(id, name, owner);
        this.storage = new BigIntStorage();
    }

    @Override
    public void onEndServerTick() {

    }

    @Override
    public long addEnergy(long energyToAdd) {
        EUNetworkManager.getInstance().setDirty();
        return storage.add(energyToAdd);
    }

    @Override
    public long removeEnergy(long energyToRemove) {
        EUNetworkManager.getInstance().setDirty();
        return storage.remove(energyToRemove);
    }

    @Override
    public BigInteger addEnergy(BigInteger energyToAdd) {
        EUNetworkManager.getInstance().setDirty();
        return storage.add(energyToAdd);
    }

    @Override
    public BigInteger removeEnergy(BigInteger energyToRemove) {
        EUNetworkManager.getInstance().setDirty();
        return storage.remove(energyToRemove);
    }

    @Override
    public BigInteger getStorage() {
        return storage.getStorage();
    }

    @Override
    public void setStorage(BigInteger storage) {
        this.storage.setStorage(storage);
    }

    @Override
    public int changeMembership(@Nonnull Player player, @Nonnull UUID targetUUID, byte type) {
        EUNetworkManager.getInstance().setDirty();
        final AccessLevel access = getPlayerAccess(player);
        boolean editPermission = access.canEdit();
        boolean ownerPermission = access.canDelete();
        // 检查权限
        if (!editPermission) {
            return EUNetValues.RESPONSE_NO_ADMIN;
        }

        // 编辑自己
        final boolean self = player.getUUID().equals(targetUUID);
        // 网络中的当前成员
        final NetworkMember current = getMemberByUUID(targetUUID);

        // 创建新成员
        if (type == EUNetValues.MEMBERSHIP_SET_USER && current == null) {
            final Player target = ServerLifecycleHooks.getCurrentServer()
                    .getPlayerList().getPlayer(targetUUID);
            if (target != null) {
                NetworkMember m = NetworkMember.create(target, AccessLevel.USER);
                mMemberMap.put(m.getPlayerUUID(), m);
                return EUNetValues.RESPONSE_SUCCESS;
            } else {
                // 用户现在处于离线状态
                return EUNetValues.RESPONSE_INVALID_USER;
            }
        } else if (current != null) {
            if (self && current.getAccessLevel() == AccessLevel.OWNER) {
                return EUNetValues.RESPONSE_INVALID_USER;
            }
            boolean changed = false;
            if (type == EUNetValues.MEMBERSHIP_SET_USER) {
                changed = current.setAccessLevel(AccessLevel.USER);
            } else if (type == EUNetValues.MEMBERSHIP_CANCEL_MEMBERSHIP) {
                changed = mMemberMap.remove(targetUUID) != null;
            } else if (type == EUNetValues.MEMBERSHIP_TRANSFER_OWNERSHIP) {
                if (!ownerPermission) {
                    return EUNetValues.RESPONSE_NO_OWNER;
                }
                getAllMembers().forEach(f -> {
                    if (f.getAccessLevel().canDelete()) {
                        f.setAccessLevel(AccessLevel.USER);
                    }
                });
                this.owner = targetUUID;
                current.setAccessLevel(AccessLevel.OWNER);
                changed = true;
            }
            return changed ? EUNetValues.RESPONSE_SUCCESS : EUNetValues.RESPONSE_INVALID_USER;
        } else if (type == EUNetValues.MEMBERSHIP_TRANSFER_OWNERSHIP) {
            if (!ownerPermission) {
                return EUNetValues.RESPONSE_NO_OWNER;
            }
            // 超级用户仍然可以将所有权转让给自己
            if (self && access == AccessLevel.OWNER) {
                return EUNetValues.RESPONSE_INVALID_USER;
            }
            Player target = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(targetUUID);
            // is online
            if (target != null) {
                getAllMembers().forEach(f -> {
                    if (f.getAccessLevel().canDelete()) {
                        f.setAccessLevel(AccessLevel.USER);
                    }
                });
                NetworkMember m = NetworkMember.create(target, AccessLevel.OWNER);
                mMemberMap.put(m.getPlayerUUID(), m);
                this.owner = targetUUID;
                return EUNetValues.RESPONSE_SUCCESS;
            } else {
                return EUNetValues.RESPONSE_INVALID_USER;
            }
        } else {
            return EUNetValues.RESPONSE_INVALID_USER;
        }
    }

    @Override
    public void serialize(@Nonnull CompoundTag tag, byte type) {
        if (type == EUNetValues.NBT_NET_BASIC || type == EUNetValues.NBT_SAVE_ALL) {
            tag.putInt(EUNetValues.NETWORK_ID, id);
            tag.putString(NETWORK_NAME, name);
            tag.putUUID(OWNER_UUID, owner);
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

    @Override
    public IEUNetworkFactory<? extends IEUNetwork> getFactory() {
        return FACTORY;
    }

    public void deserialize(@Nonnull CompoundTag tag, byte type) {
        if (type == EUNetValues.NBT_NET_BASIC || type == EUNetValues.NBT_SAVE_ALL) {
            this.id = tag.getInt(EUNetValues.NETWORK_ID);
            this.name = tag.getString(NETWORK_NAME);
            this.owner = tag.getUUID(OWNER_UUID);
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
    /*

    @Override
    public void addNewMember(String name) {
        NetworkMember a = NetworkMember.createMemberByUsername(name);
        if (network_players.getValue().stream().noneMatch(f -> f.getPlayerUUID().equals(a.getPlayerUUID()))) {
            network_players.getValue().add(a);
        }
    }

    @Override
    public void removeMember(UUID uuid) {
        network_players.getValue().removeIf(p -> p.getPlayerUUID().equals(uuid) && !p.getAccessPermission().canDelete
        ());
    }*/
}
