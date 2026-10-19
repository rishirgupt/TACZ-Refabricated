package com.tacz.guns.util.datafixer;

import com.google.common.collect.ImmutableMap;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.item.nbt.AttachmentItemDataAccessor;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

public final class AttachmentIdFix {
   public static final Map<Identifier, Identifier> OLD_TO_NEW = ImmutableMap.<Identifier, Identifier>builder()
      .put(Identifier.fromNamespaceAndPath("tacz", "muzzle_silence_knight_qd"), Identifier.fromNamespaceAndPath("tacz", "muzzle_silencer_knight_qd"))
      .put(Identifier.fromNamespaceAndPath("tacz", "muzzle_silence_mirage"), Identifier.fromNamespaceAndPath("tacz", "muzzle_silencer_mirage"))
      .put(Identifier.fromNamespaceAndPath("tacz", "muzzle_silence_phantom_s1"), Identifier.fromNamespaceAndPath("tacz", "muzzle_silencer_phantom_s1"))
      .put(Identifier.fromNamespaceAndPath("tacz", "muzzle_silence_ptilopsis"), Identifier.fromNamespaceAndPath("tacz", "muzzle_silencer_ptilopsis"))
      .put(Identifier.fromNamespaceAndPath("tacz", "muzzle_silence_ursus"), Identifier.fromNamespaceAndPath("tacz", "muzzle_silencer_ursus"))
      .put(Identifier.fromNamespaceAndPath("tacz", "muzzle_silence_vulture"), Identifier.fromNamespaceAndPath("tacz", "muzzle_silencer_vulture"))
      .build();

   private AttachmentIdFix() {
   }

   public static boolean updateAttachmentIdInTag(CompoundTag tag) {
      Identifier old = AttachmentItemDataAccessor.getAttachmentIdFromTag(tag);
      if (!old.equals(DefaultAssets.EMPTY_ATTACHMENT_ID)) {
         Identifier fixed = updateAttachmentId(old);
         if (!old.equals(fixed)) {
            tag.putString("AttachmentId", fixed.toString());
            return true;
         }
      }

      return false;
   }

   public static Identifier updateAttachmentId(Identifier old) {
      return OLD_TO_NEW.getOrDefault(old, old);
   }
}
