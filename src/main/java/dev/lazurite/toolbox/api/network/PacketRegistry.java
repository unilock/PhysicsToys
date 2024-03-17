package dev.lazurite.toolbox.api.network;

import dev.lazurite.toolbox.impl.network.PacketRegistryImpl;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public interface PacketRegistry {
    static void registerServerbound(Identifier identifier, Consumer<ServerboundContext> packetHandler) {
        PacketRegistryImpl.registerServerbound(identifier, packetHandler);
    }

    static void registerClientbound(Identifier identifier, Consumer<ClientboundContext> packetHandler) {
        PacketRegistryImpl.registerClientbound(identifier, packetHandler);
    }

    record ServerboundContext(PacketByteBuf byteBuf, ServerPlayerEntity player) { }
    record ClientboundContext(PacketByteBuf byteBuf) { }
}