package com.openkappa.bitrules;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.collect.ImmutableMap;
import com.openkappa.bitrules.config.RuleAttributeNotRegistered;
import com.openkappa.bitrules.config.ClassifierConfig;
import com.openkappa.bitrules.config.RuleValueTypeMismatch;
import com.openkappa.bitrules.source.FileRuleSpecifications;
import com.openkappa.bitrules.source.RuleSpecifications;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ClassifierTest {


  @Test
  public void testBuildClassifierEmptyRepo() throws IOException {
    Classifier<TestDomainObject> engine = buildSimple(ArrayList::new);
    assertFalse(engine.getBestClassification(TestDomainObject.random()).isPresent());
  }

  @Test
  public void testBuildClassifierOneRule() throws IOException {
    Classifier<TestDomainObject> engine = buildSimple(
            () -> Collections.singletonList(
                    RuleSpecification.of("rule1", "rule 1",
                            ImmutableMap.of("field1", Constraint.equalTo("foo"),
                                    "measure1", Constraint.lessThan(0D)),
                            (short) 0, "RED")
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
    Classifier<TestDomainObject> engine = buildSimple(
            () -> Arrays.asList(
                    RuleSpecification.of("rule1", "rule 1",
                            ImmutableMap.of("field1", Constraint.equalTo("foo"),
                                    "measure1", Constraint.lessThan(0D)),
                            (short) 0, "RED"),
                    RuleSpecification.of("rule2", "rule 2",
                            ImmutableMap.of("field1", Constraint.equalTo("bar"),
                                    "measure1", Constraint.greaterThan(0D)),
                            (short) 0, "BLUE")
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
    Classifier<TestDomainObject> engine = buildSimple(
            () -> Arrays.asList(
                    RuleSpecification.of("rule1", "rule 1",
                            ImmutableMap.of("field1", Constraint.equalTo("foo"),
                                    "measure1", Constraint.greaterThan(0D)),
                            (short) 0, "RED"),
                    RuleSpecification.of("rule2", "rule 2",
                            ImmutableMap.of("field1", Constraint.equalTo("foo"),
                                    "measure1", Constraint.greaterThan(1D)),
                            (short) 1, "BLUE")
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
    Classifier<TestDomainObject> engine = buildSimple(
            () -> Arrays.asList(
                    RuleSpecification.of("rule1", "rule 1",
                            ImmutableMap.of("field1", Constraint.equalTo("foo")), (short)0, "RED"),
                    RuleSpecification.of("rule2", "rule 2",
                            ImmutableMap.of("field2", Constraint.equalTo("bar")), (short)1, "BLUE")
            )
    );
    TestDomainObject value = TestDomainObject.random();
    assertFalse(engine.getBestClassification(value).isPresent());
    assertEquals(engine.getBestClassification(value.setField1("foo")).get(), "RED");
    assertEquals(engine.getBestClassification(value.setField2("bar")).get(), "BLUE");
  }

  @Test
  public void testBuildClassifierWithScoringFunction() throws IOException {
    Classifier<TestDomainObject> engine = buildWithContext(
            () -> Arrays.asList(
                    RuleSpecification.of("rule1", "rule 1",
                            ImmutableMap.of("field1", Constraint.equalTo("foo")), (short)0, "RED"),
                    RuleSpecification.of("rule2", "rule 2",
                            ImmutableMap.of("field2", Constraint.equalTo("bar")), (short)1, "BLUE"),
                    RuleSpecification.of("rule3", "rule 3",
                            ImmutableMap.of("computed",
                                    Constraint.greaterThan(ImmutableMap.of("factor", -1D), 2)),
                            (short)2, "YELLOW"),
                    RuleSpecification.of("rule3", "rule 3",
                            ImmutableMap.of("measure1",
                                    Constraint.greaterThan(2)),
                            (short)1, "ORANGE")
            )
    );
    TestDomainObject value = TestDomainObject.random();
    assertFalse(engine.getBestClassification(value).isPresent());
    assertEquals(engine.getBestClassification(value.setField1("foo")).get(), "RED");
    assertEquals(engine.getBestClassification(value.setField2("bar")).get(), "BLUE");
    assertEquals(engine.getBestClassification(value.setMeasure1(-3)).get(), "YELLOW");
    assertEquals(engine.getBestClassification(value.setMeasure1(3)).get(), "ORANGE");
  }

  @Test
  public void testIntegerRules() throws IOException {
    Classifier<TestDomainObject> classifier = ImmutableClassifier.<TestDomainObject>builder()
            .withConfig(ClassifierConfig.<TestDomainObject>newInstance()
                  .withIntAttribute("measure2", TestDomainObject::getMeasure2)
            ).build(() -> Arrays.asList(
                    RuleSpecification.of("rule1", "rule1",
                            ImmutableMap.of("measure2", Constraint.equalTo(999)),
                            (short)0, "RED"),
                    RuleSpecification.of("rule1", "rule1",
                            ImmutableMap.of("measure2", Constraint.lessThan(1000)),
                            (short)1, "BLUE")
            ));

    TestDomainObject test = TestDomainObject.random();
    assertFalse(classifier.getBestClassification(test.setMeasure2(1000)).isPresent());
    assertEquals("BLUE", classifier.getBestClassification(test.setMeasure2(998)).get());
    assertEquals(2, classifier.classify(test.setMeasure2(999)).collect(toList()).size());
    assertEquals("RED", classifier.classify(test.setMeasure2(999)).collect(toList()).get(1));
  }

  @Test
  public void testLongRules() throws IOException {
    Classifier<TestDomainObject> classifier = ImmutableClassifier.<TestDomainObject>builder()
            .withConfig(ClassifierConfig.<TestDomainObject>newInstance()
                    .withLongAttribute("measure3", TestDomainObject::getMeasure3)
            ).build(() -> Arrays.asList(
                    RuleSpecification.of("rule1", "rule1",
                            ImmutableMap.of("measure3", Constraint.equalTo(999)),
                            (short)0, "RED"),
                    RuleSpecification.of("rule1", "rule1",
                            ImmutableMap.of("measure3", Constraint.lessThan(1000)),
                            (short)1, "BLUE")
            ));

    TestDomainObject test = TestDomainObject.random();
    assertFalse(classifier.getBestClassification(test.setMeasure3(1000)).isPresent());
    assertEquals("BLUE", classifier.getBestClassification(test.setMeasure3(998)).get());
    assertEquals(2, classifier.classify(test.setMeasure3(999)).collect(toList()).size());
    assertEquals("RED", classifier.classify(test.setMeasure3(999)).collect(toList()).get(1));
  }

  @Test
  public void testRangeBasedRules() throws IOException {
    Classifier<TestDomainObject> engine = buildWithContinuousAttributes(() -> Arrays.asList(
        RuleSpecification.of("rule1", "rule 1",
                ImmutableMap.of("measure1", Constraint.greaterThan(10)), (short)1, "RED"),
            RuleSpecification.of("rule2", "rule 2",
                    ImmutableMap.of("computed", Constraint.lessThan(ImmutableMap.of("factor", 2D), 10)),
                    (short)2, "BLUE"),
            RuleSpecification.of("rule3", "rule 3",
                    ImmutableMap.of("measure1", Constraint.equalTo(5)), (short)3, "YELLOW")
    ));
    TestDomainObject value = TestDomainObject.random().setMeasure1(11);
    assertEquals("RED", engine.getBestClassification(value).get());
    assertEquals("YELLOW", engine.getBestClassification(value.setMeasure1(5)).get());
    assertEquals("BLUE", engine.getBestClassification(value.setMeasure1(2.5)).get());
  }

  @Test
  public void testBuildRuleClassifierWithDynamicRule() throws IOException {
    Classifier<TestDomainObject> engine = buildWithDataContext(
            () -> Arrays.asList(
                    RuleSpecification.of("rule1", "rule 1",
                            ImmutableMap.of("measure1", Constraint.greaterThan(200)), (short)0, "RED"),
                    RuleSpecification.of("rule1", "rule 1",
                            ImmutableMap.of("dynamic", Constraint.greaterThan(2)), (short)0, "BLUE")
                    ), tdo -> Optional.of(2D));
    TestDomainObject value = TestDomainObject.random();
    assertFalse(engine.getBestClassification(value).isPresent());
    assertEquals(engine.getBestClassification(value.setMeasure1(201)).get(), "BLUE");
  }

  @Test(expected = RuleAttributeNotRegistered.class)
  public void testBuildRuleClassifierUnregisteredAttribute() throws IOException {
    buildSimple(() -> Collections.singletonList(
            RuleSpecification.of("missing", "missing", ImmutableMap.of("missing", Constraint.equalTo("missing")),
                    (short)0, "MISSING")));
  }


  @Test(expected = RuleValueTypeMismatch.class)
  public void testBuildRuleClassifierWithBadTypeConstraint() throws IOException {
    buildSimple(() -> Collections.singletonList(
            RuleSpecification.of("bad type", "bad type", ImmutableMap.of("field1", Constraint.lessThan(0D)),
                    (short)0, "BAD TYPE")));
  }

  @Test(expected = IOException.class)
  public void testBuildRuleClassifierFromInvalidYAML() throws IOException {
    ImmutableClassifier.<TestDomainObject>builder()
            .withConfig(ClassifierConfig.newInstance())
            .build(new FileRuleSpecifications("invalid.yaml", new YAMLMapper()));
  }

  @Test
  public void testBuildRuleClassifierFromYAML() throws IOException {
    buildSimple(new FileRuleSpecifications("test.yaml", new YAMLMapper()));
  }

  @Test
  public void testBuildSpecFromYAML() throws IOException {
    RuleSpecifications specs = new FileRuleSpecifications("test.yaml", new YAMLMapper());
    assertEquals("rule1", specs.get("rule1").get().getId());
    assertEquals(2, specs.get().size());
  }


  private Classifier<TestDomainObject> buildSimple(RuleSpecifications repo) throws IOException {
    return ImmutableClassifier.<TestDomainObject>builder()
            .withConfig(ClassifierConfig.<TestDomainObject>newInstance()
                    .withStringAttribute("field1", TestDomainObject::getField1)
                    .withStringAttribute("field2", TestDomainObject::getField2)
                    .withDoubleAttribute("measure1", TestDomainObject::getMeasure1)
            ).build(repo);
  }


  private Classifier<TestDomainObject> buildWithContext(RuleSpecifications repo) throws IOException {
    return ImmutableClassifier.<TestDomainObject>builder()
            .withConfig(ClassifierConfig.<TestDomainObject>newInstance()
                    .withStringAttribute("field1", TestDomainObject::getField1)
                    .withStringAttribute("field2", TestDomainObject::getField2)
                    .withDoubleAttribute("measure1", TestDomainObject::getMeasure1)
                    .withContextualDoubleAttribute("computed", ctx ->
                            value -> (double)ctx.get("factor") * value.getMeasure1())
            ).build(repo);
  }


  private Classifier<TestDomainObject> buildWithDataContext(RuleSpecifications repo,
                                                            Function<TestDomainObject, Optional<Double>> dataContextProvider) throws IOException {
    return ImmutableClassifier.<TestDomainObject>builder()
            .withConfig(ClassifierConfig.<TestDomainObject>newInstance()
                    .withStringAttribute("field1", TestDomainObject::getField1)
                    .withStringAttribute("field2", TestDomainObject::getField2)
                    .withDoubleAttribute("measure1", TestDomainObject::getMeasure1)
                    .withContextualDoubleAttribute("computed", ctx ->
                            value -> (double)ctx.get("factor") * value.getMeasure1())
                    .withDynamicAttribute("dynamic", dataContextProvider, (rc, v) -> new BiPredicateWithPriority<TestDomainObject, Optional<Double>>() {
                      @Override
                      public short priority() {
                        return 1;
                      }

                      @Override
                      public boolean test(TestDomainObject testDomainObject, Optional<Double> context) {
                        return context.map(val -> val < testDomainObject.getMeasure1())
                                .orElse(false);
                      }
                    })
            ).build(repo);
  }

  private Classifier<TestDomainObject> buildWithContinuousAttributes(RuleSpecifications repo) throws IOException {
    return ImmutableClassifier.<TestDomainObject>builder()
            .withConfig(ClassifierConfig.<TestDomainObject>newInstance()
                    .withDoubleAttribute("measure1", TestDomainObject::getMeasure1)
                    .withDoubleAttribute("measure2", TestDomainObject::getMeasure2)
                    .withDoubleAttribute("measure3", TestDomainObject::getMeasure3)
                    .withContextualDoubleAttribute("computed", ctx ->
                            value -> (double)ctx.get("factor") * value.getMeasure1())
            ).build(repo);
  }

}