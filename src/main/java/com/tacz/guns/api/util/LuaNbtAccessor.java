package com.tacz.guns.api.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import org.jetbrains.annotations.ApiStatus.Internal;

public record LuaNbtAccessor(@Internal CompoundTag nbt) {
   public static LuaNbtAccessor from(ItemStack stack) {
      return new LuaNbtAccessor(((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag());
   }

   public static LuaNbtAccessor from(CompoundTag nbt) {
      return new LuaNbtAccessor(nbt);
   }

   public boolean contains(String key) {
      return this.nbt.contains(key);
   }

   public boolean contains(String key, int type) {
      net.minecraft.nbt.Tag value = this.nbt.get(key);
      return value != null && (type == 99 ? value instanceof net.minecraft.nbt.NumericTag : value.getId() == type);
   }

   public LuaNbtAccessor newCompoundTag() {
      return new LuaNbtAccessor(new CompoundTag());
   }

   public int getInt(String key) {
      return this.nbt.getIntOr(key, 0);
   }

   public double getDouble(String key) {
      return this.nbt.getDoubleOr(key, 0.0);
   }

   public float getFloat(String key) {
      return this.nbt.getFloatOr(key, 0.0F);
   }

   public long getLong(String key) {
      return this.nbt.getLongOr(key, 0L);
   }

   public String getString(String key) {
      return this.nbt.getStringOr(key, "");
   }

   public boolean getBoolean(CompoundTag nbt, String key) {
      return nbt.getBooleanOr(key, false);
   }

   public LuaNbtAccessor getCompound(String key) {
      return !this.nbt.contains(key) ? null : from(this.nbt.getCompoundOrEmpty(key));
   }

   public void putInt(String key, int value) {
      this.nbt.putInt(key, value);
   }

   public void putDouble(String key, double value) {
      this.nbt.putDouble(key, value);
   }

   public void putFloat(String key, float value) {
      this.nbt.putFloat(key, value);
   }

   public void putLong(String key, long value) {
      this.nbt.putLong(key, value);
   }

   public void putString(String key, String value) {
      this.nbt.putString(key, value);
   }

   public void putBoolean(String key, boolean value) {
      this.nbt.putBoolean(key, value);
   }

   public void putCompound(String key, LuaNbtAccessor value) {
      if (value != null) {
         this.nbt.put(key, value.nbt());
      }
   }
}
