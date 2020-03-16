package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.Mask;
import io.github.richardstartin.multimatcher.core.Matcher;
import io.github.richardstartin.multimatcher.core.masks.MaskStore;

import java.util.function.Function;

import static io.github.richardstartin.multimatcher.core.matchers.SelectivityHeuristics.avgCardinality;

class GenericMatcher<T, U, MaskType extends Mask<MaskType>> implements Matcher<T, MaskType> {

    private final Function<T, U> accessor;
    private final ClassificationNode<U, MaskType>[] nodes;
    private final int wildcard;
    private final MaskStore<MaskType> store;

    GenericMatcher(MaskStore<MaskType> store,
                   Function<T, U> accessor,
                   ClassificationNode<U, MaskType>[] nodes,
                   int wildcard) {
        this.accessor = accessor;
        this.nodes = nodes;
        this.wildcard = wildcard;
        this.store = store;
    }

    @Override
    public void match(T input, MaskType context) {
        U value = accessor.apply(input);
        var temp = store.getTemp(wildcard);
        for (var node : nodes) {
            store.orInto(temp, node.match(value));
        }
        context.inPlaceAnd(temp);
    }

    @Override
    public float averageSelectivity() {
        return avgCardinality(nodes, ClassificationNode::averageSelectivity);
    }

}
