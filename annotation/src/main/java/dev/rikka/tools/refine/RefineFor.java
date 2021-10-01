package dev.rikka.tools.refine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TODO
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
public @interface RefineFor {
    /**
     * @return target class to refine
     */
    Class<?> value();
}
