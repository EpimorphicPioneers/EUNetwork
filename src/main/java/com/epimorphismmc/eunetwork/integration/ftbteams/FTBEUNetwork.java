package com.epimorphismmc.eunetwork.integration.ftbteams;

import com.epimorphismmc.eunetwork.EUNet;
import com.epimorphismmc.eunetwork.api.AccessLevel;
import com.epimorphismmc.eunetwork.api.IEUNetwork;
import com.epimorphismmc.eunetwork.api.IEUNetworkFactory;
import com.epimorphismmc.eunetwork.api.NetworkMember;
import com.epimorphismmc.eunetwork.common.ServerEUNetwork;
import com.epimorphismmc.monomorphism.utility.MOUtils;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.TeamManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.epimorphismmc.eunetwork.api.EUNetValues.*;

public class FTBEUNetwork extends ServerEUNetwork {

    public static final IEUNetworkFactory<FTBEUNetwork> FACTORY = new IEUNetworkFactory<>() {
        @Override
        public FTBEUNetwork createEUNetwork(CompoundTag tag, byte type) {
            var network = new FTBEUNetwork();
            network.deserializeNBT(tag, type);
            return network;
        }

        @Override
        public ResourceLocation getType() {
            return EUNet.id("ftb");
        }
    };

    @NotNull
    @Override
    public Collection<NetworkMember> getAllMembers() {
        return getTeam()
                .map(Team::getMembers)
                .stream()
                .flatMap(Set::stream)
                .map(this::getMemberByUUID)
                .toList();
    }

    @Nullable
    @Override
    public NetworkMember getMemberByUUID(@NotNull UUID uuid) {
        var accessLevel = getPlayerAccess(uuid);
        if (accessLevel == AccessLevel.BLOCKED) return null;

        var player = MOUtils.getPlayerByUUID(uuid);
        if (player == null) return null;
        return NetworkMember.create(player, accessLevel);
    }

    @Override
    public boolean canPlayerAccess(@NotNull UUID uuid) {
        return getTeam()
                .map(team -> team.getRankForPlayer(uuid).isMemberOrBetter())
                .orElse(false);
    }

    @NotNull
    @Override
    public AccessLevel getPlayerAccess(@NotNull UUID uuid) {
        return getTeam()
                .map(team -> team.getRankForPlayer(uuid))
                .map(TeamUtils::rankToAccessLevel)
                .orElse(AccessLevel.BLOCKED);
    }

    @Override
    public int changeMembership(@NotNull Player player, @NotNull UUID targetUUID, byte type) {
        return RESPONSE_REJECT; // We delegate access management to FTB Teams
    }

    @Override
    public IEUNetworkFactory<? extends IEUNetwork> getFactory() {
        return FACTORY;
    }

    private Optional<Team> getTeam() {
        return getManager().getTeamByID(getOwner());
    }

    private TeamManager getManager() {
        return FTBTeamsAPI.api().getManager();
    }
}
