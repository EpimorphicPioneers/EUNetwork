package com.epimorphismmc.eunetwork.common;

import com.epimorphismmc.eunetwork.api.EUNetValues;
import com.epimorphismmc.eunetwork.api.IEUNetwork;
import com.epimorphismmc.eunetwork.api.IEUNetworkFactory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.UUID;

public abstract class EUNetwork extends EUNetworkBase {

    public EUNetwork() {
    }

    public EUNetwork(int id, String name, @NotNull Player owner) {
        super(id, name, owner);
    }

    public abstract void serialize(@Nonnull CompoundTag tag, byte type);
    public abstract FriendlyByteBuf toByteBuf();
    public abstract IEUNetworkFactory<? extends IEUNetwork> getFactory();

    /**
     * Ticks the server. Server only.
     */
    public void onEndServerTick() {
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

    public void setStorage(BigInteger storage) {/**/}

}
