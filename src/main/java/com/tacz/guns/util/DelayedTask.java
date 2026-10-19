package com.tacz.guns.util;

import com.google.common.collect.Lists;
import java.util.LinkedList;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class DelayedTask {
   public static LinkedList<BooleanSupplier> SUPPLIERS = Lists.newLinkedList();

   private DelayedTask() {
   }

   public static void add(Runnable runnable, int delayedTick) {
      int[] tickArray = new int[]{delayedTick};
      SUPPLIERS.add(() -> {
         if (--tickArray[0] < 0) {
            runnable.run();
            return true;
         } else {
            return false;
         }
      });
   }

   public static void add(Consumer<Integer> consumer, int delayedTick, int times) {
      int[] tickArray = new int[]{delayedTick};
      SUPPLIERS.add(() -> {
         if (--tickArray[0] < 0) {
            consumer.accept(times);
            return true;
         } else {
            return false;
         }
      });
   }
}
