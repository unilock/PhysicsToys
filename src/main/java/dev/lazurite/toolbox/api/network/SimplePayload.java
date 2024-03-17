package dev.lazurite.toolbox.api.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SimplePayload(Identifier id, PacketByteBuf buffer) implements CustomPayload {
    @Override
    public void write(PacketByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBytes(buffer);
    }
}
