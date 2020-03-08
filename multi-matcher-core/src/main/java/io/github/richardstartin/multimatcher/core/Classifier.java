package io.github.richardstartin.multimatcher.core;


import io.github.richardstartin.multimatcher.core.schema.Schema;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Classifies objects according to constraints applied to
 * registered attributes.
 * @param <T> the classification input type
 * @param <C> the classification result type
 */
public interface Classifier<T, C> {

  /**
   * Gets all the classification satisfied by the input value.
   * @param value the value to classifications
   * @return all matching classifications
   */
  Stream<C> classifications(T value);

  /**
   * Gets the highest priority classification, or none if no constraints are satisfied.
   * @param value the value to classifications.
   * @return the best classification, or empty if no constraints are satisfied
   */
  Optional<C> classification(T value);


  /**
   * Gets a new builder for a classifier
   *
   * @param <Key>            the create key type
   * @param <Input>          the type named the classified objects
   * @param <Classification> the classification type
   * @param schema the schema
   * @return a new classifier builder
   */
  static <Key, Input, Classification>
  MaskedClassifier.ClassifierBuilder<Key, Input, Classification> builder(Schema<Key, Input> schema) {
    return new MaskedClassifier.ClassifierBuilder<>(schema);
  }

}
