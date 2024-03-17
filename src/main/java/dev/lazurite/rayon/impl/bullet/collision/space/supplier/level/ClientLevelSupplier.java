package dev.lazurite.rayon.impl.bullet.collision.space.supplier.level;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This is a {@link LevelSupplier} which returns a single
 * {@link ClientWorld} object in a {@link List} object.
 */
public record ClientLevelSupplier(MinecraftClient minecraft) implements LevelSupplier {
    @Override
    public List<World> getAll() {
        final var out = new ArrayList<World>();

        if (minecraft.world != null) {
            out.add(minecraft.world);
        }

        return out;
    }

    @Override
    public World get(RegistryKey<World> key) {
        if (minecraft.world != null && minecraft.world.getRegistryKey().equals(key)) {
            return minecraft.world;
        }

        return null;
    }

    @Override
    public Optional<World> getOptional(RegistryKey<World> key) {
        return Optional.ofNullable(get(key));
    }
}
