package com.tacz.guns.compat.playeranimator;

import com.tacz.guns.client.resource.GunDisplayInstance;
import java.io.File;
import java.util.function.Consumer;
import java.util.zip.ZipFile;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

/**
 * Third-person gun poses through Player Animator. That library does not exist for 26.2 (its successor,
 * Player Animation Library, has a different API), so these hooks do nothing and players are posed by the
 * built-in humanoid model adjustments instead.
 */
public class PlayerAnimatorCompat {
   public static Identifier LOWER_ANIMATION = Identifier.fromNamespaceAndPath("tacz", "lower_animation");
   public static Identifier LOOP_UPPER_ANIMATION = Identifier.fromNamespaceAndPath("tacz", "loop_upper_animation");
   public static Identifier ONCE_UPPER_ANIMATION = Identifier.fromNamespaceAndPath("tacz", "once_upper_animation");
   public static Identifier ROTATION_ANIMATION = Identifier.fromNamespaceAndPath("tacz", "rotation");

   public static void init() {
   }

   public static boolean loadAnimationFromZip(ZipFile zipFile, String zipPath) {
      return false;
   }

   public static void loadAnimationFromFile(File file) {
   }

   public static void clearAllAnimationCache() {
   }

   public static boolean hasPlayerAnimator3rd(LivingEntity livingEntity, GunDisplayInstance display) {
      return false;
   }

   public static void stopAllAnimation(LivingEntity livingEntity) {
   }

   public static void stopAllAnimation(LivingEntity livingEntity, int fadeTime) {
   }

   public static void playAnimation(LivingEntity livingEntity, GunDisplayInstance display, float limbSwingAmount) {
   }

   public static boolean isInstalled() {
      return false;
   }

   public static void registerReloadListener(Consumer<net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener> register) {
   }
}
