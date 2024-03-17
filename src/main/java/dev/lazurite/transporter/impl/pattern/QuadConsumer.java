package dev.lazurite.transporter.impl.pattern;

import dev.lazurite.transporter.api.Disassembler;
import dev.lazurite.transporter.api.pattern.Pattern;
import dev.lazurite.transporter.impl.compat.Sodium;
import dev.lazurite.transporter.impl.pattern.model.Quad;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

/**
 * Another implementation of {@link Pattern} other than {@link BufferEntry}, this class
 * is used by {@link Disassembler} to capture vertex information and translate it into
 * a list of {@link Quad}s.
 * @see Disassembler
 * @see Quad
 */
public class QuadConsumer extends BufferBuilder implements Pattern {

    protected final List<Quad> quads = new LinkedList<>();
    protected final List<Vec3d> points = new LinkedList<>();

    public static QuadConsumer create() {
        if (Sodium.isInstalled()) {
            return Sodium.getSodiumCompatibleConsumer();
        }
        return new QuadConsumer();
    }

    public QuadConsumer() {
        super(0x200000);
        this.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION);
    }

    @Override
    public @NotNull VertexConsumer vertex(double x, double y, double z) {
        points.add(new Vec3d(x, y, z));
        return this;
    }

    /**
     * For every four points, create a new {@link Quad} and add it to quads.
     */
    @Override
    public void next() {
        if (points.size() >= 4) {
            quads.add(new Quad(points));
            points.clear();
        }
    }

    @Override
    public List<Quad> getQuads() {
        return this.quads;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof QuadConsumer quadConsumer) {
            return quadConsumer.getQuads().equals(getQuads());
        }
        return false;
    }

    public Provider asProvider() {
        return new Provider(this);
    }

    /**
     * In some instances, a {@link VertexConsumerProvider} is required
     * instead of a {@link VertexConsumer}. In this situation, {@link QuadConsumer#asProvider()}
     * can be called and one of these objects will be returned containing the original
     * {@link QuadConsumer}.
     */
    private static class Provider implements VertexConsumerProvider {
        private final QuadConsumer pattern;

        public Provider(QuadConsumer pattern) {
            this.pattern = pattern;
        }

        @Override
        public @NotNull VertexConsumer getBuffer(RenderLayer type) {
            return pattern;
        }
    }

}