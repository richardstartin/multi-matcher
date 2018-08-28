package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.Operation;

import java.util.Iterator;

class Nodes {

  static String toString(int count,
                         Operation op,
                         Iterator<? extends Object> thresholds,
                         Iterator<? extends Object> matches) {
    StringBuilder sb = new StringBuilder().append(count).append(" thresholds): [");
    while (thresholds.hasNext() && matches.hasNext()) {
      sb.append("(_ ").append(op).append(" ").append(thresholds.next()).append(": ").append(matches.next()).append("), ");
    }
    sb.setCharAt(sb.length() - 2, ']');
    return sb.toString();
  }
}
