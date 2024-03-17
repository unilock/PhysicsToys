package dev.lazurite.rayon.impl.bullet.collision.space.cache;

import dev.lazurite.rayon.impl.bullet.collision.body.shape.MinecraftShape;
import dev.lazurite.rayon.impl.bullet.collision.space.block.BlockProperty;
import dev.lazurite.transporter.api.pattern.Pattern;
import dev.lazurite.transporter.impl.Transporter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

public final class ShapeCache {
    private static final MinecraftShape FALLBACK_SHAPE = MinecraftShape.convex(new Box(-0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f));

    private static final IdentityHashMap<BlockState, MinecraftShape> SHAPES_SERVER = new IdentityHashMap<>();
    private static final IdentityHashMap<BlockState, MinecraftShape> SHAPES_CLIENT = new IdentityHashMap<>();

    public static MinecraftShape getShapeFor(BlockState blockState, World level, BlockPos blockPos) {
        if (blockState.getBlock().hasDynamicBounds()) {
            return createShapeFor(blockState, level, blockPos);
        }

        final var shapes = getShapes(level.isClient);
        var shape = shapes.get(blockState);

        if (shape == null) {
            shape = createShapeFor(blockState, level, BlockPos.ORIGIN);
            shapes.put(blockState, shape);
        }

        return shape;
    }

    private static Map<BlockState, MinecraftShape> getShapes(boolean isClientSide) {
        return isClientSide ? SHAPES_CLIENT : SHAPES_SERVER;
    }

    @Nullable
    private static MinecraftShape createShapeFor(BlockState blockState, World level, BlockPos blockPos) {
        final var properties = BlockProperty.getBlockProperty(blockState.getBlock());
        MinecraftShape shape = null;

        if (!blockState.isFullCube(level, blockPos) || (properties != null && !properties.isFullBlock())) {
            Pattern pattern;

            if (level.isClient) {
                pattern = ChunkCache.genShapeForBlock(level, blockPos, blockState);
            } else {
                pattern = Transporter.getPatternBuffer().getBlock(Block.getRawIdFromState(blockState));
            }

            if (pattern != null && !pattern.getQuads().isEmpty()) {
                shape = MinecraftShape.concave(pattern);
            }
        }

        if (shape == null) {
            final var voxelShape = blockState.getCollisionShape(level, blockPos);
            if (!voxelShape.isEmpty()) {
                shape = MinecraftShape.convex(voxelShape);
            } else {
                shape = FALLBACK_SHAPE;
            }
        }
        return shape;
    }
}
