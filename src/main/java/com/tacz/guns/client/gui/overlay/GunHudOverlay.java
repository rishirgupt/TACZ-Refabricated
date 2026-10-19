package com.tacz.guns.client.gui.overlay;

import net.minecraft.client.renderer.RenderPipelines;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.tacz.guns.client.resource.pojo.display.gun.AmmoCountStyle;
import com.tacz.guns.config.client.RenderConfig;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.util.AttachmentDataUtils;
import java.text.DecimalFormat;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.SharedConstants;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.DeltaTracker;
import org.jetbrains.annotations.NotNull;

public class GunHudOverlay {
   private static final Identifier SEMI = Identifier.fromNamespaceAndPath("tacz", "textures/hud/fire_mode_semi.png");
   private static final Identifier AUTO = Identifier.fromNamespaceAndPath("tacz", "textures/hud/fire_mode_auto.png");
   private static final Identifier BURST = Identifier.fromNamespaceAndPath("tacz", "textures/hud/fire_mode_burst.png");
   private static final Identifier HEATBAR = Identifier.fromNamespaceAndPath("tacz", "textures/hud/heat_bar.png");
   private static final Identifier HEATBASE = Identifier.fromNamespaceAndPath("tacz", "textures/hud/heat_base.png");
   private static final DecimalFormat CURRENT_AMMO_FORMAT = new DecimalFormat("000");
   private static final DecimalFormat CURRENT_AMMO_FORMAT_PERCENT = new DecimalFormat("000%");
   private static final DecimalFormat INVENTORY_AMMO_FORMAT = new DecimalFormat("0000");
   private static long checkAmmoTimestamp = -1L;
   private static int cacheMaxAmmoCount = 0;
   private static int cacheInventoryAmmoCount = 0;
   private static final int MAX_AMMO_COUNT = 9999;
   public static final GunHudOverlay INSTANCE = new GunHudOverlay();

