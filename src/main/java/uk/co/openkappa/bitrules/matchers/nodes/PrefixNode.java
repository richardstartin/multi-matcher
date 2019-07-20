package uk.co.openkappa.bitrules.matchers.nodes;

import org.apache.commons.collections4.trie.PatriciaTrie;
import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.matchers.ClassificationNode;
import uk.co.openkappa.bitrules.matchers.MutableNode;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static uk.co.openkappa.bitrules.Mask.with;
import static uk.co.openkappa.bitrules.matchers.SelectivityHeuristics.avgCardinality;

public class PrefixNode<MaskType extends Mask<MaskType>> implements MutableNode<String, MaskType> {

  private final MaskType empty;
  private final Map<String, MaskType> map;
  private int longest;

  public PrefixNode(MaskType empty) {
    this(empty, new HashMap<>(), 0);
  }

  private PrefixNode(MaskType empty, Map<String, MaskType> map, int longest) {
    this.empty = empty;
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
    return empty;
  }

  @Override
  public ClassificationNode<String, MaskType> optimise() {
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
    return new PrefixNode<>(empty, map, longest);
  }

  public void add(String prefix, int id) {
    map.compute(prefix, (p, mask) -> with(null == mask ? empty.clone() : mask, id));
  }

  @Override
  public float averageSelectivity() {
    // probably completely wrong
    return avgCardinality(map.values());
  }
}
