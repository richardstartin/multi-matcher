package uk.co.openkappa.bitrules;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FileRuleSpecifications implements RuleSpecifications<String, String> {

  private final String filename;
  private final ObjectMapper mapper;

  public FileRuleSpecifications(String filename, ObjectMapper mapper) {
    this.filename = filename;
    this.mapper = mapper;
  }

  @Override
  public List<RuleSpecification<String, String>> get() throws IOException {
    try (InputStream in = ClassLoader.getSystemResourceAsStream(filename);
         MappingIterator<RuleSpecification<String, String>> it = mapper.readerFor(RuleSpecification.class).readValues(in)) {
      return it.readAll();
    }
  }
}
