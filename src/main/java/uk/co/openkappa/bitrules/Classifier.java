package uk.co.openkappa.bitrules;


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

}
