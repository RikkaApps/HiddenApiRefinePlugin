package dev.rikka.tools.refine;

// FIXME: comments fix

/**
 * Utils to use refine plugin.
 */
public final class Refine {
    /**
     * Unsafe cast an object to {@link T}. NOTE: may throw {@link java.lang.ClassCastException} if refine plugin broken.
     *
     * @param obj any object
     * @param <T> target type
     * @return object with target type
     */
    @SuppressWarnings("unchecked")
    public static <T> T unsafeCast(final Object obj) {
        return (T) obj;
    }
}
