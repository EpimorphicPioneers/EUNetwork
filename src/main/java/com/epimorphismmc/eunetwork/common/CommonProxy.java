package com.epimorphismmc.eunetwork.common;

import com.epimorphismmc.eunetwork.EUNet;
import com.epimorphismmc.eunetwork.common.data.EUNetMachines;
import com.epimorphismmc.monomorphism.proxy.base.ICommonProxyBase;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import net.minecraft.resources.ResourceLocation;

public class CommonProxy implements ICommonProxyBase {

    public CommonProxy() {
        EUNet.logger().info("EUNetwork's Initialization Completed!");
    }

    @Override
    public void registerEventHandlers() {

    }

    @Override
    public void registerCapabilities() {

    }

    /* -------------------------------------------------- Registration Methods -------------------------------------------------- */

    @Override
    public void registerMachineDefinitions(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        EUNetMachines.init();
    }
}
