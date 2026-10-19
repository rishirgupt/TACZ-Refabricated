package com.tacz.guns.client.resource.pojo.display.gun;

import com.google.gson.annotations.SerializedName;
import org.joml.Vector3f;

public class ShellEjection {
   @SerializedName("initial_velocity")
   private Vector3f initialVelocity = new Vector3f(8.0F, 5.0F, -0.5F);
   @SerializedName("random_velocity")
   private Vector3f randomVelocity = new Vector3f(2.5F, 1.5F, 0.25F);
   @SerializedName("acceleration")
   private Vector3f acceleration = new Vector3f(0.0F, -20.0F, 0.0F);
   @SerializedName("angular_velocity")
   private Vector3f angularVelocity = new Vector3f(-720.0F, -720.0F, 90.0F);
   @SerializedName("living_time")
   private float livingTime = 1.0F;

   public Vector3f getInitialVelocity() {
      return this.initialVelocity;
   }

   public Vector3f getRandomVelocity() {
      return this.randomVelocity;
   }

   public Vector3f getAcceleration() {
      return this.acceleration;
   }

   public Vector3f getAngularVelocity() {
      return this.angularVelocity;
   }

   public float getLivingTime() {
      return this.livingTime;
   }
}
