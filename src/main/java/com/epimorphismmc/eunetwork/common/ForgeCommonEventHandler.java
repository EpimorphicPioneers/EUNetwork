package com.epimorphismmc.eunetwork.common;

import com.epimorphismmc.eunetwork.EUNet;
import com.epimorphismmc.eunetwork.common.data.EUNetCommands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EUNet.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeCommonEventHandler {

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        // mainly used to reload data while changing single-player saves, unnecessary on dedicated server
        EUNetworkManager.release();
    }

    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event) {
        EUNetCommands.init(event.getDispatcher());
    }

//    @SubscribeEvent
//    public static void onServerTick(@Nonnull TickEvent.ServerTickEvent event) {
//        if (event.phase == TickEvent.Phase.END) {
//            EUNetworkData.getAllNetworks().forEach(EUNetworkBase::onEndServerTick);
//        }
//    }

}
