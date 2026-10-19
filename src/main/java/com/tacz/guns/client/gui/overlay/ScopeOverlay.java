package com.tacz.guns.client.gui.overlay;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.client.resource.index.ClientAttachmentIndex;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Looking through a scope. 26.2 has no stencil buffer to cut a lens out of a 3D ocular, so the scope view is a screen
 * overlay: the field of view already zooms the world, this blacks out everything outside a round lens and draws a
 * reticle, and the gun itself is hidden while the lens is up.
 */
public class ScopeOverlay {
   public static final ScopeOverlay INSTANCE = new ScopeOverlay();
   private static final float FADE_START = 0.45F;
   private static final float FADE_END = 0.7F;

   /** Aiming progress at which the scope overlay is fully up, or -1 when the player is not aiming through a scope. */
   private static float scopeProgress(float partialTick) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      if (player == null || !mc.options.getCameraType().isFirstPerson() || mc.gui.screen() != null) {
         return -1.0F;
      }

      ItemStack stack = player.getMainHandItem();
      if (!(stack.getItem() instanceof IGun iGun) || !(player instanceof IClientPlayerGunOperator operator)) {
         return -1.0F;
      }

      Identifier scopeId = iGun.getAttachmentId(stack, AttachmentType.SCOPE);
      if (DefaultAssets.isEmptyAttachmentId(scopeId)) {
         scopeId = iGun.getBuiltInAttachmentId(stack, AttachmentType.SCOPE);
      }

      if (DefaultAssets.isEmptyAttachmentId(scopeId)) {
         return -1.0F;
      }

      ClientAttachmentIndex index = TimelessAPI.getClientAttachmentIndex(scopeId).orElse(null);
      if (index == null || !index.isScope()) {
         return -1.0F;
      }

      return operator.getClientAimingProgress(partialTick);
   }

   /** True while the scope overlay is fully up and the gun model should not be drawn. */
   public static boolean shouldHideGun() {
      return scopeProgress(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false)) >= FADE_END;
   }

   public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
      ScopeLens.render(graphics);
      float progress = scopeProgress(deltaTracker.getGameTimeDeltaPartialTick(false));
      if (progress < FADE_START) {
         return;
      }

      float fade = Math.min(1.0F, (progress - FADE_START) / (FADE_END - FADE_START));
      int black = (int)(fade * 255.0F) << 24;
      int width = graphics.guiWidth();
      int height = graphics.guiHeight();
      int cx = width / 2;
      int cy = height / 2;
      int radius = (int)(height * 0.46F);
      for (int y = 0; y < height; y++) {
         int dy = y - cy;
         if (Math.abs(dy) >= radius) {
            graphics.fill(0, y, width, y + 1, black);
         } else {
            int half = (int)Math.sqrt((double)radius * radius - (double)dy * dy);
            graphics.fill(0, y, cx - half, y + 1, black);
            graphics.fill(cx + half, y, width, y + 1, black);
         }
      }

      // Duplex reticle: thin lines that thicken toward the edge, and a dot in the middle.
      int line = ((int)(fade * 255.0F) << 24) | 0x000000;
      int gap = radius / 14;
      graphics.fill(cx - radius, cy, cx - gap, cy + 1, line);
      graphics.fill(cx + gap, cy, cx + radius, cy + 1, line);
      graphics.fill(cx, cy - radius, cx + 1, cy - gap, line);
      graphics.fill(cx, cy + gap, cx + 1, cy + radius, line);
      graphics.fill(cx - 1, cy - 1, cx + 2, cy + 2, ((int)(fade * 255.0F) << 24) | 0xE02020);
   }
}
