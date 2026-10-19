package com.tacz.guns.config.client;

import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;

public class ZoomConfig {
   public static DoubleValue SCREEN_DISTANCE_COEFFICIENT;
   public static DoubleValue ZOOM_SENSITIVITY_BASE_MULTIPLIER;

   public static void init(Builder builder) {
      builder.push("Zoom");
      builder.comment("Screen distance coefficient for zoom, using MDV standard, default is MDV133");
      SCREEN_DISTANCE_COEFFICIENT = builder.defineInRange("ScreenDistanceCoefficient", 1.33, 0.0, 3.0);
      builder.comment("Zoom sensitivity is multiplied by this factor");
      ZOOM_SENSITIVITY_BASE_MULTIPLIER = builder.defineInRange("ZoomSensitivityBaseMultiplier", 1.0, 0.0, 2.0);
      builder.pop();
   }
}
