package com.tacz.guns.resource.filter;

import java.util.List;

public interface IFilter<T> {
   boolean test(T var1);

   default List<T> filter(List<T> input, boolean whitelist) {
      input.removeIf(recipeId -> whitelist != this.test((T)recipeId));
      return input;
   }
}
