package uk.co.openkappa.bitrules;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.co.openkappa.bitrules.config.AttributeNotRegistered;
import uk.co.openkappa.bitrules.config.Schema;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static uk.co.openkappa.bitrules.MatchingConstraint.newRule;

public class ClassifierTest {

  @Test
  public void testBuildClassifierEmptyRepo() throws IOException {
    Classifier<TestDomainObject, String> engine = buildSimple(ArrayList::new);
    assertFalse(engine.getBestClassification(TestDomainObject.random()).isPresent());
  }

  @Test
  public void testBuildClassifierOneRule() throws IOException {
    Classifier<TestDomainObject, String> engine = buildSimple(
            () -> Collections.singletonList(
                    newRule("rule1")
                    .eq("field1", "foo")
                    .lt("measure1", 0D)
                    .priority(0)
                    .classification("RED")
                    .build()
            )
    );

    assertFalse(engine.getBestClassification(TestDomainObject.random()).isPresent());
    TestDomainObject testTrigger = TestDomainObject.random();
    testTrigger.setField1("foo");
    testTrigger.setMeasure1(-1);
    String triggered = engine.getBestClassification(testTrigger).orElseThrow(AssertionError::new);
    assertEquals(triggered, "RED");
    testTrigger.setMeasure1(1);
    assertFalse(engine.getBestClassification(testTrigger).isPresent());
  }


  @Test
  public void testBuildClassifierTwoDisjointRules() throws IOException {
    Classifier<TestDomainObject, String> engine = buildSimple(
            () -> Arrays.asList(
                    newRule("rule1")
                            .eq("field1", "foo")
                            .lt("measure1", 0D)
                            .priority(0)
                    .classification("RED")
                    .build(),
                    newRule("rule2")
                            .eq("field1", "bar")
                            .lt("measure1", 0D)
                            .priority(0)
                            .classification("BLUE")
                    .build()
            )
    );

    assertFalse(engine.getBestClassification(TestDomainObject.random()).isPresent());
    TestDomainObject testTrigger = TestDomainObject.random();
    testTrigger.setField1("foo");
    testTrigger.setMeasure1(-1);
    List<String> classifications = engine.classify(testTrigger).collect(toList());
    assertEquals(1, classifications.size());
    assertEquals("RED", classifications.get(0));
    testTrigger.setMeasure1(1);
    assertFalse(engine.getBestClassification(testTrigger).isPresent());
  }

  @Test
  public void testBuildClassifierTwoOverlappingRules() throws IOException {
    Classifier<TestDomainObject, String> engine = buildSimple(
            () -> Arrays.asList(
                    newRule("rule1")
                            .eq("field1", "foo")
                            .gt("measure1", 0D)
                            .priority(0)
                    .classification("RED")
                    .build(),
                    newRule("rule2")
                    .eq("field1", "foo")
                    .gt("measure1", 1D)
                    .priority(1)
                    .classification("BLUE")
                            .build()
            )
    );
    assertFalse(engine.getBestClassification(TestDomainObject.random()).isPresent());
    TestDomainObject testTrigger = TestDomainObject.random();
    testTrigger.setField1("foo");
    testTrigger.setMeasure1(-1);
    assertFalse(engine.getBestClassification(testTrigger).isPresent());
    testTrigger.setMeasure1(0.5);
    List<String> classifications = engine.classify(testTrigger).collect(toList());
    assertEquals(1, classifications.size());
    assertEquals("RED", classifications.get(0));
    testTrigger.setMeasure1(1.5);
    classifications = engine.classify(testTrigger).collect(toList());
    assertEquals(2, classifications.size());
    assertEquals("BLUE", classifications.get(0));
    assertEquals("RED", classifications.get(1));
  }


  @Test
  public void testBuildClassifierWithRulesOnDifferentAttributes() throws IOException {
    Classifier<TestDomainObject, String> engine = buildSimple(
            () -> Arrays.asList(
                    newRule("rule1").eq("field1", "foo").priority(0).classification("RED").build(),
                    newRule("rule2").eq("field2", "bar").priority(1).classification("BLUE").build()
            )
    );
    TestDomainObject value = TestDomainObject.random();
    assertFalse(engine.getBestClassification(value).isPresent());
    assertEquals(engine.getBestClassification(value.setField1("foo")).get(), "RED");
    assertEquals(engine.getBestClassification(value.setField2("bar")).get(), "BLUE");
  }

  @Test
  public void testBuildClassifierComparableAttributes() throws IOException {
    Classifier<TestDomainObject, String> engine = buildComparable(
            () -> Arrays.asList(
                    newRule("rule1").eq("field1", "foo").priority(0).classification("RED").build(),
                    newRule("rule2").eq("field2", "bar").priority(1).classification("BLUE").build()
            )
    );
    TestDomainObject value = TestDomainObject.random();
    assertFalse(engine.getBestClassification(value).isPresent());
    assertEquals(engine.getBestClassification(value.setField1("foo")).get(), "RED");
    assertEquals(engine.getBestClassification(value.setField2("bar")).get(), "BLUE");
  }

  @Test
  public void testIntegerRules() throws IOException {
    Classifier<TestDomainObject, String> classifier =
            ImmutableClassifier.<String, TestDomainObject, String>definedBy(Schema.<String, TestDomainObject>newInstance()
                    .withAttribute("measure2", TestDomainObject::getMeasure2)
            ).build(() -> Arrays.asList(
                    newRule("rule1").eq("measure2", 999).priority(0).classification("RED").build(),
                    newRule("rule1").lt("measure2", 1000).priority(1).classification("BLUE").build()
            ));

    TestDomainObject test = TestDomainObject.random();
    assertFalse(classifier.getBestClassification(test.setMeasure2(1000)).isPresent());
    assertEquals("BLUE", classifier.getBestClassification(test.setMeasure2(998)).get());
    assertEquals(2, classifier.classify(test.setMeasure2(999)).collect(toList()).size());
    assertEquals("RED", classifier.classify(test.setMeasure2(999)).collect(toList()).get(1));
  }

