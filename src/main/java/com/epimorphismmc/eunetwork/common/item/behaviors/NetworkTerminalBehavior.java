package com.epimorphismmc.eunetwork.common.item.behaviors;

import com.epimorphismmc.eunetwork.api.machine.feature.IEUNetworkMachine;
import com.epimorphismmc.eunetwork.common.EUNetworkData;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class NetworkTerminalBehavior implements IInteractionItem {

    @Nullable
    protected CompoundTag getTerminalStatsTag(ItemStack itemStack) {
        return itemStack.getTagElement("EUNet.TerminalStats");
    }

    protected CompoundTag getOrCreateTerminalStatsTag(ItemStack itemStack) {
        return itemStack.getOrCreateTagElement("EUNet.TerminalStats");
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() != null) {
            Level level = context.getLevel();
            BlockPos blockPos = context.getClickedPos();
            if (context.getPlayer() != null && MetaMachine.getMachine(level, blockPos) instanceof IEUNetworkMachine networkMachine) {
                if (!level.isClientSide) {
                    var network = EUNetworkData.getInstance().createNetwork(context.getPlayer(), "Test");
                    if (network != null) {
                        networkMachine.setEUNetwork(context.getPlayer(), network);
                        getOrCreateTerminalStatsTag(context.getItemInHand()).putInt("network", network.getId());
                    } else if (getOrCreateTerminalStatsTag(context.getItemInHand()).contains("network")) {
                        networkMachine.setEUNetwork(context.getPlayer(), EUNetworkData.getNetwork(getOrCreateTerminalStatsTag(context.getItemInHand()).getInt("network")));
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }
        return InteractionResult.PASS;
    }
}
