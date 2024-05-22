package com.epimorphismmc.eunetwork.common.data;

import com.epimorphismmc.eunetwork.EUNet;
import com.epimorphismmc.eunetwork.common.machine.multiblock.part.WirelessEnergyHatchPartMachine;
import com.epimorphismmc.monomorphism.client.renderer.machine.CustomWorkableMachineRenderer;
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

import static com.epimorphismmc.eunetwork.EUNet.registrate;
import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.GTMachines.ELECTRIC_TIERS;
import static com.gregtechceu.gtceu.common.data.GTMachines.MULTI_HATCH_TIERS;

public class EUNetMachines {
    public static final MachineDefinition[] WIRELESS_ENERGY_INPUT_HATCH = registerWirelessEnergyHatches(2, IO.IN, ELECTRIC_TIERS);
    public static final MachineDefinition[] WIRELESS_ENERGY_INPUT_HATCH_4A = registerWirelessEnergyHatches(4, IO.IN, MULTI_HATCH_TIERS);
    public static final MachineDefinition[] WIRELESS_ENERGY_INPUT_HATCH_16A = registerWirelessEnergyHatches(16, IO.IN, MULTI_HATCH_TIERS);
    public static final MachineDefinition[] WIRELESS_ENERGY_INPUT_HATCH_64A = registerWirelessEnergyHatches(64, IO.IN, MULTI_HATCH_TIERS);
    public static final MachineDefinition[] WIRELESS_ENERGY_INPUT_HATCH_256A = registerWirelessEnergyHatches(256, IO.IN, MULTI_HATCH_TIERS);
    public static final MachineDefinition[] WIRELESS_ENERGY_INPUT_HATCH_1024A = registerWirelessEnergyHatches(1024, IO.IN, MULTI_HATCH_TIERS);
    public static final MachineDefinition[] WIRELESS_ENERGY_INPUT_HATCH_4096A = registerWirelessEnergyHatches(4096, IO.IN, MULTI_HATCH_TIERS);
    public static final MachineDefinition[] WIRELESS_ENERGY_INPUT_HATCH_16384A = registerWirelessEnergyHatches(16384, IO.IN, MULTI_HATCH_TIERS);
    public static final MachineDefinition[] WIRELESS_ENERGY_INPUT_HATCH_65536A = registerWirelessEnergyHatches(65536, IO.IN, MULTI_HATCH_TIERS);
    public static final MachineDefinition[] WIRELESS_ENERGY_INPUT_HATCH_262144A = registerWirelessEnergyHatches(262144, IO.IN, MULTI_HATCH_TIERS);
    public static final MachineDefinition[] WIRELESS_ENERGY_INPUT_HATCH_104857A = registerWirelessEnergyHatches(1048576, IO.IN, MULTI_HATCH_TIERS);

    public static final MachineDefinition[] WIRELESS_ENERGY_OUTPUT_HATCH = registerWirelessEnergyHatches(2, IO.OUT, ELECTRIC_TIERS);
    public static final MachineDefinition[] WIRELESS_ENERGY_OUTPUT_HATCH_4A = registerWirelessEnergyHatches(4, IO.OUT, MULTI_HATCH_TIERS);
    public static final MachineDefinition[] WIRELESS_ENERGY_OUTPUT_HATCH_16A = registerWirelessEnergyHatches(16, IO.OUT, MULTI_HATCH_TIERS);
    public static final MachineDefinition[] WIRELESS_ENERGY_OUTPUT_HATCH_64A = registerWirelessEnergyHatches(64, IO.OUT, MULTI_HATCH_TIERS);
    public static final MachineDefinition[] WIRELESS_ENERGY_OUTPUT_HATCH_256A = registerWirelessEnergyHatches(256, IO.OUT, MULTI_HATCH_TIERS);
    public static final MachineDefinition[] WIRELESS_ENERGY_OUTPUT_HATCH_1024A = registerWirelessEnergyHatches(1024, IO.OUT, MULTI_HATCH_TIERS);
    public static final MachineDefinition[] WIRELESS_ENERGY_OUTPUT_HATCH_4096A = registerWirelessEnergyHatches(4096, IO.OUT, MULTI_HATCH_TIERS);
    public static final MachineDefinition[] WIRELESS_ENERGY_OUTPUT_HATCH_16384A = registerWirelessEnergyHatches(16384, IO.OUT, MULTI_HATCH_TIERS);
    public static final MachineDefinition[] WIRELESS_ENERGY_OUTPUT_HATCH_65536A = registerWirelessEnergyHatches(65536, IO.OUT, MULTI_HATCH_TIERS);
    public static final MachineDefinition[] WIRELESS_ENERGY_OUTPUT_HATCH_262144A = registerWirelessEnergyHatches(262144, IO.OUT, MULTI_HATCH_TIERS);
    public static final MachineDefinition[] WIRELESS_ENERGY_OUTPUT_HATCH_104857A = registerWirelessEnergyHatches(1048576, IO.OUT, MULTI_HATCH_TIERS);

    public static void init() {

    }

    public static MachineDefinition[] registerWirelessEnergyHatches(int amperage, IO io, int... tiers) {
        return registerTieredEUNetMachines("wireless_energy_%s_hatch_%sa".formatted(io == IO.IN ? "input" : "output", amperage),
                (holder, tier) -> new WirelessEnergyHatchPartMachine(holder, tier, io, amperage),
                (tier, builder) -> builder
                        .langValue("%s §r%sA Wireless %s Hatch".formatted(VNF[tier], amperage, io == IO.IN ? "Energy" : "Dynamo"))
                        .rotationState(RotationState.ALL)
                        .abilities(io == IO.IN ? PartAbility.INPUT_ENERGY : PartAbility.OUTPUT_ENERGY)
                        .renderer(() -> new CustomWorkableMachineRenderer(tier,
                                EUNet.id("block/multiblock/part/wireless_energy_hatch/%sa".formatted(amperage)), WirelessEnergyHatchPartMachine::isActive))
                        .tooltips(Component.translatable("block.eunetwork.wireless_energy_%s_hatch.desc".formatted(io == IO.IN ? "input" : "output")))
                        .register(),
                tiers);
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
