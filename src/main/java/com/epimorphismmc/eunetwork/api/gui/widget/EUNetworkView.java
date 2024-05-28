package com.epimorphismmc.eunetwork.api.gui.widget;

import com.epimorphismmc.eunetwork.api.gui.EUNetGuiTextures;
import com.epimorphismmc.eunetwork.common.EUNetworkManager;
import com.epimorphismmc.eunetwork.common.data.EUNetCommands;
import com.epimorphismmc.eunetwork.common.data.EUNetworkTypes;
import com.epimorphismmc.eunetwork.utils.EUNetUtils;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.utils.Size;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Objects;

public class EUNetworkView extends WidgetGroup {

    private final List<String> networkTypes = List.copyOf(EUNetworkManager.getNetworkTypes());
    @Setter
    @Getter
    private String type = EUNetworkTypes.BUILT;
    private Player player;
    private int networkId;
    private Widget textInput;

    public EUNetworkView(int xPosition, int yPosition, int width, int height) {
        super(xPosition, yPosition, width, height);

        addWidget(createDisplay());
        addWidget(new EUNetworkListWidget(6, 6, 96, getSizeHeight() - 12, id -> {
            this.networkId = id;
            writeClientAction(2, buf -> buf.writeVarInt(id));
        }));
        var button = new ButtonWidget(104, getSizeHeight() - 18 - 6 - 1, 18, 18, widget -> {
        })
            .setButtonTexture(GuiTextures.BUTTON, EUNetGuiTextures.ADD_OVERLAY);
        var selector = new SelectorWidget(104 + 18 + 2, getSizeHeight() - 18 - 6, 44, 16, networkTypes, -1)
            .setOnChanged(this::setType)
            .setSupplier(this::getType)
            .setValue(EUNetworkTypes.BUILT)
            .setIsUp(true)
            .setButtonBackground(ColorPattern.BLACK.rectTexture(), ColorPattern.GRAY.borderTexture(1))
            .setBackground(ColorPattern.BLACK.rectTexture(), ColorPattern.GRAY.borderTexture(1));
        var display = new DraggableScrollableWidgetGroup(104 + 2, getSizeHeight() - 18 - 12 - 50, 44 + 18, 50)
            .addWidget(new ComponentPanelWidget(4, 4, this::addDisplay).setMaxWidthLimit(60))
            .setBackground(ColorPattern.BLACK.rectTexture(), ColorPattern.GRAY.borderTexture(1));
        addWidget(display);
        addWidget(button);
        addWidget(selector);
    }

    private void addDisplay(List<Component> components) {
        if (this.player != null) {
            components.add(ComponentPanelWidget.withHoverTextTranslate(Component.translatable("eunetwork.player", player.getDisplayName()), Component.literal(player.getStringUUID())));
            components.add(Component.translatable("eunetwork.network_number", EUNetworkManager.getInstance().getAllNetworks().stream().filter(euNetwork -> euNetwork.canPlayerAccess(player)).count()));
        }
    }

    private void addMainDisplay(List<Component> components) {
        var network = EUNetworkManager.getInstance().getNetwork(networkId);
        if (network != null) {
            components.add(Component.literal(network.getName())
                .append(ComponentPanelWidget.withButton(Component.literal(" [✎]")
                        .withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("message.eunetwork.click_to_change_name")))),
                    "name", Objects.requireNonNull(ChatFormatting.GREEN.getColor()))));
            components.add(Component.literal("------------------------------------"));
            components.add(Component.translatable("message.eunetwork.network_id", Component.literal("" + network.getId()).withStyle(ChatFormatting.GOLD)));
            components.add(Component.translatable("message.eunetwork.network_storage", EUNetCommands.numberText(network.getStorage())));
        }
    }

    private Widget createDisplay() {
        int width = 222;
        int height = 106;
        var group = new WidgetGroup(getSizeWidth() - (width + 8) - 6, 6, width + 8, height + 8);
        group.addWidget(new DraggableScrollableWidgetGroup(4, 4, width, height).setBackground(GuiTextures.DISPLAY)
            .addWidget(new ComponentPanelWidget(4, 4, this::addMainDisplay)
                .clickHandler(this::handleDisplayClick)
                .setMaxWidthLimit(225)));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    public void handleDisplayClick(String componentData, ClickData clickData) {
        if (componentData.equals("name") && textInput == null) {
            Size size = getSize();
            String networkName;
            if (!isRemote()) {
                networkName = EUNetworkManager.getInstance().getNetwork(networkId).getName();
            } else {
                networkName = "";
            }
            this.textInput = new TextInputWidget((size.width - 120) / 2, (size.height - 60) / 2, 120, 60)
                .setText(networkName)
                .setValidator(string -> EUNetUtils.isBadNetworkName(string) ? "" : string)
                .setOnConfirm(string -> {
                    if (!isRemote()) {
                        EUNetworkManager.getInstance().getNetwork(networkId).setName(string);
                    }
                    removeWidget(textInput);
                    this.textInput = null;
                })
                .setOnCancel(string -> {
                    removeWidget(textInput);
                    this.textInput = null;
                })
                .setBackground(GuiTextures.BACKGROUND);
            addWidget(textInput);
        }
    }

    @Override
    public void initWidget() {
        super.initWidget();
        this.player = gui.entityPlayer;
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        super.handleClientAction(id, buffer);
        if (id == 2) {
            this.networkId = buffer.readVarInt();
        }
    }
}
