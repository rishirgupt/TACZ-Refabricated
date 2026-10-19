package cn.sh1rocu.tacz;

import cn.sh1rocu.tacz.api.event.AddReloadListenerEvent;
import cn.sh1rocu.tacz.api.event.EntityJoinLevelEvent;
import cn.sh1rocu.tacz.api.event.LivingHurtEvent;
import cn.sh1rocu.tacz.api.event.LivingKnockBackEvent;
import cn.sh1rocu.tacz.api.event.PlayerEvent;
import cn.sh1rocu.tacz.api.event.PlayerTickEvent;
import cn.sh1rocu.tacz.util.forge.EnumArgument;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.event.server.AmmoHitBlockEvent;
import com.tacz.guns.config.ClientConfig;
import com.tacz.guns.config.CommonConfig;
import com.tacz.guns.config.PreLoadConfig;
import com.tacz.guns.config.ServerConfig;
import com.tacz.guns.crafting.NBTIngredient;
import com.tacz.guns.event.CommonLoadPack;
import com.tacz.guns.event.EntityDamageEvent;
import com.tacz.guns.event.HitboxHelperEvent;
import com.tacz.guns.event.KnockbackChange;
import com.tacz.guns.event.LoadingConfigEvent;
import com.tacz.guns.event.PlayerRespawnEvent;
import com.tacz.guns.event.PreventGunClick;
import com.tacz.guns.event.ServerTickEvent;
import com.tacz.guns.event.SyncBaseTimestamp;
import com.tacz.guns.event.SyncedEntityDataEvent;
import com.tacz.guns.event.TravelToDimensionEvent;
import com.tacz.guns.event.ammo.BellRing;
import com.tacz.guns.event.ammo.DestroyGlassBlock;
import com.tacz.guns.init.CapabilityRegistry;
import com.tacz.guns.init.CommandRegistry;
import com.tacz.guns.init.CommonRegistry;
import com.tacz.guns.init.CompatRegistry;
import com.tacz.guns.network.HandshakeNetworking;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.network.message.handshake.AcknowledgeC2SPacket;
import com.tacz.guns.network.message.handshake.SyncedEntityDataMappingS2CPacket;
import com.tacz.guns.resource.CommonAssetsManager;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.v5.ModConfigEvents;
import java.lang.ref.WeakReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarted;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarting;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents.Configure;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.config.ModConfig.Type;
import org.jetbrains.annotations.Nullable;

public class TaCZFabric implements ModInitializer {
   public static final Identifier HIGHEST = Identifier.fromNamespaceAndPath("tacz", "event_highest_priority");
   public static final Identifier HIGH = Identifier.fromNamespaceAndPath("tacz", "event_high_priority");
   public static final Identifier LOW = Identifier.fromNamespaceAndPath("tacz", "event_low_priority");
   public static final Identifier LOWEST = Identifier.fromNamespaceAndPath("tacz", "event_lowest_priority");
   public static final Provider VANILLA_ACCESS = VanillaRegistries.createLookup();
   @Nullable
   private static WeakReference<MinecraftServer> server;

   @Nullable
   public static MinecraftServer getServer() {
      return server == null ? null : server.get();
   }

