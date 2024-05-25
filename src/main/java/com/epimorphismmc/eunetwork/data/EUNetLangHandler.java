package com.epimorphismmc.eunetwork.data;

import com.epimorphismmc.eunetwork.api.AccessLevel;
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

        for (var accessLevel : AccessLevel.values()) {
            provider.add(accessLevel.getTranslationKey(), accessLevel.getEnString(), accessLevel.getCnString());
        }

        provider.add("message.eunetwork.network_id", "Network ID: %d", "网络 ID：%d");
        provider.add("message.eunetwork.network_storage", "Storage: %s EU", "已存储：%s EU");
        provider.add("message.eunetwork.invalid_number", "Invalid number: %s", "无效的数字：%s");
        provider.add("message.eunetwork.invalid_network", "Invalid Network: %d", "无效的网络：%d");
        provider.add("message.eunetwork.add_successed", "Network %d Energy added successfully, added: %s", "网络 %d 能量添加成功，已添加：%s");
        provider.add("message.eunetwork.all_members", "All Members", "所有成员");
        provider.add("message.eunetwork.invite_successes", "Invite %s Successes", "邀请 %s 成功");
        provider.add("message.eunetwork.not_network_owner", "Not Network Owner", "不是网络所有者");
        provider.add("message.eunetwork.not_network_admin", "Not Network Admin", "不是网络管理员");
        provider.add("message.eunetwork.not_space", "Not Enough Space", "没有足够的空间");
        provider.add("message.eunetwork.invalid_user", "Invalid User", "无效的用户");
        provider.add("message.eunetwork.unknow_response", "UnKnow response: %d", "未知的回应：%d");
    }

    private static void addWirelessEnergyHatchName(MOLangProvider provider, int amperage, boolean isInput, int... tiers) {
        provider.addTieredMachineName(
            tier -> "%s_wireless_energy_%s_hatch_%sa".formatted(GTValues.VN[tier].toLowerCase(Locale.ROOT), isInput ? "input" : "output", amperage),
            tier -> "%s安%s§r无线%s仓".formatted(amperage, GTValues.VNF[tier], isInput ? "能源" : "动力"), tiers);
    }
}
