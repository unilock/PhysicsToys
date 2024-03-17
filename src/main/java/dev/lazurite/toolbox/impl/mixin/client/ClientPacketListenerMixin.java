package dev.lazurite.toolbox.impl.mixin.client;

import dev.lazurite.toolbox.api.event.ClientEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPacketListenerMixin {

    @Shadow private ClientWorld world;

    /**
     * @see ClientEvents.Lifecycle#PRE_LOGIN
     */
    @Inject(
            method = "onGameJoin",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V",
                    shift = At.Shift.AFTER
            )
    )
    public void handleLogin_PRE(GameJoinS2CPacket packet, CallbackInfo info) {
        ClientEvents.Lifecycle.PRE_LOGIN.invoke(MinecraftClient.getInstance());
    }

    /**
     * @see ClientEvents.Lifecycle#POST_LOGIN
     */
    @Inject(method = "onGameJoin", at = @At("RETURN"))
    public void handleLogin_POST(GameJoinS2CPacket packet, CallbackInfo info) {
        ClientEvents.Lifecycle.POST_LOGIN.invoke(MinecraftClient.getInstance(), world, MinecraftClient.getInstance().player);
    }
}