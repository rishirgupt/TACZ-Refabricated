package com.tacz.guns.client.model.listener.model;

import com.tacz.guns.api.client.animation.AnimationListener;
import com.tacz.guns.api.client.animation.ObjectAnimationChannel;
import com.tacz.guns.client.model.BedrockGunModel;

public class ModelAdditionalMagazineListener implements AnimationListener {
   private final AnimationListener listener;
   private final BedrockGunModel model;

   public ModelAdditionalMagazineListener(AnimationListener listener, BedrockGunModel model) {
      this.listener = listener;
      this.model = model;
   }

   @Override
   public void update(float[] values, boolean blend) {
      this.listener.update(values, blend);
      net.minecraft.client.player.LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;
      boolean reloading = player != null && com.tacz.guns.api.entity.IGunOperator.fromLivingEntity(player).getSynReloadState().getStateType().isReloading();
      if (reloading && this.model.getAdditionalMagazineNode() != null) {
         this.model.getAdditionalMagazineNode().visible = true;
      }
   }

   @Override
   public float[] initialValue() {
      return this.listener.initialValue();
   }

   @Override
   public ObjectAnimationChannel.ChannelType getType() {
      return this.listener.getType();
   }
}
