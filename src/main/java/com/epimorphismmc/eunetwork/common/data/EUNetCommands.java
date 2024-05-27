package com.epimorphismmc.eunetwork.common.data;

import com.epimorphismmc.eunetwork.EUNet;
import com.epimorphismmc.eunetwork.api.AccessLevel;
import com.epimorphismmc.eunetwork.api.EUNetValues;
import com.epimorphismmc.eunetwork.api.NetworkMember;
import com.epimorphismmc.eunetwork.common.EUNetwork;
import com.epimorphismmc.eunetwork.common.EUNetworkBase;
import com.epimorphismmc.eunetwork.common.EUNetworkManager;
import com.epimorphismmc.eunetwork.utils.EUNetUtils;
import com.epimorphismmc.monomorphism.utility.MOFormattingUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.function.Predicate;

public class EUNetCommands {
    private static final NumberFormat nf = NumberFormat.getInstance();

    private static final Style NUMBER = Style.EMPTY.withColor(0x886EFF);
    private static final Style UNIT = Style.EMPTY.withColor(0xEE82EE);

    private static final Predicate<CommandSourceStack> HAS_PERMISSION = s -> s.hasPermission(2);
    private static final SuggestionProvider<CommandSourceStack> ALL_NETWORK_SUGGESTIONS = (ctx, builder) -> {
        EUNetworkManager.getInstance().getAllNetworks().forEach(n -> builder.suggest(n.getId()));
        return builder.buildFuture();
    };
    private static final SuggestionProvider<CommandSourceStack> OWNER_NETWORK_SUGGESTIONS = (ctx, builder) -> {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return Suggestions.empty();
        EUNetworkManager.getInstance().getAllNetworks().stream()
            .filter(n -> n.getOwner().equals(player.getUUID()))
            .forEach(n -> builder.suggest(n.getId()));
        return builder.buildFuture();
    };
    private static final SuggestionProvider<CommandSourceStack> ACCESSABLE_NETWORK_SUGGESTIONS = (ctx, builder) -> {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return Suggestions.empty();
        EUNetworkManager.getInstance().getAllNetworks().stream()
            .filter(n -> n.canPlayerAccess(player.getUUID()))
            .forEach(n -> builder.suggest(n.getId()));
        return builder.buildFuture();
    };
    private static final SuggestionProvider<CommandSourceStack> ALL_PLAYER_SUGGESTIONS = (ctx, builder) -> {
        for (String playerName : ctx.getSource().getServer().getPlayerNames()) {
            builder.suggest(playerName);
        }
        return builder.buildFuture();
    };

