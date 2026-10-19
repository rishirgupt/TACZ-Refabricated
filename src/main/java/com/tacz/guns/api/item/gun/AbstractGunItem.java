package com.tacz.guns.api.item.gun;

import cn.sh1rocu.tacz.api.extension.IItem;
import cn.sh1rocu.tacz.util.itemhandler.IItemHandler;
import cn.sh1rocu.tacz.util.itemhandler.ItemHandlerHelper;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.item.GunTabType;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IAnimationItem;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.client.renderer.item.GunItemRendererWrapper;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import com.tacz.guns.inventory.tooltip.GunTooltip;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.FeedType;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.util.AllowAttachmentTagMatcher;
import com.tacz.guns.util.AttachmentDataUtils;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.core.HolderLookup.Provider;

public abstract class AbstractGunItem extends Item implements IGun, IAnimationItem, IItem {
   protected AbstractGunItem(Properties pProperties) {
      super(pProperties);
   }

   private static Comparator<Entry<Identifier, CommonGunIndex>> idNameSort() {
      return Comparator.comparingInt(m -> m.getValue().getSort());
   }

   public abstract boolean startBolt(ShooterDataHolder var1, ItemStack var2, LivingEntity var3);

   public abstract boolean tickBolt(ShooterDataHolder var1, ItemStack var2, LivingEntity var3);

   public abstract void shoot(ShooterDataHolder var1, ItemStack var2, Supplier<Float> var3, Supplier<Float> var4, LivingEntity var5);

   public abstract boolean startReload(ShooterDataHolder var1, ItemStack var2, LivingEntity var3);

   public abstract ReloadState tickReload(ShooterDataHolder var1, ItemStack var2, LivingEntity var3);

   public abstract void interruptReload(ShooterDataHolder var1, ItemStack var2, LivingEntity var3);

   public abstract void fireSelect(ShooterDataHolder var1, ItemStack var2);

   public abstract void melee(ShooterDataHolder var1, LivingEntity var2, ItemStack var3);

   public void tickHeat(ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter) {
   }

   public void doBulletSpread(
      ShooterDataHolder dataHolder,
      ItemStack gunItem,
      LivingEntity shooter,
      Projectile projectile,
      int bulletCnt,
      float processedSpeed,
      float inaccuracy,
      float pitch,
      float yaw
   ) {
      projectile.shootFromRotation(shooter, pitch, yaw, 0.0F, processedSpeed, inaccuracy);
   }

   public boolean canReload(LivingEntity shooter, ItemStack gunItem) {
      Identifier gunId = this.getGunId(gunItem);
      CommonGunIndex gunIndex = TimelessAPI.getCommonGunIndex(gunId).orElse(null);
      if (gunIndex == null) {
         return false;
      } else {
         int currentAmmoCount = this.getCurrentAmmoCount(gunItem);
         int maxAmmoCount = AttachmentDataUtils.getAmmoCountWithAttachment(gunItem, gunIndex.getGunData());
         if (currentAmmoCount >= maxAmmoCount) {
            return false;
         } else if (this.useInventoryAmmo(gunItem)) {
            return false;
         } else if (gunIndex.getGunData().getReloadData().isInfinite()) {
            return true;
         } else {
            return this.useDummyAmmo(gunItem) ? this.getDummyAmmoAmount(gunItem) > 0 : shooter.tacz$getItemHandler(null).map(cap -> {
               for (int i = 0; i < cap.getSlots(); i++) {
                  ItemStack checkAmmoStack = cap.getStackInSlot(i);
                  if (checkAmmoStack.getItem() instanceof IAmmo iAmmo && iAmmo.isAmmoOfGun(gunItem, checkAmmoStack)) {
                     return true;
                  }

                  if (checkAmmoStack.getItem() instanceof IAmmoBox iAmmoBox && iAmmoBox.isAmmoBoxOfGun(gunItem, checkAmmoStack)) {
                     return true;
                  }
               }

               return false;
            }).orElse(false);
         }
      }
   }

