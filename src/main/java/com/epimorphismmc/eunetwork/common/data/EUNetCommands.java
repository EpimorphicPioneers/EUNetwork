package com.epimorphismmc.eunetwork.common.data;

import com.epimorphismmc.eunetwork.EUNet;
import com.epimorphismmc.eunetwork.common.EUNetworkBase;
import com.epimorphismmc.eunetwork.common.EUNetworkData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.math.BigInteger;
import java.text.NumberFormat;

public class EUNetCommands {
    private static final NumberFormat nf = NumberFormat.getInstance();

    private static final RequiredArgumentBuilder<CommandSourceStack, Integer> NETWORK_ARGUMENT =
        Commands.argument("id", IntegerArgumentType.integer())
            .suggests((ctx, builder) -> {
                EUNetworkData.getAllNetworks().forEach(n -> builder.suggest(n.getId()));
                return builder.buildFuture();
            });

    private static final LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(EUNet.MODID)
        .requires(src -> src.hasPermission(2))
        .then(Commands.literal("get")
            .executes(src -> {
                ServerPlayer player = src.getSource().getPlayerOrException();
                for (EUNetworkBase network : EUNetworkData.getAllNetworks()) {
                    if (network.canPlayerAccess(player)) {
                        src.getSource().sendSuccess(
                            () -> Component.translatable("message.eunetwork.network_id", network.getId()),
                            true
                        );
                        src.getSource().sendSuccess(
                            () -> Component.translatable("message.eunetwork.network_storage", nf.format(network.getStorage())),
                            true
                        );
                    }
                }
                return 1;
            })
            .then(NETWORK_ARGUMENT
                .executes(src -> {
                    int id = IntegerArgumentType.getInteger(src, "id");
                    EUNetworkBase network = EUNetworkData.getNetwork(id);
                    if (network == null) {
                        src.getSource().sendFailure(Component.translatable("message.eunetwork.invalid_network", id));
                    } else {
                        src.getSource().sendSuccess(
                            () -> Component.translatable("message.eunetwork.network_id", network.getId()),
                            true
                        );
                        src.getSource().sendSuccess(
                            () -> Component.translatable("message.eunetwork.network_storage", nf.format(network.getStorage())),
                            true
                        );
                    }
                    return 1;
                })))
        .then(Commands.literal("add")
            .then(NETWORK_ARGUMENT
                .then(Commands.argument("value", StringArgumentType.string())
                    .executes(src -> {
                        int id = IntegerArgumentType.getInteger(src, "id");
                        String valueString = StringArgumentType.getString(src, "value");
                        BigInteger value;
                        try {
                            value = new BigInteger(valueString);
                        } catch (NumberFormatException e) {
                            src.getSource().sendFailure(Component.translatable("message.eunetwork.invalid_number", valueString));
                            return 0;
                        }
                        EUNetworkBase network = EUNetworkData.getNetwork(id);
                        if (network == null) {
                            src.getSource().sendFailure(Component.translatable("message.eunetwork.invalid_network", id));
                        } else {
                            BigInteger inserted = network.addEnergy(value);
                            src.getSource().sendSuccess(
                                () -> Component.translatable("message.eunetwork.add_successed", id, nf.format(inserted)),
                                true
                            );
                        }
                        return 1;
                    })))
        );

    public static void init(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(root);
    }
}
