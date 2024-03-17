package dev.lazurite.transporter.api.buffer;

import dev.lazurite.transporter.api.pattern.Pattern;

import java.util.List;

/**
 * A {@link PatternBuffer} extension that allows for modification.
 * @since 1.4.0
 */
public interface ServerPatternBuffer extends PatternBuffer {

    /**
     * Adds a pattern to the buffer.
     */
    void put(Pattern pattern);

    /**
     * Clears the buffer.
     */
    void clear();

    /**
     * Returns all patterns in the buffer, regardless of type.
     */
    List<Pattern> getAll();

}
