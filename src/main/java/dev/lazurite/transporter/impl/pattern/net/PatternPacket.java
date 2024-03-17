package dev.lazurite.transporter.impl.pattern.net;

import dev.lazurite.transporter.api.event.PatternBufferEvents;
import dev.lazurite.transporter.impl.Transporter;
import dev.lazurite.transporter.impl.buffer.PatternBufferImpl;
import dev.lazurite.transporter.impl.pattern.BufferEntry;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public record PatternPacket(BufferEntry pattern) implements FabricPacket {

    public static final PacketType<PatternPacket> TYPE = PacketType.create(new Identifier(Transporter.MODID, "pattern"), PatternPacket::new);

    public PatternPacket(PacketByteBuf buf) {
        this(BufferEntry.deserialize(buf));
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeEnumConstant(pattern.getType());
        buf.writeInt(pattern.getRegistryId());
        buf.writeInt(pattern.getQuads().size());
        pattern.getQuads().forEach(quad -> quad.serialize(buf));
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public static void accept(PatternPacket packet, ServerPlayerEntity player, PacketSender sender) {
        ((PatternBufferImpl) Transporter.getPatternBuffer()).put(packet.pattern);
        PatternBufferEvents.BUFFER_UPDATE.invoker().onBufferUpdate(packet.pattern);
    }

}