  @Test
  public void testLongRules() throws IOException {
    Classifier<TestDomainObject, String> classifier = ImmutableClassifier.<String, TestDomainObject, String>
            definedBy(Schema.<String, TestDomainObject>newInstance()
            .withAttribute("measure3", TestDomainObject::getMeasure3)
    ).build(() -> Arrays.asList(
            newRule("rule1").eq("measure3", 999).priority(0).classification("RED").build(),
            newRule("rule1").lt("measure3", 1000).priority(1).classification("BLUE").build()
    ));

    TestDomainObject test = TestDomainObject.random();
    assertFalse(classifier.getBestClassification(test.setMeasure3(1000)).isPresent());
    assertEquals("BLUE", classifier.getBestClassification(test.setMeasure3(998)).get());
    assertEquals(2, classifier.classify(test.setMeasure3(999)).collect(toList()).size());
    assertEquals("RED", classifier.classify(test.setMeasure3(999)).collect(toList()).get(1));
  }

  @Test
  public void testRangeBasedRules() throws IOException {
    Classifier<TestDomainObject, String> engine = buildWithContinuousAttributes(() -> Arrays.asList(
            newRule("rule1").gt("measure1", 10).priority(1).classification("RED").build(),
            newRule("rule2").lt("measure1", 8).priority(2).classification("BLUE").build(),
            newRule("rule3").eq("measure1", 5).priority(3).classification("YELLOW").build()
    ));
    TestDomainObject value = TestDomainObject.random().setMeasure1(11);
    assertEquals("RED", engine.getBestClassification(value).get());
    assertEquals("YELLOW", engine.getBestClassification(value.setMeasure1(5)).get());
    assertEquals("BLUE", engine.getBestClassification(value.setMeasure1(2.5)).get());
  }

  @Test(expected = AttributeNotRegistered.class)
  public void testBuildRuleClassifierUnregisteredAttribute() throws IOException {
    buildSimple(() -> Collections.singletonList(
            newRule("missing").eq("missing", "missing")
                    .priority(0).classification("MISSING").build()));
  }


  @Test(expected = ClassCastException.class)
  public void testBuildRuleClassifierWithBadTypeConstraint() throws IOException {
    buildSimple(() -> Collections.singletonList(
            newRule("measure1").eq("measure1", "foo")
                    .priority(0).classification("BAD TYPE").build()));
  }

  @Test(expected = IOException.class)
  public void testBuildRuleClassifierFromInvalidYAML() throws IOException {
    ImmutableClassifier.<String, TestDomainObject, String>definedBy(Schema.newInstance())
            .build(new FileRuleSpecifications("invalid.yaml", new YAMLMapper()));
  }

  @Test
  public void testBuildRuleClassifierFromYAML() throws IOException {
    buildSimple(new FileRuleSpecifications("test.yaml", new YAMLMapper()));
  }

  @Test
  public void testBuildSpecFromYAML() throws IOException {
    RuleSpecifications<String, String> specs = new FileRuleSpecifications("test.yaml", new YAMLMapper());
    assertEquals("rule1", specs.specification("rule1").get().getId());
    assertEquals(2, specs.specifications().size());
  }

  @Test
  public void testBuildFromEnumSchema() throws IOException {
    Classifier<TestDomainObject, Duration> classifier =
            ImmutableClassifier.<TestDomainObject.Fields, TestDomainObject, Duration>definedBy(Schema.<TestDomainObject.Fields, TestDomainObject>newInstance(TestDomainObject.Fields.class)
                    .withAttribute(TestDomainObject.Fields.FIELD1, TestDomainObject::getField1)
                    .withAttribute(TestDomainObject.Fields.MEASURE1, TestDomainObject::getMeasure1))
                    .build(() -> Arrays.asList(
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
            classifier.getBestClassification(TestDomainObject.random().setMeasure1(11)).get());
    assertEquals(Duration.ofDays(2),
            classifier.getBestClassification(TestDomainObject.random().setMeasure1(11).setField1("foo")).get());
  }


  private Classifier<TestDomainObject, String> buildSimple(RuleSpecifications repo) throws IOException {
    return ImmutableClassifier.<String, TestDomainObject, String>definedBy(Schema.<String, TestDomainObject>newInstance()
            .withAttribute("field1", TestDomainObject::getField1)
            .withAttribute("field2", TestDomainObject::getField2)
            .withAttribute("measure1", TestDomainObject::getMeasure1)
    ).build(repo);
  }


  private Classifier<TestDomainObject, String> buildComparable(RuleSpecifications repo) throws IOException {
    return ImmutableClassifier.<String, TestDomainObject, String>definedBy(Schema.<String, TestDomainObject>newInstance()
            .withAttribute("field1", TestDomainObject::getField1, Comparator.naturalOrder())
            .withAttribute("field2", TestDomainObject::getField2, Comparator.naturalOrder())
    ).build(repo);
  }

  private Classifier<TestDomainObject, String> buildWithContinuousAttributes(RuleSpecifications repo) throws IOException {
    return ImmutableClassifier.<String, TestDomainObject, String>definedBy(Schema.<String, TestDomainObject>newInstance()
            .withAttribute("measure1", TestDomainObject::getMeasure1)
            .withAttribute("measure2", TestDomainObject::getMeasure2)
            .withAttribute("measure3", TestDomainObject::getMeasure3)
    ).build(repo);
  }

}