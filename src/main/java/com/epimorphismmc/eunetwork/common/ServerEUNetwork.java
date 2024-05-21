package com.epimorphismmc.eunetwork.common;

import com.epimorphismmc.eunetwork.api.AccessLevel;
import com.epimorphismmc.eunetwork.api.EUNetValues;
import com.epimorphismmc.eunetwork.api.NetworkMember;
import com.epimorphismmc.monomorphism.utility.BigIntStorage;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.UUID;

public class ServerEUNetwork extends EUNetworkBase {

    private final BigIntStorage storage;

    ServerEUNetwork() {
        this.storage = new BigIntStorage();
    }

    ServerEUNetwork(int id, String name,  @Nonnull Player owner) {
        super(id, name, owner);
        this.storage = new BigIntStorage();
    }

    @Override
    public void onEndServerTick() {

    }

    @Override
    public long addEnergy(long energyToAdd) {
        EUNetworkData.markDirty();
        return storage.add(energyToAdd);
    }

    @Override
    public long removeEnergy(long energyToRemove) {
        EUNetworkData.markDirty();
        return storage.remove(energyToRemove);
    }

    @Override
    public BigInteger addEnergy(BigInteger energyToAdd) {
        EUNetworkData.markDirty();
        return storage.add(energyToAdd);
    }

    @Override
    public BigInteger removeEnergy(BigInteger energyToRemove) {
        EUNetworkData.markDirty();
        return storage.remove(energyToRemove);
    }

    @Override
    public int changeMembership(@Nonnull Player player, @Nonnull UUID targetUUID, byte type) {
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
                this.ownerUUID = targetUUID;
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
                this.ownerUUID = targetUUID;
                return EUNetValues.RESPONSE_SUCCESS;
            } else {
                return EUNetValues.RESPONSE_INVALID_USER;
            }
        } else {
            return EUNetValues.RESPONSE_INVALID_USER;
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
