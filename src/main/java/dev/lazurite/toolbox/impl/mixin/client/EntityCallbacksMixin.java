package dev.lazurite.toolbox.impl.mixin.client;

import dev.lazurite.toolbox.api.event.ClientEvents;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/client/world/ClientWorld$ClientEntityHandler")
public class EntityCallbacksMixin {
    @Inject(method = "startTracking*", at = @At("TAIL"))
    private void startTracking(Entity entity, CallbackInfo ci) {
        ClientEvents.Entity.LOAD.invoke(entity);
    }

    @Inject(method = "stopTracking*", at = @At("HEAD"))
    private void stopTracking(Entity entity, CallbackInfo info) {
        ClientEvents.Entity.UNLOAD.invoke(entity);
    }
}