   public void render(GuiGraphicsExtractor graphics, @NotNull DeltaTracker deltaTracker) {
      int width = graphics.guiWidth();
      int height = graphics.guiHeight();
      if ((Boolean)RenderConfig.GUN_HUD_ENABLE.get()) {
         Minecraft mc = Minecraft.getInstance();
         LocalPlayer player = mc.player;
         if (player instanceof IClientPlayerGunOperator) {
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof IGun iGun) {
               Identifier gunId = iGun.getGunId(stack);
               GunData gunData = TimelessAPI.getClientGunIndex(gunId).map(ClientGunIndex::getGunData).orElse(null);
               GunDisplayInstance display = TimelessAPI.getGunDisplay(stack).orElse(null);
               if (gunData != null && display != null) {
                  boolean useInventoryAmmo = iGun.useInventoryAmmo(stack);
                  boolean useDummyAmmo = iGun.useDummyAmmo(stack);
                  boolean overheatLocked = gunData.hasHeatData() && iGun.isOverheatLocked(stack);
                  int ammoCount = useInventoryAmmo
                     ? cacheInventoryAmmoCount + (iGun.hasBulletInBarrel(stack) && gunData.getBolt() != Bolt.OPEN_BOLT ? 1 : 0)
                     : iGun.getCurrentAmmoCount(stack) + (iGun.hasBulletInBarrel(stack) && gunData.getBolt() != Bolt.OPEN_BOLT ? 1 : 0);
                  ammoCount = Math.min(ammoCount, 9999);
                  int ammoCountColor;
                  if ((!(ammoCount < cacheMaxAmmoCount * 0.25) || ammoCount >= 10) && !overheatLocked) {
                     ammoCountColor = useInventoryAmmo && useDummyAmmo ? 5636095 : (useInventoryAmmo ? 16777045 : 16777215);
                  } else {
                     ammoCountColor = 16733525;
                  }

                  int inventoryAmmoCountColor;
                  if (!useInventoryAmmo && useDummyAmmo) {
                     inventoryAmmoCountColor = 5636095;
                  } else {
                     inventoryAmmoCountColor = 11184810;
                  }

                  String currentAmmoCountText;
                  if (display.getAmmoCountStyle() == AmmoCountStyle.PERCENT) {
                     currentAmmoCountText = CURRENT_AMMO_FORMAT_PERCENT.format(ammoCount / (cacheMaxAmmoCount == 0 ? 1.0F : cacheMaxAmmoCount));
                  } else {
                     currentAmmoCountText = CURRENT_AMMO_FORMAT.format(ammoCount);
                  }

                  String inventoryAmmoCountText = useInventoryAmmo ? "" : INVENTORY_AMMO_FORMAT.format(cacheInventoryAmmoCount);
                  if (!useInventoryAmmo && gunData.getReloadData().isInfinite()) {
                     inventoryAmmoCountText = "∞";
                  }

                  handleCacheCount(player, stack, gunData, iGun, useInventoryAmmo);
                  graphics.fill(width - 75, height - 43, width - 74, height - 25, -1);
                  org.joml.Matrix3x2fStack poseStack = graphics.pose();
                  Font font = mc.font;
                  poseStack.pushMatrix();
                  poseStack.scale(1.5F, 1.5F);
                  graphics.text(font, currentAmmoCountText, (int)((width - 70) / 1.5F), (int)((height - 43) / 1.5F), 0xFF000000 | ammoCountColor, false);
                  poseStack.popMatrix();
                  poseStack.pushMatrix();
                  poseStack.scale(0.8F, 0.8F);
                  graphics.text(
                     font,
                     inventoryAmmoCountText,
                     (int)((width - 68 + mc.font.width(currentAmmoCountText) * 1.5F) / 0.8F),
                     (int)((height - 43) / 0.8F),
                     0xFF000000 | inventoryAmmoCountColor,
                     false
                  );
                  poseStack.popMatrix();
                  String minecraftVersion = SharedConstants.getCurrentVersion().name();
                  String modVersion = ((ModContainer)FabricLoader.getInstance().getModContainer("tacz").orElseThrow())
                     .getMetadata()
                     .getVersion()
                     .getFriendlyString();
                  String debugInfo = String.format("%s-%s", minecraftVersion, modVersion);
                  poseStack.pushMatrix();
                  poseStack.scale(0.5F, 0.5F);
                  graphics.text(font, debugInfo, (int)((width - 70) / 0.5F), (int)((height - 29.0F) / 0.5F), -5592406);
                  poseStack.popMatrix();
                  int hudColor = -1;
                  Identifier hudTexture = display.getHUDTexture();
                  Identifier hudEmptyTexture = display.getHudEmptyTexture();
                  if (ammoCount <= 0 || overheatLocked) {
                     if (hudEmptyTexture == null) {
                        hudColor = 0xFFFF4D4D;
                     } else {
                        hudTexture = hudEmptyTexture;
                     }
                  }

                  graphics.blit(RenderPipelines.GUI_TEXTURED, hudTexture, width - 117, height - 44, 0.0F, 0.0F, 39, 13, 39, 13, hudColor);
                  FireMode fireMode = IGun.getMainHandFireMode(player);

                  Identifier fireModeTexture = switch (fireMode) {
                     case AUTO -> AUTO;
                     case BURST -> BURST;
                     default -> SEMI;
                  };
                  graphics.blit(RenderPipelines.GUI_TEXTURED, fireModeTexture, (int)(width - 68.5 + mc.font.width(currentAmmoCountText) * 1.5), height - 38, 0.0F, 0.0F, 10, 10, 10, 10, -1);
               }
            }
         }
      }
   }

   private static void handleCacheCount(LocalPlayer player, ItemStack stack, GunData gunData, IGun iGun, boolean useInventoryAmmo) {
      if (System.currentTimeMillis() - checkAmmoTimestamp > 50L) {
         checkAmmoTimestamp = System.currentTimeMillis();
         cacheMaxAmmoCount = AttachmentDataUtils.getAmmoCountWithAttachment(stack, gunData);
         if (IGunOperator.fromLivingEntity(player).needCheckAmmo()) {
            if (iGun.useDummyAmmo(stack)) {
               cacheInventoryAmmoCount = iGun.getDummyAmmoAmount(stack);
            } else {
               handleInventoryAmmo(stack, player.getInventory());
            }
         } else {
            cacheInventoryAmmoCount = 9999;
         }

         if (useInventoryAmmo) {
            iGun.setCurrentAmmoCount(stack, cacheInventoryAmmoCount);
         }
      }
   }

   private static void handleInventoryAmmo(ItemStack stack, Inventory inventory) {
      cacheInventoryAmmoCount = 0;

      for (int i = 0; i < inventory.getContainerSize(); i++) {
         ItemStack inventoryItem = inventory.getItem(i);
         if (inventoryItem.getItem() instanceof IAmmo iAmmo && iAmmo.isAmmoOfGun(stack, inventoryItem)) {
            cacheInventoryAmmoCount = cacheInventoryAmmoCount + inventoryItem.getCount();
         }

         if (inventoryItem.getItem() instanceof IAmmoBox iAmmoBox && iAmmoBox.isAmmoBoxOfGun(stack, inventoryItem)) {
            if (iAmmoBox.isAllTypeCreative(inventoryItem) || iAmmoBox.isCreative(inventoryItem)) {
               cacheInventoryAmmoCount = 9999;
               return;
            }

            cacheInventoryAmmoCount = cacheInventoryAmmoCount + iAmmoBox.getAmmoCount(inventoryItem);
         }
      }
   }
}
