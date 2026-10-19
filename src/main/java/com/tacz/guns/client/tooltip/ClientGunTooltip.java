package com.tacz.guns.client.tooltip;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.client.input.RefitKey;
import com.tacz.guns.client.resource.ClientAssetsManager;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.pojo.PackInfo;
import com.tacz.guns.client.resource.pojo.display.gun.AmmoCountStyle;
import com.tacz.guns.client.resource.pojo.display.gun.DamageStyle;
import com.tacz.guns.config.sync.SyncConfig;
import com.tacz.guns.inventory.tooltip.GunTooltip;
import com.tacz.guns.item.GunTooltipPart;
import com.tacz.guns.item.ModernKineticGunItem;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.resource.pojo.data.gun.BulletData;
import com.tacz.guns.resource.pojo.data.gun.ExtraDamage;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.util.AllowAttachmentTagMatcher;
import com.tacz.guns.util.AttachmentDataUtils;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import org.jetbrains.annotations.Nullable;

public class ClientGunTooltip implements ClientTooltipComponent {
   private static final DecimalFormat FORMAT = new DecimalFormat("#.##%");
   private static final DecimalFormat FORMAT_P_D1 = new DecimalFormat("#.#%");
   private static final DecimalFormat DAMAGE_FORMAT = new DecimalFormat("#.##");
   private static final DecimalFormat CURRENT_AMMO_FORMAT_PERCENT = new DecimalFormat("0%");
   private final ItemStack gun;
   private final IGun iGun;
   private final CommonGunIndex gunIndex;
   @Nullable
   private final GunDisplayInstance display;
   private final ItemStack ammo;
   @Nullable
   private List<FormattedCharSequence> desc;
   private Component ammoName;
   private MutableComponent ammoCountText;
   @Nullable
   private MutableComponent gunType;
   private MutableComponent damage;
   private MutableComponent armorIgnore;
   private MutableComponent headShotMultiplier;
   private MutableComponent weight;
   private MutableComponent tips;
   private MutableComponent levelInfo;
   @Nullable
   private MutableComponent packInfo;
   private int maxWidth;

   public ClientGunTooltip(GunTooltip tooltip) {
      this.gun = tooltip.getGun();
      this.iGun = tooltip.getIGun();
      Identifier ammoId = tooltip.getAmmoId();
      this.gunIndex = tooltip.getGunIndex();
      this.display = TimelessAPI.getGunDisplay(this.gun).orElse(null);
      this.ammo = AmmoItemBuilder.create().setId(ammoId).build();
      this.maxWidth = 0;
      this.getText();
   }

   public int getHeight(Font font) {
      int height = 0;
      if (this.shouldShow(GunTooltipPart.DESCRIPTION) && this.desc != null) {
         height += 10 * this.desc.size() + 2;
      }

      if (this.shouldShow(GunTooltipPart.AMMO_INFO)) {
         height += 24;
      }

      if (this.shouldShow(GunTooltipPart.BASE_INFO)) {
         height += 34;
      }

      if (this.shouldShow(GunTooltipPart.EXTRA_DAMAGE_INFO)) {
         height += 34;
      }

      if (this.shouldShow(GunTooltipPart.UPGRADES_TIP)) {
         height += 14;
      }

      if (this.shouldShow(GunTooltipPart.PACK_INFO)) {
         height += 14;
      }

      return height;
   }

   public int getWidth(Font font) {
      return this.maxWidth;
   }

