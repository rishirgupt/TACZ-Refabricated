package com.tacz.guns.client.resource.pojo;

import com.google.gson.annotations.SerializedName;
import javax.annotation.Nullable;
import org.joml.Vector3f;

public class TransformScale {
   @SerializedName("thirdperson")
   @Nullable
   private Vector3f thirdPerson;
   @SerializedName("ground")
   @Nullable
   private Vector3f ground;
   @SerializedName("fixed")
   @Nullable
   private Vector3f fixed;

   public static TransformScale getAmmoDefault() {
      TransformScale transformScale = new TransformScale();
      transformScale.thirdPerson = new Vector3f(0.75F, 0.75F, 0.75F);
      transformScale.ground = new Vector3f(0.75F, 0.75F, 0.75F);
      transformScale.fixed = new Vector3f(1.5F, 1.5F, 1.5F);
      return transformScale;
   }

   public static TransformScale getGunDefault() {
      TransformScale transformScale = new TransformScale();
      transformScale.thirdPerson = new Vector3f(0.6F, 0.6F, 0.6F);
      transformScale.ground = new Vector3f(0.6F, 0.6F, 0.6F);
      transformScale.fixed = new Vector3f(1.2F, 1.2F, 1.2F);
      return transformScale;
   }

   @Nullable
   public Vector3f getThirdPerson() {
      return this.thirdPerson;
   }

   @Nullable
   public Vector3f getGround() {
      return this.ground;
   }

   @Nullable
   public Vector3f getFixed() {
      return this.fixed;
   }
}
