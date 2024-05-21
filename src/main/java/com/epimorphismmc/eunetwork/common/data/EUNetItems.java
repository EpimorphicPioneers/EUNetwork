package com.epimorphismmc.eunetwork.common.data;

import com.epimorphismmc.eunetwork.common.item.behaviors.NetworkTerminalBehavior;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.tterrag.registrate.util.entry.ItemEntry;

import static com.epimorphismmc.eunetwork.EUNet.registrate;
import static com.gregtechceu.gtceu.common.data.GTItems.attach;

public class EUNetItems {

    public final static ItemEntry<ComponentItem> EUNETWORK_TERMINAL = registrate().item("eunetwork_terminal", ComponentItem::create)
            .onRegister(attach(new NetworkTerminalBehavior()))
            .register();

    public static void init() {

    }
}
