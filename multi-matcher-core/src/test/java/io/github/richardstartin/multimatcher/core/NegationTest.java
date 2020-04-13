package io.github.richardstartin.multimatcher.core;

import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static io.github.richardstartin.multimatcher.core.TestDomainObject.Colour.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Execution(ExecutionMode.CONCURRENT)
public class NegationTest {


    private static final Schema<String, TestDomainObject> SCHEMA = Schema.<String, TestDomainObject>create()
            .withEnumAttribute("w", TestDomainObject::getColour, TestDomainObject.Colour.class)
            .withAttribute("x", TestDomainObject::getField1)
            .withAttribute("y", TestDomainObject::getField2)
            .withAttribute("z", TestDomainObject::getField3);


    /*
                        RED       BLUE       YELLOW
                        x1   *    x1    *    x1     *
                        0    1     0    0    0      0
                        0    0     0    1    0      0
                        0    0     0    0    0      1
                        0    0     1    0    1      0
                        1    0     0    0    1      0
                        1    0     1    0    0      0


                        RED   BLUE   YELLOW
                        1     0      0
                        0     1      0
                        0     0      1
                        0     1      1
                        1     0      1
                        1     1      0


                        x1 *
                        0  1
                        0  1
                        0  1
                        1  0
                        1  0
                        1  0


                        x1 & RED  = 110001 & 111000 = 110000 = class 5 (x=x1 & colour != BLUE)
                        x1 & BLUE = 101010 & 111000 = 101000 = class 4 (x=x1 & colour != RED)
                        x1 & YELLOW = 011100 & 111000 = 011000 = class 4 (x=x1 & colour != RED)
                        x2 & RED = * & RED = 110001 & 100111 = 1 = class 1 (x!=x1 & colour=RED)
                        x2 & BLUE = * & BLUE = 101010 & 000111 = 10 = class 2 (x!=x1 & colour=BLUE)
                        x2 & YELLOW = * & YELLOW = 011100 & 000111 = 100 = class 2 (x!=x1 & colour=YELLOW)
                     */

    private static final Classifier<TestDomainObject, String> CLASSIFIER =
            Classifier.<String, TestDomainObject, String>builder(SCHEMA).build(
            Arrays.asList(
                    MatchingConstraint.<String, String>anonymous()
                            .eq("w", RED)
                            .neq("x", "x1")
                            .classification("class1")
                            .build(),
                    MatchingConstraint.<String, String>anonymous()
                            .eq("w", BLUE)
                            .neq("x", "x1")
                            .classification("class2")
                            .build(),
                    MatchingConstraint.<String, String>anonymous()
                            .eq("w", YELLOW)
                            .neq("x", "x1")
                            .classification("class3")
                            .build(),
                    MatchingConstraint.<String, String>anonymous()
                            .neq("w", RED)
                            .eq("x", "x1")
                            .classification("class4")
                            .build(),
                    MatchingConstraint.<String, String>anonymous()
                            .neq("w", BLUE)
                            .eq("x", "x1")
                            .classification("class5")
                            .build(),
                    MatchingConstraint.<String, String>anonymous()
                            .neq("w", YELLOW)
                            .eq("x", "x1")
                            .classification("class6")
                            .build()
            )
    );


    public static Stream<Arguments> examples() {
        return Stream.of(
                Arguments.of(TestDomainObject.random().setColour(RED), "class1"),
                Arguments.of(TestDomainObject.random().setColour(BLUE), "class2"),
                Arguments.of(TestDomainObject.random().setColour(YELLOW), "class3"),
                Arguments.of(TestDomainObject.random().setColour(RED).setField1("x1"), "class5"),
                Arguments.of(TestDomainObject.random().setColour(BLUE).setField1("x1"), "class4"),
                Arguments.of(TestDomainObject.random().setColour(YELLOW).setField1("x1"), "class4")
        );
    }




    @ParameterizedTest
    @MethodSource("examples")
    public void testSimpleConfig(TestDomainObject object, String expected) {
        assertEquals(expected, CLASSIFIER.classificationOrNull(object));
    }
}
