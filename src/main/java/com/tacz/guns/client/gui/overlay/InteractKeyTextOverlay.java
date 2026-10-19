package com.tacz.guns.client.gui.overlay;

import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.block.AbstractGunSmithTableBlock;
import com.tacz.guns.client.input.InteractKey;
import com.tacz.guns.config.client.RenderConfig;
import com.tacz.guns.config.util.InteractKeyConfigRead;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.DeltaTracker;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class InteractKeyTextOverlay {
   public static final InteractKeyTextOverlay INSTANCE = new InteractKeyTextOverlay();

   public void render(GuiGraphicsExtractor graphics, @NotNull DeltaTracker deltaTracker) {
      int width = graphics.guiWidth();
      int height = graphics.guiHeight();
      if (!(Boolean)RenderConfig.DISABLE_INTERACT_HUD_TEXT.get()) {
         Minecraft mc = Minecraft.getInstance();
         LocalPlayer player = mc.player;
         if (player != null && !player.isSpectator()) {
            HitResult hitResult = mc.hitResult;
            if (hitResult != null) {
               if (hitResult instanceof BlockHitResult blockHitResult) {
                  renderBlockText(graphics, width, height, blockHitResult, player, mc);
               } else {
                  if (hitResult instanceof EntityHitResult entityHitResult) {
                     renderEntityText(graphics, width, height, entityHitResult, mc);
                  }
               }
            }
         }
      }
   }

   private static void renderBlockText(GuiGraphicsExtractor graphics, int width, int height, BlockHitResult blockHitResult, LocalPlayer player, Minecraft mc) {
      BlockPos blockPos = blockHitResult.getBlockPos();
      BlockState block = player.level().getBlockState(blockPos);
      if (InteractKeyConfigRead.canInteractBlock(block)) {
         boolean mainHandHoldGun = IGun.mainHandHoldGun(player);
         boolean hasGunSmithTableFilterItem = hasGunSmithTableFilterItem(block, player);
         boolean willFilterByHand = (Boolean)RenderConfig.AUTO_SELECT_GUN_SMITH_TABLE_FILTER.get() && hasGunSmithTableFilterItem;
         if (mainHandHoldGun) {
            renderText(graphics, width, height, mc.font, InteractKey.INTERACT_KEY.getTranslatedKeyMessage().getString(), willFilterByHand);
         } else if (hasGunSmithTableFilterItem) {
            renderText(graphics, width, height, mc.font, mc.options.keyUse.getTranslatedKeyMessage().getString(), willFilterByHand);
         }
      }
   }

   private static void renderEntityText(GuiGraphicsExtractor graphics, int width, int height, EntityHitResult entityHitResult, Minecraft mc) {
      if (mc.player != null && IGun.mainHandHoldGun(mc.player)) {
         Entity entity = entityHitResult.getEntity();
         if (InteractKeyConfigRead.canInteractEntity(entity)) {
            renderText(graphics, width, height, mc.font, InteractKey.INTERACT_KEY.getTranslatedKeyMessage().getString(), false);
         }
      }
   }

   private static boolean hasGunSmithTableFilterItem(BlockState block, LocalPlayer player) {
      if (!(block.getBlock() instanceof AbstractGunSmithTableBlock)) {
         return false;
      }

      ItemStack stack = player.getMainHandItem();
      Item item = stack.getItem();
      return item instanceof IGun || item instanceof IAttachment || item instanceof IAmmo;
   }

   private static void renderText(GuiGraphicsExtractor graphics, int width, int height, Font font, String keyName, boolean willFilterByHand) {
      Component title = Component.translatable("gui.tacz.interact_key.text.desc", new Object[]{StringUtils.capitalize(keyName)});
      graphics.text(
         font, title, (int)((width - font.width(title)) / 2.0F), (int)(height / 2.0F - 25.0F), 0xFFFFFF55, false
      );
      if (willFilterByHand) {
         Component filter = Component.translatable("gui.tacz.interact_key.text.gun_smith_table_filter");
         graphics.text(
            font, filter, (int)((width - font.width(filter)) / 2.0F), (int)(height / 2.0F - 14.0F), 0xFFAAAAAA, false
         );
      }
   }
}
