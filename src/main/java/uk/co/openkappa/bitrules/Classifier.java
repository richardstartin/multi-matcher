package uk.co.openkappa.bitrules;


import java.util.Optional;
import java.util.stream.Stream;

/**
 * Classifies objects according to constraints applied to
 * registered attributes.
 * @param <T>
 */
public interface Classifier<T> {

  /**
   * Gets all the classification satisfied by the input value.
   * @param value the value to classify
   * @return all matching claddifications
   */
  Stream<String> classify(T value);

  /**
   * Gets the highest priority classification, or none if no constraints are satisfied.
   * @param value the value to classify.
   * @return the best classification, or empty if no constraints are satisfied
   */
  Optional<String> getBestClassification(T value);

}
