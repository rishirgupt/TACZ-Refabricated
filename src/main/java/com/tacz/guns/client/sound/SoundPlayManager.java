package com.tacz.guns.client.sound;

import com.tacz.guns.GunMod;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.config.client.SoundConfig;
import com.tacz.guns.config.common.GunConfig;
import com.tacz.guns.init.ModSounds;
import com.tacz.guns.network.message.ServerMessageSound;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.sound.SoundManager;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.FileToIdConverter;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class SoundPlayManager {
   private static final FileToIdConverter TACZ_SOUND_LISTER = new FileToIdConverter("tacz_sounds", ".ogg");
   private static final Map<SoundPlayManager.SoundKey, ArrayDeque<SoundPlayManager.TrackedGunSound>> TRACKED_GUN_SOUNDS = new HashMap<>();
   private static final Map<Identifier, Boolean> SOUND_RESOURCE_EXISTS_CACHE = new HashMap<>();
   private static final Set<Identifier> MISSING_SOUND_WARNED = new HashSet<>();
   private static int soundCleanupTickCounter = 0;
   private static boolean DRY_SOUND_TRACK = true;
   private static GunSoundInstance tmpSoundInstance = null;

   @Nullable
   public static GunSoundInstance playClientSound(Entity entity, @Nullable Identifier name, float volume, float pitch, int distance, boolean mono) {
      return playClientSound(entity, name, volume, pitch, distance, mono, (Integer)SoundConfig.DEFAULT_SOUND_CONCURRENCY_LIMIT.get(), true, false);
   }

   @Nullable
   private static GunSoundInstance playClientSound(
      Entity entity,
      @Nullable Identifier name,
      float volume,
      float pitch,
      int distance,
      boolean mono,
      int concurrencyLimit,
      boolean trackEntity,
      boolean relative
   ) {
      Minecraft minecraft = Minecraft.getInstance();
      if (name != null && hasSoundResource(minecraft, name)) {
         if (concurrencyLimit > 0) {
            limitConcurrentGunSound(minecraft, entity.getId(), name, concurrencyLimit);
         }

         GunSoundInstance instance = trackEntity
            ? new EntityTrackingGunSoundInstance(ModSounds.GUN, SoundSource.PLAYERS, volume, pitch, entity, distance, name, mono)
            : new GunSoundInstance(ModSounds.GUN, SoundSource.PLAYERS, volume, pitch, entity, distance, name, mono, relative);
         minecraft.getSoundManager().play(instance);
         if (concurrencyLimit > 0) {
            trackGunSound(entity.getId(), entity.getUUID(), name, instance);
         }

         return instance;
      } else {
         return null;
      }
   }

   @Nullable
   public static GunSoundInstance playClientSound(Entity entity, @Nullable Identifier name, float volume, float pitch, int distance) {
      return playClientSound(entity, name, volume, pitch, distance, false);
   }

   @Nullable
   public static GunSoundInstance playAnimationSound(Entity entity, @Nullable Identifier name, float volume, float pitch, int distance) {
      if (isLocalPlayer(entity)) {
         boolean trackFirstPerson = (Boolean)SoundConfig.FIRST_PERSON_ANIMATION_SOUND_TRACKING.get();
         return playClientSound(
            entity,
            name,
            volume,
            pitch,
            distance,
            false,
            (Integer)SoundConfig.HIGH_FREQUENCY_SOUND_CONCURRENCY_LIMIT.get(),
            trackFirstPerson,
            !trackFirstPerson
         );
      } else {
         return playClientSound(entity, name, volume, pitch, distance, false, (Integer)SoundConfig.HIGH_FREQUENCY_SOUND_CONCURRENCY_LIMIT.get(), true, false);
      }
   }

   public static void stopPlayGunSound() {
      if (tmpSoundInstance != null) {
         tmpSoundInstance.setStop();
      }
   }

   public static void stopPlayGunSound(GunDisplayInstance gunIndex, String animationName) {
      if (tmpSoundInstance != null
         && tmpSoundInstance.getRegistryName() != null
         && tmpSoundInstance.getRegistryName().equals(gunIndex.getSounds(animationName))) {
         tmpSoundInstance.setStop();
      }
   }

   public static void playerRefitSound(ItemStack attachmentItem, LocalPlayer player, String soundName) {
      IAttachment iAttachment = IAttachment.getIAttachmentOrNull(attachmentItem);
      if (iAttachment != null) {
         Identifier attachmentId = iAttachment.getAttachmentId(attachmentItem);
         TimelessAPI.getClientAttachmentIndex(attachmentId).ifPresent(index -> {
            Map<String, Identifier> sounds = index.getSounds();
            if (sounds.containsKey(soundName)) {
               Identifier resourceLocation = sounds.get(soundName);
               playClientSound(player, resourceLocation, 1.0F, 1.0F, (Integer)GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get());
            }
         });
      }
   }

   public static void playShootSound(LivingEntity entity, GunDisplayInstance gunIndex, GunData gunData) {
      playClientSound(
         entity,
         gunIndex.getSounds(SoundManager.SHOOT_SOUND),
         0.8F,
         0.9F + entity.getRandom().nextFloat() * 0.125F,
         (int)(((Integer)GunConfig.DEFAULT_GUN_FIRE_SOUND_DISTANCE.get()).intValue() * gunData.getFireSound().getFireMultiplier()),
         false,
         (Integer)SoundConfig.HIGH_FREQUENCY_SOUND_CONCURRENCY_LIMIT.get(),
         false,
         false
      );
   }

   public static void playSilenceSound(LivingEntity entity, GunDisplayInstance gunIndex, GunData gunData) {
      playClientSound(
         entity,
         gunIndex.getSounds(SoundManager.SILENCE_SOUND),
         0.6F,
         0.9F + entity.getRandom().nextFloat() * 0.125F,
         (int)(((Integer)GunConfig.DEFAULT_GUN_SILENCE_SOUND_DISTANCE.get()).intValue() * gunData.getFireSound().getSilenceMultiplier()),
         false,
         (Integer)SoundConfig.HIGH_FREQUENCY_SOUND_CONCURRENCY_LIMIT.get(),
         false,
         false
      );
   }

   public static void playDryFireSound(LivingEntity entity, GunDisplayInstance gunIndex) {
      if (DRY_SOUND_TRACK) {
         playClientSound(
            entity,
            gunIndex.getSounds(SoundManager.DRY_FIRE_SOUND),
            1.0F,
            0.9F + entity.getRandom().nextFloat() * 0.125F,
            (Integer)GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get()
         );
         DRY_SOUND_TRACK = false;
      }
   }

   public static void resetDryFireSound() {
      DRY_SOUND_TRACK = true;
   }

   public static void playReloadSound(LivingEntity entity, GunDisplayInstance display, boolean noAmmo) {
      if (noAmmo) {
         tmpSoundInstance = playClientSound(
            entity, display.getSounds(SoundManager.RELOAD_EMPTY_SOUND), 1.0F, 1.0F, (Integer)GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get()
         );
      } else {
         tmpSoundInstance = playClientSound(
            entity, display.getSounds(SoundManager.RELOAD_TACTICAL_SOUND), 1.0F, 1.0F, (Integer)GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get()
         );
      }
   }

   public static void playInspectSound(LivingEntity entity, GunDisplayInstance display, boolean noAmmo) {
      if (noAmmo) {
         tmpSoundInstance = playClientSound(
            entity, display.getSounds(SoundManager.INSPECT_EMPTY_SOUND), 1.0F, 1.0F, (Integer)GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get()
         );
      } else {
         tmpSoundInstance = playClientSound(
            entity, display.getSounds(SoundManager.INSPECT_SOUND), 1.0F, 1.0F, (Integer)GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get()
         );
      }
   }

   public static void playBoltSound(LivingEntity entity, GunDisplayInstance display) {
      tmpSoundInstance = playClientSound(
         entity, display.getSounds(SoundManager.BOLT_SOUND), 1.0F, 1.0F, (Integer)GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get()
      );
   }

   public static void playDrawSound(LivingEntity entity, GunDisplayInstance display) {
      tmpSoundInstance = playClientSound(
         entity, display.getSounds(SoundManager.DRAW_SOUND), 1.0F, 1.0F, (Integer)GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get()
      );
   }

   public static void playPutAwaySound(LivingEntity entity, GunDisplayInstance display) {
      tmpSoundInstance = playClientSound(
         entity, display.getSounds(SoundManager.PUT_AWAY_SOUND), 1.0F, 1.0F, (Integer)GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get()
      );
   }

   public static void playFireSelectSound(LivingEntity entity, GunDisplayInstance display) {
      playClientSound(entity, display.getSounds(SoundManager.FIRE_SELECT), 1.0F, 1.0F, (Integer)GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get());
   }

   public static void playMeleeBayonetSound(LivingEntity entity, GunDisplayInstance display) {
      playClientSound(
         entity,
         display.getSounds(SoundManager.MELEE_BAYONET),
         1.0F,
         0.9F + entity.getRandom().nextFloat() * 0.125F,
         (Integer)GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get()
      );
   }

   public static void playMeleePushSound(LivingEntity entity, GunDisplayInstance display) {
      playClientSound(
         entity,
         display.getSounds(SoundManager.MELEE_PUSH),
         1.0F,
         0.9F + entity.getRandom().nextFloat() * 0.125F,
         (Integer)GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get()
      );
   }

   public static void playMeleeStockSound(LivingEntity entity, GunDisplayInstance display) {
      playClientSound(
         entity,
         display.getSounds(SoundManager.MELEE_STOCK),
         1.0F,
         0.9F + entity.getRandom().nextFloat() * 0.125F,
         (Integer)GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get()
      );
   }

   public static void playHeadHitSound(LivingEntity entity, GunDisplayInstance display) {
      boolean relative = isLocalPlayer(entity);
      playClientSound(
         entity,
         display.getSounds(SoundManager.HEAD_HIT_SOUND),
         1.0F,
         1.0F,
         (Integer)GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get(),
         false,
         (Integer)SoundConfig.HIT_SOUND_CONCURRENCY_LIMIT.get(),
         !relative,
         relative
      );
   }

   public static void playFleshHitSound(LivingEntity entity, GunDisplayInstance display) {
      boolean relative = isLocalPlayer(entity);
      playClientSound(
         entity,
         display.getSounds(SoundManager.FLESH_HIT_SOUND),
         1.0F,
         1.0F,
         (Integer)GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get(),
         false,
         (Integer)SoundConfig.HIT_SOUND_CONCURRENCY_LIMIT.get(),
         !relative,
         relative
      );
   }

   public static void playKillSound(LivingEntity entity, GunDisplayInstance display) {
      playClientSound(entity, display.getSounds(SoundManager.KILL_SOUND), 1.0F, 1.0F, (Integer)GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get());
   }

   public static void playMessageSound(ServerMessageSound message) {
      ClientLevel level = Minecraft.getInstance().level;
      if (level != null && level.getEntity(message.entityId()) instanceof LivingEntity livingEntity) {
         Identifier var5 = message.gunId();
         Identifier gunDisplayId = message.gunDisplayId();
         TimelessAPI.getGunDisplay(gunDisplayId, var5)
            .ifPresent(
               index -> {
                  String soundName = message.messageSound().soundName();
                  Identifier soundId = index.getSounds(soundName);
                  if (soundId != null) {
                     ServerMessageSound.MessageSound soundMessage = message.messageSound();
                     if (!SoundManager.SHOOT_3P_SOUND.equals(soundName) && !SoundManager.SILENCE_3P_SOUND.equals(soundName)) {
                        playClientSound(livingEntity, soundId, soundMessage.volume(), soundMessage.pitch(), soundMessage.distance());
                     } else {
                        playClientSound(
                           livingEntity,
                           soundId,
                           soundMessage.volume(),
                           soundMessage.pitch(),
                           soundMessage.distance(),
                           true,
                           (Integer)SoundConfig.HIGH_FREQUENCY_SOUND_CONCURRENCY_LIMIT.get(),
                           true,
                           false
                        );
                     }
                  }
               }
            );
      }
   }

   public static void onClientTick(Minecraft minecraft) {
      if (minecraft.level == null) {
         cleanupInvalidEntitySounds(minecraft);
      } else {
         soundCleanupTickCounter++;
         if (soundCleanupTickCounter % 5 == 0) {
            cleanupInvalidEntitySounds(minecraft);
         }
      }
   }

   public static void clearSoundResourceCache() {
      SOUND_RESOURCE_EXISTS_CACHE.clear();
      MISSING_SOUND_WARNED.clear();
   }

   private static void limitConcurrentGunSound(Minecraft minecraft, int entityId, Identifier soundId, int limit) {
      SoundPlayManager.SoundKey key = new SoundPlayManager.SoundKey(entityId, soundId);
      ArrayDeque<SoundPlayManager.TrackedGunSound> sounds = TRACKED_GUN_SOUNDS.get(key);
      if (sounds != null) {
         int activeForKey = 0;
         Iterator<SoundPlayManager.TrackedGunSound> iterator = sounds.iterator();

         while (iterator.hasNext()) {
            SoundPlayManager.TrackedGunSound tracked = iterator.next();
            if (minecraft.getSoundManager().isActive(tracked.instance())) {
               activeForKey++;
            } else {
               iterator.remove();
            }
         }

         if (sounds.isEmpty()) {
            TRACKED_GUN_SOUNDS.remove(key);
         }

         int toStop = activeForKey - limit + 1;
         if (toStop > 0) {
            int stopped = 0;
            iterator = sounds.iterator();

            while (iterator.hasNext() && stopped < toStop) {
               SoundPlayManager.TrackedGunSound tracked = iterator.next();
               if (minecraft.getSoundManager().isActive(tracked.instance())) {
                  tracked.instance().setStop();
                  iterator.remove();
                  stopped++;
               }
            }

            if (sounds.isEmpty()) {
               TRACKED_GUN_SOUNDS.remove(key);
            }
         }
      }
   }

   private static void trackGunSound(int entityId, UUID entityUuid, Identifier soundId, GunSoundInstance instance) {
      SoundPlayManager.SoundKey key = new SoundPlayManager.SoundKey(entityId, soundId);
      TRACKED_GUN_SOUNDS.computeIfAbsent(key, ignored -> new ArrayDeque<>()).addLast(new SoundPlayManager.TrackedGunSound(instance, entityUuid));
   }

   private static void cleanupInvalidEntitySounds(Minecraft minecraft) {
      if (minecraft.level == null) {
         stopAndClearTrackedSounds();
      } else {
         Iterator<Entry<SoundPlayManager.SoundKey, ArrayDeque<SoundPlayManager.TrackedGunSound>>> entryIterator = TRACKED_GUN_SOUNDS.entrySet().iterator();

         while (entryIterator.hasNext()) {
            Entry<SoundPlayManager.SoundKey, ArrayDeque<SoundPlayManager.TrackedGunSound>> entry = entryIterator.next();
            Iterator<SoundPlayManager.TrackedGunSound> soundIterator = entry.getValue().iterator();

            while (soundIterator.hasNext()) {
               SoundPlayManager.TrackedGunSound tracked = soundIterator.next();
               if (!minecraft.getSoundManager().isActive(tracked.instance())) {
                  soundIterator.remove();
               } else {
                  Entity owner = minecraft.level.getEntity(entry.getKey().entityId());
                  if (isInvalidSoundOwner(owner, tracked.entityUuid())) {
                     tracked.instance().setStop();
                     soundIterator.remove();
                  }
               }
            }

            if (entry.getValue().isEmpty()) {
               entryIterator.remove();
            }
         }
      }
   }

   private static void stopAndClearTrackedSounds() {
      for (ArrayDeque<SoundPlayManager.TrackedGunSound> sounds : TRACKED_GUN_SOUNDS.values()) {
         for (SoundPlayManager.TrackedGunSound tracked : sounds) {
            tracked.instance().setStop();
         }
      }

      TRACKED_GUN_SOUNDS.clear();
   }

   private static boolean isInvalidSoundOwner(@Nullable Entity entity, UUID entityUuid) {
      return entity == null
         || !entity.getUUID().equals(entityUuid)
         || entity.isRemoved()
         || entity instanceof LivingEntity livingEntity && livingEntity.isDeadOrDying();
   }

   private static boolean isLocalPlayer(Entity entity) {
      return entity == Minecraft.getInstance().player;
   }

   private static boolean hasSoundResource(Minecraft minecraft, Identifier soundId) {
      boolean exists = SOUND_RESOURCE_EXISTS_CACHE.computeIfAbsent(soundId, id -> {
         Identifier soundPathx = TACZ_SOUND_LISTER.idToFile(id);
         return minecraft.getResourceManager().getResource(soundPathx).isPresent();
      });
      if (!exists && MISSING_SOUND_WARNED.add(soundId)) {
         Identifier soundPath = TACZ_SOUND_LISTER.idToFile(soundId);
         GunMod.LOGGER.warn("[TACZ Sound] Missing gun sound resource, skipped. sound={}, path={}", soundId, soundPath);
      }

      return exists;
   }

   private record SoundKey(int entityId, Identifier soundId) {
   }

   private record TrackedGunSound(GunSoundInstance instance, UUID entityUuid) {
   }
}
