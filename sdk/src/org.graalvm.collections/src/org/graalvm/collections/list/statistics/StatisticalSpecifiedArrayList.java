package org.graalvm.collections.list.statistics;

import org.graalvm.collections.list.SpecifiedArrayList;

/**
 * This is an enhancement of the SpecifiedArrayList used to gather information during runtime. It
 * uses a StatisticTracker object to store the generated Information. This object can be used to
 * extract useful information about the SpecifiedArrayList like
 * <li>Size</li>
 * <li>Type Distribution</li>
 * <li>Load Factor</li>
 * <li>Distribution of Operators</li>
 *
 *
 * TODO Maybe implement function to generate code for programming language R
 */

public interface StatisticalSpecifiedArrayList<E> extends SpecifiedArrayList<E> {

}
