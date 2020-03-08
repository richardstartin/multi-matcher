package io.github.richardstartin.multimatcher.benchmarks;

import io.github.richardstartin.multimatcher.core.Classifier;
import io.github.richardstartin.multimatcher.core.schema.Schema;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import static io.github.richardstartin.multimatcher.benchmarks.FieldsEnum.*;
import static io.github.richardstartin.multimatcher.benchmarks.FieldsEnum.TIMESTAMP;

@State(Scope.Benchmark)
public class StringSchemaMatcherState {

    Schema<String, DomainObject> schema;
    Classifier<DomainObject, String> classifier;
    DomainObject matching;
    DomainObject nonMatching;

    @Setup(Level.Trial)
    public void init() {
        schema = stringSchema();
        classifier = Classifier.<String, DomainObject, String>builder(schema)
                .build(SmallBenchmarkRules.STRING_RULES);
        this.matching = SmallBenchmarkRules.matching();
        this.nonMatching = SmallBenchmarkRules.nonMatching();
    }


    private Schema<String, DomainObject> stringSchema() {
        return Schema.<String, DomainObject>create()
                .withAttribute(AMOUNT.name(), DomainObject::getAmount)
                .withAttribute(CURRENCY.name(), DomainObject::getCurrency)
                .withAttribute(ID.name(), DomainObject::getId)
                .withAttribute(IP_ADDRESS.name(), DomainObject::getIpAddress)
                .withAttribute(RATING.name(), DomainObject::getRating)
                .withAttribute(TIMESTAMP.name(), DomainObject::getTimestamp);
    }
}
