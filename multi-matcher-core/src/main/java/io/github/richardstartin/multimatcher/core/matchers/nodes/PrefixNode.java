package io.github.richardstartin.multimatcher.core.matchers.nodes;

import io.github.richardstartin.multimatcher.core.Mask;
import io.github.richardstartin.multimatcher.core.masks.MaskFactory;
import io.github.richardstartin.multimatcher.core.matchers.ClassificationNode;
import io.github.richardstartin.multimatcher.core.matchers.MutableNode;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static io.github.richardstartin.multimatcher.core.Mask.with;
import static io.github.richardstartin.multimatcher.core.matchers.SelectivityHeuristics.avgCardinality;

public class PrefixNode<MaskType extends Mask<MaskType>> implements MutableNode<String, MaskType>,
        ClassificationNode<String, MaskType> {

  private final MaskFactory<MaskType> factory;
  private final Map<String, MaskType> map;
  private int longest;

  public PrefixNode(MaskFactory<MaskType> factory) {
    this(factory, new HashMap<>(), 0);
  }

  private PrefixNode(MaskFactory<MaskType> factory, Map<String, MaskType> map, int longest) {
    this.factory = factory;
    this.map = map;
    this.longest = longest;
  }

  @Override
  public MaskType match(String value) {
    int position = longest;
    while (position > 0) {
      MaskType match = map.get(value.substring(0, position));
      if (null != match) {
        return match;
      }
      --position;
    }
    return factory.emptySingleton();
  }

  @Override
  public ClassificationNode<String, MaskType> freeze() {
    this.longest = map.keySet().stream().mapToInt(String::length).max().orElse(0);
    PatriciaTrie<MaskType> trie = new PatriciaTrie<>();
    trie.putAll(map);
    map.keySet().stream().sorted(Comparator.comparingInt(String::length))
       .forEach(key -> {
         MaskType mask = map.get(key);
         trie.prefixMap(key)
             .forEach((k, v) -> {
               if (!key.equals(k)) {
                 v.inPlaceOr(mask);
               }
             });
       });
    return new PrefixNode<>(factory, map, longest);
  }

  public void add(String prefix, int id) {
    map.compute(prefix, (p, mask) -> with(null == mask ? factory.newMask() : mask, id));
  }

  @Override
  public float averageSelectivity() {
    // probably completely wrong
    return avgCardinality(map.values());
  }
}
