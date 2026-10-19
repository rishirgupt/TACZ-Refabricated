package com.tacz.guns.client.resource.pojo.display.gun;

import com.google.gson.annotations.SerializedName;

public class TextShow {
   @SerializedName("scale")
   private float scale = 1.0F;
   @SerializedName("align")
   private Align align = Align.CENTER;
   @SerializedName("shadow")
   private boolean shadow = false;
   @SerializedName("color")
   private String colorText = "#FFFFFF";
   @SerializedName("light")
   private int textLight = 15;
   @SerializedName("text")
   private String textKey = "";
   private volatile int colorInt = 16777215;

   public float getScale() {
      return this.scale;
   }

   public Align getAlign() {
      return this.align;
   }

   public boolean isShadow() {
      return this.shadow;
   }

   public String getTextKey() {
      return this.textKey;
   }

   public String getColorText() {
      return this.colorText;
   }

   public int getTextLight() {
      return this.textLight;
   }

   public int getColorInt() {
      return this.colorInt;
   }

   public void setColorInt(int colorInt) {
      this.colorInt = colorInt;
   }
}
