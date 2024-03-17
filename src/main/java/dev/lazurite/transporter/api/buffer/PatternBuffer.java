package dev.lazurite.transporter.api.buffer;

import dev.lazurite.transporter.api.pattern.Pattern;
import dev.lazurite.transporter.impl.buffer.PatternBufferImpl;

/**
 * A pattern buffer is a list of {@link Pattern} objects that represent either a block, entity, or item.
 * @see PatternBufferImpl
 * @since 1.0.0
 */
public interface PatternBuffer {

    /**
     * Returns a {@link Pattern} that matches the provided identifier. No duplicate entries are allowed.
     * @param type the type of the pattern
     * @param registryId the id found in the type's respective registry
     * @return the matching {@link Pattern}
     * @see Pattern.Type
     */
    Pattern get(Pattern.Type type, int registryId);

    default Pattern getEntity(int registryId) {
        return get(Pattern.Type.ENTITY, registryId);
    }

    default Pattern getItem(int registryId) {
        return get(Pattern.Type.ITEM, registryId);
    }

    default Pattern getBlock(int registryId) {
        return get(Pattern.Type.BLOCK, registryId);
    }

    /**
     * Similar to {@link PatternBuffer#get}, this method simply returns true if the
     * buffer contains an entry matching the identifier or false if it does not.
     * @param type the type of the pattern
     * @param registryId the id found in the type's respective registry
     * @return whether there exists a matching {@link Pattern} entry
     */
    boolean contains(Pattern.Type type, int registryId);

    default boolean containsEntity(int registryId) {
        return contains(Pattern.Type.ENTITY, registryId);
    }

    default boolean containsItem(int registryId) {
        return contains(Pattern.Type.ITEM, registryId);
    }

    default boolean containsBlock(int registryId) {
        return contains(Pattern.Type.BLOCK, registryId);
    }

    /**
     * Gets the size of the buffer.
     */
    int size();

}