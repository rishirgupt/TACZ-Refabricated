package com.tacz.guns.client.resource.pojo.display;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.awt.Color;

public class LaserConfig {
   private Integer defaultColor;
   @Expose
   @SerializedName("default_color")
   private String color = "#FF0000";
   @Expose
   @SerializedName("can_edit")
   private boolean canEdit = true;
   @Expose
   @SerializedName("length")
   private int length = 25;
   @Expose
   @SerializedName("width")
   private float width = 0.008F;
   @Expose
   @SerializedName("third_person_length")
   private float third_person_length = 2.0F;
   @Expose
   @SerializedName("third_person_width")
   private float third_person_width = 0.008F;

   public int getDefaultColor() {
      if (this.defaultColor == null) {
         try {
            this.defaultColor = Color.decode(this.color).getRGB();
         } catch (NumberFormatException e) {
            this.defaultColor = Color.WHITE.getRGB();
         }
      }

      return this.defaultColor;
   }

   public boolean canEdit() {
      return this.canEdit;
   }

   public int getLength() {
      return this.length;
   }

   public float getWidth() {
      return this.width;
   }

   public float getLengthThird() {
      return this.third_person_length;
   }

   public float getWidthThird() {
      return this.third_person_width;
   }
}
