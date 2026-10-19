package com.tacz.guns.api.modifier;

import com.google.common.collect.ImmutableList.Builder;
import com.tacz.guns.resource.manager.AttachmentScriptManager;
import com.tacz.guns.resource.pojo.data.attachment.Modifier;
import java.util.List;

public class ParameterizedCache<T> {
   private final T defaultValue;
   private final List<AttachmentScriptManager.CompiledScript> scripts;
   private final double addend;
   private final double percent;
   private final double multiplier;

   public ParameterizedCache(List<Modifier> modifiers, T defaultValue) {
      double addend = 0.0;
      double percent = 1.0;
      double multiplier = 1.0;
      Builder<AttachmentScriptManager.CompiledScript> builder = new Builder();

      for (Modifier mod : modifiers) {
         addend += mod.getAddend();
         percent += mod.getPercent();
         multiplier *= Math.max(mod.getMultiplier(), 0.0);
         if (mod.getCompiledFunction() != null) {
            builder.add(mod.getCompiledFunction());
         }
      }

      this.addend = addend;
      this.percent = percent;
      this.multiplier = multiplier;
      this.scripts = builder.build();
      this.defaultValue = defaultValue;
   }

   public T getDefaultValue() {
      return this.defaultValue;
   }

   public double eval(double input) {
      double percent = Math.max(this.percent, 0.0);
      double value = (input + this.addend) * percent * this.multiplier;

      for (AttachmentScriptManager.CompiledScript function : this.scripts) {
         value = function.eval(value, input);
      }

      return value;
   }

   public double eval(double input, double extraAddend, double extraPercent, double extraMultiplier) {
      double percent = Math.max(this.percent + extraPercent, 0.0);
      extraMultiplier = Math.max(extraMultiplier, 0.0);
      double value = (input + this.addend + extraAddend) * percent * this.multiplier * extraMultiplier;

      for (AttachmentScriptManager.CompiledScript function : this.scripts) {
         value = function.eval(value, input);
      }

      return value;
   }

   public static <T> ParameterizedCache<T> of(T defaultValue) {
      return new ParameterizedCache<>(List.of(), defaultValue);
   }

   public static <T> ParameterizedCache<T> of(List<Modifier> modifiers, T defaultValue) {
      return new ParameterizedCache<>(modifiers, defaultValue);
   }
}
