package com.tacz.guns.api.client.animation.interpolator;

import com.tacz.guns.api.client.animation.AnimationChannelContent;

public interface Interpolator extends Cloneable {
   void compile(AnimationChannelContent var1);

   float[] interpolate(int var1, int var2, float var3);

   Interpolator clone();
}
