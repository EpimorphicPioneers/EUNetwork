package com.epimorphismmc.eunetwork.common.machine.multiblock.part;

import com.epimorphismmc.eunetwork.api.IEUNetwork;
import com.epimorphismmc.eunetwork.api.machine.feature.IEUNetworkMachine;
import com.epimorphismmc.eunetwork.api.machine.trait.NotifiableWirelessContainer;
import com.epimorphismmc.eunetwork.common.EUNetworkBase;
import com.epimorphismmc.eunetwork.common.EUNetworkManager;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WirelessEnergyHatchPartMachine extends TieredIOPartMachine implements IEUNetworkMachine, IExplosionMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(WirelessEnergyHatchPartMachine.class, TieredIOPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @Getter @Setter
    protected UUID ownerUUID;
    @Persisted @DescSynced
    public final NotifiableWirelessContainer energyContainer;
    protected TickableSubscription explosionSubs;
    @Nullable
    protected ISubscription energyListener;
    @Getter
    protected int amperage;

    public WirelessEnergyHatchPartMachine(IMachineBlockEntity holder, int tier, IO io, int amperage, Object... args) {
        super(holder, tier, io);
        this.amperage = amperage;
        this.energyContainer = createEnergyContainer(args);
    }

    //////////////////////////////////////
    //*****     Initialization    ******//
    //////////////////////////////////////

    protected NotifiableWirelessContainer createEnergyContainer(Object... args) {
        NotifiableWirelessContainer container;
        if (this.io == IO.OUT) {
            container = NotifiableWirelessContainer.emitterContainer(this, GTValues.V[tier] * 64L * amperage, GTValues.V[tier], amperage);
            container.setCapabilityValidator(s -> s == null || s == this.getFrontFacing());
        } else {
            container = NotifiableWirelessContainer.receiverContainer(this, GTValues.V[tier] * 16L * amperage, GTValues.V[tier], amperage);
            container.setCapabilityValidator(s -> s == null || s == this.getFrontFacing());
        }
        return container;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        // if machine need do check explosion conditions
        if (ConfigHolder.INSTANCE.machines.doTerrainExplosion && shouldWeatherOrTerrainExplosion()) {
            energyListener = energyContainer.addChangedListener(this::updateExplosionSubscription);
            updateExplosionSubscription();
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (energyListener != null) {
            energyListener.unsubscribe();
            energyListener = null;
        }
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }

    //////////////////////////////////////
    //********     Explosion    ********//
    //////////////////////////////////////

    protected void updateExplosionSubscription() {
        if (ConfigHolder.INSTANCE.machines.doTerrainExplosion && shouldWeatherOrTerrainExplosion() && energyContainer.getEnergyStored() > 0) {
            explosionSubs = subscribeServerTick(explosionSubs, this::checkExplosion);
        } else if (explosionSubs != null) {
            explosionSubs.unsubscribe();
            explosionSubs = null;
        }
    }

    protected void checkExplosion() {
        checkWeatherOrTerrainExplosion(tier, tier * 10);
        updateExplosionSubscription();
    }

    //////////////////////////////////////
    //**********     Misc     **********//
    //////////////////////////////////////

    @Override
    public int tintColor(int index) {
        if (index == 2) {
            return GTValues.VC[getTier()];
        }
        return super.tintColor(index);
    }

    //////////////////////////////////////
    //**********     Data     **********//
    //////////////////////////////////////

    public static boolean isActive(MetaMachine machine) {
        if (machine instanceof WirelessEnergyHatchPartMachine partMachine) {
            return partMachine.canAccessNetwork() && partMachine.energyContainer.getNetworkID() != -1;
        }
        return false;
    }

    @Override
    public boolean canAccessNetwork() {
        return this.isFormed() && this.isWorkingEnabled();
    }

    @Override
    public boolean setEUNetwork(Player player, @Nullable IEUNetwork network) {
        if (player.getUUID().equals(ownerUUID)) {
            if (network == null) {
                energyContainer.setNetworkID(-1);
                return true;
            } else if (network.canPlayerAccess(player)) {
                energyContainer.setNetworkID(network.getId());
                return true;
            }
        }
        return false;
    }

    @Override
    public @Nullable IEUNetwork getEUNetwork() {
        return EUNetworkManager.getInstance().getNetwork(energyContainer.getNetworkID());
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
