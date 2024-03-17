package dev.lazurite.transporter.api;

import dev.lazurite.transporter.api.buffer.PatternBuffer;
import dev.lazurite.transporter.api.pattern.Pattern;
import dev.lazurite.transporter.impl.pattern.BufferEntry;
import dev.lazurite.transporter.impl.pattern.QuadConsumer;
import dev.lazurite.transporter.impl.pattern.net.PatternPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.Nullable;

/**
 * The calls in this class are important in that they're necessary in order for anything within this library to work.
 * From here, you can obtain a {@link Pattern} based on a block, block entity, entity, or item.
 * <br>
 * In each of these methods, you have the option to transform the pattern any way you want before it
 * is read as a {@link Pattern}. This is done via passing a {@link MatrixStack}.
 * @see PatternBuffer
 * @see Pattern
 * @since 1.0.0
 */
public interface Disassembler {

    static Pattern getBlock(BlockState blockState) {
        return getBlock(blockState, null);
    }

    static Pattern getBlock(BlockState blockState, @Nullable MatrixStack transformation) {
        if (transformation == null) {
            transformation = new MatrixStack();
        }

        var client = MinecraftClient.getInstance();
        var consumer = QuadConsumer.create();
        var model = client.getBlockRenderManager().getModel(blockState);

        MinecraftClient.getInstance().getBlockRenderManager().getModelRenderer()
                .render(transformation.peek(), consumer, blockState, model, 0, 0, 0, 0, 0);

        var entry = new BufferEntry(consumer, Pattern.Type.BLOCK, Block.getRawIdFromState(blockState));
        ClientPlayNetworking.send(new PatternPacket(entry));
        return entry;
    }

    static Pattern getBlockEntity(BlockEntity blockEntity) {
        return getBlockEntity(blockEntity, null);
    }

    static Pattern getBlockEntity(BlockEntity blockEntity, @Nullable MatrixStack transformation) {
        if (transformation == null) {
            transformation = new MatrixStack();
        }

        var consumer = QuadConsumer.create();
        var renderer = MinecraftClient.getInstance().getBlockEntityRenderDispatcher().get(blockEntity);

        if (renderer != null) {
            renderer.render(blockEntity, 0, transformation, consumer.asProvider(), 0, 0);
        }

        var entry = new BufferEntry(consumer, Pattern.Type.BLOCK, Block.getRawIdFromState(blockEntity.getCachedState()));
        ClientPlayNetworking.send(new PatternPacket(entry));
        return entry;
    }

    static Pattern getEntity(Entity entity) {
        return getEntity(entity, null);
    }

    static Pattern getEntity(Entity entity, @Nullable MatrixStack transformation) {
        if (transformation == null) {
            transformation = new MatrixStack();
        }

        var consumer = QuadConsumer.create();
        MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(entity)
                .render(entity, 0, 0, transformation, consumer.asProvider(), 0);

        var entry = new BufferEntry(consumer, Pattern.Type.ENTITY, Registries.ENTITY_TYPE.getRawId(entity.getType()));
        ClientPlayNetworking.send(new PatternPacket(entry));
        return entry;
    }

    static Pattern getItem(Item item) {
        return getItem(item, null);
    }

    static Pattern getItem(Item item, @Nullable MatrixStack transformation) {
        if (transformation == null) {
            transformation = new MatrixStack();
        }

        var consumer = QuadConsumer.create();
        MinecraftClient.getInstance().getItemRenderer()
                .renderItem(new ItemStack(item), ModelTransformationMode.GROUND, 0, 0, transformation, consumer.asProvider(), null, 0);

        var entry = new BufferEntry(consumer, Pattern.Type.ITEM, Registries.ITEM.getRawId(item));
        ClientPlayNetworking.send(new PatternPacket(entry));
        return entry;
    }

}