package com.tacz.guns.item;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.api.item.nbt.AmmoBoxItemDataAccessor;
import com.tacz.guns.config.sync.SyncConfig;
import com.tacz.guns.init.ModItems;
import com.tacz.guns.inventory.tooltip.AmmoBoxTooltip;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;

public class AmmoBoxItem extends Item implements AmmoBoxItemDataAccessor {
   public static final Identifier PROPERTY_NAME = Identifier.fromNamespaceAndPath("tacz", "ammo_statue");
   public static final int IRON_LEVEL = 0;
   public static final int GOLD_LEVEL = 1;
   public static final int DIAMOND_LEVEL = 2;
   private static final String DISPLAY_TAG = "display";
   private static final String COLOR_TAG = "color";
   private static final int OPEN = 0;
   private static final int CLOSE = 1;
   private static final int CREATIVE_INDEX = 6;
   private static final int ALL_TYPE_CREATIVE_INDEX = 8;

   public AmmoBoxItem() {
      super(new Properties().stacksTo(1));
   }

   @Environment(EnvType.CLIENT)
   public static int getColor(ItemStack stack, int tintIndex) {
      return tintIndex > 0 ? -1 : DyedItemColor.getOrDefault(stack, -9274005);
   }

   @Environment(EnvType.CLIENT)
   public static float getStatue(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
      int openStatue = 0;
      int ammoLevel = 0;
      if (stack.getItem() instanceof IAmmoBox iAmmoBox) {
         if (iAmmoBox.isAllTypeCreative(stack)) {
            return 8.0F;
         }

         openStatue = getOpenStatue(stack, iAmmoBox);
         if (iAmmoBox.isCreative(stack)) {
            return openStatue + 6;
         }

         ammoLevel = getLevelStatue(stack, iAmmoBox);
      }

      return openStatue + 2 * ammoLevel;
   }

   private static int getOpenStatue(ItemStack stack, IAmmoBox iAmmoBox) {
      boolean idIsEmpty = iAmmoBox.getAmmoId(stack).equals(DefaultAssets.EMPTY_AMMO_ID);
      boolean countIsZero = iAmmoBox.getAmmoCount(stack) <= 0;
      return !idIsEmpty && !countIsZero ? 1 : 0;
   }

   private static int getLevelStatue(ItemStack stack, IAmmoBox iAmmoBox) {
      return iAmmoBox.getAmmoLevel(stack);
   }

