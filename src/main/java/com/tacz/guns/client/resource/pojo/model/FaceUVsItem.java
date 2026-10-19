package com.tacz.guns.client.resource.pojo.model;

import com.google.gson.annotations.SerializedName;
import net.minecraft.core.Direction;

public class FaceUVsItem {
   @SerializedName("down")
   private FaceItem down;
   @SerializedName("east")
   private FaceItem east;
   @SerializedName("north")
   private FaceItem north;
   @SerializedName("south")
   private FaceItem south;
   @SerializedName("up")
   private FaceItem up;
   @SerializedName("west")
   private FaceItem west;

   public static FaceUVsItem singleSouthFace() {
      FaceUVsItem faces = new FaceUVsItem();
      faces.north = FaceItem.EMPTY;
      faces.east = FaceItem.EMPTY;
      faces.west = FaceItem.EMPTY;
      faces.south = FaceItem.single16X();
      faces.up = FaceItem.EMPTY;
      faces.down = FaceItem.EMPTY;
      return faces;
   }

   public FaceItem getFace(Direction direction) {
      switch (direction) {
         case EAST:
            return this.west == null ? FaceItem.EMPTY : this.west;
         case WEST:
            return this.east == null ? FaceItem.EMPTY : this.east;
         case NORTH:
            return this.north == null ? FaceItem.EMPTY : this.north;
         case SOUTH:
            return this.south == null ? FaceItem.EMPTY : this.south;
         case UP:
            return this.down == null ? FaceItem.EMPTY : this.down;
         case DOWN:
         default:
            return this.up == null ? FaceItem.EMPTY : this.up;
      }
   }
}
