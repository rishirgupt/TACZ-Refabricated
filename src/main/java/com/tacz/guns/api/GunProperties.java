package com.tacz.guns.api;

import com.google.common.base.Suppliers;
import com.google.common.reflect.TypeToken;
import com.tacz.guns.api.modifier.ParameterizedCachePair;
import com.tacz.guns.resource.pojo.data.gun.ExplosionData;
import com.tacz.guns.resource.pojo.data.gun.ExtraDamage;
import com.tacz.guns.resource.pojo.data.gun.Ignite;
import com.tacz.guns.resource.pojo.data.gun.InaccuracyType;
import com.tacz.guns.resource.pojo.data.gun.MoveSpeed;
import it.unimi.dsi.fastutil.Pair;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.jetbrains.annotations.ApiStatus.Experimental;

public class GunProperties {
   static final Map<String, GunProperty<?>> ALL = new ConcurrentHashMap<>();
   static final Supplier<Map<String, GunProperty<?>>> ALL_CACHE_MODIFIABLE_BY_SCRIPT = Suppliers.memoize(
      () -> List.of(
            GunProperties.AMMO_SPEED,
            GunProperties.ARMOR_IGNORE,
            GunProperties.EFFECTIVE_RANGE,
            GunProperties.HEADSHOT_MULTIPLIER,
            GunProperties.KNOCKBACK,
            GunProperties.PIERCE,
            GunProperties.WEIGHT
         )
         .stream()
         .collect(Collectors.toMap(GunProperty::name, Function.identity()))
   );
   public static final GunProperty<Float> ADS_TIME = GunProperty.of("ads", Float.class);
   @Deprecated
   public static final GunProperty<Map<InaccuracyType, Float>> AIM_INACCURACY = GunProperty.of("inaccuracy", new TypeToken<Map<InaccuracyType, Float>>() {});
   public static final GunProperty<Float> AMMO_SPEED = GunProperty.of("ammo_speed", Float.class);
   public static final GunProperty<Float> ARMOR_IGNORE = GunProperty.of("armor_ignore", Float.class);
   public static final GunProperty<LinkedList<ExtraDamage.DistanceDamagePair>> DAMAGE = GunProperty.of(
      "damage", new TypeToken<LinkedList<ExtraDamage.DistanceDamagePair>>() {}
   );
   public static final GunProperty<Float> EFFECTIVE_RANGE = GunProperty.of("effective_range", Float.class);
   public static final GunProperty<ExplosionData> EXPLOSION = GunProperty.of("explosion", ExplosionData.class);
   public static final GunProperty<MoveSpeed> MOVE_SPEED = GunProperty.of("movement_speed", MoveSpeed.class);
   public static final GunProperty<Float> HEADSHOT_MULTIPLIER = GunProperty.of("head_shot", Float.class);
   public static final GunProperty<Ignite> IGNITE = GunProperty.of("ignite", Ignite.class);
   public static final GunProperty<Map<InaccuracyType, Float>> INACCURACY = GunProperty.of("inaccuracy", new TypeToken<Map<InaccuracyType, Float>>() {});
   public static final GunProperty<Float> KNOCKBACK = GunProperty.of("knockback", Float.class);
   public static final GunProperty<Integer> PIERCE = GunProperty.of("pierce", Integer.class);
   public static final GunProperty<ParameterizedCachePair<Float, Float>> RECOIL = GunProperty.of(
      "recoil", new TypeToken<ParameterizedCachePair<Float, Float>>() {}
   );
   public static final GunProperty<Integer> ROUNDS_PER_MINUTE = GunProperty.of("rpm", Integer.class);
   public static final GunProperty<Pair<Integer, Boolean>> SILENCE = GunProperty.of("silence", new TypeToken<Pair<Integer, Boolean>>() {});
   public static final GunProperty<Float> WEIGHT = GunProperty.of("weight_modifier", Float.class);

   public static Map<String, GunProperty<?>> all() {
      return Collections.unmodifiableMap(ALL);
   }

   public static Map<String, GunProperty<?>> allCacheModifiableByScript() {
      return ALL_CACHE_MODIFIABLE_BY_SCRIPT.get();
   }

   private GunProperties() {
   }

   @Experimental
   public static final class RuntimeOnly {
      public static final String MAX_HEAT = "max_heat";
      public static final String BULLET_AMOUNT = "bullet_amount";
      public static final String BURST_COUNT = "burst_count";
      public static final String BURST_SHOOT_INTERVAL = "burst_shoot_interval";
      public static final String BULLET_LIFE = "bullet_life";
      public static final String BULLET_GRAVITY = "bullet_gravity";
      public static final String BULLET_FRICTION = "bullet_friction";
      public static final String SOUND_DISTANCE = "sound_distance";
      public static final String IGNITE_ENTITY = "ignite_entity";
      public static final String IGNITE_ENTITY_TIME = "ignite_entity_time";
      public static final String IGNITE_BLOCK = "ignite_block";
      public static final String EXPLODE_ENABLED = "explode_enabled";
      public static final String EXPLOSION_DAMAGE = "explosion_damage";
      public static final String EXPLOSION_RADIUS = "explosion_radius";
      public static final String EXPLOSION_KNOCKBACK = "explosion_knockback";
      public static final String EXPLOSION_DESTROYS_BLOCK = "explosion_destroys_block";
      public static final String EXPLOSION_DELAY = "explosion_delay";
   }
}
