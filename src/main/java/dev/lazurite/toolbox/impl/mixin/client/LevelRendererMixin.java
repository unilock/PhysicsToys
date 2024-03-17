package dev.lazurite.toolbox.impl.mixin.client;

import dev.lazurite.toolbox.api.event.ClientEvents;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class LevelRendererMixin {
    @Shadow @Nullable private ClientWorld world;
    private MatrixStack poseStack;
    private Camera camera;
    private float tickDelta;

    @Inject(method = "render", at = @At("HEAD"))
    public void renderLevel(MatrixStack poseStack, float tickDelta, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightTexture, Matrix4f matrix4f, CallbackInfo info) {
        this.poseStack = poseStack;
        this.camera = camera;
        this.tickDelta = tickDelta;
    }

    @Inject(
        method = "render",
        at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/render/debug/DebugRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;DDD)V",
                ordinal = 0
        )
    )
    public void renderLevel(CallbackInfo info) {
        ClientEvents.Render.BEFORE_DEBUG.invoke(poseStack, camera, world, tickDelta);
    }
}
