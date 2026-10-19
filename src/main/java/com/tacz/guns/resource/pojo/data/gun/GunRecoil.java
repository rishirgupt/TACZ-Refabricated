package com.tacz.guns.resource.pojo.data.gun;

import com.google.gson.annotations.SerializedName;
import javax.annotation.Nullable;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

public class GunRecoil {
   private static final SplineInterpolator INTERPOLATOR = new SplineInterpolator();
   @SerializedName("pitch")
   @Nullable
   private GunRecoilKeyFrame[] pitch;
   @SerializedName("yaw")
   @Nullable
   private GunRecoilKeyFrame[] yaw;

   public GunRecoilKeyFrame[] getPitch() {
      return this.pitch;
   }

   public void setPitch(@Nullable GunRecoilKeyFrame[] pitch) {
      this.pitch = pitch;
   }

   public GunRecoilKeyFrame[] getYaw() {
      return this.yaw;
   }

   public void setYaw(@Nullable GunRecoilKeyFrame[] yaw) {
      this.yaw = yaw;
   }

   @Nullable
   public PolynomialSplineFunction genPitchSplineFunction(float modifier) {
      return this.getSplineFunction(this.pitch, modifier);
   }

   @Nullable
   public PolynomialSplineFunction genYawSplineFunction(float modifier) {
      return this.getSplineFunction(this.yaw, modifier);
   }

   private PolynomialSplineFunction getSplineFunction(GunRecoilKeyFrame[] keyFrames, float modifier) {
      if (keyFrames != null && keyFrames.length != 0) {
         double[] values = new double[keyFrames.length + 1];
         double[] times = new double[keyFrames.length + 1];
         times[0] = 0.0;
         values[0] = 0.0;

         for (int i = 0; i < keyFrames.length; i++) {
            times[i + 1] = keyFrames[i].getTime() * 1000.0F + 30.0F;
         }

         for (int i = 0; i < keyFrames.length; i++) {
            float[] value = keyFrames[i].getValue();
            values[i + 1] = (value[0] + Math.random() * (value[1] - value[0])) * modifier;
         }

         return INTERPOLATOR.interpolate(times, values);
      } else {
         return null;
      }
   }
}
