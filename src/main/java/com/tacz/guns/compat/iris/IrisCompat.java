package com.tacz.guns.compat.iris;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import net.fabricmc.loader.api.FabricLoader;

/** Reads Iris state through reflection, so the mod needs no compile-time dependency on it. */
public final class IrisCompat {
   private static MethodHandle API_INSTANCE;
   private static MethodHandle SHADER_PACK_IN_USE;
   private static MethodHandle SHADOW_PASS;

   public static void initCompat() {
      if (!FabricLoader.getInstance().isModLoaded("iris")) {
         return;
      }

      try {
         Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
         MethodHandles.Lookup lookup = MethodHandles.publicLookup();
         API_INSTANCE = lookup.findStatic(api, "getInstance", MethodType.methodType(api));
         SHADER_PACK_IN_USE = lookup.findVirtual(api, "isShaderPackInUse", MethodType.methodType(boolean.class));
         SHADOW_PASS = lookup.findVirtual(api, "isRenderingShadowPass", MethodType.methodType(boolean.class));
      } catch (ReflectiveOperationException | LinkageError exception) {
         API_INSTANCE = null;
      }
   }

   public static boolean endBatch(com.tacz.guns.client.render26.MultiBufferSource bufferSource) {
      return false;
   }

   public static boolean isPackInUseQuick() {
      return isUsingRenderPack();
   }

   public static boolean isRenderShadow() {
      return query(SHADOW_PASS);
   }

   public static boolean isUsingRenderPack() {
      return query(SHADER_PACK_IN_USE);
   }

   private static boolean query(MethodHandle handle) {
      if (API_INSTANCE == null || handle == null) {
         return false;
      }

      try {
         return (boolean)handle.invoke(API_INSTANCE.invoke());
      } catch (Throwable throwable) {
         return false;
      }
   }
}
