package dev.lazurite.toolbox.impl.mixin.common;

import com.mojang.authlib.GameProfile;
import dev.lazurite.toolbox.api.network.PacketRegistry;
import dev.lazurite.toolbox.impl.network.PacketRegistryImpl;
import io.netty.buffer.Unpooled;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonNetworkHandler.class)
public abstract class ServerCommonPacketListenerImplMixin {

    @Shadow protected abstract GameProfile getProfile();

    @Shadow @Final protected MinecraftServer server;

    @Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
    public void handleCustomPayload(CustomPayloadC2SPacket serverboundCustomPayloadPacket, CallbackInfo ci) {
        if (!((Object) this instanceof ServerPlayNetworkHandler)) return;

        NetworkThreadUtils.forceMainThread(serverboundCustomPayloadPacket, (ServerPlayPacketListener) this, this.server);

        PacketRegistryImpl.getServerbound(serverboundCustomPayloadPacket.payload().id()).ifPresent(consumer -> {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            serverboundCustomPayloadPacket.payload().write(buf);
            consumer.accept(new PacketRegistry.ServerboundContext(buf, this.server.getPlayerManager().getPlayer(this.getProfile().getId())));
            ci.cancel();
        });
    }

}
