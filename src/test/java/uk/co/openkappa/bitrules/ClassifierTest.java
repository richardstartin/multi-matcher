package uk.co.openkappa.bitrules;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.co.openkappa.bitrules.config.RuleAttributeNotRegistered;
import uk.co.openkappa.bitrules.config.Schema;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
                    RuleSpecification.of("rule1",
                            ImmutableMap.of("field1", Constraint.equalTo("foo"),
                                    "measure1", Constraint.lessThan(0D)),
                            0, "RED")
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
                    RuleSpecification.of("rule1",
                            ImmutableMap.of("field1", Constraint.equalTo("foo"),
                                    "measure1", Constraint.lessThan(0D)),
                            0, "RED"),
                    RuleSpecification.of("rule2",
                            ImmutableMap.of("field1", Constraint.equalTo("bar"),
                                    "measure1", Constraint.greaterThan(0D)),
                            0, "BLUE")
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
                    RuleSpecification.of("rule1",
                            ImmutableMap.of("field1", Constraint.equalTo("foo"),
                                    "measure1", Constraint.greaterThan(0D)),
                            0, "RED"),
                    RuleSpecification.of("rule2",
                            ImmutableMap.of("field1", Constraint.equalTo("foo"),
                                    "measure1", Constraint.greaterThan(1D)),
                            1, "BLUE")
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
                    RuleSpecification.of("rule1",
                            ImmutableMap.of("field1", Constraint.equalTo("foo")), (short) 0, "RED"),
                    RuleSpecification.of("rule2",
                            ImmutableMap.of("field2", Constraint.equalTo("bar")), (short) 1, "BLUE")
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
                    RuleSpecification.of("rule1",
                            ImmutableMap.of("field1", Constraint.equalTo("foo")), (short) 0, "RED"),
                    RuleSpecification.of("rule2",
                            ImmutableMap.of("field2", Constraint.equalTo("bar")), (short) 1, "BLUE")
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
            ImmutableClassifier.<String, TestDomainObject, String>forSchema(Schema.<String, TestDomainObject>newInstance()
                    .withAttribute("measure2", TestDomainObject::getMeasure2)
            ).build(() -> Arrays.asList(
                    RuleSpecification.of("rule1",
                            ImmutableMap.of("measure2", Constraint.equalTo(999)),
                            0, "RED"),
                    RuleSpecification.of("rule1",
                            ImmutableMap.of("measure2", Constraint.lessThan(1000)),
                            1, "BLUE")
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
            forSchema(Schema.<String, TestDomainObject>newInstance()
            .withAttribute("measure3", TestDomainObject::getMeasure3)
    ).build(() -> Arrays.asList(
            RuleSpecification.of("rule1",
                    ImmutableMap.of("measure3", Constraint.equalTo(999)),
                    0, "RED"),
            RuleSpecification.of("rule1",
                    ImmutableMap.of("measure3", Constraint.lessThan(1000)),
                    1, "BLUE")
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
            RuleSpecification.of("rule1",
                    ImmutableMap.of("measure1", Constraint.greaterThan(10)), 1, "RED"),
            RuleSpecification.of("rule2",
                    ImmutableMap.of("measure1", Constraint.lessThan(8)), 2, "BLUE"),
            RuleSpecification.of("rule3",
                    ImmutableMap.of("measure1", Constraint.equalTo(5)), 3, "YELLOW")
    ));
    TestDomainObject value = TestDomainObject.random().setMeasure1(11);
    assertEquals("RED", engine.getBestClassification(value).get());
    assertEquals("YELLOW", engine.getBestClassification(value.setMeasure1(5)).get());
    assertEquals("BLUE", engine.getBestClassification(value.setMeasure1(2.5)).get());
  }

  @Test(expected = RuleAttributeNotRegistered.class)
  public void testBuildRuleClassifierUnregisteredAttribute() throws IOException {
    buildSimple(() -> Collections.singletonList(
            RuleSpecification.of("missing", ImmutableMap.of("missing", Constraint.equalTo("missing")),
                    0, "MISSING")));
  }


  @Test(expected = ClassCastException.class)
  public void testBuildRuleClassifierWithBadTypeConstraint() throws IOException {
    buildSimple(() -> Collections.singletonList(
            RuleSpecification.of("measure1", ImmutableMap.of("measure1", Constraint.equalTo("foo")),
                    0, "BAD TYPE")));
  }

  @Test(expected = IOException.class)
  public void testBuildRuleClassifierFromInvalidYAML() throws IOException {
    ImmutableClassifier.<String, TestDomainObject, String>forSchema(Schema.newInstance())
            .build(new FileRuleSpecifications("invalid.yaml", new YAMLMapper()));
  }

  @Test
  public void testBuildRuleClassifierFromYAML() throws IOException {
    buildSimple(new FileRuleSpecifications("test.yaml", new YAMLMapper()));
  }

  @Test
  public void testBuildSpecFromYAML() throws IOException {
    RuleSpecifications<String, String> specs = new FileRuleSpecifications("test.yaml", new YAMLMapper());
    assertEquals("rule1", specs.get("rule1").get().getId());
    assertEquals(2, specs.get().size());
  }

  @Test
  public void testBuildFromEnumSchema() throws IOException {
    Classifier<TestDomainObject, Duration> classifier =
            ImmutableClassifier.<TestDomainObject.Fields, TestDomainObject, Duration>forSchema(Schema.<TestDomainObject.Fields, TestDomainObject>newInstance(TestDomainObject.Fields.class)
                    .withAttribute(TestDomainObject.Fields.FIELD1, TestDomainObject::getField1)
                    .withAttribute(TestDomainObject.Fields.MEASURE1, TestDomainObject::getMeasure1))
                    .build(() -> Arrays.asList(
                            new RuleSpecification<>("rule1",
                                    ImmutableMap.of(TestDomainObject.Fields.MEASURE1, Constraint.greaterThan(10)),
                                    0, Duration.ofDays(1)
                            ),
                            new RuleSpecification<>("rule2",
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
    return ImmutableClassifier.<String, TestDomainObject, String>forSchema(Schema.<String, TestDomainObject>newInstance()
            .withAttribute("field1", TestDomainObject::getField1)
            .withAttribute("field2", TestDomainObject::getField2)
            .withAttribute("measure1", TestDomainObject::getMeasure1)
    ).build(repo);
  }


  private Classifier<TestDomainObject, String> buildComparable(RuleSpecifications repo) throws IOException {
    return ImmutableClassifier.<String, TestDomainObject, String>forSchema(Schema.<String, TestDomainObject>newInstance()
            .withAttribute("field1", TestDomainObject::getField1, Comparator.naturalOrder())
            .withAttribute("field2", TestDomainObject::getField2, Comparator.naturalOrder())
    ).build(repo);
  }

  private Classifier<TestDomainObject, String> buildWithContinuousAttributes(RuleSpecifications repo) throws IOException {
    return ImmutableClassifier.<String, TestDomainObject, String>forSchema(Schema.<String, TestDomainObject>newInstance()
            .withAttribute("measure1", TestDomainObject::getMeasure1)
            .withAttribute("measure2", TestDomainObject::getMeasure2)
            .withAttribute("measure3", TestDomainObject::getMeasure3)
    ).build(repo);
  }

}