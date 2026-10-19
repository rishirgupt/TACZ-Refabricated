package com.tacz.guns.api.client.animation;

public interface AnimationListener {
   void update(float[] var1, boolean var2);

   float[] initialValue();

   ObjectAnimationChannel.ChannelType getType();
}
