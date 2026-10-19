package com.tacz.guns.util.math;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SecondOrderDynamics {
   public static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(15, Thread::new);
   private final float k1;
   private final float k2;
   private final float k3;
   private float py;
   private float pyd;
   private float px;
   private float target;
   private boolean stop = false;

   public SecondOrderDynamics(float f, float z, float r, float x0) {
      this.k1 = (float)(z / (Math.PI * f));
      this.k2 = (float)(1.0 / ((Math.PI * 2) * f * ((Math.PI * 2) * f)));
      this.k3 = (float)(r * z / ((Math.PI * 2) * f));
      this.py = this.px = x0;
      this.pyd = 0.0F;
      this.target = x0;
      executorService.execute(this::update);
   }

   public float update(float x) {
      this.target = x;
      return this.get();
   }

   public float get() {
      if (Float.isNaN(this.py)) {
         this.py = 0.0F;
      }

      if (Float.isNaN(this.pyd)) {
         this.pyd = 0.0F;
      }

      return this.py + 0.05F * this.pyd;
   }

   public void stop() {
      this.stop = true;
   }

   private void update() {
      while (!this.stop) {
         if (Float.isNaN(this.py)) {
            this.py = 0.0F;
         }

         if (Float.isNaN(this.pyd)) {
            this.pyd = 0.0F;
         }

         float t = 0.05F;
         float xd = (this.target - this.px) / t;
         float y = this.py + t * this.pyd;
         this.pyd = this.pyd + t * (this.px + this.k3 * xd - this.py - this.k1 * this.pyd) / this.k2;
         this.px = this.target;
         this.py = y;

         try {
            Thread.sleep(6L);
         } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
         }
      }
   }

   static {
      for (int i = 0; i < 15; i++) {
         executorService.execute(() -> {});
      }
   }
}
