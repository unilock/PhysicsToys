package dev.lazurite.transporter.impl.compat.pattern;

import dev.lazurite.transporter.impl.pattern.QuadConsumer;

public interface QuadConsumerProvider {

    static QuadConsumer createSodiumQuadConsumer() {
        return new SodiumQuadConsumer();
    }

}
