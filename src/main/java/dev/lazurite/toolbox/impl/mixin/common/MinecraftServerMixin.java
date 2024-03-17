package dev.lazurite.toolbox.impl.mixin.common;

import dev.lazurite.toolbox.api.event.ServerEvents;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Map;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Shadow @Final private Map<RegistryKey<World>, ServerWorld> worlds;

    /**
     * @see ServerEvents.Lifecycle#LOAD_SERVER
     */
    @Inject(
            method = "runServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;setupServer()Z"
            )
    )
    public void runServer(CallbackInfo info) {
        ServerEvents.Lifecycle.LOAD_SERVER.invoke(this);
    }

    /**
     * @see ServerEvents.Lifecycle#UNLOAD_SERVER
     */
    @Inject(method = "shutdown", at = @At("HEAD"))
    public void stopServer(CallbackInfo info) {
        ServerEvents.Lifecycle.UNLOAD_SERVER.invoke(this);
    }

    /**
     * @see ServerEvents.Tick#START_SERVER_TICK
     */
    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;tickWorlds(Ljava/util/function/BooleanSupplier;)V"
            )
    )
    public void tickServer_Start(BooleanSupplier shouldKeepTicking, CallbackInfo info) {
        ServerEvents.Tick.START_SERVER_TICK.invoke(this);
    }

    /**
     * @see ServerEvents.Tick#END_SERVER_TICK
     */
    @Inject(method = "tick", at = @At("TAIL"))
    public void tickServer_End(BooleanSupplier shouldKeepTicking, CallbackInfo info) {
        ServerEvents.Tick.END_SERVER_TICK.invoke(this);
    }

    /**
     * @see ServerEvents.Lifecycle#LOAD_LEVEL
     */
    @Inject(method = "createWorlds", at = @At("TAIL"))
    public void createLevels(CallbackInfo info) {
        // yeah i know this is lazy but it suits my needs. sorry i5
        for (var level : this.worlds.values()) {
            ServerEvents.Lifecycle.LOAD_LEVEL.invoke(this, level);
        }
    }

    /**
     * @see ServerEvents.Lifecycle#UNLOAD_LEVEL
     */
    @Inject(
            method = "shutdown",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;close()V"
            ),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    public void stopServer(CallbackInfo info, Iterator<ServerWorld> levels, ServerWorld level) {
        ServerEvents.Lifecycle.UNLOAD_LEVEL.invoke(this, level);
    }
}
