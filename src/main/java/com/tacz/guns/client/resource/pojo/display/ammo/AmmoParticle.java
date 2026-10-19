package com.tacz.guns.client.resource.pojo.display.ammo;

import com.google.gson.annotations.SerializedName;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleOptions;
import org.joml.Vector3f;

public class AmmoParticle {
   private static final Vector3f ZERO = new Vector3f(0.0F, 0.0F, 0.0F);
   @SerializedName("name")
   private String name;
   @SerializedName("delta")
   private Vector3f delta = ZERO;
   @SerializedName("speed")
   private float speed = 0.0F;
   @SerializedName("life_time")
   private int lifeTime = 20;
   @SerializedName("count")
   private int count = 1;
   private transient ParticleOptions particleOptions;

   public String getName() {
      return this.name;
   }

   public Vector3f getDelta() {
      return this.delta;
   }

   public float getSpeed() {
      return this.speed;
   }

   public int getCount() {
      return this.count;
   }

   public int getLifeTime() {
      return this.lifeTime;
   }

   @Nullable
   public ParticleOptions getParticleOptions() {
      return this.particleOptions;
   }

   public void setParticleOptions(ParticleOptions particleOptions) {
      this.particleOptions = particleOptions;
   }
}
