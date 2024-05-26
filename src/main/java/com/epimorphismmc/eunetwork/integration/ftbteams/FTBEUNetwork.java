package com.epimorphismmc.eunetwork.integration.ftbteams;

import com.epimorphismmc.eunetwork.api.AccessLevel;
import com.epimorphismmc.eunetwork.api.IEUNetwork;
import com.epimorphismmc.eunetwork.api.IEUNetworkFactory;
import com.epimorphismmc.eunetwork.api.NetworkMember;
import com.epimorphismmc.eunetwork.common.ServerEUNetwork;
import com.epimorphismmc.eunetwork.common.data.EUNetworkTypes;
import com.epimorphismmc.eunetwork.integration.EUNetIntegration;
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

import static com.epimorphismmc.eunetwork.api.EUNetValues.RESPONSE_REJECT;

public class FTBEUNetwork extends ServerEUNetwork {

    public static final IEUNetworkFactory<FTBEUNetwork> FACTORY = new IEUNetworkFactory<>() {

        @Override
        public FTBEUNetwork createEUNetwork(int id, String name, @NotNull Player owner) {
            return new FTBEUNetwork(id, name, owner);
        }

        @Override
        public FTBEUNetwork deserialize(CompoundTag tag, byte type) {
            var network = new FTBEUNetwork();
            network.deserializeNBT(tag, type);
            return network;
        }

        @Override
        public ResourceLocation getType() {
            return EUNetworkTypes.FTB;
        }
    };

    private InnerEUNetwork innerNetwork;

    public FTBEUNetwork() {
        if (!EUNetIntegration.isFTBTeamsLoaded()) {
            this.innerNetwork = new InnerEUNetwork();
        }
    }

    public FTBEUNetwork(int id, String name, @NotNull Player owner) {
        super(id, name, owner);
        if (!EUNetIntegration.isFTBTeamsLoaded()) {
            this.innerNetwork = new InnerEUNetwork();
        }
    }

    @NotNull
    @Override
    public Collection<NetworkMember> getAllMembers() {
        if (EUNetIntegration.isFTBTeamsLoaded()) {
            return this.innerNetwork.getAllMembers();
        }
        return super.getAllMembers();
    }

    @Nullable
    @Override
    public NetworkMember getMemberByUUID(@NotNull UUID uuid) {
        if (EUNetIntegration.isFTBTeamsLoaded()) {
            return this.innerNetwork.getMemberByUUID(uuid);
        }
        return super.getMemberByUUID(uuid);
    }

    @Override
    public boolean canPlayerAccess(@NotNull UUID uuid) {
        if (EUNetIntegration.isFTBTeamsLoaded()) {
            return this.innerNetwork.canPlayerAccess(uuid);
        }
        return super.canPlayerAccess(uuid);
    }

    @NotNull
    @Override
    public AccessLevel getPlayerAccess(@NotNull UUID uuid) {
        if (EUNetIntegration.isFTBTeamsLoaded()) {
            return this.innerNetwork.getPlayerAccess(uuid);
        }
        return super.getPlayerAccess(uuid);
    }

    @Override
    public int changeMembership(@NotNull Player player, @NotNull UUID targetUUID, byte type) {
        if (EUNetIntegration.isFTBTeamsLoaded()) {
            return this.innerNetwork.changeMembership(player, targetUUID, type);
        }
        return super.changeMembership(player, targetUUID, type);
    }

    @Override
    public IEUNetworkFactory<? extends IEUNetwork> getFactory() {
        return FACTORY;
    }

    private class InnerEUNetwork {
        @NotNull
        public Collection<NetworkMember> getAllMembers() {
            return getTeam()
                    .map(Team::getMembers)
                    .stream()
                    .flatMap(Set::stream)
                    .map(this::getMemberByUUID)
                    .toList();
        }

        @Nullable
        public NetworkMember getMemberByUUID(@NotNull UUID uuid) {
            var accessLevel = getPlayerAccess(uuid);
            if (accessLevel == AccessLevel.BLOCKED) return null;

            var player = MOUtils.getPlayerByUUID(uuid);
            if (player == null) return null;
            return NetworkMember.create(player, accessLevel);
        }

        public boolean canPlayerAccess(@NotNull UUID uuid) {
            return getTeam()
                    .map(team -> team.getRankForPlayer(uuid).isMemberOrBetter())
                    .orElse(false);
        }

        @NotNull
        public AccessLevel getPlayerAccess(@NotNull UUID uuid) {
            return getTeam()
                    .map(team -> team.getRankForPlayer(uuid))
                    .map(TeamUtils::rankToAccessLevel)
                    .orElse(AccessLevel.BLOCKED);
        }

        public int changeMembership(@NotNull Player player, @NotNull UUID targetUUID, byte type) {
            return RESPONSE_REJECT; // We delegate access management to FTB Teams
        }

        private Optional<Team> getTeam() {
            return getManager().getTeamByID(getOwner());
        }

        private TeamManager getManager() {
            return FTBTeamsAPI.api().getManager();
        }
    }
}
