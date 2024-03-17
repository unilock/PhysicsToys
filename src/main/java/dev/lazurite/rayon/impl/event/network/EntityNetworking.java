package dev.lazurite.rayon.impl.event.network;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import dev.lazurite.rayon.impl.Rayon;
import dev.lazurite.rayon.impl.bullet.collision.body.EntityRigidBody;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import dev.lazurite.rayon.impl.event.ClientEventHandler;
import dev.lazurite.rayon.impl.event.ServerEventHandler;
import dev.lazurite.toolbox.api.math.QuaternionHelper;
import dev.lazurite.toolbox.api.math.VectorHelper;
import dev.lazurite.toolbox.api.network.ClientNetworking;
import dev.lazurite.toolbox.api.network.PacketRegistry;
import dev.lazurite.toolbox.api.network.ServerNetworking;
import dev.lazurite.toolbox.api.util.PlayerUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.UUID;
import java.util.function.Consumer;

public interface EntityNetworking {
    Identifier MOVEMENT = new Identifier(Rayon.MODID, "movement");
    Identifier PROPERTIES = new Identifier(Rayon.MODID, "properties");

    static void register() {
        PacketRegistry.registerServerbound(MOVEMENT, ServerEventHandler::onMovementPacketReceived);
    }

    static void registerClient() {
        PacketRegistry.registerClientbound(PROPERTIES, ClientEventHandler::onPropertiesPacketReceived);
        PacketRegistry.registerClientbound(MOVEMENT, ClientEventHandler::onMovementPacketReceived);
    }

    static void sendMovement(EntityRigidBody rigidBody) {
        final Consumer<PacketByteBuf> encoder = buf -> {
            buf.writeInt(rigidBody.getElement().cast().getId());
            QuaternionHelper.toBuffer(buf, Convert.toMinecraft(rigidBody.getPhysicsRotation(new Quaternion())));
            VectorHelper.toBuffer(buf, Convert.toMinecraft(rigidBody.getPhysicsLocation(new Vector3f())));
            VectorHelper.toBuffer(buf, Convert.toMinecraft(rigidBody.getLinearVelocity(new Vector3f())));
            VectorHelper.toBuffer(buf, Convert.toMinecraft(rigidBody.getAngularVelocity(new Vector3f())));
        };

        if (rigidBody.getSpace().isServer()) {
            PlayerUtil.tracking(rigidBody.getElement().cast()).forEach(player -> {
                if (!player.equals(rigidBody.getPriorityPlayer())) {
                    ServerNetworking.send(player, MOVEMENT, encoder);
                }
            });
        } else {
            ClientNetworking.send(MOVEMENT, encoder);
        }
    }

    static void sendProperties(EntityRigidBody rigidBody) {
        final Consumer<PacketByteBuf> encoder = buf -> {
            buf.writeInt(rigidBody.getElement().cast().getId());
            buf.writeFloat(rigidBody.getMass());
            buf.writeFloat(rigidBody.getDragCoefficient());
            buf.writeFloat(rigidBody.getFriction());
            buf.writeFloat(rigidBody.getRestitution());
            buf.writeBoolean(rigidBody.terrainLoadingEnabled());
            buf.writeEnumConstant(rigidBody.getBuoyancyType());
            buf.writeEnumConstant(rigidBody.getDragType());
            buf.writeUuid(rigidBody.getPriorityPlayer() == null ? new UUID(0, 0) : rigidBody.getPriorityPlayer().getUuid());
        };

        if (rigidBody.getSpace().isServer()) {
            PlayerUtil.tracking(rigidBody.getElement().cast()).forEach(player -> ServerNetworking.send(player, PROPERTIES, encoder));
        }
    }
}