    private static @NotNull Component kickButton(String command) {
        return Component.literal(" [X]").withStyle(style -> style.withColor(ChatFormatting.RED)
            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("message.eunetwork.click_to_kick")))
            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))
        );
    }

    private static @NotNull Component transferButton(String command) {
        return Component.literal(" [→]").withStyle(style -> style.withColor(ChatFormatting.DARK_RED)
            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("message.eunetwork.click_to_transfer")))
            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))
        );
    }

    private static @NotNull Component modifyButton(String command) {
        return Component.literal(" [✎]").withStyle(style -> style.withColor(ChatFormatting.GREEN)
            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("message.eunetwork.click_to_change_name")))
            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))
        );
    }

    public static @NotNull Component numberText(BigInteger number) {
        String text = MOFormattingUtils.abbreviate2F(number);

        if (text.matches(".*[a-zA-Z]$")) {
            return Component.literal(text.substring(0, text.length() - 2)).withStyle(NUMBER).append(Component.literal(text.substring(text.length()-1)).withStyle(UNIT));
        } else if (text.contains("E")) {
            int count = 0;
            for (int i = 0; i < text.length(); i++) {
                if (text.charAt(i) == 'E') {
                    count++;
                    if (i == text.length() - 1) {
                        count = -1;
                        break;
                    }
                }
            }
            if (count == 1) {
                String[] split = text.split("E");
                return Component.literal(split[0]).withStyle(NUMBER)
                    .append(Component.literal("e+").withStyle(UNIT))
                    .append(Component.literal(split[1]).withStyle(NUMBER));
            }
        }
        return Component.literal(text).withStyle(NUMBER);
    }

    private static int sendHelpMessage(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(
            () -> Component.empty()
                .append(Component.literal("======= ").withStyle(ChatFormatting.GRAY))
                .append(Component.translatable("message.eunetwork.help_message").withStyle(ChatFormatting.AQUA))
                .append(Component.literal(" =======").withStyle(ChatFormatting.GRAY)),
            true
        );
        for (var sub : EUNetSubCommand.values()) {
            ctx.getSource().sendSuccess(
                () -> Component.literal("/%s %s: ".formatted(EUNet.MODID, sub.getSubCommand()))
                    .withStyle(style -> style.withColor(ChatFormatting.GRAY)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/%s %s ".formatted(EUNet.MODID, sub.getSubCommand())))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("message.eunetwork.click_to_fill")))
                    )
                    .append(Component.translatable(sub.getTranslateKey()).withStyle(ChatFormatting.GREEN)),
                true
            );
        }
        return 1;
    }

    private static int createNewNetwork(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
        if (EUNetUtils.isBadNetworkName(name)) {
            ctx.getSource().sendFailure(Component.translatable("message.eunetwork.invalid_network_name", name));
        } else {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            EUNetworkBase network = EUNetworkManager.getInstance().createNetwork(player, name, EUNetworkTypes.BUILT);
            if (network == null) {
                ctx.getSource().sendFailure(Component.translatable("message.eunetwork.network_limited"));
            } else {
                ctx.getSource().sendSuccess(
                    () -> Component.translatable("message.eunetwork.create_success"),
                    true
                );
            }
        }
        return 1;
    }

    private static void sendNetworkInfo(CommandContext<CommandSourceStack> ctx, EUNetwork network) {
        ctx.getSource().sendSuccess(
            () -> Component.translatable("message.eunetwork.network_name", Component.literal(network.getName()).withStyle(ChatFormatting.AQUA))
                .append(modifyButton("/%s %s %s %d %s".formatted(EUNet.MODID, "modify", "name", network.getId(), network.getName()))),
            true
        );
        ctx.getSource().sendSuccess(
            () -> Component.translatable("message.eunetwork.network_id", Component.literal("" + network.getId()).withStyle(ChatFormatting.GOLD)),
            true
        );
        ctx.getSource().sendSuccess(
            () -> Component.translatable("message.eunetwork.network_storage", numberText(network.getStorage())),
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
                    .append(kickButton("/%s %s %d %s".formatted(EUNet.MODID, "kick", network.getId(), member.getCachedName())))
                    .append(transferButton("/%s %s %s %d".formatted(EUNet.MODID, "transfer", member.getCachedName(), network.getId())))
                ,
                true
            );
        }
    }


    private static void sendChangeMembershipResponse(CommandContext<CommandSourceStack> ctx, int response) {
        switch (response) {
            case EUNetValues.RESPONSE_SUCCESS -> ctx.getSource().sendSuccess(
                () -> Component.translatable("message.eunetwork.successes"),
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

    private static final LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(EUNet.MODID)
        .executes(EUNetCommands::sendHelpMessage)
        .then(Commands.literal("help")
            .executes(EUNetCommands::sendHelpMessage)
        )
        .then(Commands.literal("info")
            .executes(ctx -> {
                ServerPlayer player = ctx.getSource().getPlayerOrException();
                for (EUNetwork network : EUNetworkManager.getInstance().getAllNetworks()) {
                    if (network.canPlayerAccess(player)) {
                        sendNetworkInfo(ctx, network);
                    }
                }
                return 1;
            })
            .then(Commands.argument("id", IntegerArgumentType.integer())
                .suggests(ACCESSABLE_NETWORK_SUGGESTIONS)
                .executes(ctx -> {
                    int id = IntegerArgumentType.getInteger(ctx, "id");
                    EUNetwork network = EUNetworkManager.getInstance().getNetwork(id);
                    if (network == null) {
                        ctx.getSource().sendFailure(Component.translatable("message.eunetwork.invalid_network", id));
                    } else {
                        sendNetworkInfo(ctx, network);
                    }
                    return 1;
                })
            )
        )
        .then(Commands.literal("invite")
            .then(Commands.argument("id", IntegerArgumentType.integer())
                .suggests(OWNER_NETWORK_SUGGESTIONS)
                .then(Commands.argument("player", EntityArgument.player())
                    .suggests(ALL_PLAYER_SUGGESTIONS)
                    .executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        int id = IntegerArgumentType.getInteger(ctx, "id");
                        ServerPlayer playerInvited = EntityArgument.getPlayer(ctx, "player");
                        EUNetwork network = EUNetworkManager.getInstance().getNetwork(id);
                        if (network == null) {
                            ctx.getSource().sendFailure(Component.translatable("message.eunetwork.invalid_network", id));
                        } else {
                            int response = network.changeMembership(player, playerInvited.getUUID(), EUNetValues.MEMBERSHIP_SET_USER);
                            sendChangeMembershipResponse(ctx, response);
                        }
                        return 1;
                    })
                )
            )
        )
        .then(Commands.literal("leave")
            .then(Commands.argument("id", IntegerArgumentType.integer())
                .suggests(ACCESSABLE_NETWORK_SUGGESTIONS)
                .executes(ctx -> {
                    int id = IntegerArgumentType.getInteger(ctx, "id");
                    EUNetwork network = EUNetworkManager.getInstance().getNetwork(id);
                    if (network == null) {
                        ctx.getSource().sendFailure(Component.translatable("message.eunetwork.invalid_network", id));
                    } else {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        int response = network.changeMembership(player, player.getUUID(), EUNetValues.MEMBERSHIP_CANCEL_MEMBERSHIP);
                        sendChangeMembershipResponse(ctx, response);
                    }
                    return 1;
                })
            )
        )
        .then(Commands.literal("kick")
            .then(Commands.argument("id", IntegerArgumentType.integer())
                .suggests(OWNER_NETWORK_SUGGESTIONS)
                .then(Commands.argument("player", EntityArgument.player())
                    .suggests(ALL_PLAYER_SUGGESTIONS)
                    .executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        int id = IntegerArgumentType.getInteger(ctx, "id");
                        ServerPlayer playerKicked = EntityArgument.getPlayer(ctx, "player");
                        EUNetwork network = EUNetworkManager.getInstance().getNetwork(id);
                        if (network == null) {
                            ctx.getSource().sendFailure(Component.translatable("message.eunetwork.invalid_network", id));
                        } else {
                            int response = network.changeMembership(player, playerKicked.getUUID(), EUNetValues.MEMBERSHIP_CANCEL_MEMBERSHIP);
                            sendChangeMembershipResponse(ctx, response);
                        }
                        return 1;
                    })
                )
            )
        )
        .then(Commands.literal("transfer")
            .then(Commands.argument("id", IntegerArgumentType.integer())
                .suggests(OWNER_NETWORK_SUGGESTIONS)
                .then(Commands.argument("player", EntityArgument.player())
                    .suggests(ALL_PLAYER_SUGGESTIONS)
                    .executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        int id = IntegerArgumentType.getInteger(ctx, "id");
                        ServerPlayer playerTransfer = EntityArgument.getPlayer(ctx, "player");
                        EUNetwork network = EUNetworkManager.getInstance().getNetwork(id);
                        if (network == null) {
                            ctx.getSource().sendFailure(Component.translatable("message.eunetwork.invalid_network", id));
                        } else {
                            int response = network.changeMembership(player, playerTransfer.getUUID(), EUNetValues.MEMBERSHIP_TRANSFER_OWNERSHIP);
                            sendChangeMembershipResponse(ctx, response);
                        }
                        return 1;
                    })
                )
            )
        )
        .then(Commands.literal("create")
            .executes(ctx -> {
                ServerPlayer player = ctx.getSource().getPlayerOrException();
                return createNewNetwork(ctx, player.getName().getString() + " s' Network");
            })
            .then(Commands.argument("name", StringArgumentType.greedyString())
                .executes(ctx -> {
                    String name = StringArgumentType.getString(ctx, "name");
                    return createNewNetwork(ctx, name);
                })
            )
        )
        .then(Commands.literal("modify")
            .then(Commands.literal("name")
                .then(Commands.argument("id", IntegerArgumentType.integer())
                    .suggests(OWNER_NETWORK_SUGGESTIONS)
                    .then(Commands.argument("name", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "name");
                            int id = IntegerArgumentType.getInteger(ctx, "id");
                            EUNetwork network = EUNetworkManager.getInstance().getNetwork(id);
                            if (network == null) {
                                ctx.getSource().sendFailure(Component.translatable("message.eunetwork.invalid_network", id));
                            } else {
                                if (network.setName(name)) {
                                    ctx.getSource().sendSuccess(
                                        () -> Component.translatable("message.eunetwork.modify_name_successed"),
                                        true
                                    );
                                } else {
                                    ctx.getSource().sendFailure(Component.translatable("message.eunetwork.invalid_network_name", name));
                                }
                            }
                            return 1;
                        })
                    )
                )
            )
        )
        .then(Commands.literal("add")
            .requires(HAS_PERMISSION)
            .then(Commands.argument("id", IntegerArgumentType.integer())
                .suggests(ALL_NETWORK_SUGGESTIONS)
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
                        EUNetwork network = EUNetworkManager.getInstance().getNetwork(id);
                        if (network == null) {
                            ctx.getSource().sendFailure(Component.translatable("message.eunetwork.invalid_network", id));
                        } else {
                            BigInteger inserted = network.addEnergy(value);
                            ctx.getSource().sendSuccess(
                                () -> Component.translatable(
                                    "message.eunetwork.add_successed",
                                    Component.literal("" + network.getId()).withStyle(ChatFormatting.GOLD),
                                    Component.literal(nf.format(inserted)).withStyle(NUMBER)),
                                true
                            );
                        }
                        return 1;
                    })
                )
            )
        );

    public static void init(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(root);
    }

    @Getter
    public enum EUNetSubCommand {
        HELP("help", "Show the message", "显示这条信息"),
        INFO("info", "Show the network information", "显示网络信息"),
        INVITE("invite", "Invite other to the network", "邀请玩家来到这个网络"),
        LEAVE("leave", "Leave the network", "离开这个网络"),
        KICK("kick", "Kick other from your network", "从你的网络踢出玩家"),
        TRANSFER("transfer", "Transfer your network to other", "转让你的网络所有权给别的玩家"),
        CREATE("create", "Create a new network", "创建一个新网络"),
        MODIFY("modify", "Modify network", "修改网络"),
        ADD("add", "Add energy to the network", "添加能量到网络中");

        private final String subCommand;
        private final String enHelpMessage;
        private final String cnHelpMessage;

        EUNetSubCommand(String subCommand, String enHelpMessage, String cnHelpMessage) {
            this.subCommand = subCommand;
            this.enHelpMessage = enHelpMessage;
            this.cnHelpMessage = cnHelpMessage;
        }

        public String getTranslateKey() {
            return "message.%s.command.%s".formatted(EUNet.MODID, subCommand);
        }
    }
}