   public void onInitialize() {
      PreLoadConfig.init();
      ConfigRegistry.INSTANCE.register("tacz", Type.COMMON, CommonConfig.init());
      ConfigRegistry.INSTANCE.register("tacz", Type.SERVER, ServerConfig.init());
      ConfigRegistry.INSTANCE.register("tacz", Type.CLIENT, ClientConfig.init());
      PayloadTypeRegistry.clientboundConfiguration().register(SyncedEntityDataMappingS2CPacket.TYPE, SyncedEntityDataMappingS2CPacket.STREAM_CODEC);
      PayloadTypeRegistry.serverboundConfiguration().register(AcknowledgeC2SPacket.TYPE, AcknowledgeC2SPacket.STREAM_CODEC);
      ServerConfigurationNetworking.registerGlobalReceiver(AcknowledgeC2SPacket.TYPE, AcknowledgeC2SPacket::handle);
      ServerConfigurationConnectionEvents.CONFIGURE.register((Configure)(handler, server) -> {
         if (ServerConfigurationNetworking.canSend(handler, SyncedEntityDataMappingS2CPacket.TYPE)) {
            handler.addTask(new HandshakeNetworking.SyncedEntityDataTask(handler));
         }
      });
      NetworkHandler.registerPackets();
      net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> NetworkHandler.sendRecipes(server, java.util.List.of(handler.getPlayer())));
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> {
         com.tacz.guns.resource.CommonAssetsManager.onReload(server.registryAccess(), false);
         NetworkHandler.sendRecipes(server, server.getPlayerList().getPlayers());
      });
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED.register(server -> com.tacz.guns.resource.CommonAssetsManager.onReload(server.registryAccess(), false));
      GunMod.setup();
      CommandRegistry.onServerStaring();
      CompatRegistry.onEnqueue();
      ArgumentTypeRegistry.registerArgumentType(Identifier.fromNamespaceAndPath("tacz", "enum_argument"), EnumArgument.class, new EnumArgument.Info());
      if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
         CommonLoadPack.loadGunPack();
      }

      CustomIngredientSerializer.register(NBTIngredient.Serializer.INSTANCE);
      ServerLifecycleEvents.SERVER_STARTING.register((ServerStarting)server -> TaCZFabric.server = new WeakReference<>(server));
      this.subscribeEvents();
   }

   private void subscribeEvents() {
      CapabilityRegistry.init();
      AddReloadListenerEvent.CALLBACK.register(CommonAssetsManager::onReload);
      CommonLifecycleEvents.TAGS_LOADED.register(CommonAssetsManager::onReload);
      ServerLifecycleEvents.SERVER_STOPPED.register(CommonAssetsManager::onServerStopped);
      ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(CommonAssetsManager::OnDatapackSync);
      ServerLifecycleEvents.SERVER_STARTED.register((ServerStarted)server -> CommonRegistry.onLoadComplete());
      AmmoHitBlockEvent.CALLBACK.register(BellRing::onAmmoHitBlock);
      AmmoHitBlockEvent.CALLBACK.register(DestroyGlassBlock::onAmmoHitBlock);
      LivingHurtEvent.CALLBACK.register(LOW, EntityDamageEvent::onLivingHurt);
      PlayerTickEvent.END.register(HitboxHelperEvent::onPlayerTick);
      PlayerEvent.LOGGED_OUT.register(HitboxHelperEvent::onPlayerLoggedOut);
      LivingKnockBackEvent.CALLBACK.register(KnockbackChange::onKnockback);
      ModConfigEvents.loading("tacz").register(LoadingConfigEvent::onLoadingConfig);
      ModConfigEvents.reloading("tacz").register(LoadingConfigEvent::onReloadingConfig);
      ServerPlayerEvents.AFTER_RESPAWN.register(PlayerRespawnEvent::onPlayerRespawn);
      AttackBlockCallback.EVENT.register(PreventGunClick::onLeftClickBlock);
      ServerTickEvents.START_SERVER_TICK.register(ServerTickEvent::onServerTick);
      ServerTickEvents.END_SERVER_TICK.register(ServerTickEvent::onServerTick);
      EntityJoinLevelEvent.CALLBACK.register(SyncBaseTimestamp::onPlayerJoinWorld);
      EntityTrackingEvents.START_TRACKING.register(SyncedEntityDataEvent::onStartTracking);
      EntityJoinLevelEvent.CALLBACK.register(SyncedEntityDataEvent::onPlayerJoinWorld);
      ServerPlayerEvents.COPY_FROM.register(SyncedEntityDataEvent::onPlayerClone);
      ServerTickEvents.END_SERVER_TICK.register(SyncedEntityDataEvent::onServerTick);
      ServerEntityLevelChangeEvents.AFTER_ENTITY_CHANGE_LEVEL.register(TravelToDimensionEvent::onTravelToDimension);
      ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register(TravelToDimensionEvent::onTravelToDimension);
   }
}
