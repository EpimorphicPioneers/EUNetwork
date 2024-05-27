package com.epimorphismmc.eunetwork.api.gui.widget;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.util.Mth;

import java.util.function.Consumer;
import java.util.function.Function;

public class TextInputWidget extends DraggableWidgetGroup {

    @Setter @Accessors(chain = true)
    private Consumer<String> onConfirm;
    @Setter @Accessors(chain = true)
    private Consumer<String> onCancel;
    private TextFieldWidget textField;
    @Setter @Getter
    private String text = "";

    public TextInputWidget() {
    }

    public TextInputWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
        int widgetHeight = (height - 18) / 2;
        int widgetWidth = (width - 18) / 2;
        this.textField = new TextFieldWidget(6, 6, width - 12, widgetHeight, this::getText, this::setText);
        addWidget(textField);
        addWidget(new ButtonWidget(6, widgetHeight + 12, widgetWidth, widgetHeight, cd -> {
            if (onConfirm != null) onConfirm.accept(text);
        }).setButtonTexture(GuiTextures.BUTTON, new TextTexture(LocalizationUtils.format("eunetwork.confirm"))));
        addWidget(new ButtonWidget(widgetWidth + 12, widgetHeight + 12, widgetWidth, widgetHeight, cd -> {
            if (onCancel != null) onCancel.accept(text);
        }).setButtonTexture(GuiTextures.BUTTON, new TextTexture(LocalizationUtils.format("eunetwork.cancel"))));
    }

    public TextInputWidget setValidator(Function<String, String> validator) {
        this.textField.setValidator(validator);
        return this;
    }

}
