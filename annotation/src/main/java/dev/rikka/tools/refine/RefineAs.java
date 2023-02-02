package dev.rikka.tools.refine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// FIXME: comments fix

/**
 * Refine marked class to {@link #value()} referenced class.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface RefineAs {
    /**
     * @return target class
     */
    Class<?> value();
}
