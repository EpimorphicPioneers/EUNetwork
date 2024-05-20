package com.epimorphismmc.eunetwork.common.data;

import com.epimorphismmc.eunetwork.EUNetwork;
import com.epimorphismmc.eunetwork.common.machine.multiblock.part.WirelessEnergyHatchPartMachine;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.BiFunction;

import static com.epimorphismmc.eunetwork.EUNetwork.registrate;
import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.GTMachines.ELECTRIC_TIERS;

public class EUNetMachines {

    public static final MachineDefinition[] WIRELESS_ENERGY_INPUT_HATCH = registerTieredEUNetMachines("wireless_energy_input_hatch",
            (holder, tier) -> new WirelessEnergyHatchPartMachine(holder, tier, IO.IN, 2),
            (tier, builder) -> builder
                    .langValue("%s §rWireless Energy Input Hatch".formatted(VNF[tier]))
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.INPUT_ENERGY)
                    .workableTieredHullRenderer(EUNetwork.id("block/multiblock/part/wireless_energy_input_hatch"))
                    .tooltips(
                            Component.translatable("block.eunetwork.wireless_energy_input_hatch.desc.0"),
                            Component.translatable("block.eunetwork.wireless_energy_input_hatch.desc.1"),
                            Component.translatable("block.eunetwork.wireless_energy_input_hatch.desc.2")
                    )
                    .register(),
            ELECTRIC_TIERS);

    public static final MachineDefinition[] WIRELESS_ENERGY_OUTPUT_HATCH = registerTieredEUNetMachines("wireless_energy_output_hatch",
            (holder, tier) -> new WirelessEnergyHatchPartMachine(holder, tier, IO.OUT, 2),
            (tier, builder) -> builder
                    .langValue("%s §rWireless Energy Output Hatch".formatted(VNF[tier]))
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.OUTPUT_ENERGY)
                    .workableTieredHullRenderer(EUNetwork.id("block/multiblock/part/wireless_energy_output_hatch"))
                    .tooltips(
                            Component.translatable("block.eunetwork.wireless_energy_output_hatch.desc.0"),
                            Component.translatable("block.eunetwork.wireless_energy_output_hatch.desc.1"),
                            Component.translatable("block.eunetwork.wireless_energy_output_hatch.desc.2")
                    )
                    .register(),
            ELECTRIC_TIERS);

    public static void init() {

    }

    public static MachineDefinition[] registerTieredEUNetMachines(String name,
                                                                  BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                                  BiFunction<Integer, MachineBuilder<MachineDefinition>, MachineDefinition> builder,
                                                                  int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[TIER_COUNT];
        for (int tier : tiers) {
            var register = registrate().machine(VN[tier].toLowerCase(Locale.ROOT) + "_" + name, holder -> factory.apply(holder, tier))
                    .itemBuilder(itemBuilder -> itemBuilder.tab(GTCreativeModeTabs.MACHINE.getKey()))
                    .tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }

}
