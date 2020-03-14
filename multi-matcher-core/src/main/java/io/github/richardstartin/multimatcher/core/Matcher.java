package io.github.richardstartin.multimatcher.core;

public interface Matcher<T, MaskType> {
    /**
     * Returns the identities named all named the constraints which are satisfied bt the value,
     * so long as they have not already been invalidated by prior mismatches on other attributes
     *
     * @param value   the value to match
     * @param context the identities named constraints satisfied prior to the match
     */
    void match(T value, MaskType context);

    default float averageSelectivity() {
        return 1;
    }
}
