package com.epimorphismmc.eunetwork.api.machine.trait;

import com.epimorphismmc.eunetwork.EUNet;
import com.epimorphismmc.eunetwork.api.machine.feature.IEUNetworkMachine;
import com.epimorphismmc.eunetwork.common.EUNetworkData;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Direction;

public class NotifiableWirelessContainer extends NotifiableEnergyContainer {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(NotifiableWirelessContainer.class, NotifiableEnergyContainer.MANAGED_FIELD_HOLDER);

    @Persisted @DescSynced @RequireRerender
    @Getter @Setter
    private int networkID = -1;

    public NotifiableWirelessContainer(MetaMachine machine, long maxCapacity, long maxInputVoltage, long maxInputAmperage, long maxOutputVoltage, long maxOutputAmperage) {
        super(machine, maxCapacity, maxInputVoltage, maxInputAmperage, maxOutputVoltage, maxOutputAmperage);
    }

    public static NotifiableWirelessContainer emitterContainer(MetaMachine machine, long maxCapacity, long maxOutputVoltage, long maxOutputAmperage) {
        return new NotifiableWirelessContainer(machine, maxCapacity, 0L, 0L, maxOutputVoltage, maxOutputAmperage);
    }

    public static NotifiableWirelessContainer receiverContainer(MetaMachine machine, long maxCapacity, long maxInputVoltage, long maxInputAmperage) {
        return new NotifiableWirelessContainer(machine, maxCapacity, maxInputVoltage, maxInputAmperage, 0L, 0L);
    }

    private IEUNetworkMachine getEUNetworkMachine() {
        return (IEUNetworkMachine) getMachine();
    }

    @Override
    public void updateTick() {
        super.updateTick();
        if (networkID < 0) return;
        if (getMachine().getLevel().isClientSide()) return;
        if (getMachine().getOffsetTimer() % 20L != 0L) return;
        if (!getEUNetworkMachine().canAccessNetwork()) return;

        var network = EUNetworkData.getNetwork(networkID);
        if (network != null) {
            if (network.canPlayerAccess(getEUNetworkMachine().getOwnerUUID())) {
                if (this.getInputVoltage() == 0) {
                    if (this.getEnergyStored() > 0) {
                        this.removeEnergy(network.addEnergy(this.getEnergyStored()));
                    }
                } else {
                    long consumeEnergy = this.getEnergyCapacity() - this.getEnergyStored();
                    if (consumeEnergy > 0) {
                        this.addEnergy(network.removeEnergy(consumeEnergy));
                    }
                }
            } else {
                this.networkID = -1;
            }
        }
    }

    @Override
    public void checkOutputSubscription() {
        // We don't need to interact with wires
    }

    @Override
    public void serverTick() {
        // We don't need to interact with wires
    }

    @Override
    public boolean outputsEnergy(Direction side) {
        return false;
    }

    @Override
    public boolean inputsEnergy(Direction side) {
        return false;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

}