   @Override
   public void dropAllAmmo(Player player, ItemStack gunItem) {
      if (!this.useInventoryAmmo(gunItem)) {
         int ammoCount = this.getCurrentAmmoCount(gunItem);
         if (ammoCount > 0) {
            Identifier gunId = this.getGunId(gunItem);
            TimelessAPI.getCommonGunIndex(gunId).ifPresent(index -> {
               if (this.useDummyAmmo(gunItem)) {
                  this.setCurrentAmmoCount(gunItem, 0);
                  if (!index.getGunData().getReloadData().getType().equals(FeedType.FUEL)) {
                     this.addDummyAmmoAmount(gunItem, ammoCount);
                  }
               } else {
                  Identifier ammoId = index.getGunData().getAmmoId();
                  if (player.isCreative()) {
                     int maxAmmCount = AttachmentDataUtils.getAmmoCountWithAttachment(gunItem, index.getGunData());
                     this.setCurrentAmmoCount(gunItem, maxAmmCount);
                  } else if (index.getGunData().getReloadData().getType().equals(FeedType.FUEL)) {
                     this.setCurrentAmmoCount(gunItem, 0);
                  } else {
                     TimelessAPI.getCommonAmmoIndex(ammoId).ifPresent(ammoIndex -> {
                        int stackSize = ammoIndex.getStackSize();
                        int tmpAmmoCount = ammoCount;
                        int roundCount = tmpAmmoCount / (stackSize + 1);

                        for (int i = 0; i <= roundCount; i++) {
                           int count = Math.min(tmpAmmoCount, stackSize);
                           ItemStack ammoItem = AmmoItemBuilder.create().setId(ammoId).setCount(count).build();
                           ItemHandlerHelper.giveItemToPlayer(player, ammoItem);
                           tmpAmmoCount -= stackSize;
                        }

                        this.setCurrentAmmoCount(gunItem, 0);
                     });
                  }
               }
            });
         }
      }
   }

   @Deprecated
   public int findAndExtractInventoryAmmos(IItemHandler itemHandler, ItemStack gunItem, int needAmmoCount) {
      return this.findAndExtractInventoryAmmo(itemHandler, gunItem, needAmmoCount);
   }

   public int findAndExtractInventoryAmmo(IItemHandler itemHandler, ItemStack gunItem, int needAmmoCount) {
      int cnt = needAmmoCount;

      for (int i = 0; i < itemHandler.getSlots(); i++) {
         ItemStack checkAmmoStack = itemHandler.getStackInSlot(i);
         if (checkAmmoStack.getItem() instanceof IAmmo iAmmo && iAmmo.isAmmoOfGun(gunItem, checkAmmoStack)) {
            ItemStack extractItem = itemHandler.extractItem(i, cnt, false);
            cnt -= extractItem.getCount();
            if (cnt <= 0) {
               break;
            }
         }

         if (checkAmmoStack.getItem() instanceof IAmmoBox iAmmoBox && iAmmoBox.isAmmoBoxOfGun(gunItem, checkAmmoStack)) {
            int boxAmmoCount = iAmmoBox.getAmmoCount(checkAmmoStack);
            int extractCount = Math.min(boxAmmoCount, cnt);
            int remainCount = boxAmmoCount - extractCount;
            iAmmoBox.setAmmoCount(checkAmmoStack, remainCount);
            if (remainCount <= 0) {
               iAmmoBox.setAmmoId(checkAmmoStack, DefaultAssets.EMPTY_AMMO_ID);
            }

            cnt -= extractCount;
            if (cnt <= 0) {
               break;
            }
         }
      }

      return needAmmoCount - cnt;
   }

   public int findAndExtractDummyAmmo(ItemStack gunItem, int needAmmoCount) {
      int dummyAmmoCount = this.getDummyAmmoAmount(gunItem);
      int extractCount = Math.min(dummyAmmoCount, needAmmoCount);
      this.addDummyAmmoAmount(gunItem, -extractCount);
      return extractCount;
   }

   @Override
   public boolean allowAttachment(ItemStack gun, ItemStack attachmentItem) {
      IAttachment iAttachment = IAttachment.getIAttachmentOrNull(attachmentItem);
      IGun iGun = IGun.getIGunOrNull(gun);
      if (iGun != null && iAttachment != null) {
         Identifier gunId = iGun.getGunId(gun);
         Identifier attachmentId = iAttachment.getAttachmentId(attachmentItem);
         return AllowAttachmentTagMatcher.match(gunId, attachmentId);
      } else {
         return false;
      }
   }

   @Override
   public boolean allowAttachmentType(ItemStack gun, AttachmentType type) {
      IGun iGun = IGun.getIGunOrNull(gun);
      return iGun != null ? TimelessAPI.getCommonGunIndex(iGun.getGunId(gun)).map(gunIndex -> {
         List<AttachmentType> allowAttachments = gunIndex.getGunData().getAllowAttachments();
         return allowAttachments == null ? false : allowAttachments.contains(type);
      }).orElse(false) : false;
   }

   @Nonnull
   @Environment(EnvType.CLIENT)
   public Component getName(@Nonnull ItemStack stack) {
      Identifier gunId = this.getGunId(stack);
      Optional<ClientGunIndex> gunIndex = TimelessAPI.getClientGunIndex(gunId);
      return (Component)(gunIndex.isPresent() ? Component.translatable(gunIndex.get().getName()) : super.getName(stack));
   }

