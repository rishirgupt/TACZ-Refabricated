package com.tacz.guns.client.gui;

import cn.sh1rocu.tacz.mixin.accessor.ScreenAccessor;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.client.animation.screen.RefitTransform;
import com.tacz.guns.client.gui.components.FlatColorButton;
import com.tacz.guns.client.gui.components.refit.GunAttachmentSlot;
import com.tacz.guns.client.gui.components.refit.GunPropertyDiagrams;
import com.tacz.guns.client.gui.components.refit.HSVSliderGroup;
import com.tacz.guns.client.gui.components.refit.IComponentTooltip;
import com.tacz.guns.client.gui.components.refit.IStackTooltip;
import com.tacz.guns.client.gui.components.refit.InventoryAttachmentSlot;
import com.tacz.guns.client.gui.components.refit.RefitTurnPageButton;
import com.tacz.guns.client.gui.components.refit.RefitUnloadButton;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.index.ClientAttachmentIndex;
import com.tacz.guns.client.sound.SoundPlayManager;
import com.tacz.guns.network.message.ClientMessageLaserColor;
import com.tacz.guns.network.message.ClientMessageRefitGun;
import com.tacz.guns.network.message.ClientMessageUnloadAttachment;
import com.tacz.guns.sound.SoundManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class GunRefitScreen extends Screen {
   public static final Identifier SLOT_TEXTURE = Identifier.fromNamespaceAndPath("tacz", "textures/gui/refit_slot.png");
   public static final Identifier TURN_PAGE_TEXTURE = Identifier.fromNamespaceAndPath("tacz", "textures/gui/refit_turn_page.png");
   public static final Identifier UNLOAD_TEXTURE = Identifier.fromNamespaceAndPath("tacz", "textures/gui/refit_unload.png");
   public static final Identifier ICONS_TEXTURE = Identifier.fromNamespaceAndPath("tacz", "textures/gui/refit_slot_icons.png");
   public static final int ICON_UV_SIZE = 32;
   public static final int SLOT_SIZE = 18;
   private static final int INVENTORY_ATTACHMENT_SLOT_COUNT = 8;
   private static boolean HIDE_GUN_PROPERTY_DIAGRAMS = true;
   private int currentPage = 0;

   public GunRefitScreen() {
      super(Component.literal("Gun Refit Screen"));
      RefitTransform.init();
   }

   public static int getSlotTextureXOffset(ItemStack gunItem, AttachmentType attachmentType) {
      IGun iGun = IGun.getIGunOrNull(gunItem);
      if (iGun == null) {
         return -1;
      }

      if (!iGun.allowAttachmentType(gunItem, attachmentType)) {
         return 192;
      }

      switch (attachmentType) {
         case GRIP:
            return 0;
         case LASER:
            return 32;
         case MUZZLE:
            return 64;
         case SCOPE:
            return 96;
         case STOCK:
            return 128;
         case EXTENDED_MAG:
            return 160;
         default:
            return -1;
      }
   }

   public static int getSlotsTextureWidth() {
      return 224;
   }

   @Override
   public void extractBackground(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
   }

   @Override
   protected void extractBlurredBackground(GuiGraphicsExtractor graphics) {
   }

   public void init() {
      this.clearWidgets();
      this.addAttachmentTypeButtons();
      this.addInventoryAttachmentButtons();
      if (HIDE_GUN_PROPERTY_DIAGRAMS) {
         this.addRenderableWidget(
            new FlatColorButton(11, 11, 288, 16, Component.translatable("gui.tacz.gun_refit.property_diagrams.show"), b -> this.switchHideButton())
         );
      } else {
         this.addRenderableWidget(new FlatColorButton(14, 14, 12, 12, Component.literal("S"), b -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && !player.isSpectator()) {
               if (IGun.mainHandHoldGun(player)) {
                  IClientPlayerGunOperator.fromLocalPlayer(player).fireSelect();
                  this.init();
               }
            }
         }).setTooltips(Component.translatable("gui.tacz.gun_refit.property_diagrams.fire_mode.switch")));
         int buttonYOffset = GunPropertyDiagrams.getHidePropertyButtonYOffset();
         this.addRenderableWidget(
            new FlatColorButton(11, buttonYOffset, 288, 12, Component.translatable("gui.tacz.gun_refit.property_diagrams.hide"), b -> this.switchHideButton())
         );
      }
   }

   protected void renderBlurredBackground(float partialTick) {
   }

   public void render(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float pPartialTick) {
      super.extractRenderState(graphics, mouseX, mouseY, pPartialTick);
      if (!HIDE_GUN_PROPERTY_DIAGRAMS) {
         GunPropertyDiagrams.draw(graphics, this.font, 11, 11);
      }

      ((ScreenAccessor)this)
         .tacz$getRenderables()
         .stream()
         .filter(w -> w instanceof IComponentTooltip)
         .forEach(w -> ((IComponentTooltip)w).renderTooltip(component -> graphics.setComponentTooltipForNextFrame(this.font, component, mouseX, mouseY)));
      ((ScreenAccessor)this)
         .tacz$getRenderables()
         .stream()
         .filter(w -> w instanceof IStackTooltip)
         .forEach(w -> ((IStackTooltip)w).renderTooltip(stack -> graphics.setTooltipForNextFrame(this.font, stack, mouseX, mouseY)));
   }

   public boolean isPauseScreen() {
      return false;
   }

   private void addInventoryAttachmentButtons() {
      LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;
      if (RefitTransform.getCurrentTransformType() != AttachmentType.NONE && player != null) {
         int startX = this.width - 30;
         int startY = 50;
         int pageStart = this.currentPage * 8;
         int count = 0;
         int currentY = startY;
         Inventory inventory = player.getInventory();

         for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack inventoryItem = inventory.getItem(i);
            IAttachment attachment = IAttachment.getIAttachmentOrNull(inventoryItem);
            IGun iGun = IGun.getIGunOrNull(player.getMainHandItem());
            if (attachment != null
               && iGun != null
               && attachment.getType(inventoryItem) == RefitTransform.getCurrentTransformType()
               && iGun.allowAttachment(player.getMainHandItem(), inventoryItem)) {
               count++;
               if (count > pageStart && count <= pageStart + 8) {
                  InventoryAttachmentSlot button = new InventoryAttachmentSlot(startX, currentY, i, inventory, b -> {
                     int slotIndex = ((InventoryAttachmentSlot)b).getSlotIndex();
                     SoundPlayManager.playerRefitSound(inventory.getItem(slotIndex), player, SoundManager.INSTALL_SOUND);
                     ClientMessageRefitGun message = new ClientMessageRefitGun(slotIndex, inventory.getSelectedSlot(), RefitTransform.getCurrentTransformType());
                     ClientPlayNetworking.send(message);
                  });
                  this.addRenderableWidget(button);
                  currentY += 18;
               }
            }
         }

         int totalPage = (count - 1) / 8;
         RefitTurnPageButton turnPageButtonUp = new RefitTurnPageButton(startX, startY - 10, true, b -> {
            if (this.currentPage > 0) {
               this.currentPage--;
               this.init();
            }
         });
         RefitTurnPageButton turnPageButtonDown = new RefitTurnPageButton(startX, startY + 144 + 2, false, b -> {
            if (this.currentPage < totalPage) {
               this.currentPage++;
               this.init();
            }
         });
         if (this.currentPage < totalPage) {
            this.addRenderableWidget(turnPageButtonDown);
         }

         if (this.currentPage > 0) {
            this.addRenderableWidget(turnPageButtonUp);
         }
      }
   }

   private void addAttachmentTypeButtons() {
      LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;
      if (player != null) {
         IGun iGun = IGun.getIGunOrNull(player.getMainHandItem());
         if (iGun != null) {
            int startX = this.width - 30;
            int startY = 10;
            Inventory inventory = player.getInventory();

            for (AttachmentType type : AttachmentType.values()) {
               if (type == AttachmentType.NONE) {
                  if (RefitTransform.getCurrentTransformType() == AttachmentType.NONE) {
                     TimelessAPI.getGunDisplay(player.getMainHandItem())
                        .map(GunDisplayInstance::getLaserConfig)
                        .ifPresent(
                           laserConfig -> {
                              if (laserConfig.canEdit()) {
                                 HSVSliderGroup hsvSliderGroup = new HSVSliderGroup(
                                    this.width - 140, this.height - 64, 120, 16, inventory, inventory.getSelectedSlot(), AttachmentType.NONE
                                 );
                                 this.addRenderableWidget(hsvSliderGroup.getHueSlider());
                                 this.addRenderableWidget(hsvSliderGroup.getSaturationSlider());
                              }
                           }
                        );
                  }
               } else {
                  GunAttachmentSlot button = new GunAttachmentSlot(startX, startY, type, inventory.getSelectedSlot(), inventory, b -> {
                     AttachmentType buttonType = ((GunAttachmentSlot)b).getType();
                     if (!((GunAttachmentSlot)b).isAllow()) {
                        if (RefitTransform.changeRefitScreenView(AttachmentType.NONE)) {
                           this.init();
                        }
                     } else if (RefitTransform.getCurrentTransformType() == buttonType && buttonType != AttachmentType.NONE) {
                        if (RefitTransform.changeRefitScreenView(AttachmentType.NONE)) {
                           this.init();
                        }
                     } else {
                        if (RefitTransform.changeRefitScreenView(buttonType)) {
                           this.init();
                        }
                     }
                  });
                  if (RefitTransform.getCurrentTransformType() == type) {
                     button.setSelected(true);
                     RefitUnloadButton unloadButton = new RefitUnloadButton(
                        startX + 5,
                        startY + 18 + 2,
                        b -> {
                           ItemStack attachmentItem = button.getAttachmentItem();
                           if (!attachmentItem.isEmpty()) {
                              int freeSlot = inventory.getFreeSlot();
                              if (freeSlot != -1) {
                                 SoundPlayManager.playerRefitSound(attachmentItem, player, SoundManager.UNINSTALL_SOUND);
                                 ClientMessageUnloadAttachment message = new ClientMessageUnloadAttachment(
                                    inventory.getSelectedSlot(), RefitTransform.getCurrentTransformType()
                                 );
                                 ClientPlayNetworking.send(message);
                              } else {
                                 player.sendSystemMessage(Component.translatable("gui.tacz.gun_refit.unload.no_space"));
                              }
                           }
                        }
                     );
                     if (!button.getAttachmentItem().isEmpty()) {
                        this.addRenderableWidget(unloadButton);
                        if (button.getAttachmentItem().getItem() instanceof IAttachment iAttachment) {
                           TimelessAPI.getClientAttachmentIndex(iAttachment.getAttachmentId(button.getAttachmentItem()))
                              .map(ClientAttachmentIndex::getLaserConfig)
                              .ifPresent(
                                 laserConfig -> {
                                    if (laserConfig.canEdit()) {
                                       HSVSliderGroup hsvSliderGroup = new HSVSliderGroup(
                                          this.width - 140, this.height - 64, 120, 16, inventory, inventory.getSelectedSlot(), type
                                       );
                                       this.addRenderableWidget(hsvSliderGroup.getHueSlider());
                                       this.addRenderableWidget(hsvSliderGroup.getSaturationSlider());
                                    }
                                 }
                              );
                        }
                     }
                  }

                  this.addRenderableWidget(button);
                  startX -= 18;
               }
            }
         }
      }
   }

   public void onClose() {
      LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;
      if (player != null) {
         ItemStack gun = player.getMainHandItem();
         if (player.getMainHandItem().getItem() instanceof IGun) {
            ClientMessageLaserColor message = new ClientMessageLaserColor(gun, player.getInventory().getSelectedSlot());
            ClientPlayNetworking.send(message);
         }
      }

      super.onClose();
   }

   private void switchHideButton() {
      HIDE_GUN_PROPERTY_DIAGRAMS = !HIDE_GUN_PROPERTY_DIAGRAMS;
      this.init();
   }
}
