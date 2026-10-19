package com.tacz.guns.client.resource.pojo.display.gun;

import com.google.gson.annotations.SerializedName;
import org.joml.Vector3f;

public class LayerGunShow {
   @SerializedName("pos")
   private Vector3f pos = new Vector3f(-2.0F, 20.0F, 4.0F);
   @SerializedName("rotate")
   private Vector3f rotate = new Vector3f(0.0F, 0.0F, -30.0F);
   @SerializedName("scale")
   private Vector3f scale = new Vector3f(0.6F, 0.6F, 0.6F);

   public Vector3f getPos() {
      return this.pos;
   }

   public Vector3f getRotate() {
      return this.rotate;
   }

   public Vector3f getScale() {
      return this.scale;
   }
}
