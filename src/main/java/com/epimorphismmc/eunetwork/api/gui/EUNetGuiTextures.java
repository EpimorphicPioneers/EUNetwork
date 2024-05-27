package com.epimorphismmc.eunetwork.api.gui;

import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

public class EUNetGuiTextures {

    public static final ResourceTexture ADD_OVERLAY = createTexture("overlay/add_overlay.png");

    public static final ResourceBorderTexture BUTTON_NO_BORDER_LIGHT = createBorderTexture("widget/button_no_border_light.png", 32, 32, 1, 1);
    public static final ResourceBorderTexture BUTTON_HALF_NO_BORDER = createBorderTexture("widget/button_half_no_border.png", 32, 32, 1, 1);

    private static ResourceTexture createTexture(String imageLocation) {
        return new ResourceTexture("eunetwork:textures/gui/%s".formatted(imageLocation));
    }

    private static ResourceBorderTexture createBorderTexture(String imageLocation, int imageWidth, int imageHeight, int cornerWidth, int cornerHeight) {
        return new ResourceBorderTexture("eunetwork:textures/gui/%s".formatted(imageLocation), imageWidth, imageHeight, cornerWidth, cornerHeight);
    }
}
