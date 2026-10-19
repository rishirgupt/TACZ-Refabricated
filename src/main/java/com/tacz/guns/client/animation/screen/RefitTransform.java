package com.tacz.guns.client.animation.screen;

import cn.sh1rocu.simplebedrockmodel.api.event.RenderTickEvent;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.client.gui.GunRefitScreen;
import java.util.Objects;
import javax.annotation.Nonnull;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public class RefitTransform {
   private static final float REFIT_SCREEN_TRANSFORM_TIMES = 0.25F;
   private static float refitScreenTransformProgress = 1.0F;
   private static long refitScreenTransformTimestamp = -1L;
   private static AttachmentType oldTransformType = AttachmentType.NONE;
   private static AttachmentType currentTransformType = AttachmentType.NONE;
   private static float refitScreenOpeningProgress = 0.0F;
   private static long refitScreenOpeningTimestamp = -1L;

   public static void init() {
      refitScreenTransformProgress = 1.0F;
      refitScreenTransformTimestamp = System.currentTimeMillis();
      oldTransformType = AttachmentType.NONE;
      currentTransformType = AttachmentType.NONE;
   }

   public static float getOpeningProgress() {
      return refitScreenOpeningProgress;
   }

   @Nonnull
   public static AttachmentType getOldTransformType() {
      return Objects.requireNonNullElse(oldTransformType, AttachmentType.NONE);
   }

   @Nonnull
   public static AttachmentType getCurrentTransformType() {
      return Objects.requireNonNullElse(currentTransformType, AttachmentType.NONE);
   }

   public static float getTransformProgress() {
      return refitScreenTransformProgress;
   }

   public static boolean changeRefitScreenView(AttachmentType attachmentType) {
      if (refitScreenTransformProgress == 1.0F && refitScreenOpeningProgress == 1.0F) {
         oldTransformType = currentTransformType;
         currentTransformType = attachmentType;
         refitScreenTransformProgress = 0.0F;
         refitScreenTransformTimestamp = System.currentTimeMillis();
         return true;
      } else {
         return false;
      }
   }

   public static void tickInterpolation(RenderTickEvent event) {
      if (refitScreenOpeningTimestamp == -1L) {
         refitScreenOpeningTimestamp = System.currentTimeMillis();
      }

      if (Minecraft.getInstance().gui.screen() instanceof GunRefitScreen) {
         refitScreenOpeningProgress = refitScreenOpeningProgress + (float)(System.currentTimeMillis() - refitScreenOpeningTimestamp) / 250.0F;
         if (refitScreenOpeningProgress > 1.0F) {
            refitScreenOpeningProgress = 1.0F;
         }
      } else {
         refitScreenOpeningProgress = refitScreenOpeningProgress - (float)(System.currentTimeMillis() - refitScreenOpeningTimestamp) / 250.0F;
         if (refitScreenOpeningProgress < 0.0F) {
            refitScreenOpeningProgress = 0.0F;
         }
      }

      refitScreenOpeningTimestamp = System.currentTimeMillis();
      if (refitScreenTransformTimestamp == -1L) {
         refitScreenTransformTimestamp = System.currentTimeMillis();
      }

      refitScreenTransformProgress = refitScreenTransformProgress + (float)(System.currentTimeMillis() - refitScreenTransformTimestamp) / 250.0F;
      if (refitScreenTransformProgress > 1.0F) {
         refitScreenTransformProgress = 1.0F;
      }

      refitScreenTransformTimestamp = System.currentTimeMillis();
   }
}
