package com.tacz.guns.api.modifier;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;

public abstract class JsonProperty<T> {
   protected List<Component> components = Lists.newArrayList();
   @Nullable
   private T value;

   public JsonProperty(@Nullable T value) {
      this.value = value;
   }

   @Nullable
   public T getValue() {
      return this.value;
   }

   public void setValue(@Nullable T value) {
      this.value = value;
   }

   public List<Component> getComponents() {
      return this.components;
   }

   public abstract void initComponents();
}
