package com.epimorphismmc.eunetwork.data;

import com.epimorphismmc.monomorphism.datagen.lang.MOLangProvider;
import com.gregtechceu.gtceu.api.GTValues;

import java.util.Locale;

import static com.epimorphismmc.eunetwork.common.data.EUNetItems.*;
import static com.gregtechceu.gtceu.common.data.GTMachines.ELECTRIC_TIERS;
import static com.gregtechceu.gtceu.common.data.GTMachines.MULTI_HATCH_TIERS;

public class EUNetLangHandler {
    private EUNetLangHandler() {/**/}

    public static void init(MOLangProvider provider) {
        provider.addItemName(EUNETWORK_TERMINAL, "EUNetwork Terminal", "无线电网终端");

        provider.addTieredMachineName("wireless_energy_input_hatch_2a", "无线能源仓", ELECTRIC_TIERS);
        provider.addBlockWithTooltip("wireless_energy_input_hatch",
                "Extracting energy from Wireless EU Network",
                "从无线电网中提取能量");

        addWirelessEnergyHatchName(provider, 4, true, MULTI_HATCH_TIERS);
        addWirelessEnergyHatchName(provider, 16, true, MULTI_HATCH_TIERS);
        addWirelessEnergyHatchName(provider, 64, true, MULTI_HATCH_TIERS);
        addWirelessEnergyHatchName(provider, 256, true, MULTI_HATCH_TIERS);
        addWirelessEnergyHatchName(provider, 1024, true, MULTI_HATCH_TIERS);
        addWirelessEnergyHatchName(provider, 4096, true, MULTI_HATCH_TIERS);
        addWirelessEnergyHatchName(provider, 16384, true, MULTI_HATCH_TIERS);
        addWirelessEnergyHatchName(provider, 65536, true, MULTI_HATCH_TIERS);
        addWirelessEnergyHatchName(provider, 262144, true, MULTI_HATCH_TIERS);
        addWirelessEnergyHatchName(provider, 1048576, true, MULTI_HATCH_TIERS);

        provider.addTieredMachineName("wireless_energy_output_hatch_2a", "无线动力仓", ELECTRIC_TIERS);
        provider.addBlockWithTooltip("wireless_energy_output_hatch",
                "Input energy to Wireless EU Network",
                "向无线电网输入能量");

        addWirelessEnergyHatchName(provider, 4, false, MULTI_HATCH_TIERS);
        addWirelessEnergyHatchName(provider, 16, false, MULTI_HATCH_TIERS);
        addWirelessEnergyHatchName(provider, 64, false, MULTI_HATCH_TIERS);
        addWirelessEnergyHatchName(provider, 256, false, MULTI_HATCH_TIERS);
        addWirelessEnergyHatchName(provider, 1024, false, MULTI_HATCH_TIERS);
        addWirelessEnergyHatchName(provider, 4096, false, MULTI_HATCH_TIERS);
        addWirelessEnergyHatchName(provider, 16384, false, MULTI_HATCH_TIERS);
        addWirelessEnergyHatchName(provider, 65536, false, MULTI_HATCH_TIERS);
        addWirelessEnergyHatchName(provider, 262144, false, MULTI_HATCH_TIERS);
        addWirelessEnergyHatchName(provider, 1048576, false, MULTI_HATCH_TIERS);
    }

    private static void addWirelessEnergyHatchName(MOLangProvider provider, int amperage, boolean isInput, int... tiers) {
        provider.addTieredMachineName(
                tier -> "%s_wireless_energy_%s_hatch_%sa".formatted(GTValues.VN[tier].toLowerCase(Locale.ROOT), isInput ? "input" : "output", amperage),
                tier -> "%s安%s§r无线%s仓".formatted(amperage, GTValues.VNF[tier], isInput ? "能源" : "动力"), tiers);
    }
}
