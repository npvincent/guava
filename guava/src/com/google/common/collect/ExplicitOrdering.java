/*
 * Copyright (C) 2007 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/** An ordering that compares objects according to a given order. */
@GwtCompatible(serializable = true)
final class ExplicitOrdering<T> extends Ordering<T> implements Serializable {
  final ImmutableMap<T, Integer> rankMap;

  ExplicitOrdering(List<T> valuesInOrder) {
    this(buildRankMap(valuesInOrder));
  }

  ExplicitOrdering(ImmutableMap<T, Integer> rankMap) {
    this.rankMap = rankMap;
  }

  @Override public int compare(T left, T right) {
    return rank(left) - rank(right); // safe because both are nonnegative
  }

  private int rank(T value) {
    Integer rank = rankMap.get(value);
    if (rank == null) {
      throw new IncomparableValueException(value);
    }
    return rank;
  }

  private static <T> ImmutableMap<T, Integer> buildRankMap(
      List<T> valuesInOrder) {
    ImmutableMap.Builder<T, Integer> builder = ImmutableMap.builder();
    int rank = 0;
    for (T value : valuesInOrder) {
      builder.put(value, rank++);
    }
    return builder.build();
  }

    public Ordering<T> unknownsFirst()
    {
        return new UnrankedValueOrdering<T>(this, UnrankedValueOrdering.Position.FIRST);
    }

    public Ordering<T> unknownsLast()
    {
        return new UnrankedValueOrdering<T>(this, UnrankedValueOrdering.Position.LAST);
    }

  @Override public boolean equals(@Nullable Object object) {
    if (object instanceof ExplicitOrdering) {
      ExplicitOrdering<?> that = (ExplicitOrdering<?>) object;
      return this.rankMap.equals(that.rankMap);
    }
    return false;
  }

  @Override public int hashCode() {
    return rankMap.hashCode();
  }

  @Override public String toString() {
    return "Ordering.explicit(" + rankMap.keySet() + ")";
  }

  private static final long serialVersionUID = 0;

  private static class UnrankedValueOrdering<T> extends Ordering<T> implements Serializable
  {
      final ExplicitOrdering<? super T> ordering;
      final Position position;
      public static enum Position { FIRST, LAST }

      UnrankedValueOrdering(ExplicitOrdering<? super T> ordering, Position position) {
          this.ordering = ordering;
          this.position = position;
      }

      @Override
      public int compare(T left, T right) {
          boolean hasLeft = ordering.rankMap.containsKey(left);
          boolean hasRight = ordering.rankMap.containsKey(right);
          if( hasLeft ^ hasRight ) {
              if(position==Position.FIRST) {
                return hasLeft ? LEFT_IS_GREATER : RIGHT_IS_GREATER;
              } else {
                  return hasLeft ? RIGHT_IS_GREATER : LEFT_IS_GREATER;
              }
          } else if (!hasLeft) {
              return 0;
          }
          return ordering.compare(left,right);
      }

      @Override
      public String toString() {
          return ordering + ( (position==Position.FIRST) ? ".unknownsFirst()" : ".unknownsLast()");
      }

      private static final long serialVersionUID = 0;
  }

}
