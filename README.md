# multi-matcher
[![Build Status](https://travis-ci.org/richardstartin/multi-matcher.svg?branch=master)](https://travis-ci.org/richardstartin/multi-matcher)
[![Coverage Status](https://coveralls.io/repos/github/richardstartin/bitrules/badge.svg?branch=master)](https://coveralls.io/github/richardstartin/multi-matcher?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/uk.co.openkappa/bitrules/badge.svg)](https://maven-badges.herokuapp.com/maven-central/uk.co.openkappa/bitrules)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Javadoc](https://javadoc-badge.appspot.com/uk.co.openkappa/multi-matcher.svg?label=javadoc)](http://www.javadoc.io/doc/uk.co.openkappa/multi-matcher)
[![Total alerts](https://img.shields.io/lgtm/alerts/g/richardstartin/multi-matcher.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/richardstartin/multi-matcher/alerts/)

I have often needed to implement tedious classification logic in data processing projects. The requirements are often ambiguous to the extent that it would be difficult to implement them even in SQL, with aspects such as fallback and overlap. This logic often ends up expressed as large blocks of nested if statements which are hard to read or modify and perform poorly. This small project aims to make such classification logic easier, and improve performance too. 

# usage

Build a generic classification engine
```java
    Classifier<Product, String> classifier = Classifier.<String, Product, String>builder(
                Schema.<String, Product, String>create()
                        .withAttribute("productType", Product::getProductType)
                        .withAttribute("issueDate", Product::getIssueDate, Comparator.naturalOrder().reversed())
                        .withAttribute("productName", Product::getProductName)
                        .withAttribute("availability", Product::getAvailability)
                        .withAttribute("discountedPrice", value -> 0.2 * value.getPrice())
                ).build(Arrays.asList(
                    MatchingConstraint.<String, String>named("rule1") 
                            .eq("productType", "silk")
                            .startsWith("productName", "luxury")
                            .gt("discountedPrice", 1000)
                            .priority(0)
                            .classification("EXPENSIVE_LUXURY_PRODUCTS")
                            .build(),
                    MatchingConstraint.<String, String>named("rule2")
                            .eq("productType", "caviar")
                            .gt("discountedPrice", 100)
                            .priority(1)
                            .classification("EXPENSIVE_LUXURY_PRODUCTS")
                            .build(),
                    MatchingConstraint.<String, String>anonymous()
                            .eq("productName", "baked beans")
                            .priority(2)
                            .classification("CHEAP_FOOD")
                            .build()
                )
            );
```

Classify

```java
  Product p = getProduct();
  String classification = classifier.classification(p).orElse("UNCLASSIFIED");
```
