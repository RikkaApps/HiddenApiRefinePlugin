package dev.rikka.tools.refine;

import java.util.Iterator;

public final class Iterables {
    public static <T> T singleOrNull(Iterable<T> iterable) {
        final Iterator<T> iterator = iterable.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }

        return null;
    }
}
