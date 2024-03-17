package dev.lazurite.toolbox.api.event;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;

/**
 * These events are slightly more useful versions of what exist in FAPI.
 * @see Event
 * @since 1.0.0
 */
public final class ClientEvents {
    public static class Lifecycle {
        public static final Event<LoadLevel> LOAD_LEVEL = Event.create();
        public static final Event<PreLogin> PRE_LOGIN = Event.create();
        public static final Event<PostLogin> POST_LOGIN = Event.create();
        public static final Event<Disconnect> DISCONNECT = Event.create();

        @FunctionalInterface
        public interface LoadLevel {
            void onLoadLevel(MinecraftClient minecraft, ClientWorld level);
        }

        @FunctionalInterface
        public interface PreLogin {
            void onPreLogin(MinecraftClient minecraft);
        }

        @FunctionalInterface
        public interface PostLogin {
            void onPostLogin(MinecraftClient minecraft, ClientWorld level, ClientPlayerEntity player);
        }

        @FunctionalInterface
        public interface Disconnect {
            void onDisconnect(MinecraftClient minecraft, ClientWorld level);
        }
    }

    public static class Tick {
        public static final Event<StartLevelTick> START_LEVEL_TICK = Event.create();
        public static final Event<EndLevelTick> END_LEVEL_TICK = Event.create();
        public static final Event<StartClientTick> START_CLIENT_TICK = Event.create();
        public static final Event<EndClientTick> END_CLIENT_TICK = Event.create();

        @FunctionalInterface
        public interface StartLevelTick {
            void onStartTick(ClientWorld level);
        }

        @FunctionalInterface
        public interface EndLevelTick {
            void onEndTick(ClientWorld level);
        }

        @FunctionalInterface
        public interface StartClientTick {
            void onStartTick(MinecraftClient minecraft);
        }

        @FunctionalInterface
        public interface EndClientTick {
            void onEndTick(MinecraftClient minecraft);
        }
    }

    public static class Entity {
        public static final Event<EntityLoad> LOAD = Event.create();
        public static final Event<EntityUnload> UNLOAD = Event.create();

        @FunctionalInterface
        public interface EntityLoad {
            void onStartTick(net.minecraft.entity.Entity entity);
        }

        @FunctionalInterface
        public interface EntityUnload {
            void onEndTick(net.minecraft.entity.Entity entity);
        }
    }

    public static class Player {
        public static final Event<PlayerAdd> ADD = Event.create();

        @FunctionalInterface
        public interface PlayerAdd {
            void onAdd(AbstractClientPlayerEntity abstractClientPlayer, boolean isLocalPlayer);
        }
    }

    public static class Render {
        public static final Event<BeforeDebug> BEFORE_DEBUG = Event.create();

        @FunctionalInterface
        public interface BeforeDebug {
            void onBeforeDebug(MatrixStack poseStack, Camera camera, ClientWorld level, float tickDelta);
        }
    }
}