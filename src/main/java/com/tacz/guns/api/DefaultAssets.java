package com.tacz.guns.api;

import net.minecraft.resources.Identifier;

public final class DefaultAssets {
   public static Identifier DEFAULT_GUN_DISPLAY_ID = Identifier.fromNamespaceAndPath("tacz", "default");
   public static Identifier EMPTY_GUN_ID = Identifier.fromNamespaceAndPath("tacz", "empty");
   public static Identifier DEFAULT_AMMO_ID = Identifier.fromNamespaceAndPath("tacz", "762x39");
   public static Identifier EMPTY_AMMO_ID = Identifier.fromNamespaceAndPath("tacz", "empty");
   public static Identifier DEFAULT_BLOCK_ID = Identifier.fromNamespaceAndPath("tacz", "gun_smith_table");
   public static Identifier EMPTY_BLOCK_ID = Identifier.fromNamespaceAndPath("tacz", "empty");
   public static Identifier DEFAULT_ATTACHMENT_ID = Identifier.fromNamespaceAndPath("tacz", "sight_sro_dot");
   public static Identifier EMPTY_ATTACHMENT_ID = Identifier.fromNamespaceAndPath("tacz", "empty");
   public static Identifier DEFAULT_ATTACHMENT_SKIN_ID = Identifier.fromNamespaceAndPath("tacz", "sight_sro_dot_blue");
   public static Identifier EMPTY_ATTACHMENT_SKIN_ID = Identifier.fromNamespaceAndPath("tacz", "empty");

   public static boolean isEmptyAttachmentId(Identifier attachmentId) {
      return EMPTY_ATTACHMENT_ID.equals(attachmentId);
   }
}
