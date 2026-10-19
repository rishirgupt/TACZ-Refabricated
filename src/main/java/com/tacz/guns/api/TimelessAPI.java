package com.tacz.guns.api;

import com.tacz.guns.api.client.other.IThirdPersonAnimation;
import com.tacz.guns.api.client.other.ThirdPersonManager;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.resource.ClientIndexManager;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.index.ClientAmmoIndex;
import com.tacz.guns.client.resource.index.ClientAttachmentIndex;
import com.tacz.guns.client.resource.index.ClientBlockIndex;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.tacz.guns.crafting.GunSmithTableRecipe;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.index.CommonAmmoIndex;
import com.tacz.guns.resource.index.CommonAttachmentIndex;
import com.tacz.guns.resource.index.CommonBlockIndex;
import com.tacz.guns.resource.index.CommonGunIndex;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;

public final class TimelessAPI {
   @Environment(EnvType.CLIENT)
   public static Optional<GunDisplayInstance> getGunDisplay(ItemStack stack) {
      if (stack.getItem() instanceof IGun iGun) {
         Identifier gunId = iGun.getGunId(stack);
         if (getCommonGunIndex(gunId).isEmpty()) {
            return Optional.empty();
         }

         Identifier displayId = iGun.getGunDisplayId(stack);
         return displayId.equals(DefaultAssets.DEFAULT_GUN_DISPLAY_ID)
            ? getClientGunIndex(gunId).map(ClientGunIndex::getDefaultDisplay)
            : getGunDisplay(displayId, gunId);
      } else {
         return Optional.empty();
      }
   }

   @Environment(EnvType.CLIENT)
   public static Optional<ClientGunIndex> getClientGunIndex(Identifier gunId) {
      return Optional.ofNullable(ClientIndexManager.GUN_INDEX.get(gunId));
   }

   @Environment(EnvType.CLIENT)
   public static Optional<GunDisplayInstance> getGunDisplay(Identifier displayId, Identifier fallbackGunId) {
      if (displayId != null && !displayId.equals(DefaultAssets.DEFAULT_GUN_DISPLAY_ID)) {
         GunDisplayInstance instance = ClientIndexManager.getOrCreateGunDisplay(displayId);
         return instance == null ? getClientGunIndex(fallbackGunId).map(ClientGunIndex::getDefaultDisplay) : Optional.of(instance);
      } else {
         return getClientGunIndex(fallbackGunId).map(ClientGunIndex::getDefaultDisplay);
      }
   }

   @Environment(EnvType.CLIENT)
   public static Optional<ClientAttachmentIndex> getClientAttachmentIndex(Identifier attachmentId) {
      return Optional.ofNullable(ClientIndexManager.ATTACHMENT_INDEX.get(attachmentId));
   }

   @Environment(EnvType.CLIENT)
   public static Optional<ClientAmmoIndex> getClientAmmoIndex(Identifier ammoId) {
      return Optional.ofNullable(ClientIndexManager.AMMO_INDEX.get(ammoId));
   }

   @Environment(EnvType.CLIENT)
   public static Optional<ClientBlockIndex> getClientBlockIndex(Identifier blockId) {
      return Optional.ofNullable(ClientIndexManager.BLOCK_INDEX.get(blockId));
   }

   @Environment(EnvType.CLIENT)
   public static Set<Entry<Identifier, ClientGunIndex>> getAllClientGunIndex() {
      return ClientIndexManager.getAllGuns();
   }

   @Environment(EnvType.CLIENT)
   public static Set<Entry<Identifier, ClientAmmoIndex>> getAllClientAmmoIndex() {
      return ClientIndexManager.getAllAmmo();
   }

   @Environment(EnvType.CLIENT)
   public static Set<Entry<Identifier, ClientAttachmentIndex>> getAllClientAttachmentIndex() {
      return ClientIndexManager.getAllAttachments();
   }

   public static Optional<CommonBlockIndex> getCommonBlockIndex(Identifier blockId) {
      return Optional.ofNullable(CommonAssetsManager.get().getBlockIndex(blockId));
   }

   public static Optional<CommonGunIndex> getCommonGunIndex(Identifier gunId) {
      return Optional.ofNullable(CommonAssetsManager.get().getGunIndex(gunId));
   }

   public static Optional<CommonAttachmentIndex> getCommonAttachmentIndex(Identifier attachmentId) {
      return Optional.ofNullable(CommonAssetsManager.get().getAttachmentIndex(attachmentId));
   }

   public static Optional<CommonAmmoIndex> getCommonAmmoIndex(Identifier ammoId) {
      return Optional.ofNullable(CommonAssetsManager.get().getAmmoIndex(ammoId));
   }

   @Deprecated
   public static Optional<GunSmithTableRecipe> getRecipe(Identifier recipeId) {
      return Optional.empty();
   }

   public static Set<Entry<Identifier, CommonBlockIndex>> getAllCommonBlockIndex() {
      return CommonAssetsManager.get().getAllBlocks();
   }

   public static Set<Entry<Identifier, CommonGunIndex>> getAllCommonGunIndex() {
      return CommonAssetsManager.get().getAllGuns();
   }

   public static Set<Entry<Identifier, CommonAmmoIndex>> getAllCommonAmmoIndex() {
      return CommonAssetsManager.get().getAllAmmos();
   }

   public static Set<Entry<Identifier, CommonAttachmentIndex>> getAllCommonAttachmentIndex() {
      return CommonAssetsManager.get().getAllAttachments();
   }

   @Deprecated
   public static Map<Identifier, GunSmithTableRecipe> getAllRecipes() {
      return Map.of();
   }

   public static void registerThirdPersonAnimation(String name, IThirdPersonAnimation animation) {
      ThirdPersonManager.register(name, animation);
   }
}