   public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack pOther, Slot slot, ClickAction action, Player player, SlotAccess access) {
      return super.overrideOtherStackedOnMe(stack, pOther, slot, action, player, access);
   }

   public boolean overrideStackedOnOther(ItemStack ammoBox, Slot slot, ClickAction action, Player player) {
      if (action == ClickAction.SECONDARY) {
         ItemStack slotItem = slot.getItem();
         Identifier boxAmmoId = this.getAmmoId(ammoBox);
         if (slotItem.isEmpty()) {
            if (!this.isAllTypeCreative(ammoBox) && !this.isCreative(ammoBox)) {
               if (boxAmmoId.equals(DefaultAssets.EMPTY_AMMO_ID)) {
                  return false;
               }

               int boxAmmoCount = this.getAmmoCount(ammoBox);
               if (boxAmmoCount <= 0) {
                  return false;
               }

               return TimelessAPI.getCommonAmmoIndex(boxAmmoId).map(index -> {
                  int takeCount = Math.min(index.getStackSize(), boxAmmoCount);
                  ItemStack takeAmmo = AmmoItemBuilder.create().setId(boxAmmoId).setCount(takeCount).build();
                  ItemStack remainingAmmo = slot.safeInsert(takeAmmo);
                  int insertedCount = takeCount - remainingAmmo.getCount();
                  if (insertedCount <= 0) {
                     return false;
                  }

                  int remainCount = boxAmmoCount - insertedCount;
                  this.setAmmoCount(ammoBox, remainCount);
                  if (remainCount <= 0) {
                     this.setAmmoId(ammoBox, DefaultAssets.EMPTY_AMMO_ID);
                  }

                  this.playRemoveOneSound(player);
                  return true;
               }).orElse(false);
            }

            return false;
         }

         if (slotItem.getItem() instanceof IAmmo iAmmo) {
            if (this.isAllTypeCreative(ammoBox)) {
               return false;
            }

            Identifier slotAmmoId = iAmmo.getAmmoId(slotItem);
            if (slotAmmoId.equals(DefaultAssets.EMPTY_AMMO_ID)) {
               return false;
            }

            if (boxAmmoId.equals(DefaultAssets.EMPTY_AMMO_ID)) {
               this.setAmmoId(ammoBox, slotAmmoId);
            } else if (!slotAmmoId.equals(boxAmmoId)) {
               return false;
            }

            TimelessAPI.getCommonAmmoIndex(slotAmmoId).ifPresent(index -> {
               if (this.isCreative(ammoBox)) {
                  this.setAmmoCount(ammoBox, Integer.MAX_VALUE);
               } else {
                  int boxAmmoCount = this.getAmmoCount(ammoBox);
                  int boxLevelMultiplier = this.getAmmoLevel(ammoBox) + 1;
                  int maxSize = index.getStackSize() * (Integer)SyncConfig.AMMO_BOX_STACK_SIZE.get() * boxLevelMultiplier;
                  int needCount = maxSize - boxAmmoCount;
                  ItemStack takeItem = slot.safeTake(slotItem.getCount(), needCount, player);
                  this.setAmmoCount(ammoBox, boxAmmoCount + takeItem.getCount());
               }
            });
            this.playInsertSound(player);
            return true;
         }
      }

      return false;
   }

   private void playRemoveOneSound(Entity entity) {
      entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
   }

   private void playInsertSound(Entity entity) {
      entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
   }

   public boolean isBarVisible(ItemStack stack) {
      return !this.isAllTypeCreative(stack) && !this.isCreative(stack)
         ? !this.getAmmoId(stack).equals(DefaultAssets.EMPTY_AMMO_ID) && this.getAmmoCount(stack) > 0
         : false;
   }

   public int getBarWidth(ItemStack stack) {
      Identifier ammoId = this.getAmmoId(stack);
      int ammoCount = this.getAmmoCount(stack);
      int boxLevelMultiplier = this.getAmmoLevel(stack) + 1;
      double widthPercent = TimelessAPI.getCommonAmmoIndex(ammoId).map(index -> {
         double totalCount = index.getStackSize() * (Integer)SyncConfig.AMMO_BOX_STACK_SIZE.get() * boxLevelMultiplier;
         return ammoCount / totalCount;
      }).orElse(0.0);
      return (int)Math.min(1.0 + 12.0 * widthPercent, 13.0);
   }

   public Component getName(ItemStack stack) {
      if (this.isAllTypeCreative(stack)) {
         return Component.translatable("item.tacz.ammo_box.all_type_creative").withStyle(ChatFormatting.DARK_PURPLE);
      }

      if (this.isCreative(stack)) {
         return Component.translatable("item.tacz.ammo_box.creative").withStyle(ChatFormatting.DARK_PURPLE);
      }

      int ammoLevel = this.getAmmoLevel(stack);
      switch (ammoLevel) {
         case 1:
            return Component.translatable("item.tacz.ammo_box.gold").withStyle(ChatFormatting.YELLOW);
         case 2:
            return Component.translatable("item.tacz.ammo_box.diamond").withStyle(ChatFormatting.AQUA);
         default:
            return Component.translatable("item.tacz.ammo_box.iron");
      }
   }

   public boolean isFoil(ItemStack stack) {
      return !this.isAllTypeCreative(stack) && !this.isCreative(stack) ? super.isFoil(stack) : true;
   }

   public int getBarColor(ItemStack stack) {
      return Mth.hsvToRgb(0.33333334F, 1.0F, 1.0F);
   }

   public static void fillItemCategory(Output output) {
      ItemStack ammoBox = ModItems.AMMO_BOX.getDefaultInstance();
      if (ammoBox.getItem() instanceof IAmmoBox iAmmoBox) {
         output.accept(iAmmoBox.setAmmoLevel(ammoBox.copy(), 0));
         output.accept(iAmmoBox.setAmmoLevel(ammoBox.copy(), 1));
         output.accept(iAmmoBox.setAmmoLevel(ammoBox.copy(), 2));
         output.accept(iAmmoBox.setCreative(ammoBox.copy(), false));
         output.accept(iAmmoBox.setCreative(ammoBox.copy(), true));
      }
   }

   public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
      if (stack.getItem() instanceof IAmmoBox iAmmoBox) {
         Identifier ammoId = iAmmoBox.getAmmoId(stack);
         if (ammoId.equals(DefaultAssets.EMPTY_AMMO_ID)) {
            return Optional.empty();
         }

         int ammoCount = iAmmoBox.getAmmoCount(stack);
         if (ammoCount <= 0) {
            return Optional.empty();
         }

         ItemStack ammoStack = AmmoItemBuilder.create().setId(ammoId).build();
         return Optional.of(new AmmoBoxTooltip(stack, ammoStack, ammoCount));
      } else {
         return Optional.empty();
      }
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag isAdvanced) {
      if (this.isAllTypeCreative(stack)) {
         components.add(Component.translatable("tooltip.tacz.ammo_box.usage.all_type_creative").withStyle(ChatFormatting.GOLD));
      } else if (this.isCreative(stack)) {
         components.add(Component.translatable("tooltip.tacz.ammo_box.usage.creative.1").withStyle(ChatFormatting.YELLOW));
         components.add(Component.translatable("tooltip.tacz.ammo_box.usage.creative.2").withStyle(ChatFormatting.YELLOW));
      } else {
         components.add(Component.translatable("tooltip.tacz.ammo_box.usage.deposit").withStyle(ChatFormatting.GRAY));
         components.add(Component.translatable("tooltip.tacz.ammo_box.usage.remove").withStyle(ChatFormatting.GRAY));
      }
   }
}
