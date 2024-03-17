package dev.lazurite.toolbox.api.util;

import dev.lazurite.toolbox.impl.mixin.common.access.IChunkMapMixin;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class PlayerUtil {
    /**
     * @param entity the {@link Entity} being tracked.
     * @return the set of {@link ServerPlayerEntity}s tracking said {@link Entity}
     * @author tmvkrpxl0
     */
    public static Set<ServerPlayerEntity> tracking(Entity entity) {
        final var chunkMap = ((ServerChunkManager) entity.getWorld().getChunkManager()).threadedAnvilChunkStorage;
        final var tracked = ((IChunkMapMixin) chunkMap).getEntityTrackers().get(entity.getId());

        if (tracked != null) {
            return tracked.getListeners().stream().map(PlayerAssociatedNetworkHandler::getPlayer).collect(Collectors.toSet());
        }

        return Collections.emptySet();
    }

    public static Set<ServerPlayerEntity> around(ServerWorld level, Vec3d pos, double radius) {
        return level(level).stream()
                .filter(player -> player.squaredDistanceTo(pos) <= radius * radius)
                .collect(Collectors.toSet());
    }

    public static Set<ServerPlayerEntity> level(ServerWorld level) {
        return Set.copyOf(level.getPlayers());
    }

    public static Set<ServerPlayerEntity> all(MinecraftServer server) {
        return Set.copyOf(server.getPlayerManager().getPlayerList());
    }
}