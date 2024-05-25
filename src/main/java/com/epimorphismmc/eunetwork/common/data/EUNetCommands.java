package com.epimorphismmc.eunetwork.common.data;

import com.epimorphismmc.eunetwork.EUNet;
import com.epimorphismmc.eunetwork.common.EUNetworkBase;
import com.epimorphismmc.eunetwork.common.EUNetworkData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.function.Predicate;

public class EUNetCommands {
    private static final NumberFormat nf = NumberFormat.getInstance();

    private static final Style NUMBER = Style.EMPTY.withColor(0xffde7d);

    private static final Predicate<CommandSourceStack> HAS_PERMISSION = s -> s.hasPermission(2);
    private static final RequiredArgumentBuilder<CommandSourceStack, Integer> NETWORK_ARGUMENT =
        Commands.argument("id", IntegerArgumentType.integer())
            .suggests((ctx, builder) -> {
                EUNetworkData.getAllNetworks().forEach(n -> builder.suggest(n.getId()));
                return builder.buildFuture();
            });

    private static void sendStorage(CommandContext<CommandSourceStack> ctx, EUNetworkBase network) {
        ctx.getSource().sendSuccess(
            () -> Component.translatable("message.eunetwork.network_id", Component.literal("" + network.getId()).withStyle(NUMBER)),
            true
        );
        ctx.getSource().sendSuccess(
            () -> Component.translatable("message.eunetwork.network_storage", Component.literal(nf.format(network.getStorage())).withStyle(NUMBER)),
            true
        );
    }

    private static void sendNetworkInfo(CommandContext<CommandSourceStack> ctx, EUNetworkBase network) {

    }

    private static final LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(EUNet.MODID)
        .then(Commands.literal("get")
            .executes(ctx -> {
                ServerPlayer player = ctx.getSource().getPlayerOrException();
                for (EUNetworkBase network : EUNetworkData.getAllNetworks()) {
                    if (network.canPlayerAccess(player)) {
                        sendStorage(ctx, network);
                    }
                }
                return 1;
            })
            .then(NETWORK_ARGUMENT
                .executes(ctx -> {
                    int id = IntegerArgumentType.getInteger(ctx, "id");
                    EUNetworkBase network = EUNetworkData.getNetwork(id);
                    if (network == null) {
                        ctx.getSource().sendFailure(Component.translatable("message.eunetwork.invalid_network", id));
                    } else {
                        sendStorage(ctx, network);
                    }
                    return 1;
                })))
        .then(Commands.literal("add")
            .requires(HAS_PERMISSION)
            .then(NETWORK_ARGUMENT
                .then(Commands.argument("value", StringArgumentType.string())
                    .executes(ctx -> {
                        int id = IntegerArgumentType.getInteger(ctx, "id");
                        String valueString = StringArgumentType.getString(ctx, "value");
                        BigInteger value;
                        try {
                            value = new BigInteger(valueString);
                        } catch (NumberFormatException e) {
                            ctx.getSource().sendFailure(Component.translatable("message.eunetwork.invalid_number", valueString));
                            return 0;
                        }
                        EUNetworkBase network = EUNetworkData.getNetwork(id);
                        if (network == null) {
                            ctx.getSource().sendFailure(Component.translatable("message.eunetwork.invalid_network", id));
                        } else {
                            BigInteger inserted = network.addEnergy(value);
                            ctx.getSource().sendSuccess(
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
