package com.tacz.guns.resource.pojo.data.attachment;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.tacz.guns.resource.manager.AttachmentScriptManager;
import com.tacz.guns.resource.serialize.ModifierTypeAdapterFactory;
import javax.annotation.Nullable;

@JsonAdapter(ModifierTypeAdapterFactory.class)
public class Modifier {
   @SerializedName("addend")
   private double addend = 0.0;
   @SerializedName("percent")
   private double percent = 0.0;
   @SerializedName("multiplier")
   private double multiplier = 1.0;
   @Nullable
   @SerializedName("function")
   private String function = null;
   @Nullable
   private transient AttachmentScriptManager.CompiledScript compiledFunction;

   public double getAddend() {
      return this.addend;
   }

   public double getPercent() {
      return this.percent;
   }

   public double getMultiplier() {
      return this.multiplier;
   }

   @Nullable
   public String getFunction() {
      return this.function;
   }

   public void compileFunction() {
      this.compiledFunction = AttachmentScriptManager.compile(this.function);
   }

   @Nullable
   public AttachmentScriptManager.CompiledScript getCompiledFunction() {
      return this.compiledFunction;
   }

   @Deprecated
   public void setAddend(double addend) {
      this.addend = addend;
   }

   @Deprecated
   public void setPercent(double percent) {
      this.percent = percent;
   }
}
