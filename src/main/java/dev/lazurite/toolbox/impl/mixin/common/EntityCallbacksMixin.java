package dev.lazurite.toolbox.impl.mixin.common;

import dev.lazurite.toolbox.api.event.ServerEvents;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/server/world/ServerWorld$ServerEntityHandler")
public class EntityCallbacksMixin {
    @Inject(method = "startTracking*", at = @At("TAIL"))
    public void onTrackingStart(Entity entity, CallbackInfo ci) {
        ServerEvents.Entity.LOAD.invoke(entity);
    }

    @Inject(method = "stopTracking*", at = @At("HEAD"))
    public void onTrackingEnd(Entity entity, CallbackInfo info) {
        ServerEvents.Entity.UNLOAD.invoke(entity);
    }
}
