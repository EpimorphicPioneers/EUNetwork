package com.epimorphismmc.eunetwork.common;

import com.epimorphismmc.eunetwork.EUNet;
import com.epimorphismmc.eunetwork.common.data.EUNetMachines;
import com.epimorphismmc.eunetwork.integration.EUNetIntegration;
import com.epimorphismmc.eunetwork.integration.ftbteams.FTBEUNetwork;
import com.epimorphismmc.monomorphism.proxy.base.ICommonProxyBase;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

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

    @Override
    public void onModLoadCompleteEvent(FMLLoadCompleteEvent event) {
        EUNetworkManager.registerFactory(ServerEUNetwork.FACTORY);
        event.enqueueWork(() -> {
            if (EUNetIntegration.isFTBTeamsLoaded()) {
                EUNet.logger().info("FTB Teams found. Enabling integration...");
                EUNetworkManager.registerFactory(FTBEUNetwork.FACTORY);
            }
        });
    }

    /* -------------------------------------------------- Registration Methods -------------------------------------------------- */

    @Override
    public void registerMachineDefinitions(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        EUNetMachines.init();
    }
}
