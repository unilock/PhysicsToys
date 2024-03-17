package dev.lazurite.toolbox.api.event;

import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ServerEvents {
    public static class Lifecycle {
        public static final Event<LoadLevel> LOAD_LEVEL = Event.create();
        public static final Event<UnloadLevel> UNLOAD_LEVEL = Event.create();
        public static final Event<ServerLoad> LOAD_SERVER = Event.create();
        public static final Event<ServerUnload> UNLOAD_SERVER = Event.create();
        public static final Event<Join> JOIN = Event.create();

        @FunctionalInterface
        public interface LoadLevel {
            void onLoadLevel(MinecraftServer server, ServerWorld level);
        }

        @FunctionalInterface
        public interface UnloadLevel {
            void onUnloadLevel(MinecraftServer server, ServerWorld level);
        }

        @FunctionalInterface
        public interface ServerLoad {
            void onServerLoad(MinecraftServer server);
        }

        @FunctionalInterface
        public interface ServerUnload {
            void onServerUnload(MinecraftServer server);
        }

        @FunctionalInterface
        public interface Join {
            void onJoin(ServerPlayerEntity player);
        }
    }

    public static class Tick {
        public static final Event<StartLevelTick> START_LEVEL_TICK = Event.create();
        public static final Event<EndLevelTick> END_LEVEL_TICK = Event.create();
        public static final Event<StartServerTick> START_SERVER_TICK = Event.create();
        public static final Event<EndServerTick> END_SERVER_TICK = Event.create();

        @FunctionalInterface
        public interface StartLevelTick {
            void onStartLevelTick(ServerWorld level);
        }

        @FunctionalInterface
        public interface EndLevelTick {
            void onEndLevelTick(ServerWorld level);
        }

        @FunctionalInterface
        public interface StartServerTick {
            void onStartServerTick(MinecraftServer server);
        }

        @FunctionalInterface
        public interface EndServerTick {
            void onEndServerTick(MinecraftServer server);
        }
    }

    public static class Block {
        public static final Event<BlockUpdate> BLOCK_UPDATE = Event.create();

        @FunctionalInterface
        public interface BlockUpdate {
            void onBlockUpdate(World level, BlockState blockState, BlockPos blockPos);
        }
    }
    public static class Entity {
        public static final Event<Load> LOAD = Event.create();
        public static final Event<Unload> UNLOAD = Event.create();
        public static final Event<StartTracking> START_TRACKING = Event.create();
        public static final Event<StopTracking> STOP_TRACKING = Event.create();

        @FunctionalInterface
        public interface Load {
            void onLoad(net.minecraft.entity.Entity entity);
        }

        @FunctionalInterface
        public interface Unload {
            void onUnload(net.minecraft.entity.Entity entity);
        }

        @FunctionalInterface
        public interface StartTracking {
            void onStartTracking(net.minecraft.entity.Entity entity, ServerPlayerEntity player);
        }

        @FunctionalInterface
        public interface StopTracking{
            void onStopTracking(net.minecraft.entity.Entity entity, ServerPlayerEntity player);
        }
    }
}
