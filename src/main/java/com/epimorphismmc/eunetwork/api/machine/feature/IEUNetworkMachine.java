package com.epimorphismmc.eunetwork.api.machine.feature;

import com.epimorphismmc.eunetwork.api.IEUNetwork;
import com.epimorphismmc.eunetwork.common.EUNetworkBase;
import com.epimorphismmc.monomorphism.machine.feature.IOwnableMachine;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IEUNetworkMachine extends IOwnableMachine {

    boolean setEUNetwork(@NotNull Player player, @Nullable IEUNetwork network);

    @Nullable IEUNetwork getEUNetwork();

    boolean canAccessNetwork();

}
