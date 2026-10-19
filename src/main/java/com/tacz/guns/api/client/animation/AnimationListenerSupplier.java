package com.tacz.guns.api.client.animation;

import javax.annotation.Nullable;

public interface AnimationListenerSupplier {
   @Nullable
   AnimationListener supplyListeners(String var1, ObjectAnimationChannel.ChannelType var2);
}
