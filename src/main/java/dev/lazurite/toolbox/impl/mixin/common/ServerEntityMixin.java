package dev.lazurite.toolbox.impl.mixin.common;

import dev.lazurite.toolbox.api.event.ServerEvents;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTrackerEntry.class)
public class ServerEntityMixin {
    @Shadow @Final private Entity entity;

    @Inject(method = "startTracking", at = @At("HEAD"))
    public void addPairing(ServerPlayerEntity player, CallbackInfo info) {
        ServerEvents.Entity.START_TRACKING.invoke(entity, player);
    }

    @Inject(method = "stopTracking", at = @At("TAIL"))
    public void onStopTracking(ServerPlayerEntity player, CallbackInfo info) {
        ServerEvents.Entity.STOP_TRACKING.invoke(entity, player);
    }
}
