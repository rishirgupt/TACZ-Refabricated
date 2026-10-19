package com.tacz.guns.client.event;

import cn.sh1rocu.tacz.api.event.ClientPlayerNetworkEvent;
import com.tacz.guns.api.client.event.SwapItemWithOffHand;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.item.IAnimationItem;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.resource.ClientIndexManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

@Environment(EnvType.CLIENT)
public class InventoryEvent {
   private static final int HOTBAR_WARM_UP_INTERVAL_TICKS = 7;
   private static final int BACKPACK_WARM_UP_INTERVAL_TICKS = 41;
   private static int oldHotbarSelected = -1;
   private static ItemStack oldHotbarSelectItem = ItemStack.EMPTY;

   public static void onPlayerChangeSelect(Minecraft client, boolean isPhaseEnd) {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null) {
         Inventory inventory = player.getInventory();
         if (oldHotbarSelected != inventory.getSelectedSlot()) {
            ClientIndexManager.warmUpItem(inventory.getItem(inventory.getSelectedSlot()));
            if (oldHotbarSelected == -1) {
               IClientPlayerGunOperator.fromLocalPlayer(player).draw(ItemStack.EMPTY);
            } else {
               IClientPlayerGunOperator.fromLocalPlayer(player).draw(inventory.getItem(oldHotbarSelected));
            }

            oldHotbarSelected = inventory.getSelectedSlot();
            oldHotbarSelectItem = inventory.getItem(inventory.getSelectedSlot()).copy();
         } else {
            ItemStack currentItem = inventory.getItem(inventory.getSelectedSlot());
            if (currentItem.getItem() instanceof IAnimationItem item) {
               if (!item.isSame(oldHotbarSelectItem, currentItem)) {
                  IClientPlayerGunOperator.fromLocalPlayer(player).draw(oldHotbarSelectItem);
               }
            } else if (!ItemStack.matches(oldHotbarSelectItem, currentItem)) {
               IClientPlayerGunOperator.fromLocalPlayer(player).draw(oldHotbarSelectItem);
            }

            if (!ItemStack.matches(oldHotbarSelectItem, currentItem)) {
               oldHotbarSelectItem = currentItem.copy();
            }

            if (isPhaseEnd) {
               if (player.tickCount % 7 == 0) {
                  ClientIndexManager.warmUpEquippedAndHotbarModels();
               }

               if (player.tickCount % 41 == 0) {
                  ClientIndexManager.warmUpBackpackModels();
               }
            }
         }
      }
   }

   public static void onPlayerSwapMainHand(SwapItemWithOffHand event) {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null) {
         IClientPlayerGunOperator.fromLocalPlayer(player).draw(player.getMainHandItem());
      }
   }

   public static void onPlayerLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
      oldHotbarSelected = -1;
      oldHotbarSelectItem = ItemStack.EMPTY;
   }

   private static boolean isSame(ItemStack i, ItemStack j) {
      IGun iGun1 = IGun.getIGunOrNull(i);
      IGun iGun2 = IGun.getIGunOrNull(j);
      if (iGun1 != null && iGun2 != null) {
         return iGun1.getGunId(i).equals(iGun2.getGunId(j));
      } else {
         return !i.isEmpty() && !j.isEmpty() ? ItemStack.matches(i, j) : i.isEmpty() && j.isEmpty();
      }
   }
}
