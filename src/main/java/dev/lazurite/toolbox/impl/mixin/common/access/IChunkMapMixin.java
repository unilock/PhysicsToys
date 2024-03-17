package dev.lazurite.toolbox.impl.mixin.common.access;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ThreadedAnvilChunkStorage.class)
public interface IChunkMapMixin {
    @Accessor Int2ObjectMap<ITrackedEntityMixin> getEntityTrackers();
}