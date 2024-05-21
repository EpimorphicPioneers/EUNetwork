package com.epimorphismmc.eunetwork;

import com.epimorphismmc.eunetwork.client.ClientProxy;
import com.epimorphismmc.eunetwork.common.CommonProxy;
import com.epimorphismmc.eunetwork.config.EUNetConfigHolder;
import com.epimorphismmc.eunetwork.data.EUNetLangHandler;
import com.epimorphismmc.eunetwork.network.c2s.CPacketEUNetworkPayload;
import com.epimorphismmc.eunetwork.network.s2c.SPacketEUNetworkPayload;
import com.epimorphismmc.monomorphism.MOMod;
import com.epimorphismmc.monomorphism.datagen.MOProviderTypes;
import com.epimorphismmc.monomorphism.registry.registrate.MORegistrate;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.networking.INetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(EUNet.MODID)
public class EUNet extends MOMod<CommonProxy> {
    public static final String MODID = "eunetwork";
    public static final String NAME = "EUNetwork";

    public static EUNet instance;

    public EUNet() {
        super();
    }

    @Override
    public String getModId() {
        return MODID;
    }

    @Override
    public String getModName() {
        return NAME;
    }

    @Override
    protected void onModConstructed() {
        instance = this;
        EUNetConfigHolder.init();
    }

    @Override
    protected CommonProxy createClientProxy() {
        return new ClientProxy();
    }

    @Override
    protected CommonProxy createServerProxy() {
        return new CommonProxy();
    }

    @Override
    public void addDataGenerator(MORegistrate registrate) {
        registrate.addDataGenerator(MOProviderTypes.MO_LANG, EUNetLangHandler::init);
    }

    @Override
    public void registerPackets(INetworking network) {
        network.registerS2C(SPacketEUNetworkPayload.class);
        network.registerC2S(CPacketEUNetworkPayload.class);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, FormattingUtil.toLowerCaseUnder(path));
    }

    public static Logger logger() {
        return instance.getLogger();
    }

    public static CommonProxy proxy() {
        return instance.getProxy();
    }

    public static MORegistrate registrate() {
        return instance.getRegistrate();
    }

    public static INetworking network() {
        return instance.getNetwork();
    }
}
