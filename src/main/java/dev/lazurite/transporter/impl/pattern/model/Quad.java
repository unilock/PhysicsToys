package dev.lazurite.transporter.impl.pattern.model;

import com.google.common.collect.Lists;
import dev.lazurite.transporter.api.pattern.Pattern;
import dev.lazurite.transporter.impl.pattern.BufferEntry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * A quad represents a basic quad shape which is what Minecraft uses
 * when rendering vertex information to the screen. In this way, quads
 * can be captured from the renderer and stored in a {@link Pattern}.
 * @see BufferEntry
 * @see Pattern
 */
public class Quad {

    private final Vec3d p1;
    private final Vec3d p2;
    private final Vec3d p3;
    private final Vec3d p4;

    public Quad(List<Vec3d> points) {
        this(points.get(0), points.get(1), points.get(2), points.get(3));
    }

    public Quad(Vec3d p1, Vec3d p2, Vec3d p3, Vec3d p4) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
        this.p4 = p4;
    }

    public List<Vec3d> getPoints() {
        return Lists.newArrayList(p1, p2, p3, p4);
    }

    public void serialize(PacketByteBuf buf) {
        for (var point : getPoints()) {
            buf.writeDouble(point.x);
            buf.writeDouble(point.y);
            buf.writeDouble(point.z);
        }
    }

    public static Quad deserialize(PacketByteBuf buf) {
        var points = new ArrayList<Vec3d>();
        for (var j = 0; j < 4; j++) {
            points.add(new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble()));
        }
        return new Quad(points);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Quad quad) {
            return quad.getPoints().equals(getPoints());
        }
        return false;
    }

}