package com.epimorphismmc.eunetwork.common.item.behaviors;

import com.epimorphismmc.eunetwork.api.machine.feature.IEUNetworkMachine;
import com.epimorphismmc.eunetwork.common.EUNetworkData;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
        var player = context.getPlayer();
        if (player != null) {
            Level level = context.getLevel();
            BlockPos blockPos = context.getClickedPos();
            if (MetaMachine.getMachine(level, blockPos) instanceof IEUNetworkMachine networkMachine) {
                if (!level.isClientSide) {
                    var tag = getOrCreateTerminalStatsTag(context.getItemInHand());
                    if (tag.contains("network", Tag.TAG_ANY_NUMERIC)) {
                        networkMachine.setEUNetwork(player, EUNetworkData.getNetwork(tag.getInt("network")));
                    } else {
                        for (var network : EUNetworkData.getAllNetworks()) {
                            if (network.getOwnerUUID().equals(player.getUUID())) {
                                networkMachine.setEUNetwork(player, network);
                                tag.putInt("network", network.getId());
                                return InteractionResult.CONSUME;
                            }
                        }
                        var network = EUNetworkData.getInstance().createNetwork(player, player.getName().getString() + " s' Network");
                        if (network != null) {
                            networkMachine.setEUNetwork(context.getPlayer(), network);
                            tag.putInt("network", network.getId());
                        }
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }
}
