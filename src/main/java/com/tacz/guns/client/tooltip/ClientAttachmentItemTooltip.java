package com.tacz.guns.client.tooltip;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.builder.AttachmentItemBuilder;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.client.resource.ClientAssetsManager;
import com.tacz.guns.client.resource.pojo.PackInfo;
import com.tacz.guns.inventory.tooltip.AttachmentItemTooltip;
import com.tacz.guns.resource.pojo.data.attachment.AttachmentData;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

public class ClientAttachmentItemTooltip implements ClientTooltipComponent {
   private static final Cache<Identifier, List<ItemStack>> CACHE = CacheBuilder.newBuilder().expireAfterAccess(5L, TimeUnit.SECONDS).build();
   private final Identifier attachmentId;
   private final List<Component> components = Lists.newArrayList();
   private final MutableComponent tips = Component.translatable("tooltip.tacz.attachment.yaw.shift");
   private final MutableComponent support = Component.translatable("tooltip.tacz.attachment.yaw.support");
   @Nullable
   private MutableComponent packInfo;
   private List<ItemStack> showGuns = Lists.newArrayList();
   private ItemStack attachment;

   public ClientAttachmentItemTooltip(AttachmentItemTooltip tooltip) {
      this.attachmentId = tooltip.getAttachmentId();
      this.attachment = tooltip.getAttachmentItem();
      this.addText(tooltip.getType());
      this.getShowGuns();
      this.addPackInfo();
   }

