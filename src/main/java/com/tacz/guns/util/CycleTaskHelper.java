package com.tacz.guns.util;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BooleanSupplier;

public final class CycleTaskHelper {
   private static final List<CycleTaskHelper.CycleTaskTicker> CYCLE_TASKS = new LinkedList<>();
   private static final List<CycleTaskHelper.CycleTaskTicker> TEMP_CYCLE_TASKS = new LinkedList<>();

   public static void addCycleTask(BooleanSupplier task, long periodMs, int cycles) {
      CycleTaskHelper.CycleTaskTicker ticker = new CycleTaskHelper.CycleTaskTicker(task, periodMs, cycles);
      if (ticker.tick()) {
         CYCLE_TASKS.add(ticker);
      }
   }

   public static void addCycleTask(BooleanSupplier task, long delayMs, long periodMs, int cycles) {
      if (delayMs <= 0L) {
         addCycleTask(task, periodMs, cycles);
      } else {
         CycleTaskHelper.CycleTaskTicker ticker = new CycleTaskHelper.CycleTaskTicker(task, delayMs, periodMs, cycles);
         CYCLE_TASKS.add(ticker);
      }
   }

   public static void tick() {
      TEMP_CYCLE_TASKS.addAll(CYCLE_TASKS);
      CYCLE_TASKS.clear();
      TEMP_CYCLE_TASKS.removeIf(ticker -> !ticker.tick());
      CYCLE_TASKS.addAll(TEMP_CYCLE_TASKS);
      TEMP_CYCLE_TASKS.clear();
   }

   private static class CycleTaskTicker {
      private final BooleanSupplier task;
      private final float periodS;
      private final int cycles;
      private float delayS = 0.0F;
      private long timestamp = -1L;
      private float compensation = 0.0F;
      private int count = 0;

      private CycleTaskTicker(BooleanSupplier task, long periodMs, int cycles) {
         this.task = task;
         this.periodS = (float)periodMs / 1000.0F;
         this.cycles = cycles;
      }

      private CycleTaskTicker(BooleanSupplier task, long delayMs, long periodMs, int cycles) {
         this.delayS = (float)delayMs / 1000.0F;
         this.timestamp = System.currentTimeMillis();
         this.task = task;
         this.periodS = (float)periodMs / 1000.0F;
         this.cycles = cycles;
      }

      private boolean tick() {
         if (this.timestamp == -1L) {
            this.timestamp = System.currentTimeMillis();
            return this.cycles > 0 && ++this.count > this.cycles ? false : this.task.getAsBoolean();
         }

         float duration = (float)(System.currentTimeMillis() - this.timestamp) / 1000.0F + this.compensation;
         if (this.delayS > 0.0F) {
            if (this.delayS > duration) {
               this.delayS -= duration;
               return true;
            }

            this.delayS = 0.0F;
            duration = duration - this.delayS + this.periodS;
         }

         if (duration > this.periodS) {
            this.compensation = duration;

            for (this.timestamp = System.currentTimeMillis(); this.compensation > this.periodS; this.compensation = this.compensation - this.periodS) {
               if (this.cycles > 0 && ++this.count > this.cycles) {
                  return false;
               }

               if (!this.task.getAsBoolean()) {
                  return false;
               }
            }
         }

         return true;
      }
   }
}
