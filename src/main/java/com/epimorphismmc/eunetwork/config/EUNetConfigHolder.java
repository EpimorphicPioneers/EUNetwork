package com.epimorphismmc.eunetwork.config;

import com.epimorphismmc.eunetwork.EUNetwork;
import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.format.ConfigFormats;

@Config(id = EUNetwork.MODID)
public class EUNetConfigHolder {

    public static EUNetConfigHolder INSTANCE;

    public static void init() {
        if (INSTANCE == null) {
            INSTANCE = Configuration.registerConfig(EUNetConfigHolder.class, ConfigFormats.yaml()).getConfigInstance();
        }
    }

    @Configurable
    @Configurable.Comment({"", "Default: 1"})
    public int maximumPerPlayer = 1;

}
