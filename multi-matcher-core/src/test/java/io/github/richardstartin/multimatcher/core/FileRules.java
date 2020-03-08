package io.github.richardstartin.multimatcher.core;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FileRules implements RuleSet<String, String> {

  private final String filename;
  private final ObjectMapper mapper;

  public FileRules(String filename, ObjectMapper mapper) {
    this.filename = filename;
    this.mapper = mapper;
  }

  @Override
  public List<MatchingConstraint<String, String>> constraints() throws IOException {
    try (InputStream in = ClassLoader.getSystemResourceAsStream(filename);
         MappingIterator<MatchingConstraint<String, String>> it = mapper.readerFor(MatchingConstraint.class).readValues(in)) {
      return it.readAll();
    }
  }
}
