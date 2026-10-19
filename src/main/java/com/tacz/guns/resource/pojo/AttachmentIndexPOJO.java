package com.tacz.guns.resource.pojo;

import com.google.gson.annotations.SerializedName;
import com.tacz.guns.api.item.attachment.AttachmentType;
import javax.annotation.Nullable;
import net.minecraft.resources.Identifier;

public class AttachmentIndexPOJO {
   @SerializedName("name")
   private String name;
   @SerializedName("tooltip")
   @Nullable
   private String tooltip;
   @SerializedName("display")
   private Identifier display;
   @SerializedName("data")
   private Identifier data;
   @SerializedName("type")
   private AttachmentType type;
   @SerializedName("sort")
   private int sort;
   @SerializedName("hidden")
   private boolean hidden = false;

   public String getName() {
      return this.name;
   }

   @Nullable
   public String getTooltip() {
      return this.tooltip;
   }

   public Identifier getDisplay() {
      return this.display;
   }

   public Identifier getData() {
      return this.data;
   }

   public AttachmentType getType() {
      return this.type;
   }

   public boolean isHidden() {
      return this.hidden;
   }

   public int getSort() {
      return this.sort;
   }
}
