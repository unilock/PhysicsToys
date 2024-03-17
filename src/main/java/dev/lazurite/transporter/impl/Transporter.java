package dev.lazurite.transporter.impl;

import dev.lazurite.transporter.api.buffer.PatternBuffer;
import dev.lazurite.transporter.impl.buffer.PatternBufferImpl;
import dev.lazurite.transporter.impl.pattern.net.PatternPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Transporter {

    public static final String MODID = "transporter";
    public static final Logger LOGGER = LogManager.getLogger("Transporter");
    private static PatternBuffer BUFFER;

    public static void initialize() {
        LOGGER.info("Beam me up, Scotty!");
        ServerPlayNetworking.registerGlobalReceiver(PatternPacket.TYPE, PatternPacket::accept);
    }

    public static PatternBuffer getPatternBuffer() {
        if (BUFFER == null) {
            BUFFER = new PatternBufferImpl();
        }
        return BUFFER;
    }

}