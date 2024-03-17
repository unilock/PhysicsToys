package dev.lazurite.rayon.impl.bullet.thread.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

public class ClientUtil {
    public static boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    public static boolean isPaused() {
        if (isClient()) {
            return MinecraftClient.getInstance().isPaused();
        }
        return false;
    }

    public static boolean isConnectedToServer() {
        if (isClient()) {
            return MinecraftClient.getInstance().getNetworkHandler() != null;
        }
        return false;
    }
}
