package com.tacz.guns.resource.modifier.custom;

import com.google.gson.annotations.SerializedName;
import com.tacz.guns.api.GunProperties;
import com.tacz.guns.api.modifier.CacheValue;
import com.tacz.guns.api.modifier.IAttachmentModifier;
import com.tacz.guns.api.modifier.JsonProperty;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.resource.pojo.data.gun.MoveSpeed;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class ExtraMovementModifier implements IAttachmentModifier<MoveSpeed, MoveSpeed> {
   public static final String ID = GunProperties.MOVE_SPEED.name();

   @Override
   public String getId() {
      return ID;
   }

   @Override
   public JsonProperty<MoveSpeed> readJson(String json) {
      ExtraMovementModifier.Data data = (ExtraMovementModifier.Data)CommonAssetsManager.GSON.fromJson(json, ExtraMovementModifier.Data.class);
      MoveSpeed moveSpeed = data.getMoveSpeed();
      return new ExtraMovementModifier.ExtraSpeedJsonProperty(moveSpeed);
   }

   @Override
   public CacheValue<MoveSpeed> initCache(ItemStack gunItem, GunData gunData) {
      return new CacheValue<>(gunData.getMoveSpeed());
   }

   @Override
   public void eval(List<MoveSpeed> modifiers, CacheValue<MoveSpeed> cache) {
      cache.setValue(MoveSpeed.of(cache.getValue(), modifiers));
   }

   public static class Data {
      @SerializedName("movement_speed")
      @Nullable
      private MoveSpeed moveSpeed = null;

      @Nullable
      public MoveSpeed getMoveSpeed() {
         return this.moveSpeed;
      }
   }

   public static class ExtraSpeedJsonProperty extends JsonProperty<MoveSpeed> {
      public ExtraSpeedJsonProperty(MoveSpeed value) {
         super(value);
      }

      @Override
      public void initComponents() {
         MoveSpeed speed = this.getValue();
         if (speed != null) {
            this.resolveComponent(speed.getBaseMultiplier(), "movement_speed");
            this.resolveComponent(speed.getAimMultiplier(), "aim_speed");
            this.resolveComponent(speed.getReloadMultiplier(), "reload_speed");
         }
      }

      private void resolveComponent(float amount, String key) {
         if (amount > 0.0F) {
            this.components.add(Component.translatable(String.format("tooltip.tacz.attachment.%s.increase", key)).withStyle(ChatFormatting.GREEN));
         } else if (amount < 0.0F) {
            this.components.add(Component.translatable(String.format("tooltip.tacz.attachment.%s.decrease", key)).withStyle(ChatFormatting.RED));
         }
      }
   }
}
