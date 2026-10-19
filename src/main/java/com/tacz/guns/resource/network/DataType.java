package com.tacz.guns.resource.network;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum DataType implements StringRepresentable {
   GUN_DATA,
   ATTACHMENT_DATA,
   AMMO_INDEX,
   GUN_INDEX,
   ATTACHMENT_INDEX,
   RECIPES,
   RECIPE_FILTER,
   ATTACHMENT_TAGS,
   ALLOW_ATTACHMENT_TAGS,
   BLOCK_DATA,
   BLOCK_INDEX;

   public static final Codec<DataType> CODEC = StringRepresentable.fromEnum(DataType::values);

   public String getSerializedName() {
      return this.name();
   }
}
