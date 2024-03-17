package dev.lazurite.toolbox.impl.mixin.client;

import dev.lazurite.toolbox.api.event.ClientEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

/**
 * @see ClientEvents
 */
@Mixin(ClientWorld.class)
public class ClientLevelMixin {
    @Shadow @Final private MinecraftClient client;

    /**
     * @see ClientEvents.Lifecycle#LOAD_LEVEL
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    public void init(ClientPlayNetworkHandler clientPacketListener, ClientWorld.Properties clientLevelData, RegistryKey resourceKey, RegistryEntry holder, int i, int j, Supplier supplier, WorldRenderer levelRenderer, boolean bl, long l, CallbackInfo ci) {
        ClientEvents.Lifecycle.LOAD_LEVEL.invoke(client, this);
    }

    /**
     * @see ClientEvents.Tick#START_LEVEL_TICK
     */
    @Inject(method = "tickEntities", at = @At("HEAD"))
    public void tickEntities(CallbackInfo info) {
        ClientEvents.Tick.START_LEVEL_TICK.invoke(this);
    }

    /**
     * @see ClientEvents.Player#ADD
     */
    @Inject(method = "addEntity", at = @At("TAIL"))
    public void addPlayer(Entity entity, CallbackInfo ci) {
        if (entity instanceof AbstractClientPlayerEntity abstractClientPlayer) {
            ClientEvents.Player.ADD.invoke(abstractClientPlayer, abstractClientPlayer instanceof ClientPlayerEntity);
        }
    }

    /**
     * @see ClientEvents.Lifecycle#DISCONNECT
     */
    @Inject(method = "disconnect", at = @At("HEAD"))
    public void disconnect(CallbackInfo info) {
        ClientEvents.Lifecycle.DISCONNECT.invoke(client, this);
    }
}