package dev.lazurite.toolbox.impl.network;

import dev.lazurite.toolbox.api.network.PacketRegistry;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public final class PacketRegistryImpl {
    private static final Map<Identifier, Consumer<PacketRegistry.ServerboundContext>> serverboundPackets = new HashMap<>();
    private static final Map<Identifier, Consumer<PacketRegistry.ClientboundContext>> clientboundPackets = new HashMap<>();

    public static void registerServerbound(Identifier identifier, Consumer<PacketRegistry.ServerboundContext> packetHandler) {
        serverboundPackets.put(identifier, packetHandler);
    }

    public static void registerClientbound(Identifier identifier, Consumer<PacketRegistry.ClientboundContext> packetHandler) {
        clientboundPackets.put(identifier, packetHandler);
    }

    public static Optional<Consumer<PacketRegistry.ServerboundContext>> getServerbound(Identifier identifier) {
        return Optional.ofNullable(serverboundPackets.get(identifier));
    }

    public static Optional<Consumer<PacketRegistry.ClientboundContext>> getClientbound(Identifier identifier) {
        return Optional.ofNullable(clientboundPackets.get(identifier));
    }

    private PacketRegistryImpl() { }
}