   private void addPackInfo() {
      PackInfo packInfoObject = ClientAssetsManager.INSTANCE.getPackInfo(this.attachmentId);
      if (packInfoObject != null) {
         this.packInfo = Component.translatable(packInfoObject.getName()).withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.ITALIC);
      }
   }

   private static List<ItemStack> getAllAllowGuns(List<ItemStack> output, Identifier attachmentId) {
      ItemStack attachment = AttachmentItemBuilder.create().setId(attachmentId).build();
      TimelessAPI.getAllCommonGunIndex().forEach(entry -> {
         Identifier gunId = entry.getKey();
         ItemStack gun = GunItemBuilder.create().setId(gunId).build(Minecraft.getInstance().level.registryAccess());
         if (gun.getItem() instanceof IGun iGun) {
            if (iGun.allowAttachment(gun, attachment)) {
               output.add(gun);
            }
         }
      });
      return output;
   }

   public int getHeight(Font font) {
      return !Minecraft.getInstance().hasShiftDown() ? this.components.size() * 10 + 28 : (this.showGuns.size() - 1) / 16 * 18 + 50 + this.components.size() * 10;
   }

   public int getWidth(Font font) {
      int[] width = new int[]{0};
      if (this.packInfo != null) {
         width[0] = Math.max(width[0], font.width(this.packInfo) + 4);
      }

      this.components.forEach(c -> width[0] = Math.max(width[0], font.width(c)));
      if (!Minecraft.getInstance().hasShiftDown()) {
         return Math.max(width[0], font.width(this.tips) + 4);
      }

      width[0] = Math.max(width[0], font.width(this.support) + 4);
      return this.showGuns.size() > 15 ? Math.max(width[0], 260) : Math.max(width[0], this.showGuns.size() * 16 + 4);
   }

   public void extractText(GuiGraphicsExtractor textGraphics, Font font, int pX, int pY) {
      int yOffset = pY;

      for (Component component : this.components) {
         textGraphics.text(font, component, pX, yOffset, (0xFF000000 | 16755200), false);
         yOffset += 10;
      }

      if (!Minecraft.getInstance().hasShiftDown()) {
         textGraphics.text(font, this.tips, pX, pY + 5 + this.components.size() * 10, (0xFF000000 | 10395294), false);
         yOffset += 10;
      } else {
         yOffset += (this.showGuns.size() - 1) / 16 * 18 + 32;
      }

      if (this.packInfo != null) {
         textGraphics.text(font, this.packInfo, pX, yOffset + 8, (0xFF000000 | 16777215), false);
      }
   }

   public void extractImage(Font font, int mouseX, int mouseY, int imageWidth, int imageHeight, GuiGraphicsExtractor gui) {
      if (Minecraft.getInstance().hasShiftDown()) {
         int minY = this.components.size() * 10 + 3;
         int maxX = this.getWidth(font);
         gui.fill(mouseX, mouseY + minY, mouseX + maxX, mouseY + minY + 11, -1895780097);
         gui.text(font, this.support, mouseX + 2, mouseY + minY + 2, (0xFF000000 | 14938877));

         for (int i = 0; i < this.showGuns.size(); i++) {
            ItemStack stack = this.showGuns.get(i);
            int x = i % 16 * 16 + 2;
            int y = i / 16 * 18 + minY + 15;
            gui.item(stack, mouseX + x, mouseY + y);
         }
      }
   }

   private void getShowGuns() {
      try {
         this.showGuns = (List<ItemStack>)CACHE.get(this.attachmentId, () -> getAllAllowGuns(Lists.newArrayList(), this.attachmentId));
      } catch (ExecutionException e) {
         e.printStackTrace();
      }
   }

   public static String rgbToHex(int rgb) {
      int r = rgb >> 16 & 0xFF;
      int g = rgb >> 8 & 0xFF;
      int b = rgb & 0xFF;
      return String.format("#%02X%02X%02X", r, g, b);
   }

   private void addText(AttachmentType type) {
      TimelessAPI.getClientAttachmentIndex(this.attachmentId)
         .ifPresent(
            index -> {
               AttachmentData data = index.getData();
               String tooltipKey = index.getTooltipKey();
               if (tooltipKey != null) {
                  String text = I18n.get(tooltipKey, new Object[0]);
                  String[] split = text.split("\n");
                  Arrays.stream(split).forEach(s -> this.components.add(Component.literal(s).withStyle(ChatFormatting.GRAY)));
               }

               if (this.attachment.getItem() instanceof IAttachment iAttachment) {
                  TimelessAPI.getClientAttachmentIndex(this.attachmentId)
                     .ifPresent(
                        attachmentIndex -> {
                           if (iAttachment.hasCustomLaserColor(this.attachment)) {
                              int color = iAttachment.getLaserColor(this.attachment);
                              this.components
                                 .add(
                                    Component.translatable("tooltip.tacz.attachment.laser.color", new Object[]{rgbToHex(color)})
                                       .withStyle(Style.EMPTY.withColor(color))
                                 );
                           } else if (attachmentIndex.getLaserConfig() != null) {
                              int color = attachmentIndex.getLaserConfig().getDefaultColor();
                              this.components
                                 .add(
                                    Component.translatable("tooltip.tacz.attachment.laser.color", new Object[]{rgbToHex(color)})
                                       .withStyle(Style.EMPTY.withColor(color))
                                 );
                           }
                        }
                     );
               }

               if (type == AttachmentType.SCOPE) {
                  float[] zoom = index.getZoom();
                  if (zoom != null) {
                     String[] zoomText = new String[zoom.length];

                     for (int i = 0; i < zoom.length; i++) {
                        zoomText[i] = "x" + zoom[i];
                     }

                     String zoomJoinText = StringUtils.join(zoomText, ", ");
                     this.components
                        .add(Component.translatable("tooltip.tacz.attachment.zoom", new Object[]{zoomJoinText}).withStyle(ChatFormatting.GOLD));
                  }
               }

               if (type == AttachmentType.EXTENDED_MAG) {
                  int magLevel = data.getExtendedMagLevel();
                  if (magLevel == 1) {
                     this.components.add(Component.translatable("tooltip.tacz.attachment.extended_mag_level_1").withStyle(ChatFormatting.GRAY));
                  } else if (magLevel == 2) {
                     this.components.add(Component.translatable("tooltip.tacz.attachment.extended_mag_level_2").withStyle(ChatFormatting.BLUE));
                  } else if (magLevel == 3) {
                     this.components.add(Component.translatable("tooltip.tacz.attachment.extended_mag_level_3").withStyle(ChatFormatting.LIGHT_PURPLE));
                  }
               }

               data.getModifier().forEach((key, value) -> {
                  List<Component> result = value.getComponents();
                  this.components.addAll(result);
               });
            }
         );
   }
}
