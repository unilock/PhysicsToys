package dev.lazurite.toolbox.api.network;

import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public interface ClientNetworking {
    static void send(Identifier identifier, Consumer<PacketByteBuf> consumer) {
        final var buf = new PacketByteBuf(Unpooled.buffer());
        consumer.accept(buf);
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(new CustomPayloadC2SPacket(new SimplePayload(identifier, buf)));
    }
}
