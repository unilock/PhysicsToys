package dev.lazurite.toolbox.impl.mixin.common;

import dev.lazurite.toolbox.api.event.ClientEvents;
import dev.lazurite.toolbox.api.event.ServerEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public class LevelMixin {
    @Shadow @Final public boolean isClient;

    /**
     * @see ClientEvents.Tick#END_LEVEL_TICK
     * @see ServerEvents.Tick#END_LEVEL_TICK
     */
    @Inject(method = "tickBlockEntities", at = @At("RETURN"))
    public void tickBlockEntities(CallbackInfo info) {
        if (this.isClient) {
            ClientEvents.Tick.END_LEVEL_TICK.invoke(this);
        } else {
            ServerEvents.Tick.END_LEVEL_TICK.invoke(this);
        }
    }
}
