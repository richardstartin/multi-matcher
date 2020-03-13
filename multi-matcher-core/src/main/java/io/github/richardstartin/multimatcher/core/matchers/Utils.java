package io.github.richardstartin.multimatcher.core.matchers;

import java.lang.reflect.Array;

public class Utils {

    static <T> int nullCount(T... values) {
        int count = 0;
        for (T value : values) {
            count += null == value ? 1 : 0;
        }
        return count;
    }

    @SuppressWarnings("unchecked")
    static <T> T[] newArray(Class<T> type, int size) {
        return (T[])Array.newInstance(type, size);
    }
}
