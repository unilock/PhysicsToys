package dev.lazurite.rayon.impl.mixin.client;

import com.jme3.math.Vector3f;
import dev.lazurite.rayon.api.EntityPhysicsElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldRenderer.class)
public class LevelRendererMixin {
    @Redirect(
            method = "renderEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;render(Lnet/minecraft/entity/Entity;DDDFFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"
            )
    )
    public void renderEntity_render(EntityRenderDispatcher dispatcher, Entity entity, double d, double e, double f, float g, float h, MatrixStack poseStack, VertexConsumerProvider multiBufferSource, int i) {
        if (EntityPhysicsElement.is(entity)) {
            var element = EntityPhysicsElement.get(entity);
            var cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
            var location = element.getPhysicsLocation(new Vector3f(), h);
            dispatcher.render(entity, location.x - cameraPos.x, location.y - cameraPos.y, location.z - cameraPos.z, g, h, poseStack, multiBufferSource, i);
        }

        dispatcher.render(entity, d, e, f, g, h, poseStack, multiBufferSource, i);
    }

}
