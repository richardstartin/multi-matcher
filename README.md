# bitrules
[![Build Status](https://travis-ci.org/richardstartin/bitrules.svg?branch=master)](https://travis-ci.org/richardstartin/bitrules)
[![Coverage Status](https://coveralls.io/repos/github/richardstartin/bitrules/badge.svg?branch=master)](https://coveralls.io/github/richardstartin/bitrules?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/uk.co.openkappa/bitrules/badge.svg)](https://maven-badges.herokuapp.com/maven-central/uk.co.openkappa/bitrules)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Javadoc](https://javadoc-badge.appspot.com/uk.co.openkappa/bitrules.svg?label=javadoc)](http://www.javadoc.io/doc/uk.co.openkappa/bitrules)

I have often needed to implement tedious classification logic in data processing projects. The requirements are often ambiguous to the extent that it would be difficult to implement them even in SQL, with aspects such as fallback and overlap. This logic often ends up expressed as large blocks of nested if statements which are hard to read or modify and perform poorly. This small project aims to make such classification logic easier, and improve performance too. 

# usage

Step 1: Build a classification engine
```java
    Classifier<Product, String> classifier = ImmutableClassifier.<String, Product, String>definedBy(
                Schema.<String, Product, String>newInstance()
                        .withAttribute("productType", Product::getProductType)
                        .withAttribute("issueDate", Product::getIssueDate, Comparator.naturalOrder().reversed())
                        .withAttribute("productName", Product::getProductName)
                        .withAttribute("availability", Product::getAvailability)
                        .withAttribute("discountedPrice", value -> 0.2 * value.getPrice())
                ).build(() -> Arrays.asList(
                    RuleSpecification.of("rule1", 
                            ImmutableMap.of("productType", Constraint.equalTo("silk"),
                                            "discountedPrice", Constraint.greaterThan(1000)),
                            0, "EXPENSIVE_LUXURY_PRODUCTS"),
                    RuleSpecification.of("rule2", 
                            ImmutableMap.of("productType", Constraint.equalTo("caviar"),
                                            "discountedPrice", Constraint.greaterThan(100)),
                            1, "EXPENSIVE_LUXURY_PRODUCTS"),
                    RuleSpecification.of("rule2", 
                            ImmutableMap.of("productName", Constraint.equalTo("baked beans"))
                            2, "CHEAP_FOOD")
                )
            );
```

Step 2: Classify products

```java
  Product p = getProduct();
  String classification = classifier.getBestClassification(p).orElse("UNCLASSIFIED");
```
