package com.tacz.guns.client.resource;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.handler.FirstPersonRenderHandler;
import com.google.common.collect.Maps;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.resource.index.ClientAmmoIndex;
import com.tacz.guns.client.resource.index.ClientAttachmentIndex;
import com.tacz.guns.client.resource.index.ClientBlockIndex;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import com.tacz.guns.resource.pojo.AmmoIndexPOJO;
import com.tacz.guns.resource.pojo.AttachmentIndexPOJO;
import com.tacz.guns.resource.pojo.BlockIndexPOJO;
import com.tacz.guns.resource.pojo.GunIndexPOJO;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

@Environment(EnvType.CLIENT)
public class ClientIndexManager {
   private static final int HOTBAR_SLOT_COUNT = 9;
   public static final Map<Identifier, GunDisplayInstance> GUN_DISPLAY = Maps.newHashMap();
   public static final Map<Identifier, ClientGunIndex> GUN_INDEX = Maps.newHashMap();
   public static final Map<Identifier, ClientAmmoIndex> AMMO_INDEX = Maps.newHashMap();
   public static final Map<Identifier, ClientAttachmentIndex> ATTACHMENT_INDEX = Maps.newHashMap();
   public static final Map<Identifier, ClientBlockIndex> BLOCK_INDEX = Maps.newHashMap();

   public static void clear() {
      GUN_DISPLAY.clear();
      GUN_INDEX.clear();
      AMMO_INDEX.clear();
      ATTACHMENT_INDEX.clear();
      BLOCK_INDEX.clear();
   }

