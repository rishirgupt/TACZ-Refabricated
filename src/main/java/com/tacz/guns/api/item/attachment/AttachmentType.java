package com.tacz.guns.api.item.attachment;

import com.google.gson.annotations.SerializedName;
import com.mojang.serialization.Codec;
import java.lang.reflect.Field;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum AttachmentType implements StringRepresentable {
   @SerializedName("scope")
   SCOPE,
   @SerializedName("muzzle")
   MUZZLE,
   @SerializedName("stock")
   STOCK,
   @SerializedName("grip")
   GRIP,
   @SerializedName("laser")
   LASER,
   @SerializedName("extended_mag")
   EXTENDED_MAG,
   NONE;

   public static final Codec<AttachmentType> CODEC = StringRepresentable.fromEnum(AttachmentType::values);
   private final String serializedName;
   private static final AttachmentType[] VALUES = values();

   AttachmentType() {
      String name = this.name().toLowerCase(Locale.ROOT);

      try {
         Field field = this.getClass().getField(this.name());
         SerializedName serializedName = field.getAnnotation(SerializedName.class);
         if (serializedName != null) {
            name = serializedName.value();
         }
      } catch (NoSuchFieldException var6) {
      }

      this.serializedName = name;
   }

   @NotNull
   public String getSerializedName() {
      return this.serializedName;
   }

   public static AttachmentType fromId(int id) {
      return VALUES[id];
   }
}
