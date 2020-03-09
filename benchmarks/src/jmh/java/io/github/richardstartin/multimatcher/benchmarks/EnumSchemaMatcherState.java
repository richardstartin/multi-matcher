package io.github.richardstartin.multimatcher.benchmarks;

import io.github.richardstartin.multimatcher.core.Classifier;
import io.github.richardstartin.multimatcher.core.schema.Schema;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import static io.github.richardstartin.multimatcher.benchmarks.FieldsEnum.*;

@State(Scope.Thread)
public class EnumSchemaMatcherState {


    Schema<FieldsEnum, DomainObject> schema;
    Classifier<DomainObject, String> classifier;
    DomainObject matching;
    DomainObject nonMatching;

    @Setup(Level.Trial)
    public void init() {
        schema = enumSchema();
        classifier = Classifier.<FieldsEnum, DomainObject, String>builder(schema)
                .build(SmallBenchmarkRules.ENUM_RULES);
        this.matching = SmallBenchmarkRules.matching();
        this.nonMatching = SmallBenchmarkRules.nonMatching();
    }

    private Schema<FieldsEnum, DomainObject> enumSchema() {
        return Schema.<FieldsEnum, DomainObject>create()
                .withAttribute(AMOUNT, DomainObject::getAmount)
                .withStringAttribute(CURRENCY, DomainObject::getCurrency)
                .withAttribute(ID, DomainObject::getId)
                .withAttribute(IP_ADDRESS, DomainObject::getIpAddress)
                .withAttribute(RATING, DomainObject::getRating)
                .withAttribute(TIMESTAMP, DomainObject::getTimestamp);
    }
}