   public static void reload() {
      clear();
      loadGunDisplay();
      loadGunIndex();
      loadAmmoIndex();
      loadAttachmentIndex();
      loadBlockIndex();
      warmUpInventoryModels();
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null && IGun.mainHandHoldGun(player)) {
         AttachmentPropertyManager.postChangeEvent(player, player.getMainHandItem());
         IClientPlayerGunOperator.fromLocalPlayer(player).draw(ItemStack.EMPTY);
         FirstPersonRenderHandler.reset();
      }
   }

   public static void loadGunIndex() {
      TimelessAPI.getAllCommonGunIndex().forEach(index -> {
         Identifier id = index.getKey();
         GunIndexPOJO pojo = index.getValue().getPojo();

         try {
            GUN_INDEX.put(id, ClientGunIndex.getInstance(pojo));
         } catch (IllegalArgumentException exception) {
            GunMod.LOGGER.warn("{} index file read fail!", id, exception);
         }
      });
   }

   public static void loadGunDisplay() {
      ClientAssetsManager.INSTANCE.getGunDisplays().forEach(entry -> {
         Identifier displayId = entry.getKey();

         try {
            GUN_DISPLAY.put(displayId, GunDisplayInstance.create(displayId, entry.getValue()));
         } catch (IllegalArgumentException exception) {
            GunMod.LOGGER.warn("{} display file read fail!", displayId, exception);
         }
      });
   }

   public static GunDisplayInstance getOrCreateGunDisplay(Identifier displayId) {
      GunDisplayInstance instance = GUN_DISPLAY.get(displayId);
      if (instance != null) {
         return instance;
      }

      GunMod.LOGGER.warn("{} display instance is missing from cache", displayId);
      return null;
   }

   public static void loadAmmoIndex() {
      TimelessAPI.getAllCommonAmmoIndex().forEach(index -> {
         Identifier id = index.getKey();
         AmmoIndexPOJO pojo = index.getValue().getPojo();

         try {
            AMMO_INDEX.put(id, ClientAmmoIndex.getInstance(pojo));
         } catch (IllegalArgumentException exception) {
            GunMod.LOGGER.warn("{} index file read fail!", id, exception);
         }
      });
   }

   public static void loadAttachmentIndex() {
      TimelessAPI.getAllCommonAttachmentIndex().forEach(index -> {
         Identifier id = index.getKey();
         AttachmentIndexPOJO pojo = index.getValue().getPojo();

         try {
            ATTACHMENT_INDEX.put(id, ClientAttachmentIndex.getInstance(id, pojo));
         } catch (IllegalArgumentException exception) {
            GunMod.LOGGER.warn("{} index file read fail!", id, exception);
         }
      });
   }

   public static void loadBlockIndex() {
      TimelessAPI.getAllCommonBlockIndex().forEach(index -> {
         Identifier id = index.getKey();
         BlockIndexPOJO pojo = index.getValue().getPojo();

         try {
            BLOCK_INDEX.put(id, ClientBlockIndex.getInstance(pojo));
         } catch (IllegalArgumentException exception) {
            GunMod.LOGGER.warn("{} index file read fail!", id, exception);
         }
      });
   }

   public static Set<Entry<Identifier, ClientGunIndex>> getAllGuns() {
      return GUN_INDEX.entrySet();
   }

   public static Set<Entry<Identifier, ClientAmmoIndex>> getAllAmmo() {
      return AMMO_INDEX.entrySet();
   }

   public static Set<Entry<Identifier, ClientAttachmentIndex>> getAllAttachments() {
      return ATTACHMENT_INDEX.entrySet();
   }

   public static Set<Entry<Identifier, ClientBlockIndex>> getAllBlocks() {
      return BLOCK_INDEX.entrySet();
   }

   public static void warmUpInventoryModels() {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null) {
         warmUpItemForUse(player.getMainHandItem());
         warmUpItemForUse(player.getOffhandItem());
         warmUpHotbarModels(player);
         warmUpBackpackModels(player);
      }
   }

   public static void warmUpEquippedAndHotbarModels() {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null) {
         warmUpItemForUse(player.getMainHandItem());
         warmUpItemForUse(player.getOffhandItem());
         warmUpHotbarModels(player);
      }
   }

   public static void warmUpBackpackModels() {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null) {
         warmUpBackpackModels(player);
      }
   }

   private static void warmUpHotbarModels(LocalPlayer player) {
      NonNullList<ItemStack> items = player.getInventory().getNonEquipmentItems();
      int hotbarSize = Math.min(9, items.size());

      for (int i = 0; i < hotbarSize; i++) {
         warmUpItemModel((ItemStack)items.get(i));
      }
   }

   private static void warmUpBackpackModels(LocalPlayer player) {
      NonNullList<ItemStack> items = player.getInventory().getNonEquipmentItems();

      for (int i = Math.min(9, items.size()); i < items.size(); i++) {
         warmUpItemModel((ItemStack)items.get(i));
      }
   }

   public static void warmUpItem(ItemStack stack) {
      warmUpItemForUse(stack);
   }

   public static void warmUpItemModel(ItemStack stack) {
      if (!stack.isEmpty()) {
         if (stack.getItem() instanceof IGun) {
            TimelessAPI.getGunDisplay(stack).ifPresent(display -> {
               display.warmUpLod();
               display.warmUpModel();
               display.warmUpRuntime();
            });
         } else {
            IAttachment attachment = IAttachment.getIAttachmentOrNull(stack);
            if (attachment != null) {
               TimelessAPI.getClientAttachmentIndex(attachment.getAttachmentId(stack)).ifPresent(ClientAttachmentIndex::warmUp);
            } else {
               IAmmo ammo = IAmmo.getIAmmoOrNull(stack);
               if (ammo != null) {
                  TimelessAPI.getClientAmmoIndex(ammo.getAmmoId(stack)).ifPresent(ClientAmmoIndex::warmUp);
               }
            }
         }
      }
   }

   public static void warmUpItemForUse(ItemStack stack) {
      if (!stack.isEmpty()) {
         if (stack.getItem() instanceof IGun) {
            TimelessAPI.getGunDisplay(stack).ifPresent(display -> {
               display.warmUpLod();
               display.warmUpModel();
               display.warmUpRuntime();
            });
         } else {
            warmUpItemModel(stack);
         }
      }
   }
}
