package io.github.richardstartin.multimatcher.benchmarks;

import io.github.richardstartin.multimatcher.core.Classifier;
import io.github.richardstartin.multimatcher.core.MatchingConstraint;
import io.github.richardstartin.multimatcher.core.Schema;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import static io.github.richardstartin.multimatcher.benchmarks.TestDomainObject.Colour.RED;


@State(Scope.Benchmark)
public class OverlappingRulesBenchmark {

    static TestDomainObject prototype() {
        return new TestDomainObject("a_1", "b_1",
                "c_1", "d_1", "e_1",
                0D, 0, 0, RED);
    }

    private static Schema<Integer, TestDomainObject> schema() {
        return Schema.<Integer, TestDomainObject>create()
                .withAttribute(0, TestDomainObject::getField1)
                .withAttribute(1, TestDomainObject::getField2)
                .withAttribute(2, TestDomainObject::getField3)
                .withAttribute(3, TestDomainObject::getField4)
                .withAttribute(4, TestDomainObject::getField5)
                .withAttribute(5, TestDomainObject::getMeasure1)
                .withAttribute(6, TestDomainObject::getMeasure2)
                .withAttribute(7, TestDomainObject::getMeasure3)
                .withAttribute(8, TestDomainObject::getColour);
    }


    @Param({"32", "63", "1500", "15000", "20000"})
    int count;

    private List<TestDomainObject> inputs;
    private Classifier<TestDomainObject, String> classifier;


    int index;
    private int[] indices;

    @Setup(Level.Trial)
    public void init() {
        inputs = new ArrayList<>(count);
        int[] factors = new int[] { count, count / 2, count / 4, count / 8, count / 16};
        var constraints = expand(prototype(), x -> nextOverlapping(x, factors), inputs, count);
        classifier = Classifier.<Integer, TestDomainObject, String>builder(schema())
                .useDirectBuffers(true)
                .withOptimisedStorageSpace(100 << 20)
                .build(constraints);

        indices = new int[Integer.lowestOneBit(inputs.size())];
        for (int i = 0; i < inputs.size(); ++i) {
            if (i < indices.length) {
                indices[i] = i;
            } else {
                int replacement = ThreadLocalRandom.current().nextInt(0, i);
                if (replacement < indices.length) {
                    indices[replacement] = i;
                }
            }
        }
    }

    private TestDomainObject next() {
        return inputs.get(indices[(index + 1) & (indices.length - 1)]);
    }


    @Benchmark
    public String classify() {
        return classifier.classificationOrNull(next());
    }


    private static List<MatchingConstraint<Integer, String>> expand(TestDomainObject prototype,
                                                                     Function<TestDomainObject, TestDomainObject> next,
                                                                     List<TestDomainObject> inputs,
                                                                     int count) {
        var constraints = new ArrayList<MatchingConstraint<Integer, String>>(count);
        for (int i = 0; i < count; ++i) {
            constraints.add(
                    MatchingConstraint.<Integer, String>anonymous()
                            .eq(0, prototype.getField1())
                            .eq(1, prototype.getField2())
                            .eq(2, prototype.getField3())
                            .eq(3, prototype.getField4())
                            .eq(4, prototype.getField5())
                            .gt(5, prototype.getMeasure1() - 1e-7)
                            .le(6, prototype.getMeasure2())
                            .ge(7, prototype.getMeasure3())
                            .eq(8, prototype.getColour())
                            .priority(i)
                            .classification("class" + i)
                            .build()
            );
            inputs.add(prototype);
            prototype = next.apply(prototype);
        }
        return constraints;
    }

    private static TestDomainObject nextOverlapping(TestDomainObject prototype, int[] counts) {
        return prototype.clone()
                .setField1(next(prototype.getField1(), counts[0]))
                .setField2(next(prototype.getField2(), counts[1]))
                .setField3(next(prototype.getField3(), counts[2]))
                .setField4(next(prototype.getField4(), counts[3]))
                .setField5(next(prototype.getField5(), counts[4]))
                .setMeasure1(prototype.getMeasure1() + 1)
                .setMeasure2(prototype.getMeasure2() - 1)
                .setMeasure3(prototype.getMeasure2() + 1)
                .setColour(TestDomainObject.Colour.next(prototype.getColour()));
    }

    private static String next(String x, int count) {
        var split = x.split("_");
        return split[0] + "_" + ((Integer.parseInt(split[1]) + 1) % count);
    }
}
