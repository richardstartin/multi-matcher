package io.github.richardstartin.multimatcher.benchmarks;
import org.openjdk.jol.info.GraphLayout;

import static io.github.richardstartin.multimatcher.benchmarks.LargeClassifierBenchmark.largeDiscreteClassifier;

public class Layout {

    public static void main(String... args) {
        int size = Integer.parseInt(args[0]);
        var classifier = largeDiscreteClassifier(size);
        var parsed = GraphLayout.parseInstance(classifier);
        System.out.println(parsed.toPrintable());
        System.out.println(parsed.toFootprint());

    }
}
