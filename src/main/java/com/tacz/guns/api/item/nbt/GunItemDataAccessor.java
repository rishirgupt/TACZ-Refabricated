package com.tacz.guns.api.item.nbt;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.builder.AttachmentItemBuilder;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.index.ClientAttachmentIndex;
import com.tacz.guns.resource.index.CommonGunIndex;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.HolderLookup.Provider;
import org.jetbrains.annotations.NotNull;

public interface GunItemDataAccessor extends IGun {
   String GUN_ID_TAG = "GunId";
   String GUN_FIRE_MODE_TAG = "GunFireMode";
   String GUN_HAS_BULLET_IN_BARREL = "HasBulletInBarrel";
   String GUN_CURRENT_AMMO_COUNT_TAG = "GunCurrentAmmoCount";
   String GUN_ATTACHMENT_BASE = "Attachment";
   String GUN_EXP_TAG = "GunLevelExp";
   String GUN_DUMMY_AMMO = "DummyAmmo";
   String GUN_MAX_DUMMY_AMMO = "MaxDummyAmmo";
   String GUN_ATTACHMENT_LOCK = "AttachmentLock";
   String GUN_DISPLAY_ID_TAG = "GunDisplayId";
   String LASER_COLOR_TAG = "LaserColor";
   String GUN_OVERHEAT_TAG = "HeatAmount";
   String GUN_OVERHEAT_LOCK_TAG = "OverHeated";

   @Override
   default boolean useDummyAmmo(ItemStack gun) {
      CompoundTag nbt = ((CustomData)gun.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).tag;
      return nbt.contains("DummyAmmo");
   }

   @Override
   default int getDummyAmmoAmount(ItemStack gun) {
      CompoundTag nbt = ((CustomData)gun.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).tag;
      return Math.max(0, nbt.getIntOr("DummyAmmo", 0));
   }

