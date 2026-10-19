package com.tacz.guns.api.modifier;

public class CacheValue<T> {
   private T value;

   public CacheValue(T value) {
      this.value = value;
   }

   public T getValue() {
      return this.value;
   }

   public void setValue(T value) {
      this.value = value;
   }
}
