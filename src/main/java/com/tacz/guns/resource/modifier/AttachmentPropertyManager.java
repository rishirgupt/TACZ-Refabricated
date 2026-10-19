package com.tacz.guns.resource.modifier;

import com.google.common.collect.Maps;
import com.tacz.guns.api.GunProperties;
import com.tacz.guns.api.GunProperty;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.event.common.AttachmentPropertyEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.modifier.IAttachmentModifier;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import com.tacz.guns.event.ChangeGunPropertyEvent;
import com.tacz.guns.resource.manager.AttachmentScriptManager;
import com.tacz.guns.resource.modifier.custom.AdsModifier;
import com.tacz.guns.resource.modifier.custom.AmmoSpeedModifier;
import com.tacz.guns.resource.modifier.custom.ArmorIgnoreModifier;
import com.tacz.guns.resource.modifier.custom.DamageModifier;
import com.tacz.guns.resource.modifier.custom.EffectiveRangeModifier;
import com.tacz.guns.resource.modifier.custom.ExplosionModifier;
import com.tacz.guns.resource.modifier.custom.ExtraMovementModifier;
import com.tacz.guns.resource.modifier.custom.HeadShotModifier;
import com.tacz.guns.resource.modifier.custom.IgniteModifier;
import com.tacz.guns.resource.modifier.custom.InaccuracyModifier;
import com.tacz.guns.resource.modifier.custom.KnockbackModifier;
import com.tacz.guns.resource.modifier.custom.PierceModifier;
import com.tacz.guns.resource.modifier.custom.RecoilModifier;
import com.tacz.guns.resource.modifier.custom.RpmModifier;
import com.tacz.guns.resource.modifier.custom.SilenceModifier;
import com.tacz.guns.resource.modifier.custom.WeightModifier;
import com.tacz.guns.resource.pojo.data.attachment.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;

public class AttachmentPropertyManager {
   private static final Map<String, IAttachmentModifier<?, ?>> MODIFIERS = Maps.newLinkedHashMap();

   public static void registerModifier() {
      MODIFIERS.put(AdsModifier.ID, new AdsModifier());
      MODIFIERS.put(AmmoSpeedModifier.ID, new AmmoSpeedModifier());
      MODIFIERS.put(ArmorIgnoreModifier.ID, new ArmorIgnoreModifier());
      MODIFIERS.put(DamageModifier.ID, new DamageModifier());
      MODIFIERS.put(EffectiveRangeModifier.ID, new EffectiveRangeModifier());
      MODIFIERS.put(ExplosionModifier.ID, new ExplosionModifier());
      MODIFIERS.put(HeadShotModifier.ID, new HeadShotModifier());
      MODIFIERS.put(IgniteModifier.ID, new IgniteModifier());
      MODIFIERS.put(InaccuracyModifier.ID, new InaccuracyModifier());
      MODIFIERS.put(KnockbackModifier.ID, new KnockbackModifier());
      MODIFIERS.put(PierceModifier.ID, new PierceModifier());
      MODIFIERS.put(RecoilModifier.ID, new RecoilModifier());
      MODIFIERS.put(RpmModifier.ID, new RpmModifier());
      MODIFIERS.put(SilenceModifier.ID, new SilenceModifier());
      MODIFIERS.put(WeightModifier.ID, new WeightModifier());
      MODIFIERS.put(ExtraMovementModifier.ID, new ExtraMovementModifier());
   }

   public static Map<String, IAttachmentModifier<?, ?>> getModifiers() {
      return MODIFIERS;
   }

   public static void postChangeEvent(LivingEntity shooter, ItemStack gunItem) {
      if (gunItem.getItem() instanceof IGun iGun) {
         Identifier gunId = iGun.getGunId(gunItem);
         TimelessAPI.getCommonGunIndex(gunId)
            .ifPresent(
               index -> {
                  AttachmentCacheProperty cacheProperty = new AttachmentCacheProperty();
                  AttachmentPropertyEvent event = new AttachmentPropertyEvent(gunItem, cacheProperty);
                  ChangeGunPropertyEvent.internalOnAttachmentPropertyEvent(event);
                  event.postEventToKubeJS(event);
                  ((AttachmentPropertyEvent.Callback)AttachmentPropertyEvent.CALLBACK.invoker()).post(event);
                  IGunOperator operator = IGunOperator.fromLivingEntity(shooter);
                  ShooterDataHolder dataHolder = operator.getDataHolder();
                  GunProperties.allCacheModifiableByScript()
                     .forEach(
                        (id, property) -> modifyCached(iGun, dataHolder, gunItem, shooter, property, cacheProperty)
                     );
                  operator.updateCacheProperty(cacheProperty);
               }
            );
      }
   }

   private static <T> void modifyCached(IGun iGun, ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter, GunProperty<T> property, AttachmentCacheProperty cacheProperty) {
      iGun.modifyProperty(dataHolder, gunItem, shooter, "modify_cached_property", property.name(), property.type(), cacheProperty.getCache(property));
   }

   public static double eval(Modifier modifier, double defaultValue) {
      return eval(Collections.singletonList(modifier), defaultValue);
   }

   public static double eval(List<Modifier> modifiers, double defaultValue) {
      double addend = defaultValue;
      double percent = 1.0;
      double multiplier = 1.0;

      for (Modifier modifier : modifiers) {
         addend += modifier.getAddend();
         percent += modifier.getPercent();
         multiplier *= Math.max(modifier.getMultiplier(), 0.0);
      }

      percent = Math.max(percent, 0.0);
      double value = addend * percent * multiplier;

      for (Modifier modifier : modifiers) {
         AttachmentScriptManager.CompiledScript function = modifier.getCompiledFunction();
         if (function != null) {
            value = function.eval(value, defaultValue);
         }
      }

      return value;
   }

   public static boolean eval(List<Boolean> modified, boolean defaultValue) {
      return defaultValue ? modified.stream().allMatch(s -> s) : modified.stream().anyMatch(s -> s);
   }

   public static double functionEval(double value, double defaultValue, String script) {
      AttachmentScriptManager.CompiledScript function = AttachmentScriptManager.compile(script);
      return function == null ? value : function.eval(value, defaultValue);
   }
}