   @Override
   default void setDummyAmmoAmount(ItemStack gun, int amount) {
      gun.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> tag.putInt("DummyAmmo", Math.max(amount, 0))));
   }

   @Override
   default void addDummyAmmoAmount(ItemStack gun, int amount) {
      if (this.useDummyAmmo(gun)) {
         int maxDummyAmmo = Integer.MAX_VALUE;
         if (this.hasMaxDummyAmmo(gun)) {
            maxDummyAmmo = this.getMaxDummyAmmoAmount(gun);
         }

         int dummyAmmo = Math.min(this.getDummyAmmoAmount(gun) + amount, maxDummyAmmo);
         gun.update(
            DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> tag.putInt("DummyAmmo", Math.max(dummyAmmo, 0)))
         );
      }
   }

   @Override
   default boolean hasMaxDummyAmmo(ItemStack gun) {
      CompoundTag nbt = ((CustomData)gun.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).tag;
      return nbt.contains("MaxDummyAmmo");
   }

   @Override
   default int getMaxDummyAmmoAmount(ItemStack gun) {
      CompoundTag nbt = ((CustomData)gun.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).tag;
      return Math.max(0, nbt.getIntOr("MaxDummyAmmo", 0));
   }

   @Override
   default void setMaxDummyAmmoAmount(ItemStack gun, int amount) {
      gun.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> tag.putInt("MaxDummyAmmo", Math.max(amount, 0))));
   }

   @Override
   default boolean hasAttachmentLock(ItemStack gun) {
      CompoundTag nbt = ((CustomData)gun.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).tag;
      return nbt.contains("AttachmentLock") ? nbt.getBooleanOr("AttachmentLock", false) : false;
   }

   @Override
   default void setAttachmentLock(ItemStack gun, boolean lock) {
      gun.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> tag.putBoolean("AttachmentLock", lock)));
   }

   @Nonnull
   @Override
   default Identifier getGunId(ItemStack gun) {
      CompoundTag nbt = ((CustomData)gun.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).tag;
      if (nbt.contains("GunId")) {
         Identifier gunId = Identifier.tryParse(nbt.getStringOr("GunId", ""));
         return Objects.requireNonNullElse(gunId, DefaultAssets.EMPTY_GUN_ID);
      } else {
         return DefaultAssets.EMPTY_GUN_ID;
      }
   }

   @Override
   default void setGunId(ItemStack gun, @Nullable Identifier gunId) {
      gun.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> {
         if (gunId != null) {
            tag.putString("GunId", gunId.toString());
         }
      }));
   }

   @NotNull
   @Override
   default Identifier getGunDisplayId(ItemStack gun) {
      CompoundTag nbt = ((CustomData)gun.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).tag;
      if (nbt.contains("GunDisplayId")) {
         Identifier gunDisplayId = Identifier.tryParse(nbt.getStringOr("GunDisplayId", ""));
         return Objects.requireNonNullElse(gunDisplayId, DefaultAssets.DEFAULT_GUN_DISPLAY_ID);
      } else {
         return DefaultAssets.DEFAULT_GUN_DISPLAY_ID;
      }
   }

   @Override
   default void setGunDisplayId(ItemStack gun, Identifier displayId) {
      gun.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> {
         if (displayId != null) {
            tag.putString("GunDisplayId", displayId.toString());
         }
      }));
   }

   @Override
   default int getLevel(ItemStack gun) {
      CompoundTag nbt = ((CustomData)gun.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).tag;
      return nbt.contains("GunLevelExp") ? this.getLevel(nbt.getIntOr("GunLevelExp", 0)) : 0;
   }

   @Override
   default int getExp(ItemStack gun) {
      CompoundTag nbt = ((CustomData)gun.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).tag;
      return nbt.contains("GunLevelExp") ? nbt.getIntOr("GunLevelExp", 0) : 0;
   }

   @Override
   default int getExpToNextLevel(ItemStack gun) {
      int exp = this.getExp(gun);
      int level = this.getLevel(exp);
      if (level >= this.getMaxLevel()) {
         return 0;
      }

      int nextLevelExp = this.getExp(level + 1);
      return nextLevelExp - exp;
   }

   @Override
   default int getExpCurrentLevel(ItemStack gun) {
      int exp = this.getExp(gun);
      int level = this.getLevel(exp);
      return level <= 0 ? exp : exp - this.getExp(level - 1);
   }

   @Override
   default FireMode getFireMode(ItemStack gun) {
      CompoundTag nbt = ((CustomData)gun.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).tag;
      return nbt.contains("GunFireMode") ? FireMode.valueOf(nbt.getStringOr("GunFireMode", "")) : FireMode.UNKNOWN;
   }

   @Override
   default void setFireMode(ItemStack gun, @Nullable FireMode fireMode) {
      gun.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> {
         if (fireMode != null) {
            tag.putString("GunFireMode", fireMode.name());
         } else {
            tag.putString("GunFireMode", FireMode.UNKNOWN.name());
         }
      }));
   }

   @Override
   default int getCurrentAmmoCount(ItemStack gun) {
      CompoundTag nbt = ((CustomData)gun.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).tag;
      return nbt.contains("GunCurrentAmmoCount") ? nbt.getIntOr("GunCurrentAmmoCount", 0) : 0;
   }

   @Override
   default void setCurrentAmmoCount(ItemStack gun, int ammoCount) {
      gun.update(
         DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> tag.putInt("GunCurrentAmmoCount", Math.max(ammoCount, 0)))
      );
   }

   @Override
   default void reduceCurrentAmmoCount(ItemStack gun) {
      if (!this.useInventoryAmmo(gun)) {
         this.setCurrentAmmoCount(gun, this.getCurrentAmmoCount(gun) - 1);
      }
   }

   @Nullable
   @Override
   default CompoundTag getAttachmentTag(ItemStack gun, AttachmentType type) {
      if (!this.allowAttachmentType(gun, type)) {
         return null;
      }

      CompoundTag nbt = ((CustomData)gun.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).tag;
      String key = "Attachment" + type.name();
      if (!nbt.contains(key)) {
         return null;
      }

      CompoundTag stack = nbt.getCompoundOrEmpty(key);
      if (!stack.contains("components")) {
         return null;
      }

      CompoundTag components = stack.getCompoundOrEmpty("components");
      return !components.contains(DataComponents.CUSTOM_DATA.toString()) ? null : components.getCompoundOrEmpty(DataComponents.CUSTOM_DATA.toString());
   }

   @Override
   default void setAttachmentTag(ItemStack gun, AttachmentType type, CompoundTag attachmentTag) {
      if (this.allowAttachmentType(gun, type)) {
         gun.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> {
            String key = "Attachment" + type.name();
            if (tag.contains(key)) {
               CompoundTag stack = tag.getCompoundOrEmpty(key);
               if (stack.contains("components")) {
                  CompoundTag components = stack.getCompoundOrEmpty("components");
                  if (components.contains(DataComponents.CUSTOM_DATA.toString())) {
                     components.put(DataComponents.CUSTOM_DATA.toString(), attachmentTag);
                  }
               }
            }
         }));
      }
   }

   @NotNull
   @Override
   default ItemStack getBuiltinAttachment(ItemStack gun, AttachmentType type) {
      IGun iGun = IGun.getIGunOrNull(gun);
      if (iGun == null) {
         return ItemStack.EMPTY;
      }

      CommonGunIndex index = TimelessAPI.getCommonGunIndex(iGun.getGunId(gun)).orElse(null);
      if (index != null) {
         Map<AttachmentType, Identifier> builtin = index.getGunData().getBuiltInAttachments();
         if (builtin.containsKey(type)) {
            return AttachmentItemBuilder.create().setId(builtin.get(type)).build();
         }
      }

      return ItemStack.EMPTY;
   }

   @Nonnull
   @Override
   default ItemStack getAttachment(Provider provider, ItemStack gun, AttachmentType type) {
      if (!this.allowAttachmentType(gun, type)) {
         return ItemStack.EMPTY;
      }

      CompoundTag nbt = ((CustomData)gun.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).tag;
      String key = "Attachment" + type.name();
      return nbt.contains(key)
         ? ItemStack.CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), nbt.getCompoundOrEmpty(key)).result().orElse(ItemStack.EMPTY)
         : ItemStack.EMPTY;
   }

   @NotNull
   @Override
   default Identifier getBuiltInAttachmentId(ItemStack gun, AttachmentType type) {
      IGun iGun = IGun.getIGunOrNull(gun);
      if (iGun == null) {
         return DefaultAssets.EMPTY_ATTACHMENT_ID;
      }

      CommonGunIndex index = TimelessAPI.getCommonGunIndex(iGun.getGunId(gun)).orElse(null);
      if (index != null) {
         Map<AttachmentType, Identifier> builtin = index.getGunData().getBuiltInAttachments();
         if (builtin.containsKey(type)) {
            return builtin.get(type);
         }
      }

      return DefaultAssets.EMPTY_ATTACHMENT_ID;
   }

   @Nonnull
   @Override
   default Identifier getAttachmentId(ItemStack gun, AttachmentType type) {
      CompoundTag attachmentTag = this.getAttachmentTag(gun, type);
      return attachmentTag != null ? AttachmentItemDataAccessor.getAttachmentIdFromTag(attachmentTag) : DefaultAssets.EMPTY_ATTACHMENT_ID;
   }

   @Override
   default void installAttachment(Provider provider, @Nonnull ItemStack gun, @Nonnull ItemStack attachment) {
      if (this.allowAttachment(gun, attachment)) {
         IAttachment iAttachment = IAttachment.getIAttachmentOrNull(attachment);
         if (iAttachment != null) {
            gun.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> {
               String key = "Attachment" + iAttachment.getType(attachment).name();
               Tag attachmentTag = ItemStack.OPTIONAL_CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), attachment).getOrThrow();
               tag.put(key, attachmentTag);
            }));
         }
      }
   }

   @Override
   default void unloadAttachment(Provider provider, @Nonnull ItemStack gun, AttachmentType type) {
      if (this.allowAttachmentType(gun, type)) {
         gun.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> {
            String key = "Attachment" + type.name();
            tag.put(key, ItemStack.OPTIONAL_CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), ItemStack.EMPTY).getOrThrow());
         }));
      }
   }

   @Override
   default float getAimingZoom(ItemStack gunItem) {
      float zoom = 1.0F;
      Identifier scopeId = this.getAttachmentId(gunItem, AttachmentType.SCOPE);
      boolean builtin = false;
      if (scopeId.equals(DefaultAssets.EMPTY_ATTACHMENT_ID)) {
         scopeId = this.getBuiltInAttachmentId(gunItem, AttachmentType.SCOPE);
         builtin = true;
      }

      if (!DefaultAssets.isEmptyAttachmentId(scopeId)) {
         CompoundTag attachmentTag = this.getAttachmentTag(gunItem, AttachmentType.SCOPE);
         int zoomNumber = builtin ? 0 : AttachmentItemDataAccessor.getZoomNumberFromTag(attachmentTag);
         float[] zooms = TimelessAPI.getClientAttachmentIndex(scopeId).map(ClientAttachmentIndex::getZoom).orElse(null);
         if (zooms != null) {
            zoom = zooms[zoomNumber % zooms.length];
         }
      } else {
         zoom = TimelessAPI.getGunDisplay(gunItem).map(GunDisplayInstance::getIronZoom).orElse(1.0F);
      }

      return zoom;
   }

   @Override
   default boolean hasBulletInBarrel(ItemStack gun) {
      CompoundTag nbt = ((CustomData)gun.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).tag;
      return nbt.contains("HasBulletInBarrel") ? nbt.getBooleanOr("HasBulletInBarrel", false) : false;
   }

   @Override
   default void setBulletInBarrel(ItemStack gun, boolean bulletInBarrel) {
      gun.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> tag.putBoolean("HasBulletInBarrel", bulletInBarrel)));
   }

   @Override
   default boolean hasCustomLaserColor(ItemStack gun) {
      CompoundTag nbt = ((CustomData)gun.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).tag;
      return nbt.contains("LaserColor");
   }

   @Override
   default int getLaserColor(ItemStack gun) {
      CompoundTag nbt = ((CustomData)gun.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).tag;
      return !this.hasCustomLaserColor(gun) ? 16711680 : nbt.getIntOr("LaserColor", 0);
   }

   @Override
   default void setLaserColor(ItemStack gun, int color) {
      gun.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> tag.putInt("LaserColor", color)));
   }

   @Override
   default boolean hasHeatData(ItemStack gun) {
      return ((CustomData)gun.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).tag.contains("HeatAmount");
   }

   @Override
   default boolean isOverheatLocked(ItemStack gun) {
      return ((CustomData)gun.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).tag.getBooleanOr("OverHeated", false);
   }

   @Override
   default void setOverheatLocked(ItemStack gun, boolean locked) {
      gun.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> tag.putBoolean("OverHeated", locked)));
   }

   @Override
   default float getHeatAmount(ItemStack gun) {
      return this.hasHeatData(gun)
         ? ((CustomData)gun.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).tag.getFloatOr("HeatAmount", 0.0F)
         : 0.0F;
   }

   @Override
   default void setHeatAmount(ItemStack gun, float amount) {
      gun.update(
         DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> tag.putFloat("HeatAmount", amount >= 0.0F ? amount : 0.0F))
      );
   }

   @Override
   default float lerpRPM(ItemStack gun) {
      return TimelessAPI.getCommonGunIndex(this.getGunId(gun)).map(index -> index.getGunData().getHeatData()).map(heatData -> {
         float heatPercentage = this.getHeatAmount(gun) / heatData.getHeatMax();
         return Mth.lerp(heatPercentage, heatData.getMinRpmMod(), heatData.getMaxRpmMod());
      }).orElse(1.0F);
   }

   @Override
   default float lerpInaccuracy(ItemStack gun) {
      return TimelessAPI.getCommonGunIndex(this.getGunId(gun)).map(index -> index.getGunData().getHeatData()).map(heatData -> {
         float heatPercentage = this.getHeatAmount(gun) / heatData.getHeatMax();
         return Mth.lerp(heatPercentage, heatData.getMinInaccuracy(), heatData.getMaxInaccuracy());
      }).orElse(1.0F);
   }
}