   private void getText() {
      Font font = Minecraft.getInstance().font;
      BulletData bulletData = this.gunIndex.getBulletData();
      GunData gunData = this.gunIndex.getGunData();
      if (this.shouldShow(GunTooltipPart.DESCRIPTION)) {
         String tooltip = this.gunIndex.getPojo().getTooltip();
         if (tooltip != null) {
            List<FormattedCharSequence> split = font.split(Component.translatable(tooltip), 300);
            if (split.size() > 3) {
               this.desc = split.subList(0, 3);
            } else {
               this.desc = split;
            }

            for (FormattedCharSequence sequence : this.desc) {
               this.maxWidth = Math.max(font.width(sequence), this.maxWidth);
            }
         }
      }

      if (this.shouldShow(GunTooltipPart.AMMO_INFO)) {
         this.ammoName = this.ammo.getHoverName();
         this.maxWidth = Math.max(font.width(this.ammoName) + 22, this.maxWidth);
         int barrelBulletAmount = this.iGun.hasBulletInBarrel(this.gun) && this.gunIndex.getGunData().getBolt() != Bolt.OPEN_BOLT ? 1 : 0;
         int maxAmmoCount = AttachmentDataUtils.getAmmoCountWithAttachment(this.gun, this.gunIndex.getGunData()) + barrelBulletAmount;
         int currentAmmoCount = this.iGun.getCurrentAmmoCount(this.gun) + barrelBulletAmount;
         if (!this.iGun.useDummyAmmo(this.gun)) {
            if (this.display != null && this.display.getAmmoCountStyle() == AmmoCountStyle.PERCENT) {
               this.ammoCountText = Component.literal(CURRENT_AMMO_FORMAT_PERCENT.format(currentAmmoCount / (maxAmmoCount == 0 ? 1.0F : maxAmmoCount)));
            } else {
               this.ammoCountText = Component.literal("%d/%d".formatted(currentAmmoCount, maxAmmoCount));
            }
         } else {
            int dummyAmmoAmount = this.iGun.getDummyAmmoAmount(this.gun);
            if (this.display != null && this.display.getAmmoCountStyle() == AmmoCountStyle.PERCENT) {
               String p = CURRENT_AMMO_FORMAT_PERCENT.format(currentAmmoCount / (maxAmmoCount == 0 ? 1.0F : maxAmmoCount));
               this.ammoCountText = Component.literal("%s (%d)".formatted(p, dummyAmmoAmount));
            } else {
               this.ammoCountText = Component.literal("%d/%d (%d)".formatted(currentAmmoCount, maxAmmoCount, dummyAmmoAmount));
            }
         }

         if (this.iGun.useInventoryAmmo(this.gun)) {
            this.ammoCountText = Component.translatable("tooltip.tacz.gun.inventory_mode").withStyle(ChatFormatting.YELLOW);
         }

         this.maxWidth = Math.max(font.width(this.ammoCountText) + 22, this.maxWidth);
      }

      if (this.shouldShow(GunTooltipPart.BASE_INFO)) {
         int expToNextLevel = this.iGun.getExpToNextLevel(this.gun);
         int expCurrentLevel = this.iGun.getExpCurrentLevel(this.gun);
         int level = this.iGun.getLevel(this.gun);
         if (level >= this.iGun.getMaxLevel()) {
            String levelText = String.format("%d (MAX)", level);
            this.levelInfo = Component.translatable("tooltip.tacz.gun.level")
               .append(Component.literal(levelText).withStyle(ChatFormatting.DARK_PURPLE));
         } else {
            String levelText = String.format("%d (%.1f%%)", level, expCurrentLevel / (expToNextLevel + expCurrentLevel) * 100.0F);
            this.levelInfo = Component.translatable("tooltip.tacz.gun.level")
               .append(Component.literal(levelText).withStyle(ChatFormatting.YELLOW));
         }

         this.maxWidth = Math.max(font.width(this.levelInfo), this.maxWidth);
         String tabKey = "tacz.type." + this.gunIndex.getType() + ".name";
         this.gunType = Component.translatable("tooltip.tacz.gun.type").append(Component.translatable(tabKey).withStyle(ChatFormatting.AQUA));
         this.maxWidth = Math.max(font.width(this.gunType), this.maxWidth);
         double damage = AttachmentDataUtils.getDamageWithAttachment(this.gun, gunData);
         boolean hasSlugInstalled = AllowAttachmentTagMatcher.matchTag(
            ModernKineticGunItem.DefaultPropertyModification.SLUGS, this.iGun.getAttachmentId(this.gun, AttachmentType.EXTENDED_MAG)
         );
         int bulletAmount = hasSlugInstalled ? 1 : gunData.getBulletData().getBulletAmount();
         MutableComponent value;
         if (this.display != null && this.display.getDamageStyle() == DamageStyle.PER_PROJECTILE && bulletAmount > 1) {
            value = Component.literal(DAMAGE_FORMAT.format(damage / bulletAmount) + "x" + bulletAmount).withStyle(ChatFormatting.AQUA);
         } else {
            value = Component.literal(DAMAGE_FORMAT.format(damage)).withStyle(ChatFormatting.AQUA);
         }

         if (bulletData.getExplosionData() != null && (AttachmentDataUtils.isExplodeEnabled(this.gun, gunData) || bulletData.getExplosionData().isExplode())) {
            value.append(" + ")
               .append(DAMAGE_FORMAT.format(bulletData.getExplosionData().getDamage() * (Double)SyncConfig.DAMAGE_BASE_MULTIPLIER.get()))
               .append(Component.translatable("tooltip.tacz.gun.explosion"));
         }

         this.damage = Component.translatable("tooltip.tacz.gun.damage").append(value);
         this.maxWidth = Math.max(font.width(this.damage), this.maxWidth);
      }

      if (this.shouldShow(GunTooltipPart.EXTRA_DAMAGE_INFO)) {
         ExtraDamage extraDamage = bulletData.getExtraDamage();
         if (extraDamage != null) {
            double armorDamagePercent = AttachmentDataUtils.getArmorIgnoreWithAttachment(this.gun, gunData);
            double headShotMultiplierPercent = AttachmentDataUtils.getHeadshotMultiplier(this.gun, gunData);
            armorDamagePercent = Mth.clamp(armorDamagePercent, 0.0, 1.0);
            this.armorIgnore = Component.translatable("tooltip.tacz.gun.armor_ignore", new Object[]{FORMAT.format(armorDamagePercent)});
            this.headShotMultiplier = Component.translatable("tooltip.tacz.gun.head_shot_multiplier", new Object[]{FORMAT.format(headShotMultiplierPercent)});
         } else {
            this.armorIgnore = Component.translatable("tooltip.tacz.gun.armor_ignore", new Object[]{FORMAT.format(0L)});
            this.headShotMultiplier = Component.translatable("tooltip.tacz.gun.head_shot_multiplier", new Object[]{FORMAT.format(1L)});
         }

         double weightFactor = (Double)SyncConfig.WEIGHT_SPEED_MULTIPLIER.get();
         double weight = AttachmentDataUtils.getWightWithAttachment(this.gun, gunData);
         this.weight = Component.translatable("tooltip.tacz.gun.movement_speed", new Object[]{FORMAT_P_D1.format(-weightFactor * weight)})
            .withStyle(ChatFormatting.RED);
         this.maxWidth = Math.max(font.width(this.armorIgnore), this.maxWidth);
         this.maxWidth = Math.max(font.width(this.headShotMultiplier), this.maxWidth);
         this.maxWidth = Math.max(font.width(this.weight), this.maxWidth);
      }

      if (this.shouldShow(GunTooltipPart.UPGRADES_TIP)) {
         String keyName = Component.keybind(RefitKey.REFIT_KEY.getName()).getString().toUpperCase(Locale.ENGLISH);
         this.tips = Component.translatable("tooltip.tacz.gun.tips", new Object[]{keyName})
            .withStyle(ChatFormatting.YELLOW)
            .withStyle(ChatFormatting.ITALIC);
         this.maxWidth = Math.max(font.width(this.tips), this.maxWidth);
      }

      if (this.shouldShow(GunTooltipPart.PACK_INFO)) {
         Identifier gunId = this.iGun.getGunId(this.gun);
         PackInfo packInfoObject = ClientAssetsManager.INSTANCE.getPackInfo(gunId);
         if (packInfoObject != null) {
            this.packInfo = Component.translatable(packInfoObject.getName()).withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.ITALIC);
            this.maxWidth = Math.max(font.width(this.packInfo), this.maxWidth);
         }
      }
   }

   public void extractText(GuiGraphicsExtractor textGraphics, Font font, int pX, int pY) {
      int yOffset = pY;
      if (this.shouldShow(GunTooltipPart.DESCRIPTION) && this.desc != null) {
         yOffset += 2;

         for (FormattedCharSequence sequence : this.desc) {
            textGraphics.text(font, sequence, pX, yOffset, (0xFF000000 | 11184810), false);
            yOffset += 10;
         }
      }

      if (this.shouldShow(GunTooltipPart.AMMO_INFO)) {
         yOffset += 4;
         textGraphics.text(font, this.ammoName, pX + 20, yOffset, (0xFF000000 | 16755200), false);
         textGraphics.text(font, this.ammoCountText, pX + 20, yOffset + 10, (0xFF000000 | 7829367), false);
         yOffset += 20;
      }

      if (this.shouldShow(GunTooltipPart.BASE_INFO)) {
         yOffset += 4;
         textGraphics.text(font, this.levelInfo, pX, yOffset, (0xFF000000 | 7829367), false);
         yOffset += 10;
         if (this.gunType != null) {
            textGraphics.text(font, this.gunType, pX, yOffset, (0xFF000000 | 7829367), false);
            yOffset += 10;
         }

         textGraphics.text(font, this.damage, pX, yOffset, (0xFF000000 | 7829367), false);
         yOffset += 10;
      }

      if (this.shouldShow(GunTooltipPart.EXTRA_DAMAGE_INFO)) {
         yOffset += 4;
         textGraphics.text(font, this.armorIgnore, pX, yOffset, (0xFF000000 | 16755200), false);
         yOffset += 10;
         textGraphics.text(font, this.headShotMultiplier, pX, yOffset, (0xFF000000 | 16755200), false);
         yOffset += 10;
         textGraphics.text(font, this.weight, pX, yOffset, (0xFF000000 | 16777215), false);
         yOffset += 10;
      }

      if (this.shouldShow(GunTooltipPart.UPGRADES_TIP)) {
         yOffset += 4;
         textGraphics.text(font, this.tips, pX, yOffset, (0xFF000000 | 16777215), false);
         yOffset += 10;
      }

      if (this.shouldShow(GunTooltipPart.PACK_INFO) && this.packInfo != null) {
         yOffset += 4;
         textGraphics.text(font, this.packInfo, pX, yOffset, (0xFF000000 | 16777215), false);
      }
   }

   public void extractImage(Font pFont, int pX, int pY, int imageWidth, int imageHeight, GuiGraphicsExtractor guiGraphics) {
      IGun iGun = IGun.getIGunOrNull(this.gun);
      if (iGun != null) {
         if (this.shouldShow(GunTooltipPart.AMMO_INFO)) {
            int yOffset = pY;
            if (this.shouldShow(GunTooltipPart.DESCRIPTION) && this.desc != null) {
               yOffset += this.desc.size() * 10 + 2;
            }

            guiGraphics.item(this.ammo, pX, yOffset + 4);
         }
      }
   }

   private boolean shouldShow(GunTooltipPart part) {
      return (GunTooltipPart.getHideFlags(this.gun) & part.getMask()) == 0;
   }
}