   public static NonNullList<ItemStack> fillItemCategory(GunTabType type, Provider provider) {
      NonNullList<ItemStack> stacks = NonNullList.create();
      TimelessAPI.getAllCommonGunIndex()
         .stream()
         .sorted(idNameSort())
         .forEach(
            entry -> {
               CommonGunIndex index = entry.getValue();
               GunData gunData = index.getGunData();
               String key = type.name().toLowerCase(Locale.US);
               String indexType = index.getType();
               if (key.equals(indexType)) {
                  ItemStack itemStack = GunItemBuilder.create()
                     .setId(entry.getKey())
                     .setFireMode(gunData.getFireModeSet().getFirst())
                     .setAmmoCount(gunData.getAmmoAmount())
                     .setHeatData(gunData.hasHeatData())
                     .setAmmoInBarrel(true)
                     .build(provider);
                  stacks.add(itemStack);
               }
            }
         );
      return stacks;
   }

   @Override
   public boolean tacz$onEntitySwing(ItemStack stack, LivingEntity entity) {
      return true;
   }

   @Nonnull
   public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
      if (stack.getItem() instanceof IGun iGun) {
         Optional<CommonGunIndex> optional = TimelessAPI.getCommonGunIndex(this.getGunId(stack));
         if (optional.isPresent()) {
            CommonGunIndex gunIndex = optional.get();
            Identifier ammoId = gunIndex.getGunData().getAmmoId();
            return Optional.of(new GunTooltip(stack, iGun, ammoId, gunIndex));
         }
      }

      return Optional.empty();
   }

   @Override
   public boolean useInventoryAmmo(ItemStack gun) {
      if (gun.getItem() instanceof IGun) {
         Optional<CommonGunIndex> gunIndexOptional = TimelessAPI.getCommonGunIndex(this.getGunId(gun));
         if (gunIndexOptional.isEmpty()) {
            return false;
         }

         CommonGunIndex gunIndex = gunIndexOptional.get();
         return gunIndex.getGunData().getReloadData().getType().equals(FeedType.INVENTORY);
      } else {
         return false;
      }
   }

   @Override
   public boolean hasInventoryAmmo(LivingEntity shooter, ItemStack gun, boolean needCheckAmmo) {
      if (!this.useInventoryAmmo(gun)) {
         return false;
      } else if (!needCheckAmmo) {
         return true;
      } else {
         return this.useDummyAmmo(gun) ? this.getDummyAmmoAmount(gun) > 0 : shooter.tacz$getItemHandler(null).map(cap -> {
            for (int i = 0; i < cap.getSlots(); i++) {
               ItemStack checkAmmoStack = cap.getStackInSlot(i);
               if (checkAmmoStack.getItem() instanceof IAmmo iAmmo && iAmmo.isAmmoOfGun(gun, checkAmmoStack)) {
                  return true;
               }

               if (checkAmmoStack.getItem() instanceof IAmmoBox iAmmoBox && iAmmoBox.isAmmoBoxOfGun(gun, checkAmmoStack)) {
                  return true;
               }
            }

            return false;
         }).orElse(false);
      }
   }

   @Override
   public int getRPM(ItemStack gun) {
      return gun.getItem() instanceof IGun iGun ? TimelessAPI.getCommonGunIndex(this.getGunId(gun)).map(CommonGunIndex::getGunData).map(gunData -> {
         FireMode fireMode = this.getFireMode(gun);
         int rpm = gunData.getRoundsPerMinute(fireMode);
         if (iGun.hasHeatData(gun)) {
            rpm *= (int)iGun.lerpRPM(gun);
         }

         return rpm;
      }).orElse(300) : 300;
   }

   @Override
   public boolean isCanCrawl(ItemStack gun) {
      return gun.getItem() instanceof IGun
         ? TimelessAPI.getCommonGunIndex(this.getGunId(gun)).map(CommonGunIndex::getGunData).map(GunData::isCanCrawl).orElse(false)
         : false;
   }

   @Override
   public boolean isSame(ItemStack i, ItemStack j) {
      IGun iGun1 = IGun.getIGunOrNull(i);
      IGun iGun2 = IGun.getIGunOrNull(j);
      if (iGun1 != null && iGun2 != null) {
         return iGun1.getGunId(i).equals(iGun2.getGunId(j)) && iGun1.getGunDisplayId(i).equals(iGun2.getGunDisplayId(j));
      } else {
         return !i.isEmpty() && !j.isEmpty() ? ItemStack.matches(i, j) : i.isEmpty() && j.isEmpty();
      }
   }
}
