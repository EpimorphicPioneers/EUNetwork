package com.epimorphismmc.eunetwork.common;

import com.epimorphismmc.eunetwork.api.EUNetValues;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientEUNetwork extends EUNetworkBase {
    public ClientEUNetwork(int ignored) {/**/}

    public void fromNetwork(CompoundTag tag, byte type) {
        this.id = tag.getInt(EUNetValues.NETWORK_ID);
        this.name = tag.getString(NETWORK_NAME);
        this.owner = tag.getUUID(OWNER_UUID);
    }
}
