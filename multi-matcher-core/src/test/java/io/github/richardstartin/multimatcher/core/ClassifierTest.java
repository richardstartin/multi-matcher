package io.github.richardstartin.multimatcher.core;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.collect.ImmutableMap;
import io.github.richardstartin.multimatcher.core.schema.AttributeNotRegistered;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ClassifierTest {

    @Test
    public void testBuildClassifierEmptyRepo() throws IOException {
        Classifier<TestDomainObject, String> engine = buildSimple(ArrayList::new);
        assertFalse(engine.classification(TestDomainObject.random()).isPresent());
    }

    @Test
    public void testBuildClassifierOneRule() throws IOException {
        Classifier<TestDomainObject, String> engine = buildSimple(
                () -> Collections.singletonList(
                        MatchingConstraint.<String, String>named("rule1")
                                .eq("field1", "foo")
                                .lt("measure1", 0D)
                                .priority(0)
                                .classification("RED")
                                .build()
                )
        );

        assertFalse(engine.classification(TestDomainObject.random()).isPresent());
        TestDomainObject testTrigger = TestDomainObject.random();
        testTrigger.setField1("foo");
        testTrigger.setMeasure1(-1);
        String triggered = engine.classification(testTrigger).orElseThrow(AssertionError::new);
        assertEquals(triggered, "RED");
        testTrigger.setMeasure1(1);
        assertFalse(engine.classification(testTrigger).isPresent());
    }


    @Test
    public void testBuildClassifierTwoDisjointRules() throws IOException {
        Classifier<TestDomainObject, String> engine = buildSimple(
                () -> Arrays.asList(
                        MatchingConstraint.<String, String>named("rule1")
                                .eq("field1", "foo")
                                .lt("measure1", 0D)
                                .priority(0)
                                .classification("RED")
                                .build(),
                        MatchingConstraint.<String, String>named("rule2")
                                .eq("field1", "bar")
                                .lt("measure1", 0D)
                                .priority(0)
                                .classification("BLUE")
                                .build()
                )
        );

        assertFalse(engine.classification(TestDomainObject.random()).isPresent());
        TestDomainObject testTrigger = TestDomainObject.random();
        testTrigger.setField1("foo");
        testTrigger.setMeasure1(-1);
        List<String> classifications = new ArrayList<>();
        engine.forEachClassification(testTrigger, classifications::add);
        assertEquals(1, classifications.size());
        assertEquals("RED", classifications.get(0));
        testTrigger.setMeasure1(1);
        assertFalse(engine.classification(testTrigger).isPresent());
    }


    @Test
    public void testBuildClassifierWithNotEqualRule() throws IOException {
        Classifier<TestDomainObject, String> engine = buildSimple(
                () -> Arrays.asList(
                        MatchingConstraint.<String, String>named("rule1")
                                .eq("field1", "foo")
                                .lt("measure1", 0D)
                                .priority(0)
                                .classification("RED")
                                .build(),
                        MatchingConstraint.<String, String>named("rule2")
                                .neq("field1", "bar")
                                .lt("measure1", 0D)
                                .priority(1)
                                .classification("BLUE")
                                .build()
                )
        );

        assertFalse(engine.classification(TestDomainObject.random().setMeasure1(1D)).isPresent());
        TestDomainObject testTrigger = TestDomainObject.random();
        testTrigger.setField1("foo");
        testTrigger.setMeasure1(-1);
        List<String> classifications = new ArrayList<>();
        engine.forEachClassification(testTrigger, classifications::add);
        assertEquals(2, classifications.size());
        assertEquals("BLUE", classifications.get(0));
        assertEquals("RED", classifications.get(1));
        testTrigger.setMeasure1(1);
        assertFalse(engine.classification(testTrigger).isPresent());
    }

    @Test
    public void testBuildClassifierWithStringNotEqualRule() throws IOException {
        Classifier<TestDomainObject, String> engine = buildStringMatcher(
                () -> Arrays.asList(
                        MatchingConstraint.<String, String>named("rule1")
                                .eq("field1", "foo")
                                .eq("field2", "qux")
                                .priority(0)
                                .classification("RED")
                                .build(),
                        MatchingConstraint.<String, String>named("rule2")
                                .neq("field1", "bar")
                                .eq("field2", "qux")
                                .priority(1)
                                .classification("BLUE")
                                .build()
                )
        );

        assertFalse(engine.classification(TestDomainObject.random()).isPresent());
        TestDomainObject testTrigger = TestDomainObject.random();
        testTrigger.setField1("foo");
        testTrigger.setField2("qux");
        List<String> classifications = new ArrayList<>();
        engine.forEachClassification(testTrigger, classifications::add);
        assertEquals(2, classifications.size());
        assertEquals("BLUE", classifications.get(0));
        assertEquals("RED", classifications.get(1));
        testTrigger.setField1("bar");
        System.out.println(testTrigger);
        //System.out.println(engine.classification(testTrigger).orElse("none"));
        assertFalse(engine.classification(testTrigger).isPresent());
    }

    @Test
    public void testBuildClassifierTwoOverlappingRules() throws IOException {
        Classifier<TestDomainObject, String> engine = buildSimple(
                () -> Arrays.asList(
                        MatchingConstraint.<String, String>named("rule1")
                                .eq("field1", "foo")
                                .gt("measure1", 0D)
                                .priority(0)
                                .classification("RED")
                                .build(),
                        MatchingConstraint.<String, String>named("rule2")
                                .eq("field1", "foo")
                                .gt("measure1", 1D)
                                .priority(1)
                                .classification("BLUE")
                                .build()
                )
        );
        assertFalse(engine.classification(TestDomainObject.random()).isPresent());
        TestDomainObject testTrigger = TestDomainObject.random();
        testTrigger.setField1("foo");
        testTrigger.setMeasure1(-1);
        assertFalse(engine.classification(testTrigger).isPresent());
        testTrigger.setMeasure1(0.5);
        List<String> classifications = new ArrayList<>();
        engine.forEachClassification(testTrigger, classifications::add);
        assertEquals(1, classifications.size());
        assertEquals("RED", classifications.get(0));
        testTrigger.setMeasure1(1.5);
        classifications.clear();
        engine.forEachClassification(testTrigger, classifications::add);
        assertEquals(2, classifications.size());
        assertEquals("BLUE", classifications.get(0));
        assertEquals("RED", classifications.get(1));
    }


    @Test
    public void testBuildClassifierWithRulesOnDifferentAttributes() throws IOException {
        Classifier<TestDomainObject, String> engine = buildSimple(
                () -> Arrays.asList(
                        MatchingConstraint.<String, String>named("rule1").eq("field1", "foo").priority(0).classification("RED").build(),
                        MatchingConstraint.<String, String>named("rule2").eq("field2", "bar").priority(1).classification("BLUE").build()
                )
        );
        TestDomainObject value = TestDomainObject.random();
        assertFalse(engine.classification(value).isPresent());
        assertEquals(engine.classification(value.setField1("foo")).get(), "RED");
        assertEquals(engine.classification(value.setField2("bar")).get(), "BLUE");
    }

    @Test
    public void testBuildClassifierComparableAttributes() throws IOException {
        Classifier<TestDomainObject, String> engine = buildComparable(
                () -> Arrays.asList(
                        MatchingConstraint.<String, String>named("rule1").eq("field1", "foo").priority(0).classification("RED").build(),
                        MatchingConstraint.<String, String>named("rule2").eq("field2", "bar").priority(1).classification("BLUE").build()
                )
        );
        TestDomainObject value = TestDomainObject.random();
        assertFalse(engine.classification(value).isPresent());
        assertEquals(engine.classification(value.setField1("foo")).get(), "RED");
        assertEquals(engine.classification(value.setField2("bar")).get(), "BLUE");
    }

    @Test
    public void testIntegerRules() {
        Classifier<TestDomainObject, String> classifier =
                Classifier.<String, TestDomainObject, String>builder(Schema.<String, TestDomainObject>create()
                        .withAttribute("measure2", TestDomainObject::getMeasure2)
                ).build(Arrays.asList(
                        MatchingConstraint.<String, String>named("rule1").eq("measure2", 999).priority(0).classification("RED").build(),
                        MatchingConstraint.<String, String>named("rule2").lt("measure2", 1000).priority(1).classification("BLUE").build()
                ));

        TestDomainObject test = TestDomainObject.random();
        assertFalse(classifier.classification(test.setMeasure2(1000)).isPresent());
        assertEquals("BLUE", classifier.classification(test.setMeasure2(998)).get());
        List<String> classifications = new ArrayList<>();
        classifier.forEachClassification(test.setMeasure2(999), classifications::add);
        assertEquals(2, classifications.size());
        assertEquals("RED", classifications.get(1));
    }

    @Test
    public void testLongRules() {
        Classifier<TestDomainObject, String> classifier = Classifier.<String, TestDomainObject, String>
                builder(Schema.<String, TestDomainObject>create()
                .withAttribute("measure3", TestDomainObject::getMeasure3)
        ).build(Arrays.asList(
                MatchingConstraint.<String, String>named("rule1").eq("measure3", 999).priority(0).classification("RED").build(),
                MatchingConstraint.<String, String>named("rule1").lt("measure3", 1000).priority(1).classification("BLUE").build()
        ));

        TestDomainObject test = TestDomainObject.random();
        assertFalse(classifier.classification(test.setMeasure3(1000)).isPresent());
        assertEquals("BLUE", classifier.classification(test.setMeasure3(998)).get());
        List<String> classifications = new ArrayList<>();
        classifier.forEachClassification(test.setMeasure3(999), classifications::add);
        assertEquals(2, classifications.size());
        assertEquals("RED", classifications.get(1));
    }

    @Test
    public void testRangeBasedRules() throws IOException {
        Classifier<TestDomainObject, String> engine = buildWithContinuousAttributes(() -> Arrays.asList(
                MatchingConstraint.<String, String>named("rule1").gt("measure1", 10).priority(1).classification("RED").build(),
                MatchingConstraint.<String, String>named("rule2").lt("measure1", 8).priority(2).classification("BLUE").build(),
                MatchingConstraint.<String, String>named("rule3").eq("measure1", 5).priority(3).classification("YELLOW").build()
        ));
        TestDomainObject value = TestDomainObject.random().setMeasure1(11);
        assertEquals("RED", engine.classification(value).get());
        assertEquals("YELLOW", engine.classification(value.setMeasure1(5)).get());
        assertEquals("BLUE", engine.classification(value.setMeasure1(2.5)).get());
    }

    @Test
    public void testBuildRuleClassifierUnregisteredAttribute() {
        assertThrows(AttributeNotRegistered.class, () ->
                buildSimple(() -> Collections.singletonList(
                        MatchingConstraint.<String, String>named("missing").eq("missing", "missing")
                                .priority(0).classification("MISSING").build()))
        );
    }


    @Test
    public void testBuildRuleClassifierWithBadTypeConstraint() {
        assertThrows(ClassCastException.class, () ->
                buildSimple(() -> Collections.singletonList(
                        MatchingConstraint.<String, String>named("measure1").eq("measure1", "foo")
                                .priority(0).classification("BAD TYPE").build())));
    }

    @Test
    public void testBuildRuleClassifierFromInvalidYAML() {
        assertThrows(IOException.class, () ->
                Classifier.<String, TestDomainObject, String>builder(Schema.create())
                        .build(new FileRules("invalid.yaml", new YAMLMapper()).constraints()));
    }

    @Test
    public void testBuildRuleClassifierFromYAML() throws IOException {
        buildSimple(new FileRules("test.yaml", new YAMLMapper()));
    }

    @Test
    public void testBuildSpecFromYAML() throws IOException {
        RuleSet<String, String> specs = new FileRules("test.yaml", new YAMLMapper());
        assertEquals("rule1", specs.specification("rule1").get().getId());
        assertEquals(2, specs.constraints().size());
    }

    @Test
    public void testBuildFromEnumSchema() {
        Classifier<TestDomainObject, Duration> classifier =
                Classifier.<TestDomainObject.Fields, TestDomainObject, Duration>builder(Schema.<TestDomainObject.Fields, TestDomainObject>create(TestDomainObject.Fields.class)
                        .withAttribute(TestDomainObject.Fields.FIELD1, TestDomainObject::getField1)
                        .withAttribute(TestDomainObject.Fields.MEASURE1, TestDomainObject::getMeasure1))
                        .build(Arrays.asList(
                                new MatchingConstraint<>("rule1",
                                        ImmutableMap.of(TestDomainObject.Fields.MEASURE1, Constraint.greaterThan(10)),
                                        0, Duration.ofDays(1)
                                ),
                                new MatchingConstraint<>("rule2",
                                        ImmutableMap.of(
                                                TestDomainObject.Fields.FIELD1, Constraint.equalTo("foo"),
                                                TestDomainObject.Fields.MEASURE1, Constraint.greaterThan(10)),
                                        1, Duration.ofDays(2)
                                )
                        ));
        assertEquals(Duration.ofDays(1),
                classifier.classification(TestDomainObject.random().setMeasure1(11)).get());
        assertEquals(Duration.ofDays(2),
                classifier.classification(TestDomainObject.random().setMeasure1(11).setField1("foo")).get());
    }

    @Test
    public void testStringMatcher() throws IOException {
        Classifier<TestDomainObject, String> classifier = buildStringMatcher(() ->
                Arrays.asList(MatchingConstraint.<String, String>anonymous()
                                .startsWith("field1", "foo")
                                .eq("field3", "bar")
                                .priority(0)
                                .classification("RED")
                                .build(),
                        MatchingConstraint.<String, String>anonymous()
                                .startsWith("field1", "fo")
                                .priority(1)
                                .classification("BLUE")
                                .build()));
        TestDomainObject test = TestDomainObject.random();
        var classifications = new ArrayList<>();
        classifier.forEachClassification(test, classifications::add);
        assertTrue(classifications.isEmpty());
        assertFalse(classifier.classification(test).isPresent());
        assertEquals("BLUE", classifier.classification(test.setField1("foo").setField3("bar")).orElse("NONE"));
        assertEquals(2, classifier.matchCount(test.setField1("foo").setField3("bar")));
    }

    @Test
    public void testStringAndEnumMatcher() throws IOException {
        Classifier<TestDomainObject, String> classifier = buildStringAndEnumMatcher(() ->
                Arrays.asList(MatchingConstraint.<String, String>anonymous()
                                .startsWith("field1", "foo")
                                .eq("field3", "bar")
                                .eq("colour", TestDomainObject.Colour.RED)
                                .priority(0)
                                .classification("REDbar")
                                .build(),
                        MatchingConstraint.<String, String>anonymous()
                                .startsWith("field1", "fo")
                                .eq("colour", TestDomainObject.Colour.BLUE)
                                .priority(1)
                                .classification("BLUEfoo")
                                .build()));
        TestDomainObject test = TestDomainObject.random();
        var classifications = new ArrayList<>();
        classifier.forEachClassification(test, classifications::add);
        assertTrue(classifications.isEmpty());
        assertFalse(classifier.classification(test).isPresent());
        assertEquals("BLUEfoo", classifier.classification(test.setField1("foo").setField3("bar").setColour(TestDomainObject.Colour.BLUE)).orElse("NONE"));
    }

    private Classifier<TestDomainObject, String> buildSimple(RuleSet<String, String> repo) throws IOException {
        return Classifier.<String, TestDomainObject, String>builder(Schema.<String, TestDomainObject>create()
                .withAttribute("field1", TestDomainObject::getField1)
                .withStringAttribute("field2", TestDomainObject::getField2)
                .withAttribute("measure1", TestDomainObject::getMeasure1)
        ).build(repo.constraints());
    }


    private Classifier<TestDomainObject, String> buildComparable(RuleSet<String, String> repo) throws IOException {
        return Classifier.<String, TestDomainObject, String>builder(Schema.<String, TestDomainObject>create()
                .withAttribute("field1", TestDomainObject::getField1, Comparator.naturalOrder())
                .withAttribute("field2", TestDomainObject::getField2, Comparator.naturalOrder())
        ).build(repo.constraints());
    }

    private Classifier<TestDomainObject, String> buildWithContinuousAttributes(RuleSet<String, String> repo) throws IOException {
        return Classifier.<String, TestDomainObject, String>builder(Schema.<String, TestDomainObject>create()
                .withAttribute("measure1", TestDomainObject::getMeasure1)
                .withAttribute("measure2", TestDomainObject::getMeasure2)
                .withAttribute("measure3", TestDomainObject::getMeasure3)
        ).build(repo.constraints());
    }


    private Classifier<TestDomainObject, String> buildStringMatcher(RuleSet<String, String> repo) throws IOException {
        return Classifier.<String, TestDomainObject, String>builder(Schema.<String, TestDomainObject>create()
                .withStringAttribute("field1", TestDomainObject::getField1)
                .withStringAttribute("field2", TestDomainObject::getField2)
                .withStringAttribute("field3", TestDomainObject::getField3)
        ).build(repo.constraints());
    }

    private Classifier<TestDomainObject, String> buildStringAndEnumMatcher(RuleSet<String, String> repo) throws IOException {
        return Classifier.<String, TestDomainObject, String>builder(Schema.<String, TestDomainObject>create()
                .withStringAttribute("field1", TestDomainObject::getField1)
                .withStringAttribute("field2", TestDomainObject::getField2)
                .withStringAttribute("field3", TestDomainObject::getField3)
                .withEnumAttribute("colour", TestDomainObject::getColour, TestDomainObject.Colour.class)
        ).build(repo.constraints());
    }

}