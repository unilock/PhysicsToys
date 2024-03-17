package dev.lazurite.rayon.api.event.render;

import dev.lazurite.rayon.impl.bullet.collision.space.MinecraftSpace;
import dev.lazurite.rayon.impl.util.debug.CollisionObjectDebugger;
import dev.lazurite.toolbox.api.event.Event;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

/**
 * The events available through this class are:
 * <ul>
 *     <li><b>Before Render:</b> Called before each frame of the {@link CollisionObjectDebugger}</li>
 * </ul>
 * @since 1.3.0
 */
public class DebugRenderEvents {
    public static final Event<BeforeRender> BEFORE_RENDER = Event.create();

    private DebugRenderEvents() { }

    @FunctionalInterface
    public interface BeforeRender {
        void onRender(Context context);
    }

    public record Context(MinecraftSpace space, VertexConsumer vertices, MatrixStack matrices, Vec3d cameraPos, float tickDelta) { }
}

