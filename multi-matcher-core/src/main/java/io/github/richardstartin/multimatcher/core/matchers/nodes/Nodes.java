package io.github.richardstartin.multimatcher.core.matchers.nodes;

import io.github.richardstartin.multimatcher.core.Operation;

import java.util.Iterator;
import java.util.Map;

class Nodes {

    public static String toString(int count,
                                  Operation op,
                                  Iterator<?> thresholds,
                                  Iterator<?> matches) {
        StringBuilder sb = new StringBuilder().append(count).append(" thresholds): [");
        while (thresholds.hasNext() && matches.hasNext()) {
            sb.append("(_ ")
                    .append(op)
                    .append(" ")
                    .append(thresholds.next()).append(": ")
                    .append(matches.next())
                    .append("), ");
        }
        sb.setCharAt(sb.length() - 2, ']');
        return sb.toString();
    }

    public static String toString(int count,
                                  Operation op,
                                  Map<?, ?> conditions) {
        StringBuilder sb = new StringBuilder().append(count).append(" thresholds): [");
        for (var condition : conditions.entrySet()) {
            sb.append("(_ ")
                    .append(op)
                    .append(" ")
                    .append(condition.getKey())
                    .append(": ")
                    .append(condition.getValue())
                    .append("), ");
        }
        sb.setCharAt(sb.length() - 2, ']');
        return sb.toString();
    }
}
