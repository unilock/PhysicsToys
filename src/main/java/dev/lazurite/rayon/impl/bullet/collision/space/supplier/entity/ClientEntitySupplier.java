package dev.lazurite.rayon.impl.bullet.collision.space.supplier.entity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.GameMode;

public class ClientEntitySupplier implements EntitySupplier {
    @Override
    public GameMode getGameType(PlayerEntity player) {
        var client = MinecraftClient.getInstance();
        var id = player.getUuid();

        // Is client player
        if (client.player != null && client.player.getUuid().equals(id) && client.interactionManager != null) {
            return client.interactionManager.getCurrentGameMode();
        }

        // Is remote player
        var connection = MinecraftClient.getInstance().getNetworkHandler();
        if (connection != null && connection.getPlayerUuids().contains(id)) {
            var playerInfo = connection.getPlayerListEntry(id);
            return playerInfo == null ? GameMode.SURVIVAL : playerInfo.getGameMode();
        }

        return GameMode.SURVIVAL;
    }
}
