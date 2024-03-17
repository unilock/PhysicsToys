package dev.lazurite.toolbox.impl.mixin.client;

import dev.lazurite.toolbox.api.network.PacketRegistry;
import dev.lazurite.toolbox.impl.network.PacketRegistryImpl;
import io.netty.buffer.Unpooled;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonNetworkHandler.class)
public class ClientCommonPacketListenerImplMixin {
    @Inject(
            method = "onCustomPayload(Lnet/minecraft/network/packet/s2c/common/CustomPayloadS2CPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    public void handleCustomPayload(CustomPayloadS2CPacket clientboundCustomPayloadPacket, CallbackInfo ci) {
        if (!((Object) this instanceof ClientPlayNetworkHandler)) return;

        PacketRegistryImpl.getClientbound(clientboundCustomPayloadPacket.payload().id()).ifPresent(consumer -> {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            clientboundCustomPayloadPacket.payload().write(buf);
            consumer.accept(new PacketRegistry.ClientboundContext(buf));
            buf.release();
            ci.cancel();
        });
    }
}
