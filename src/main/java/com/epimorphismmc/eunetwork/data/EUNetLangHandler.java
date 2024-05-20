package com.epimorphismmc.eunetwork.data;

import com.epimorphismmc.monomorphism.datagen.lang.MOLangProvider;

import java.util.List;

import static com.gregtechceu.gtceu.common.data.GTMachines.ELECTRIC_TIERS;

public class EUNetLangHandler {
    private EUNetLangHandler() {/**/}

    public static void init(MOLangProvider provider) {
        provider.addTieredMachineName("wireless_energy_input_hatch", "无线能源仓", ELECTRIC_TIERS);
        provider.addBlockWithTooltip("wireless_energy_input_hatch",
                List.of(
                        "§7Wireless EU Network",
                        "Extracting energy from Wireless EU Network",
                        ""),
                List.of(
                        "§7无线电网",
                        "从无线电网中提取能量",
                        ""));

        provider.addTieredMachineName("wireless_energy_output_hatch", "无线动力仓", ELECTRIC_TIERS);
        provider.addBlockWithTooltip("wireless_energy_output_hatch",
                List.of(
                        "§7Wireless EU Network",
                        "Input energy to Wireless EU Network",
                        ""),
                List.of(
                        "§7无线电网",
                        "向无线电网输入能量",
                        ""));
    }
}
