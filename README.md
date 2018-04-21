# bitrules
[![Build Status](https://travis-ci.org/richardstartin/bitrules.svg?branch=master)](https://travis-ci.org/richardstartin/bitrules)
[![Coverage Status](https://coveralls.io/repos/github/richardstartin/bitrules/badge.svg?branch=master)](https://coveralls.io/github/richardstartin/bitrules?branch=master)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

I have often needed to implement tedious classification logic in data processing projects. The requirements are often ambiguous to the extent that it would be difficult to implement them even in SQL, with aspects such as fallback and overlap. This logic often ends up expressed as large blocks of nested if statements which are hard to read or modify and perform poorly. This small project aims to make such classification logic easier, and improve performance too. 

# usage

Step 1: Build a classification engine
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

Step 2: Classify products

```java
  Product p = getProduct();
  String classification = classifier.getBestClassification(p).orElse("UNCLASSIFIED");
```
