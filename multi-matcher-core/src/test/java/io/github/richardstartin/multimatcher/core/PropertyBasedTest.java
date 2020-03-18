package io.github.richardstartin.multimatcher.core;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static io.github.richardstartin.multimatcher.core.TestDomainObject.Colour.RED;
import static org.junit.jupiter.api.Assertions.*;

public class PropertyBasedTest {

    private static final Schema<Integer, TestDomainObject> SCHEMA = Schema.<Integer, TestDomainObject>create()
            .withAttribute(0, TestDomainObject::getField1)
            .withAttribute(1, TestDomainObject::getField2)
            .withAttribute(2, TestDomainObject::getField3)
            .withAttribute(3, TestDomainObject::getField4)
            .withAttribute(4, TestDomainObject::getField5)
            .withAttribute(5, TestDomainObject::getMeasure1)
            .withAttribute(6, TestDomainObject::getMeasure2)
            .withAttribute(7, TestDomainObject::getMeasure3)
            .withAttribute(8, TestDomainObject::getColour);


    public static Stream<Arguments> prototypes() {
        return Stream.of(
               Arguments.of(new TestDomainObject("a_1", "b_1",
                       "c_1", "d_1", "e_1",
                       0D, 0, 0, RED),
                       (Function<TestDomainObject, TestDomainObject>) PropertyBasedTest::nextDisjoint,
                       (Function<TestDomainObject, TestDomainObject>) PropertyBasedTest::changeColour,
                       5),
                Arguments.of(new TestDomainObject("a_1", "b_1",
                                "c_1", "d_1", "e_1",
                                0D, 0, 0, RED),
                        (Function<TestDomainObject, TestDomainObject>) PropertyBasedTest::nextDisjoint,
                        (Function<TestDomainObject, TestDomainObject>) PropertyBasedTest::changeColour,
                        63),
                Arguments.of(new TestDomainObject("a_1", "b_1",
                                "c_1", "d_1", "e_1",
                                0D, 0, 0, RED),
                        (Function<TestDomainObject, TestDomainObject>) PropertyBasedTest::nextDisjoint,
                        (Function<TestDomainObject, TestDomainObject>) PropertyBasedTest::changeColour,
                        65),
                Arguments.of(new TestDomainObject("a_1", "b_1",
                                "c_1", "d_1", "e_1",
                                0D, 0, 0, RED),
                        (Function<TestDomainObject, TestDomainObject>) PropertyBasedTest::nextDisjoint,
                        (Function<TestDomainObject, TestDomainObject>) PropertyBasedTest::changeColour,
                        1500),
                Arguments.of(new TestDomainObject("a_1", "b_1",
                                "c_1", "d_1", "e_1",
                                0D, 0, 0, RED),
                        (Function<TestDomainObject, TestDomainObject>) PropertyBasedTest::nextDisjoint,
                        (Function<TestDomainObject, TestDomainObject>) PropertyBasedTest::changeColour,
                        15000),
                Arguments.of(new TestDomainObject("a_1", "b_1",
                                "c_1", "d_1", "e_1",
                                0D, 0, 0, RED),
                        (Function<TestDomainObject, TestDomainObject>) PropertyBasedTest::nextDisjoint,
                        (Function<TestDomainObject, TestDomainObject>) PropertyBasedTest::changeColour,
                        20000)
        );
    }


    @ParameterizedTest
    @MethodSource("prototypes")
    public void testDisjoint(TestDomainObject prototype,
                               Function<TestDomainObject, TestDomainObject> next,
                               Function<TestDomainObject, TestDomainObject> wontMatch,
                               int count) {
        var classifier = Classifier.<Integer, TestDomainObject, Integer>builder(SCHEMA)
                .build(expand(prototype, next, count));
        var input = prototype.clone();
        for (int i = 0; i < count; ++i) {
            var classification = classifier.classificationOrNull(input);
            assertNotNull(classification);
            assertEquals(i, (int)classification);
            assertNull(classifier.classificationOrNull(wontMatch.apply(input)));
            input = next.apply(input);
        }
    }

    @ValueSource(ints = {5, 63, 1500, 16485})
    @ParameterizedTest
    public void consistentOverlaps(int count) {
        var prototype = new TestDomainObject("a_1", "b_1",
                "c_1", "d_1", "e_1",
                0D, 0, 0, RED);
        var classifier = Classifier.<Integer, TestDomainObject, Integer>builder(SCHEMA)
                .build(expand(prototype.clone(), PropertyBasedTest::nextOverlapping, count));
        var matchesByClassification = new HashMap<Integer, Set<Integer>>();
        classifier.forEachClassification(prototype,
                classification -> matchesByClassification.computeIfAbsent(classification, HashSet::new).add(0));
        classifier.forEachClassification(nextOverlapping(prototype),
                classification -> matchesByClassification.computeIfAbsent(classification, HashSet::new).add(1));
        assertEquals(Set.of(0, 1), matchesByClassification.get(0));
        assertEquals(Set.of(1), matchesByClassification.get(1));

    }


    private static List<MatchingConstraint<Integer, Integer>> expand(TestDomainObject prototype,
                                                                    Function<TestDomainObject, TestDomainObject> next,
                                                                    int count) {
        var constraints = new ArrayList<MatchingConstraint<Integer, Integer>>(count);
        for (int i = 0; i < count; ++i) {
            constraints.add(
                    MatchingConstraint.<Integer, Integer>anonymous()
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
                            .classification(i)
                            .build()
            );
            prototype = next.apply(prototype);
        }
        return constraints;
    }


    private static TestDomainObject changeColour(TestDomainObject prototype) {
        return prototype.clone().setColour(TestDomainObject.Colour.next(prototype.getColour()));
    }

    private static TestDomainObject nextDisjoint(TestDomainObject prototype) {
        return prototype.clone()
                .setField1(next(prototype.getField1()))
                .setField2(next(prototype.getField2()))
                .setField3(next(prototype.getField3()))
                .setField4(next(prototype.getField4()))
                .setField5(next(prototype.getField5()))
                .setMeasure1(prototype.getMeasure1() + 1)
                .setMeasure2(prototype.getMeasure2() + 1)
                .setMeasure3(prototype.getMeasure2() + 1);
    }

    private static TestDomainObject nextOverlapping(TestDomainObject prototype) {
        return prototype.clone()
                .setMeasure1(prototype.getMeasure1() + 1)
                .setMeasure2(prototype.getMeasure2() - 1)
                .setMeasure3(prototype.getMeasure2() + 1);
    }

    private static String next(String x) {
        var split = x.split("_");
        return split[0] + "_" + (Integer.parseInt(split[1]) + 1);
    }


}
