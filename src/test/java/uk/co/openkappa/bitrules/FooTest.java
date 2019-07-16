package uk.co.openkappa.bitrules;

import org.junit.jupiter.api.Test;
import uk.co.openkappa.bitrules.schema.Schema;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FooTest {

  static class Foo {
    final String productType;
    final int quantity;
    final int price;

    Foo(String productType, int quantity, int price) {
      this.productType = productType;
      this.quantity = quantity;
      this.price = price;
    }

    public String getProductType() {
      return productType;
    }

    public int getQuantity() {
      return quantity;
    }

    public int getPrice() {
      return price;
    }
  }

  @Test
  void test() {
    // declare a schema, associating attribute accessors with some kind of key (a string here)
    Schema<String, Foo> schema = Schema.<String, Foo>create()
            .withAttribute("productType", Foo::getProductType)
            .withAttribute("qty", Foo::getQuantity)
            .withAttribute("price", Foo::getPrice);
    // build the classifier from the rules and the schema
    ImmutableClassifier<Foo, String> classifier = ImmutableClassifier.<String, Foo, String>builder(schema).build(
            Arrays.asList(
              MatchingConstraint.<String, String>anonymous()
                      .eq("productType", "electronics")
                      .gt("qty", 10)
                      .lt("price", 200)
                      .classification("class1")
                      .build(),
                  MatchingConstraint.<String, String>anonymous()
                          .eq("productType", "electronics")
                          .lt("price", 300)
                          .classification("class2")
                          .build(),
                    MatchingConstraint.<String, String>anonymous()
                            .eq("productType", "books")
                            .eq("qty", 1)
                            .classification("class3")
                            .build()
            )
    );

    assertEquals("class2",
            classifier.classification(new Foo("electronics", 2, 199)).orElse("none"));
  }
}
