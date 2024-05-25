package com.epimorphismmc.eunetwork.common.data;

import com.epimorphismmc.eunetwork.EUNet;
import com.epimorphismmc.eunetwork.api.AccessLevel;
import com.epimorphismmc.eunetwork.api.EUNetValues;
import com.epimorphismmc.eunetwork.api.NetworkMember;
import com.epimorphismmc.eunetwork.common.EUNetworkBase;
import com.epimorphismmc.eunetwork.common.EUNetworkData;
import com.epimorphismmc.eunetwork.utils.NumberUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.function.Predicate;

public class EUNetCommands {
    private static final NumberFormat nf = NumberFormat.getInstance();

    private static final Style NUMBER = Style.EMPTY.withColor(0x886eff);

    private static final Predicate<CommandSourceStack> HAS_PERMISSION = s -> s.hasPermission(2);
    private static final Supplier<RequiredArgumentBuilder<CommandSourceStack, Integer>> NETWORK_ARGUMENT = () ->
        Commands.argument("id", IntegerArgumentType.integer())
            .suggests((ctx, builder) -> {
                EUNetworkData.getAllNetworks().forEach(n -> builder.suggest(n.getId()));
                return builder.buildFuture();
            });

    private static void sendNetworkInfo(CommandContext<CommandSourceStack> ctx, EUNetworkBase network) {
        ctx.getSource().sendSuccess(
            () -> Component.translatable("message.eunetwork.network_id", Component.literal("" + network.getId()).withStyle(NUMBER)),
            true
        );
        ctx.getSource().sendSuccess(
            () -> Component.translatable("message.eunetwork.network_storage", Component.literal(NumberUtil.formatBigInteger(network.getStorage())).withStyle(NUMBER)),
            true
        );
        ctx.getSource().sendSuccess(
            () -> Component.translatable("message.eunetwork.all_members"),
            true
        );
        Collection<NetworkMember> members = network.getAllMembers().stream()
            .sorted((x, y) -> {
                if (x.getAccessLevel() == AccessLevel.OWNER) return 1;
                if (x.getAccessLevel() == AccessLevel.BLOCKED) return -1;
                return 0;
            }).toList();
        for (NetworkMember member : members) {
            ctx.getSource().sendSuccess(
                () -> Component.literal(" - ")
                    .append(Component.translatable(member.getAccessLevel().getTranslationKey()))
                    .append(Component.literal(": "))
                    .append(Component.literal(member.getCachedName()).withStyle(member.getAccessLevel().getFormatting()))
                ,
                true
            );
        }
    }

    private static final LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(EUNet.MODID)
        .then(Commands.literal("info")
            .executes(ctx -> {
                ServerPlayer player = ctx.getSource().getPlayerOrException();
                for (EUNetworkBase network : EUNetworkData.getAllNetworks()) {
                    if (network.canPlayerAccess(player)) {
                        sendNetworkInfo(ctx, network);
                    }
                }
                return 1;
            })
            .then(NETWORK_ARGUMENT.get()
                .executes(ctx -> {
                    int id = IntegerArgumentType.getInteger(ctx, "id");
                    EUNetworkBase network = EUNetworkData.getNetwork(id);
                    if (network == null) {
                        ctx.getSource().sendFailure(Component.translatable("message.eunetwork.invalid_network", id));
                    } else {
                        sendNetworkInfo(ctx, network);
                    }
                    return 1;
                })))
        .then(Commands.literal("invite")
            .then(NETWORK_ARGUMENT.get()
                .then(Commands.argument("player", EntityArgument.player())
                    .suggests((ctx, builder) -> {
                        for (String playerName : ctx.getSource().getServer().getPlayerNames()) {
                            builder.suggest(playerName);
                        }
                        return builder.buildFuture();
                    })
                    .executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        int id = IntegerArgumentType.getInteger(ctx, "id");
                        ServerPlayer playerInvited = EntityArgument.getPlayer(ctx, "player");
                        EUNetworkBase network = EUNetworkData.getNetwork(id);
                        if (network == null) {
                            ctx.getSource().sendFailure(Component.translatable("message.eunetwork.invalid_network", id));
                        } else {
                            int response = network.changeMembership(player, playerInvited.getUUID(), EUNetValues.MEMBERSHIP_SET_USER);
                            switch (response) {
                                case EUNetValues.RESPONSE_SUCCESS -> ctx.getSource().sendSuccess(
                                    () -> Component.translatable(
                                        "message.eunetwork.invite_successes",
                                        playerInvited.getDisplayName().copy().withStyle(ChatFormatting.LIGHT_PURPLE)
                                    ),
                                    true

                                );
                                case EUNetValues.RESPONSE_NO_OWNER ->
                                    ctx.getSource().sendFailure(Component.translatable("message.eunetwork.not_network_owner"));
                                case EUNetValues.RESPONSE_NO_ADMIN ->
                                    ctx.getSource().sendFailure(Component.translatable("message.eunetwork.not_network_admin"));
                                case EUNetValues.RESPONSE_NO_SPACE ->
                                    ctx.getSource().sendFailure(Component.translatable("message.eunetwork.not_space"));
                                case EUNetValues.RESPONSE_INVALID_USER ->
                                    ctx.getSource().sendFailure(Component.translatable("message.eunetwork.invalid_user"));
                                default ->
                                    ctx.getSource().sendFailure(Component.translatable("message.eunetwork.unknow_response", response));
                            }
                        }
                        return 1;
                    })
                )
            )
        )
        .then(Commands.literal("add")
            .requires(HAS_PERMISSION)
            .then(NETWORK_ARGUMENT.get()
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
                                () -> Component.translatable(
                                    "message.eunetwork.add_successed",
                                    Component.literal("" + network.getId()).withStyle(NUMBER),
                                    Component.literal(nf.format(inserted)).withStyle(NUMBER)),
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
