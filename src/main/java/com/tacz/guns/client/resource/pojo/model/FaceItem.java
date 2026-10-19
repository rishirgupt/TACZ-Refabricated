package com.tacz.guns.client.resource.pojo.model;

import com.google.gson.annotations.SerializedName;

public class FaceItem {
   public static final FaceItem EMPTY = empty();
   @SerializedName("uv")
   private float[] uv;
   @SerializedName("uv_size")
   private float[] uvSize;

   public static FaceItem single16X() {
      FaceItem face = new FaceItem();
      face.uv = new float[]{0.0F, 0.0F};
      face.uvSize = new float[]{16.0F, 16.0F};
      return face;
   }

   private static FaceItem empty() {
      FaceItem face = new FaceItem();
      face.uv = new float[]{0.0F, 0.0F};
      face.uvSize = new float[]{0.0F, 0.0F};
      return face;
   }

   public float[] getUv() {
      return this.uv;
   }

   public float[] getUvSize() {
      return this.uvSize;
   }
}
