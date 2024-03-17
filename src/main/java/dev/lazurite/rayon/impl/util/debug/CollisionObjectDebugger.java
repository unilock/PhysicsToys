package dev.lazurite.rayon.impl.util.debug;


import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.lazurite.rayon.api.event.render.DebugRenderEvents;
import dev.lazurite.rayon.impl.bullet.collision.body.ElementRigidBody;
import dev.lazurite.rayon.impl.bullet.collision.body.MinecraftRigidBody;
import dev.lazurite.rayon.impl.bullet.collision.body.shape.MinecraftShape;
import dev.lazurite.rayon.impl.bullet.collision.space.MinecraftSpace;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;

;

/**
 * This class handles debug rendering on the client. Press F3+r to render
 * all {@link ElementRigidBody} objects present in the {@link MinecraftSpace}.
 */
public final class CollisionObjectDebugger {
    private static boolean enabled;

    private CollisionObjectDebugger() {}

    public static boolean toggle() {
        enabled = !enabled;
        return enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void renderSpace(MinecraftSpace space, MatrixStack stack, float tickDelta) {
        final var cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
        final var builder = Tessellator.getInstance().getBuffer();

        DebugRenderEvents.BEFORE_RENDER.invoke(new DebugRenderEvents.Context(space, builder, stack, cameraPos, tickDelta));
        builder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        space.getTerrainMap().values().forEach(terrain -> CollisionObjectDebugger.renderBody(terrain, builder, stack, tickDelta));
        space.getRigidBodiesByClass(ElementRigidBody.class).forEach(elementRigidBody -> CollisionObjectDebugger.renderBody(elementRigidBody, builder, stack, tickDelta));
        Tessellator.getInstance().draw();
    }

    public static void renderBody(MinecraftRigidBody rigidBody, BufferBuilder builder, MatrixStack stack, float tickDelta) {
        final var position = rigidBody.isStatic() ?
                rigidBody.getPhysicsLocation(new Vector3f()) :
                ((ElementRigidBody) rigidBody).getFrame().getLocation(new Vector3f(), tickDelta);

        final var rotation = rigidBody.isStatic() ?
                rigidBody.getPhysicsRotation(new Quaternion()) :
                ((ElementRigidBody) rigidBody).getFrame().getRotation(new Quaternion(), tickDelta);

        renderShape(rigidBody.getMinecraftShape(), position, rotation, builder, stack, rigidBody.getOutlineColor(), 1.0f);
    }

    public static void renderShape(MinecraftShape shape, Vector3f position, Quaternion rotation, BufferBuilder builder, MatrixStack stack, Vector3f color, float alpha) {
        final var triangles = shape.getTriangles(Quaternion.IDENTITY);
        final var cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();

        for (var triangle : triangles) {
            final var vertices = triangle.getVertices();

            stack.push();
            stack.translate(position.x - cameraPos.x, position.y - cameraPos.y, position.z - cameraPos.z);
            stack.multiply(Convert.toMinecraft(rotation));
            final var p1 = vertices[0];
            final var p2 = vertices[1];
            final var p3 = vertices[2];

            builder.vertex(stack.peek().getPositionMatrix(), p1.x, p1.y, p1.z).color(color.x, color.y, color.z, alpha).next();
            builder.vertex(stack.peek().getPositionMatrix(), p2.x, p2.y, p2.z).color(color.x, color.y, color.z, alpha).next();
            builder.vertex(stack.peek().getPositionMatrix(), p3.x, p3.y, p3.z).color(color.x, color.y, color.z, alpha).next();
            builder.vertex(stack.peek().getPositionMatrix(), p1.x, p1.y, p1.z).color(color.x, color.y, color.z, alpha).next();
            stack.pop();
        }
    }
}