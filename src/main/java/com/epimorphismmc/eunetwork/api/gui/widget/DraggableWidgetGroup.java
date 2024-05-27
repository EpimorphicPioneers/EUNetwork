package com.epimorphismmc.eunetwork.api.gui.widget;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DraggableWidgetGroup extends WidgetGroup {
    protected double lastDeltaX, lastDeltaY;
    protected int dragOffsetX, dragOffsetY;
    protected boolean isDragging;

    public DraggableWidgetGroup() {
    }

    public DraggableWidgetGroup(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public DraggableWidgetGroup(Position position) {
        super(position);
    }

    public DraggableWidgetGroup(Position position, Size size) {
        super(position, size);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.lastDeltaX = 0;
        this.lastDeltaY = 0;
        this.isDragging = false;
        if (isMouseOver(getPosition().x, getPosition().y, getSizeWidth(), getSizeHeight(), mouseX, mouseY)) {
            isDragging = true;
            return super.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button) || isMouseOverElement(mouseX, mouseY);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        double dx = dragX + lastDeltaX;
        double dy = dragY + lastDeltaY;
        dragX = (int) dx;
        dragY = (int) dy;
        lastDeltaX = dx - dragX;
        lastDeltaY = dy - dragY;
        if (isDragging) {
            this.dragOffsetX += (int) dragX;
            this.dragOffsetY += (int) dragY;
            this.addSelfPosition((int) dragX, (int) dragY);
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY) || isMouseOverElement(mouseX, mouseY);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.lastDeltaX = 0;
        this.lastDeltaY = 0;
        this.isDragging = false;
        return super.mouseReleased(mouseX, mouseY, button) || isMouseOverElement(mouseX, mouseY);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        return super.mouseWheelMove(mouseX, mouseY, wheelDelta) || isMouseOverElement(mouseX, mouseY);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseMoved(double mouseX, double mouseY) {
        return super.mouseMoved(mouseX, mouseY) || isMouseOverElement(mouseX, mouseY);
    }
}
