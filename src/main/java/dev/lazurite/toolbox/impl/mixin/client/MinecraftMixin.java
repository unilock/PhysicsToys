package dev.lazurite.toolbox.impl.mixin.client;

import dev.lazurite.toolbox.api.event.ClientEvents;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    public void onStartTick(CallbackInfo info) {
        ClientEvents.Tick.START_CLIENT_TICK.invoke(this);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void onEndTick(CallbackInfo info) {
        ClientEvents.Tick.END_CLIENT_TICK.invoke(this);
    }
}
