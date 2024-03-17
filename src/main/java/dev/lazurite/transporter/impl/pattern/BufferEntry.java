package dev.lazurite.transporter.impl.pattern;

import dev.lazurite.transporter.api.Disassembler;
import dev.lazurite.transporter.api.buffer.PatternBuffer;
import dev.lazurite.transporter.api.pattern.Pattern;
import dev.lazurite.transporter.impl.buffer.PatternBufferImpl;
import dev.lazurite.transporter.impl.pattern.model.Quad;
import net.minecraft.network.PacketByteBuf;

import java.util.LinkedList;
import java.util.List;

/**
 * The main implementation of {@link Pattern}. This is what is stored within a {@link PatternBuffer}.
 * @see PatternBufferImpl
 * @see Disassembler
 */
public class BufferEntry implements Pattern {

    private final List<Quad> quads;
    private final Pattern.Type type;
    private final int registryId;

    public  BufferEntry(List<Quad> quads, Pattern.Type type, int registryId) {
        this.quads = quads;
        this.type = type;
        this.registryId = registryId;
    }

    public BufferEntry(Pattern pattern, Pattern.Type type, int registryId) {
        this(pattern.getQuads(), type, registryId);
    }

    public Pattern.Type getType() {
        return this.type;
    }

    public int getRegistryId() {
        return this.registryId;
    }

    @Override
    public List<Quad> getQuads() {
        return this.quads;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BufferEntry entry) {
            return entry.getQuads().equals(getQuads()) && entry.getRegistryId() == registryId;
        }
        return false;
    }

    public static BufferEntry deserialize(PacketByteBuf buf) {
        var type = buf.readEnumConstant(Pattern.Type.class);
        int registryId = buf.readInt();
        int quadCount = buf.readInt();
        var quads = new LinkedList<Quad>();
        for (int i = 0; i < quadCount; i++) {
            quads.add(Quad.deserialize(buf));
        }
        return new BufferEntry(quads, type, registryId);
    }

}