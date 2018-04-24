package com.openkappa.bitrules.nodes;

import com.openkappa.bitrules.Constraint;
import com.openkappa.bitrules.Context;
import com.openkappa.bitrules.DoubleRelation;
import com.openkappa.bitrules.Rule;
import com.openkappa.bitrules.config.FunctionProvider;
import org.roaringbitmap.Container;
import org.roaringbitmap.RunContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class NamedFunctionRule<T> implements Rule<T> {

  private final FunctionProvider<T> factory;
  private final List<NamedFunctionNode<T>> nodes = new ArrayList<>();
  private Container wildcards = RunContainer.full();

  public NamedFunctionRule(FunctionProvider<T> factory) {
    this.factory = factory;
  }

  @Override
  public Container match(T value, Container context) {
    if (!wildcards.contains(context)) {
      Container scope = context.andNot(wildcards);
      for (NamedFunctionNode<T> node : nodes) {
        context = node.apply(value, scope, context);
      }
    }
    return context;
  }

  @Override
  public void addConstraint(Constraint constraint, short priority) {
    wildcards = wildcards.flip(priority);
    DoubleRelation relation = DoubleRelation.from(constraint.getOperation());
    Map<String, Object> ctx = constraint.getContext();
    Context context = new Context(null == ctx ? new TreeMap<>() : ctx);
    nodes.add(new NamedFunctionNode<>(factory.apply(context), priority, relation,
            mustGetDoubleValue(constraint)));
  }
}
