package dev.lazurite.rayon.impl.mixin.client;

import com.jme3.math.Vector3f;
import dev.lazurite.rayon.api.EntityPhysicsElement;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Corrects the positions of shadows and debug hitboxes.
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @ModifyVariable(
            method = "renderShadow",
            at = @At(value = "STORE", opcode = Opcodes.DSTORE),
            ordinal = 1
    )
    private static double renderShadowY(double e, MatrixStack matrices, VertexConsumerProvider provider, Entity entity, float opacity, float tickDelta) {
        if (EntityPhysicsElement.is(entity)) {
            return EntityPhysicsElement.get(entity).getPhysicsLocation(new Vector3f(), tickDelta).y;
        }

        return e;
    }

    @Inject(method = "renderHitbox", at = @At("HEAD"), cancellable = true)
    private static void renderHitbox(MatrixStack matrices, VertexConsumer vertices, Entity entity, float tickDelta, CallbackInfo info) {
        if (EntityPhysicsElement.is(entity)) {
            info.cancel();
        }
    }
}
