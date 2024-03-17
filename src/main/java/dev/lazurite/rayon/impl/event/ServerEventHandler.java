package dev.lazurite.rayon.impl.event;

import com.jme3.math.Vector3f;
import dev.lazurite.rayon.api.EntityPhysicsElement;
import dev.lazurite.rayon.api.event.collision.PhysicsSpaceEvents;
import dev.lazurite.rayon.impl.bullet.collision.body.ElementRigidBody;
import dev.lazurite.rayon.impl.bullet.collision.body.EntityRigidBody;
import dev.lazurite.rayon.impl.bullet.collision.space.MinecraftSpace;
import dev.lazurite.rayon.impl.bullet.collision.space.generator.EntityCollisionGenerator;
import dev.lazurite.rayon.impl.bullet.collision.space.generator.PressureGenerator;
import dev.lazurite.rayon.impl.bullet.collision.space.generator.TerrainGenerator;
import dev.lazurite.rayon.impl.bullet.collision.space.storage.SpaceStorage;
import dev.lazurite.rayon.impl.bullet.collision.space.supplier.entity.ServerEntitySupplier;
import dev.lazurite.rayon.impl.bullet.collision.space.supplier.level.ServerLevelSupplier;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import dev.lazurite.rayon.impl.bullet.thread.PhysicsThread;
import dev.lazurite.rayon.impl.bullet.thread.util.ClientUtil;
import dev.lazurite.rayon.impl.event.network.EntityNetworking;
import dev.lazurite.toolbox.api.event.ServerEvents;
import dev.lazurite.toolbox.api.math.QuaternionHelper;
import dev.lazurite.toolbox.api.math.VectorHelper;
import dev.lazurite.toolbox.api.network.PacketRegistry;
import dev.lazurite.toolbox.api.util.PlayerUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class ServerEventHandler {
    private static PhysicsThread thread;

    public static PhysicsThread getThread() {
        return thread;
    }

    public static void register() {
        // Rayon Events
        PhysicsSpaceEvents.STEP.register(PressureGenerator::step);
        PhysicsSpaceEvents.STEP.register(TerrainGenerator::step);
        PhysicsSpaceEvents.ELEMENT_ADDED.register(ServerEventHandler::onElementAddedToSpace);

        // Server Events
        ServerEvents.Lifecycle.LOAD_SERVER.register(ServerEventHandler::onServerStart);
        ServerEvents.Lifecycle.UNLOAD_SERVER.register(ServerEventHandler::onServerStop);
        ServerEvents.Tick.END_SERVER_TICK.register(ServerEventHandler::onServerTick);

        // Level Events
        ServerEvents.Lifecycle.LOAD_LEVEL.register(ServerEventHandler::onLevelLoad);
        ServerEvents.Tick.START_LEVEL_TICK.register(ServerEventHandler::onStartLevelTick);
        ServerEvents.Tick.START_LEVEL_TICK.register(ServerEventHandler::onEntityStartLevelTick);
        ServerEvents.Block.BLOCK_UPDATE.register(ServerEventHandler::onBlockUpdate);

        // Entity Events
        ServerEvents.Entity.LOAD.register(ServerEventHandler::onEntityLoad);
        ServerEvents.Entity.START_TRACKING.register(ServerEventHandler::onStartTrackingEntity);
        ServerEvents.Entity.STOP_TRACKING.register(ServerEventHandler::onStopTrackingEntity);
    }

    public static void onBlockUpdate(World level, BlockState blockState, BlockPos blockPos) {
        MinecraftSpace.getOptional(level).ifPresent(space -> space.doBlockUpdate(blockPos));
    }

    public static void onServerStart(MinecraftServer server) {
        thread = new PhysicsThread(server, Thread.currentThread(), new ServerLevelSupplier(server), new ServerEntitySupplier(), "Server Physics Thread");
    }

    public static void onServerStop(MinecraftServer server) {
        thread.destroy();
    }

    public static void onServerTick(MinecraftServer server) {
        if (thread.throwable != null) {
            throw new RuntimeException(thread.throwable);
        }
    }

    public static void onStartLevelTick(World level) {
        if (!ClientUtil.isPaused()) {
            MinecraftSpace.get(level).step();
        }
    }

    public static void onLevelLoad(MinecraftServer server, ServerWorld level) {
        final var space = new MinecraftSpace(thread, level);
        ((SpaceStorage) level).setSpace(space);
        PhysicsSpaceEvents.INIT.invoke(space);
    }

    public static void onElementAddedToSpace(MinecraftSpace space, ElementRigidBody rigidBody) {
        if (rigidBody instanceof EntityRigidBody entityBody) {
            final var pos = entityBody.getElement().cast().getPos();
            entityBody.setPhysicsLocation(Convert.toBullet(pos));
        }
    }

    public static void onEntityLoad(Entity entity) {
        if (EntityPhysicsElement.is(entity) && !PlayerUtil.tracking(entity).isEmpty()) {
            var space = MinecraftSpace.get(entity.getWorld());
            space.getWorkerThread().execute(() -> space.addCollisionObject(EntityPhysicsElement.get(entity).getRigidBody()));
        }
    }

    public static void onStartTrackingEntity(Entity entity, ServerPlayerEntity player) {
        if (EntityPhysicsElement.is(entity)) {
            var space = MinecraftSpace.get(entity.getWorld());
            space.getWorkerThread().execute(() -> space.addCollisionObject(EntityPhysicsElement.get(entity).getRigidBody()));
        }
    }

    public static void onStopTrackingEntity(Entity entity, ServerPlayerEntity player) {
        if (EntityPhysicsElement.is(entity) && PlayerUtil.tracking(entity).isEmpty()) {
            var space = MinecraftSpace.get(entity.getWorld());
            space.getWorkerThread().execute(() -> space.removeCollisionObject(EntityPhysicsElement.get(entity).getRigidBody()));
        }
    }

    public static void onEntityStartLevelTick(World level) {
        var space = MinecraftSpace.get(level);
        EntityCollisionGenerator.step(space);

        for (var rigidBody : space.getRigidBodiesByClass(EntityRigidBody.class)) {
            if (rigidBody.isActive()) {
                /* Movement */
                if (rigidBody.isPositionDirty()) {
                    EntityNetworking.sendMovement(rigidBody);
                }

                /* Properties */
                if (rigidBody.arePropertiesDirty()) {
                    EntityNetworking.sendProperties(rigidBody);
                }
            }

            /* Set entity position */
            var location = rigidBody.getFrame().getLocation(new Vector3f(), 1.0f);
            rigidBody.getElement().cast().updatePosition(location.x, location.y, location.z);
        }
    }

    public static void onMovementPacketReceived(PacketRegistry.ServerboundContext context) {
        var buf = context.byteBuf();
        var entityId = buf.readInt();
        var rotation = Convert.toBullet(QuaternionHelper.fromBuffer(buf));
        var location = Convert.toBullet(VectorHelper.fromBuffer(buf));
        var linearVelocity = Convert.toBullet(VectorHelper.fromBuffer(buf));
        var angularVelocity = Convert.toBullet(VectorHelper.fromBuffer(buf));
        var player = context.player();
        var level = player.getWorld();
        var entity = level.getEntityById(entityId);

        if (EntityPhysicsElement.is(entity)) {
            var rigidBody = EntityPhysicsElement.get(entity).getRigidBody();

            if (player.equals(rigidBody.getPriorityPlayer())) {
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
}