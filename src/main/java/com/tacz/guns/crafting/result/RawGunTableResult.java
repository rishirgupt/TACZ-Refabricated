package com.tacz.guns.crafting.result;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.api.item.builder.AttachmentItemBuilder;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.resource.pojo.data.block.TabConfig;
import com.tacz.guns.resource.pojo.data.recipe.GunResult;
import java.util.EnumMap;
import java.util.Locale;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.HolderLookup.Provider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RawGunTableResult {
   private final String type;
   private final int count;
   private final Identifier id;
   @Nullable
   private GunResult extraData;
   @Nullable
   private CompoundTag nbt;

   public RawGunTableResult(@NotNull String type, @NotNull Identifier id, int count) {
      this.type = type;
      this.id = id;
      this.count = count;
   }

   public void setExtraData(@Nullable GunResult extraData) {
      this.extraData = extraData;
   }

   public void setNbt(@Nullable CompoundTag nbt) {
      this.nbt = nbt;
   }

   public static GunSmithTableResult init(Provider provider, RawGunTableResult raw) {
      GunSmithTableResult result = switch (raw.type) {
         case "gun" -> raw.getGunStack(provider);
         case "ammo" -> raw.getAmmoStack();
         case "attachment" -> raw.getAttachmentStack();
         default -> new GunSmithTableResult(ItemStack.EMPTY, TabConfig.TAB_EMPTY);
      };
      if (raw.nbt != null) {
         result.getResult().update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> {
            for (String key : raw.nbt.keySet()) {
               Tag value = raw.nbt.get(key);
               if (value != null) {
                  tag.put(key, value);
               }
            }
         }));
      }

      return result;
   }

   private GunSmithTableResult getGunStack(Provider provider) {
      int ammoCount;
      EnumMap<AttachmentType, Identifier> attachments;
      if (this.extraData != null) {
         ammoCount = Math.max(0, this.extraData.getAmmoCount());
         attachments = this.extraData.getAttachments();
      } else {
         ammoCount = 0;
         attachments = new EnumMap<>(AttachmentType.class);
      }

      return TimelessAPI.getCommonGunIndex(this.id)
         .map(
            gunIndex -> {
               ItemStack itemStack = GunItemBuilder.create()
                  .setCount(this.count)
                  .setId(this.id)
                  .setAmmoCount(ammoCount)
                  .setAmmoInBarrel(false)
                  .putAllAttachment(attachments)
                  .setFireMode(gunIndex.getGunData().getFireModeSet().getFirst())
                  .build(provider);
               String raw = gunIndex.getType();
               if (!raw.contains(":")) {
                  raw = "tacz:" + raw;
               }

               Identifier group = Identifier.tryParse(raw);
               return new GunSmithTableResult(itemStack, group);
            }
         )
         .orElse(new GunSmithTableResult(ItemStack.EMPTY, TabConfig.TAB_EMPTY));
   }

   private GunSmithTableResult getAmmoStack() {
      return new GunSmithTableResult(AmmoItemBuilder.create().setCount(this.count).setId(this.id).build(), TabConfig.TAB_AMMO);
   }

   private GunSmithTableResult getAttachmentStack() {
      return TimelessAPI.getCommonAttachmentIndex(this.id).map(attachmentIndex -> {
         ItemStack itemStack = AttachmentItemBuilder.create().setCount(this.count).setId(this.id).build();
         String raw = attachmentIndex.getType().name().toLowerCase(Locale.US);
         if (!raw.contains(":")) {
            raw = "tacz:" + raw;
         }

         Identifier group = Identifier.tryParse(raw);
         return new GunSmithTableResult(itemStack, group);
      }).orElse(new GunSmithTableResult(ItemStack.EMPTY, TabConfig.TAB_EMPTY));
   }
}
