# bitrules
[![Build Status](https://travis-ci.org/richardstartin/bitrules.svg?branch=master)](https://travis-ci.org/richardstartin/bitrules)
[![Coverage Status](https://coveralls.io/repos/github/richardstartin/bitrules/badge.svg?branch=master)](https://coveralls.io/github/richardstartin/bitrules?branch=master)

Build a classification engine
```java
    Classifier<Product> classifier = ImmutableClassifier.<Product>builder()
                .withConfig(ClassifierConfig.<Product>newInstance()
                        .withStringAttribute("productType", Product::getProductType)
                        .withDoubleAttribute("productName", Product::getProductName)
                        .withDoubleAttribute("availability", Product::getAvailability)
                        .withContextualDoubleAttribute("discountedPrice", ctx ->
                                value -> (double)ctx.get("discountFactor") * value.getPrice())
                ).build(() -> Arrays.asList(
                    RuleSpecification.of("rule1", "silk is an expensive luxury product",
                            ImmutableMap.of("productType", Constraint.equalTo("silk"),
                                            "discountedPrice", Constraint.greaterThan(1000)),
                            (short) 0, "EXPENSIVE_LUXURY_PRODUCTS"),
                    RuleSpecification.of("rule2", "caviar is an expensive luxury product",
                            ImmutableMap.of("productType", Constraint.equalTo("caviar"),
                                            "discountedPrice", Constraint.greaterThan(100)),
                            (short) 1, "EXPENSIVE_LUXURY_PRODUCTS"),
                    RuleSpecification.of("rule2", "Baked beans are cheap food",
                            ImmutableMap.of("productName", Constraint.equalTo("baked beans"))
                            (short) 0, "CHEAP_FOOD")
                )
            );
```

Classify products

```java
  Product p = getProduct();
  String classification = classifier.getBestClassification(p).orElse("UNCLASSIFIED");
```
