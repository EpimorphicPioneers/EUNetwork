package com.epimorphismmc.eunetwork.api.gui.widget;

import com.epimorphismmc.eunetwork.EUNet;
import com.epimorphismmc.eunetwork.api.EUNetValues;
import com.epimorphismmc.eunetwork.api.IEUNetwork;
import com.epimorphismmc.eunetwork.api.gui.EUNetGuiTextures;
import com.epimorphismmc.eunetwork.client.ClientEUNetworkManager;
import com.epimorphismmc.eunetwork.common.EUNetworkManager;
import com.epimorphismmc.eunetwork.network.eunetwork.MessageHandler;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.widget.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class EUNetworkListWidget extends WidgetGroup implements SearchComponentWidget.IWidgetSearch<Object> {
    private final DraggableScrollableWidgetGroup networkList;
    private final Map<Integer, SelectableWidgetGroup> selectedMap = new ConcurrentHashMap<>();

    private final Consumer<Integer> onSelected;

    public EUNetworkListWidget(int xPosition, int yPosition, int width, int height, Consumer<Integer> onSelected) {
        super(xPosition, yPosition, width, height);
        this.onSelected = onSelected;
        var listContainer = new WidgetGroup(0, 21, getSizeWidth(), height - 21);
        this.networkList = new DraggableScrollableWidgetGroup(3, 3,
                listContainer.getSizeWidth() - 6, listContainer.getSizeHeight() - 6)
                .setYScrollBarWidth(0);
        listContainer.addWidget(networkList)
                .setBackground(GuiTextures.BACKGROUND_INVERSE);
        addWidget(new SearchComponentWidget<>(0, 0, getSizeWidth(), 18, this));
        addWidget(listContainer);
    }

    private void addEUNetwork(IEUNetwork network) {
        if (!selectedMap.containsKey(network.getId())) {
            var index = networkList.widgets.size();
            var selectableWidgetGroup = new SelectableWidgetGroup(0, index * 24, networkList.getSize().width, 18);
            selectableWidgetGroup.setBackground(EUNetGuiTextures.BUTTON_HALF_NO_BORDER);
            selectableWidgetGroup.setSelectedTexture(ColorPattern.WHITE.borderTexture(-1));
            selectableWidgetGroup.addWidget(new LabelWidget(4, 4, network.getName()));
            selectableWidgetGroup.setOnSelected(widget -> onSelected.accept(network.getId()));
            networkList.addWidget(selectableWidgetGroup);
            selectedMap.put(network.getId(), selectableWidgetGroup);
        }
    }

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        var allNetworks = EUNetworkManager.getInstance().getAllNetworks();
        allNetworks.stream().sorted(Comparator.comparingInt(IEUNetwork::getId)).forEach(this::addEUNetwork);
        EUNet.network().sendToPlayer(MessageHandler.updateNetwork(allNetworks, EUNetValues.NBT_NET_BASIC), (ServerPlayer) gui.entityPlayer);
    }

    @Override
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        var allNetworks = ClientEUNetworkManager.getInstance().getAllNetworks();
        allNetworks.stream().sorted(Comparator.comparingInt(IEUNetwork::getId)).forEach(this::addEUNetwork);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        super.readUpdateInfo(id, buffer);
    }

    @Override
    public String resultDisplay(Object value) {
        return null;
    }

    @Override
    public void selectResult(Object value) {

    }

    @Override
    public void search(String word, Consumer<Object> find) {

    }
}
