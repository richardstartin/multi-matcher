package uk.co.openkappa.bitrules;


import java.util.Optional;
import java.util.stream.Stream;

public interface Classifier<T> {

  Stream<String> classify(T value);

  Optional<String> getBestClassification(T value);

}
