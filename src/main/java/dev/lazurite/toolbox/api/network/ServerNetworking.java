package dev.lazurite.toolbox.api.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public interface ServerNetworking {
    static void send(ServerPlayerEntity player, Identifier identifier, Consumer<PacketByteBuf> consumer) {
        final var buf = new PacketByteBuf(Unpooled.buffer());
        consumer.accept(buf);
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(new SimplePayload(identifier, buf)));
    }
}