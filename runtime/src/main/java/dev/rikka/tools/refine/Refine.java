package dev.rikka.tools.refine;

/**
 * Util class for using HiddenApiRefine plugin.
 */
public final class Refine {

    /**
     * Cast an object to {@link T}.
     * <p>
     * A typical usage is cast a "Hidden" class to its real type.
     * <br>
     * For example, {@code android.os.UserHandle} is a public class, but some of its methods are hidden.
     * Therefore, you can create a {@code android.os.UserHandleHidden} class annotated with
     * {@code @RefineAs(android.os.UserHandle.class)}) to access them.
     * <br>
     * When you have an instance of {@code android.os.UserHandle} and you need to access its hidden methods,
     * at this time you can use this method to cast it to {@code android.os.UserHandleHidden}.
     *
     * @param obj Object
     * @param <T> Target type
     * @return Object with target type
     * @throws java.lang.ClassCastException If HiddenApiRefine plugin is not correctly set or not working
     */
    @SuppressWarnings("unchecked")
    public static <T> T unsafeCast(final Object obj) {
        return (T) obj;
    }
}
