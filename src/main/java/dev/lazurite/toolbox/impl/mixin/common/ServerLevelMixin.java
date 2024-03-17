package dev.lazurite.toolbox.impl.mixin.common;

import dev.lazurite.toolbox.api.event.ServerEvents;
import net.minecraft.server.world.ServerWorld;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public class ServerLevelMixin {

    /**
     * @see ServerEvents.Tick#START_LEVEL_TICK
     */
    @Inject(
            method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/world/ServerWorld;inBlockTick:Z",
                    opcode = Opcodes.PUTFIELD,
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    public void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        ServerEvents.Tick.START_LEVEL_TICK.invoke(this);
    }
}