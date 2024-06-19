package dev.lazurite.rayon.impl.bullet.collision.body.shape;

import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.HullCollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.collision.shapes.infos.IndexedMesh;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import net.minecraft.util.shape.VoxelShape;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface MinecraftShape {
    List<Triangle> getTriangles(Quaternion quaternion);
    float getVolume();

    static Box box(net.minecraft.util.math.Box box) {
        return MinecraftShape.box(Convert.toBullet(box));
    }

    static Box box(BoundingBox box) {
        return new Box(box);
    }

    static Convex convex(net.minecraft.util.math.Box box) {
        return MinecraftShape.convex(Convert.toBullet(box));
    }

    static Convex convex(VoxelShape voxelShape) {
        return new Convex(Triangle.getMeshOf(voxelShape));
    }

    static Convex convex(BoundingBox box) {
        return new Convex(Triangle.getMeshOf(box));
    }

    /* Mostly stable */
    final class Box extends BoxCollisionShape implements MinecraftShape {

        private final List<Triangle> triangles;

        public Box(BoundingBox boundingBox) {
            super(boundingBox.getExtent(new Vector3f()).mult(0.5f));
            this.triangles = new ArrayList<>(MinecraftShape.convex(boundingBox).triangles); // a lil hacky
        }

        @Override
        public List<Triangle> getTriangles(Quaternion quaternion) {
            return this.triangles;
        }

        @Override
        public float getVolume() {
            return this.toHullShape().aabbVolume();
        }
    }

    /* Recommended */
    final class Convex extends HullCollisionShape implements MinecraftShape {
        private final List<Triangle> triangles;

        public Convex(List<Triangle> triangles) {
            super(triangles.stream().flatMap(triangle -> Stream.of(triangle.getVertices())).toList());
            this.triangles = triangles;
        }

        @Override
        public List<Triangle> getTriangles(Quaternion quaternion) {
            return this.triangles.stream().map(triangle -> triangle.transform(quaternion)).toList();
        }

        @Override
        public float getVolume() {
            return this.aabbVolume();
        }
    }

    /* Less stable :( */
    final class Concave extends MeshCollisionShape implements MinecraftShape {
        private final List<Triangle> triangles;

        public Concave(List<Triangle> triangles) {
            super(false,
                ((Supplier<IndexedMesh>) () -> {
                    final var vertices = triangles.stream().flatMap(triangle -> Stream.of(triangle.getVertices())).toArray(Vector3f[]::new);
                    final var indices = new int[vertices.length];

                    for (var i = 0; i < vertices.length; i++) {
                        indices[i] = i;
                    }

                    return new IndexedMesh(vertices, indices);
                }
            ).get());
            this.triangles = triangles;
        }

        @Override
        public List<Triangle> getTriangles(Quaternion quaternion) {
            return this.triangles.stream().map(triangle -> triangle.transform(quaternion)).toList();
        }

        @Override
        public float getVolume() {
            final var box = boundingBox(new Vector3f(), new Quaternion(), new BoundingBox());
            return box.getXExtent() * box.getYExtent() * box.getZExtent();
        }
    }
}