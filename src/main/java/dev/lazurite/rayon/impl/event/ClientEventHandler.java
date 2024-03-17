package dev.lazurite.rayon.impl.event;

import com.jme3.math.Vector3f;
import dev.lazurite.rayon.api.EntityPhysicsElement;
import dev.lazurite.rayon.api.event.collision.PhysicsSpaceEvents;
import dev.lazurite.rayon.impl.bullet.collision.body.ElementRigidBody;
import dev.lazurite.rayon.impl.bullet.collision.body.EntityRigidBody;
import dev.lazurite.rayon.impl.bullet.collision.space.MinecraftSpace;
import dev.lazurite.rayon.impl.bullet.collision.space.generator.EntityCollisionGenerator;
import dev.lazurite.rayon.impl.bullet.collision.space.storage.SpaceStorage;
import dev.lazurite.rayon.impl.bullet.collision.space.supplier.entity.ClientEntitySupplier;
import dev.lazurite.rayon.impl.bullet.collision.space.supplier.level.ClientLevelSupplier;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import dev.lazurite.rayon.impl.bullet.thread.PhysicsThread;
import dev.lazurite.rayon.impl.bullet.thread.util.ClientUtil;
import dev.lazurite.rayon.impl.event.network.EntityNetworking;
import dev.lazurite.rayon.impl.util.debug.CollisionObjectDebugger;
import dev.lazurite.toolbox.api.event.ClientEvents;
import dev.lazurite.toolbox.api.math.QuaternionHelper;
import dev.lazurite.toolbox.api.math.VectorHelper;
import dev.lazurite.toolbox.api.network.PacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public final class ClientEventHandler {
    private static PhysicsThread thread;

    public static PhysicsThread getThread() {
        return thread;
    }

    public static void register() {
        // Client Events
        ClientEvents.Lifecycle.PRE_LOGIN.register(ClientEventHandler::onGameJoin);
        ClientEvents.Lifecycle.DISCONNECT.register(ClientEventHandler::onDisconnect);
        ClientEvents.Tick.END_CLIENT_TICK.register(ClientEventHandler::onClientTick);

        // Level Events
        ClientEvents.Lifecycle.LOAD_LEVEL.register(ClientEventHandler::onLevelLoad);
        ClientEvents.Tick.END_LEVEL_TICK.register(ClientEventHandler::onStartLevelTick);
        ClientEvents.Tick.END_LEVEL_TICK.register(ClientEventHandler::onEntityStartLevelTick);

        // Render Events
        ClientEvents.Render.BEFORE_DEBUG.register((poseStack, camera, level, tickDelta) -> ClientEventHandler.onDebugRender(level, poseStack, tickDelta));

        // Entity Events
        ClientEvents.Entity.LOAD.register(ClientEventHandler::onEntityLoad);
        ClientEvents.Entity.UNLOAD.register(ClientEventHandler::onEntityUnload);
    }

    public static void onStartLevelTick(World level) {
        if (!ClientUtil.isPaused()) {
            MinecraftSpace.get(level).step();
        }
    }

    public static void onLevelLoad(MinecraftClient minecraft, ClientWorld level) {
        var space = new MinecraftSpace(thread, level);
        ((SpaceStorage) level).setSpace(space);
        PhysicsSpaceEvents.INIT.invoke(space);
    }

    public static void onClientTick(MinecraftClient minecraft) {
        if (thread != null && thread.throwable != null) {
            throw new RuntimeException(thread.throwable);
        }
    }

    public static void onGameJoin(MinecraftClient minecraft) {
//        var supplier = RayonCore.isImmersivePortalsPresent() ? new ImmersiveWorldSupplier(minecraft) : new ClientLevelSupplier(minecraft);
        var supplier = new ClientLevelSupplier(minecraft);
        thread = new PhysicsThread(minecraft, Thread.currentThread(), supplier, new ClientEntitySupplier(), "Client Physics Thread");
    }

    public static void onDisconnect(MinecraftClient minecraft, ClientWorld level) {
        thread.destroy();
    }

    public static void onDebugRender(World level, MatrixStack stack, float tickDelta) {
        if (CollisionObjectDebugger.isEnabled()) {
            CollisionObjectDebugger.renderSpace(MinecraftSpace.get(level), stack, tickDelta);
        }
    }

    public static void onEntityLoad(Entity entity) {
        if (EntityPhysicsElement.is(entity)) {
            var level = entity.getWorld();

            PhysicsThread.get(level).execute(
                    () -> MinecraftSpace.getOptional(level).ifPresent(
                            space -> space.addCollisionObject(EntityPhysicsElement.get(entity).getRigidBody())
                    )
            );
        }
    }

    public static void onEntityUnload(Entity entity) {
        if (EntityPhysicsElement.is(entity)) {
            var level = entity.getWorld();

            PhysicsThread.get(level).execute(
                    () -> MinecraftSpace.getOptional(level).ifPresent(
                            space -> space.removeCollisionObject(EntityPhysicsElement.get(entity).getRigidBody())
                    )
            );
        }
    }

    public static void onEntityStartLevelTick(World level) {
        var space = MinecraftSpace.get(level);
        EntityCollisionGenerator.step(space);

        for (var rigidBody : space.getRigidBodiesByClass(EntityRigidBody.class)) {
            var player = MinecraftClient.getInstance().player;

            /* Movement */
            if (rigidBody.isActive() && rigidBody.isPositionDirty() && player != null && player.equals(rigidBody.getPriorityPlayer())) {
                EntityNetworking.sendMovement(rigidBody);
            }

            /* Set entity position */
            var location = rigidBody.getFrame().getLocation(new Vector3f(), 1.0f);
            rigidBody.getElement().cast().updatePosition(location.x, location.y, location.z);
        }
    }

    public static void onMovementPacketReceived(PacketRegistry.ClientboundContext context) {
        var buf = context.byteBuf();
        var entityId = buf.readInt();
        var rotation = Convert.toBullet(QuaternionHelper.fromBuffer(buf));
        var location = Convert.toBullet(VectorHelper.fromBuffer(buf));
        var linearVelocity = Convert.toBullet(VectorHelper.fromBuffer(buf));
        var angularVelocity = Convert.toBullet(VectorHelper.fromBuffer(buf));
        var level = MinecraftClient.getInstance().world;

        if (level != null) {
            var entity = MinecraftClient.getInstance().world.getEntityById(entityId);

            if (EntityPhysicsElement.is(entity)) {
                var rigidBody = EntityPhysicsElement.get(entity).getRigidBody();

                PhysicsThread.get(level).execute(() -> {
                    rigidBody.setPhysicsRotation(rotation);
                    rigidBody.setPhysicsLocation(location);
                    rigidBody.setLinearVelocity(linearVelocity);
                    rigidBody.setAngularVelocity(angularVelocity);
                    rigidBody.activate();
                });
            }
        }
    }

    public static void onPropertiesPacketReceived(PacketRegistry.ClientboundContext context) {
        var buf = context.byteBuf();
        var entityId = buf.readInt();
        var mass = buf.readFloat();
        var dragCoefficient = buf.readFloat();
        var friction = buf.readFloat();
        var restitution = buf.readFloat();
        var terrainLoading = buf.readBoolean();
        var buoyancyType = buf.readEnumConstant(ElementRigidBody.BuoyancyType.class);
        var dragType = buf.readEnumConstant(ElementRigidBody.DragType.class);
        var priorityPlayer = buf.readUuid();
        var level = MinecraftClient.getInstance().world;

        if (level != null) {
            var entity = level.getEntityById(entityId);

            if (EntityPhysicsElement.is(entity)) {
                var rigidBody = EntityPhysicsElement.get(entity).getRigidBody();

                PhysicsThread.get(level).execute(() -> {
                    rigidBody.setMass(mass);
                    rigidBody.setDragCoefficient(dragCoefficient);
                    rigidBody.setFriction(friction);
                    rigidBody.setRestitution(restitution);
                    rigidBody.setTerrainLoadingEnabled(terrainLoading);
                    rigidBody.setBuoyancyType(buoyancyType);
                    rigidBody.setDragType(dragType);
                    rigidBody.prioritize(rigidBody.getSpace().getLevel().getPlayerByUuid(priorityPlayer));
                    rigidBody.activate();
                });
            }
        }
    }
}